package de.zalando.service;

import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.cognitoidentity.model.InternalErrorException;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import de.zalando.dto.AppParameter;
import de.zalando.dto.AppStack;
import de.zalando.dto.AppTag;
import de.zalando.dto.Application;
import de.zalando.util.AppConfig;

import de.zalando.util.Filters;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static de.zalando.dto.AppStack.buildFrom;
import static java.util.Calendar.MINUTE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;


@Slf4j
@Service
public class MainService {

    private static final int RESULT_KEY = 1;

    @Autowired
    private Cache<Integer, List<Application>> applicationCache;

    @Autowired
    private AwsApiService awsApiService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Filters filters;


    /**
     * Cache cleanup
     */
    public void cleanCache() {
        applicationCache.invalidateAll();
    }

    /**
     * Get all Stacks grouped by Application Id
     */
    @NonNull
    public List<Application> getApplications() {
        try {
            return applicationCache.get(RESULT_KEY, this::doGetApplications);
        }catch (ExecutionException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }


    private List<Application> doGetApplications() {
        Map<String, List<Stack>> result = awsApiService.getApplicationStacks();

        val dnsListByHostZone = getAllDNS();
        val maxWeights = getMaxWeights(dnsListByHostZone);
        val stackWeights = getStacksWeights(dnsListByHostZone);

        return result.keySet().stream()
                .map(key -> toApplication(key, result.get(key)))
                .map(app -> enrichAppStacks(app, stackWeights, maxWeights))
//                white filter
                .filter(filters::filterInclude)
//                black filter
                .filter(filters::filterExclude)
                .collect(toList());
    }

    private Application enrichAppStacks(Application application, Map<String, Map<String, Long>> stackWeights, Map<String, Map<String, Long>> maxWeights) {

        List<AppStack> newAppStacks = application.getStacks().parallelStream()
                .map(s2 -> buildFrom(s2)
                    .traffic(calculateTraffic(application.getName(), s2.getName(), stackWeights, maxWeights))
                    .build())
                .collect(toList());

        return Application.buildFrom(application)
                          .stacks(newAppStacks)
                          .build();
    }

    private Date addTTL(final List<AppStack> appStack) {
        return doAddTTL(
                appStack.stream()
                        .map(AppStack::getCreationTime)
                        .collect(Collectors.maxBy((o1, o2) -> o1.compareTo(o2)))
                        .orElse(new Date()));
    }

    private Date doAddTTL(final Date creationTime) {
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTime(creationTime);
        expirationTime.add(MINUTE, appConfig.getTtlInMinutes());

        return expirationTime.getTime();
    }

    private Application toApplication(String key, List<Stack> stacks) {

        val appStacks = stacks.stream()
                .map(stack -> AppStack.builder()
                .creationTime(stack.getCreationTime())
                .name(stack.getStackName())
                .parameters(getStackParameters(stack))
                .tags(getStackTags(stack))
                .stackId(stack.getStackId())
                .status(stack.getStackStatus())
                .build())
                .collect(toList());

        return Application.builder()
                          .name(key)
                          .expirationTime(addTTL(appStacks))
                          .stacks(appStacks).build();
    }

    private List<AppTag> getStackTags(Stack stack) {
        return stack.getTags().stream()
                              .map(t -> new AppTag(t.getKey(), t.getValue()))
                              .collect(Collectors.toList());
    }

    private List<AppParameter> getStackParameters(Stack stack) {
        return stack.getParameters().stream()
                                    .map(p -> new AppParameter(p.getParameterKey(), p.getParameterValue(), p.getUsePreviousValue()))
                                    .collect(Collectors.toList());
    }

    private int calculateTraffic(String appName, String stackName, Map<String, Map<String, Long>> stackWeights, Map<String, Map<String, Long>> maxWeights) {
        return Math.round(100 * getWeights(stackWeights, stackName, 0) / getWeights(maxWeights, appName, -1));
    }

    private int getWeights(Map<String, Map<String, Long>> weightsByZone, String key, int defaultValue) {
        for (String hostedZone : weightsByZone.keySet()) {
            Long value = weightsByZone.get(hostedZone).get(key);
            if (value != null) {
                return value.intValue();
            }
        }
        return defaultValue;
    }

    private String pureHostedZoneId(String pureHostedZoneId) {
        return pureHostedZoneId.replace("/hostedzone/", "");
    }

    @VisibleForTesting
    private Map<String, List<ResourceRecordSet>> getAllDNS() {
        val hostedZones = awsApiService.getAllZones().getHostedZones();

        Map<String, List<ResourceRecordSet>> result = Maps.newHashMap();

        for (HostedZone hostedZone : hostedZones) {
            ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();
            val hostZoneId = pureHostedZoneId(hostedZone.getId());
            request.withHostedZoneId(hostZoneId);

            // TODO support pagination.
            request.withMaxItems("" + hostedZone.getResourceRecordSetCount());

            result.put(hostZoneId, awsApiService.getAllDnsRecords(request));
        }
        return result;
    }

    private Map<String, Long> getMaxWeights(List<ResourceRecordSet> dnsNames) {
        return dnsNames.stream()
                        .collect(groupingBy(this::getAppNameFromDns, summingLong(ResourceRecordSet::getWeight)));
    }

    private Map<String, Map<String, Long>> getMaxWeights(Map<String, List<ResourceRecordSet>> dnsNames) {
        Map<String, Map<String, Long>> result = Maps.newHashMap();

        for (String key : dnsNames.keySet()) {
            result.put(key, getMaxWeights(dnsNames.get(key)));
        }
        return result;
    }

    private Map<String, Map<String, Long>> getStacksWeights(Map<String, List<ResourceRecordSet>> dnsNames) {
        Map<String, Map<String, Long>> result = Maps.newHashMap();

        for (String key : dnsNames.keySet()) {
            result.put(key, getStacksWeights(dnsNames.get(key)));
        }
        return result;
    }

    /**
     *     Main CNAME represented as <app-name>.<team-name>.<company-name>.TLD
      * @param resourceRecordSet
     * @return
     */
    private String getAppNameFromDns(ResourceRecordSet resourceRecordSet) {
        return resourceRecordSet.getSetIdentifier().replaceAll("-\\w+$", "");
    }


    private Map<String, Long> getStacksWeights(List<ResourceRecordSet> dnsNames) {
        return dnsNames.stream().collect(Collectors.toMap(ResourceRecordSet::getSetIdentifier, ResourceRecordSet::getWeight));
    }
}



















package de.zalando.service;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerNotFoundException;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.ListHostedZonesByNameRequest;
import com.amazonaws.services.route53.model.ListHostedZonesByNameResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;


@Slf4j
@Service
public class AwsApiService {

    @Autowired
    private AmazonCloudFormation cloudFormationClient;

    @Autowired
    private AmazonRoute53Client route53client;

    @Autowired
    private AmazonElasticLoadBalancingClient loadBalancingClient;

    @Retryable
    public Map<String, List<Stack>> getApplicationStacks() {
        return cloudFormationClient.describeStacks()
                             .getStacks()
                                .stream()
                                .collect(groupingBy(this::byStackName));
    }

    /**
     * Get ELB description
     * @param stackName
     * @return
     */
    @Retryable
    public Optional<LoadBalancerDescription> getELBInfo(String stackName){
        try {
            DescribeLoadBalancersRequest balancersRequest = new DescribeLoadBalancersRequest();
            balancersRequest.withLoadBalancerNames(stackName);
            val descriptions = loadBalancingClient.describeLoadBalancers(balancersRequest).getLoadBalancerDescriptions();

            return isEmpty(descriptions) ? Optional.empty() : Optional.of(descriptions.get(0));
        }catch (LoadBalancerNotFoundException e) {
            return Optional.empty();
        }
    }


    @Retryable
    public List<ResourceRecordSet> getAllDnsRecords(ListResourceRecordSetsRequest request) {
        return route53client.listResourceRecordSets(request).getResourceRecordSets().stream()
                .filter(resourceRecordSet -> !isNull(resourceRecordSet.getWeight()))
                .collect(Collectors.toList());
    }

    @Retryable
    public ListHostedZonesByNameResult getAllZones() {
        ListHostedZonesByNameRequest rZoneByName = new ListHostedZonesByNameRequest();
        ListHostedZonesByNameResult result = route53client.listHostedZonesByName(rZoneByName);

        return result;
    }


    private String byStackName(Stack stack) {

        return
        Optional.ofNullable(stack.getTags())
                .orElse(of())
                    .stream()
                        .filter(tag -> equalsIgnoreCase(tag.getKey(), "StackName"))
                        .findFirst()
                        .orElse(new Tag().withValue(stack.getStackName()))
                            .getValue();
    }

    @Retryable
    public void deleteStack(String id) {
        DeleteStackRequest request = new DeleteStackRequest();
        request.withStackName(id);

        cloudFormationClient.deleteStack(request);

        log.info("Stack {} deleted ",  id);
    }
}























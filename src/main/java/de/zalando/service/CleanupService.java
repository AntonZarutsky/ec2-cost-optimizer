package de.zalando.service;

import com.amazonaws.services.cloudformation.model.Stack;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.zalando.dto.AppStack;
import de.zalando.dto.Application;
import de.zalando.util.AppConfig;

import de.zalando.util.Filters;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.Locale.filter;


@Slf4j
@Service
public class CleanupService {

    @Autowired
    private MainService mainService;

    @Autowired
    private AwsApiService awsApiService;

    @Autowired
    private Filters filters;


    @NonNull
    public Stream<AppStack> getStacksToCleanUp(){
        mainService.cleanCache();

        return
            mainService.getApplications().stream()
                .filter(filters::filterInclude)
                .filter(filters::filterExclude)
                .filter(filters::filterOneLeft)
                .filter(filters::filterByExpirationsTime)
                .flatMap(this::toStacks)
                .filter(filters::filterByTraffic)
                .filter(filters::filterByStatus);
    }


    private Stream<AppStack> toStacks(Application application) {
        return application.getStacks().stream();
    }


    private void deleteStack(AppStack appStack) {
        log.info("appStack {} will be deleted", appStack.getStackId());
         awsApiService.deleteStack(appStack.getStackId());
    }


    @PostConstruct
//     once a 10 mins
    @Scheduled(fixedDelay = 600000)
    private void cleanup(){
        log.info("Scheduler iteration");
        getStacksToCleanUp().forEach(this::deleteStack);
    };


}

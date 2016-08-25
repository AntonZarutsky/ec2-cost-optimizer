package de.zalando.service;

import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import de.zalando.dto.AppStack;
import de.zalando.dto.Application;
import de.zalando.util.AppConfig;
import de.zalando.util.Filters;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.collect.ImmutableList.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(value = MockitoJUnitRunner.class)
public class CleanUpServiceTest {

    @Mock
    private MainService mainService;

    @Mock
    private AwsApiService awsApiService;

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    @Spy
    private Filters filters = new Filters();


    private final static String MYFEED_APP = "MyFeed";
    private final static String PROFILE_APP = "Profile";
    private final static String API_APP = "Api";
    private final static String PROXY_APP = "Proxy";


    @InjectMocks
    private CleanupService cleanupService = new CleanupService();

    private static final AppStack workingStack1 = AppStack.builder()
                                                            .traffic(3)
                                                            .build();
    private static final AppStack emptyStack1   = AppStack.builder()
                                                            .traffic(0)
                                                            .build();
    private static final AppStack emptyStack2   = AppStack.builder()
                                                            .traffic(0)
                                                            .build();


    private static Date newDateTime(int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    @Test
    public void get_stacks_to_cleanup() {
        AppStack nonExpiredStack = AppStack.builder().name("s1").traffic(13).build();
        AppStack nonExpiredStack2 = AppStack.builder().name("s2").traffic(3).build();

        AppStack expired   = AppStack.builder().name("se1").traffic(0).build();
        AppStack expired2   = AppStack.builder().name("se2").traffic(0).build();
        AppStack expired3   = AppStack.builder().name("se3").traffic(0).build();
        AppStack expired4   = AppStack.builder().name("se4").traffic(0).build();


        given(appConfig.getAppsInclude()).willReturn(of(MYFEED_APP, API_APP, PROXY_APP));
        given(appConfig.getAppsExclude()).willReturn(of());
        given(appConfig.isDeleteIfOneLeft()).willReturn(true);

        given(mainService.getApplications()).willReturn(
            of(
              Application.builder()
                            .name(MYFEED_APP)
                            .expirationTime(newDateTime(-10))
                            .stacks(of(nonExpiredStack, nonExpiredStack2, expired)).build(),
              Application.builder().name(PROFILE_APP)
                                    .expirationTime(newDateTime(-10))
                                    .stacks(of(expired2))
                                    .build(),
              Application.builder().name(API_APP)
                                    .stacks(of(expired3))
                                    .expirationTime(newDateTime(-10))
                                    .build(),
              Application.builder().name(PROXY_APP)
                                    .expirationTime(newDateTime(10))
                                    .stacks(of(expired4)).build()
            )
        );

        val result = cleanupService.getStacksToCleanUp()
                                    .map(AppStack::getName)
                                    .collect(Collectors.toList());

        assertThat(result, equalTo(of("se1", "se3")));
    }

























}

















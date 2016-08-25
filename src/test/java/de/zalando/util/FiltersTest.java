package de.zalando.util;

import de.zalando.dto.AppStack;
import de.zalando.dto.Application;
import java.util.Calendar;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(value = MockitoJUnitRunner.class)
public class FiltersTest {

    @Mock
    private AppConfig appConfig;

    private final static String MYFEED_APP = "MyFeed";
    private final static String PROFILE_APP = "Profile";
    private final static String API_APP = "Api";
    private final static String PROXY_APP = "Proxy";

    @InjectMocks
    private Filters filters = new Filters();


    private static final AppStack workingStack1 = AppStack.builder()
            .traffic(3)
            .status("CREATE_IN_PROGRESS")
            .build();

    private static final AppStack emptyStack1   = AppStack.builder()
            .traffic(0)
            .status("CREATE_COMPLETE")
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
    public void filter_working_stack_by_traffic(){
        assertThat(filters.filterByTraffic(workingStack1), equalTo(false));
    }

    @Test
    public void filter_not_working_stack_by_traffic(){
        assertThat(filters.filterByTraffic(emptyStack1), equalTo(true));
    }

    @Test
    public void filter_expired_application() {
        val app = Application.builder()
                .expirationTime(newDateTime(-10)).build();
        assertThat(filters.filterByExpirationsTime(app), equalTo(true));
    }

    @Test
    public void filter_non_expired_stack() {
        val app = Application.builder()
                .expirationTime(newDateTime(10)).build();
        assertThat(filters.filterByExpirationsTime(app), equalTo(false));
    }

    @Test
    public void filter_included_stack() {
        given(appConfig.getAppsInclude()).willReturn(of(MYFEED_APP));
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterInclude(app), equalTo(true));
    }

    @Test
    public void filter_included_stack1() {
        given(appConfig.getAppsInclude()).willReturn(of());
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterInclude(app), equalTo(true));
    }

    @Test
    public void filter_included_stack2() {
        given(appConfig.getAppsInclude()).willReturn(of(PROFILE_APP, MYFEED_APP));
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterInclude(app), equalTo(true));
    }

    @Test
    public void filter_included_stack3() {
        given(appConfig.getAppsInclude()).willReturn(null);
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterInclude(app), equalTo(true));
    }
    @Test
    public void filter_not_included_stack() {
        given(appConfig.getAppsInclude()).willReturn(of(MYFEED_APP));
        val app = Application.builder().name("another app").build();

        assertThat(filters.filterInclude(app), equalTo(false));
    }

    @Test
    public void filter_excluded_stack() {
        given(appConfig.getAppsExclude()).willReturn(of(MYFEED_APP));
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterExclude(app), equalTo(false));
    }

    @Test
    public void filter_excluded_stack1() {
        given(appConfig.getAppsExclude()).willReturn(of(PROFILE_APP, MYFEED_APP));
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterExclude(app), equalTo(false));
    }

    @Test
    public void filter_excluded_stack2() {
        given(appConfig.getAppsExclude()).willReturn(of());
        val app = Application.builder().name(MYFEED_APP).build();

        assertThat(filters.filterExclude(app), equalTo(true));
    }

    @Test
    public void filter_not_excluded_stack() {
        given(appConfig.getAppsExclude()).willReturn(of(MYFEED_APP));
        val app = Application.builder().name("another app").build();

        assertThat(filters.filterExclude(app), equalTo(true));
    }

    @Test
    public void filter_one_left_many_stacks() {
        val app = Application.builder()
                .stacks(of(workingStack1, emptyStack1))
                .build();

        assertThat(filters.filterOneLeft(app), equalTo(true));
    }
    @Test
    public void filter_one_left_one_stack() {
        given(appConfig.isDeleteIfOneLeft()).willReturn(true);

        val app = Application.builder()
                .stacks(of(workingStack1))
                .build();

        assertThat(filters.filterOneLeft(app), equalTo(true));
    }

    @Test
    public void filter_one_left_no_stacks() {
        given(appConfig.isDeleteIfOneLeft()).willReturn(true);

        val app = Application.builder()
                .stacks(of())
                .build();

        assertThat(filters.filterOneLeft(app), equalTo(true));
    }

    @Test
    public void filter_one_left_no_stacks2() {
        given(appConfig.isDeleteIfOneLeft()).willReturn(true);

        val app = Application.builder()
                .stacks(null)
                .build();

        assertThat(filters.filterOneLeft(app), equalTo(true));
    }

    @Test
    public void filter_one_left_one_stack2() {
        given(appConfig.isDeleteIfOneLeft()).willReturn(false);

        val app = Application.builder()
                .stacks(of(workingStack1))
                .build();

        assertThat(filters.filterOneLeft(app), equalTo(false));
    }

    @Test
    public void filter_in_creation() {

        assertThat(filters.filterByStatus(workingStack1), equalTo(false));
    }
    @Test
    public void filter_not_in_creation_status() {

        assertThat(filters.filterByStatus(emptyStack1), equalTo(true));
    }

}

















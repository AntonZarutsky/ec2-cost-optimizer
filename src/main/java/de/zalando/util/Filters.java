package de.zalando.util;

import de.zalando.dto.AppStack;
import de.zalando.dto.Application;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;


@Slf4j
@Component
public class Filters {

    @Autowired
    private AppConfig appConfig;

    // could be collection with "" first element
    private boolean isEmpty(List<String> config) {
        return  CollectionUtils.isEmpty(config) ||
                CollectionUtils.isEmpty(config.stream()
                                        .filter(StringUtils::isNotEmpty)
                                        .collect(Collectors.toList()));
    }

    public boolean filterInclude(Application application) {
        if(isEmpty(appConfig.getAppsInclude())  ){
            return true;
        }
        return appConfig.getAppsInclude().contains(application.getName());
    }

    public boolean filterExclude(Application application) {
        if(isEmpty(appConfig.getAppsExclude())){
            return true;
        }
        return !appConfig.getAppsExclude().contains(application.getName());
    }

    public boolean filterOneLeft(Application application) {
        if(application.getStacks() == null || application.getStacks().size() != 1)
            return true;
        return appConfig.isDeleteIfOneLeft();
    }

    public boolean filterByExpirationsTime(Application app) {
        return app.getExpirationTime().before(new Date());
    }

    public boolean filterByTraffic(AppStack appStack) {
        return appStack.getTraffic() == 0;
    }

    public boolean filterByStatus(AppStack appStack) {
        return !equalsIgnoreCase("CREATE_IN_PROGRESS", appStack.getStatus());
    }
}

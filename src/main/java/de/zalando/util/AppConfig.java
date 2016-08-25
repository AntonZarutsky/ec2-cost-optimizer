package de.zalando.util;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@Getter
public class AppConfig {

    @Value("${cache.timeout}")
    private long cacheTimeoutMin;

    @Value("${stack.cleanup.onlyoneleft}")
    private boolean deleteIfOneLeft;

    @Value("#{'${apps.include}'.split(',')}")
    private List<String> appsInclude;

    @Value("#{'${apps.exclude}'.split(',')}")
    private List<String> appsExclude;

    @Value("${stack.notraffic.ttl}")
    private int ttlInMinutes;




}

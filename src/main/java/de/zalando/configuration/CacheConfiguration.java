package de.zalando.configuration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.zalando.dto.Application;
import de.zalando.util.AppConfig;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CacheConfiguration {

    @Autowired
    private AppConfig appConfig;

    @Bean
    public Cache<Integer, List<Application>> applicationCache() {
        return CacheBuilder.newBuilder().maximumSize(10)
                           .expireAfterWrite(appConfig.getCacheTimeoutMin(), TimeUnit.MINUTES)
                           .build();

    }
}

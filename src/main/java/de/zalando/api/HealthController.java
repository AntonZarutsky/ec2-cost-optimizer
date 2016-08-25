package de.zalando.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class HealthController {

    @RequestMapping(value = "/health")
    public String health(){
        return "Ok";
    }

}

package de.zalando.api;

import de.zalando.dto.AppStack;
import de.zalando.dto.Application;
import de.zalando.service.AwsApiService;
import de.zalando.service.CleanupService;
import de.zalando.service.MainService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping(value = "/applications")
public class StackController {

    @Autowired
    private MainService mainService;

    @Autowired
    private AwsApiService awsApiService;

    @Autowired
    private CleanupService cleanupService;

    @RequestMapping(value = "/all")
    public List<Application> applications(
            @RequestParam(name = "refresh", defaultValue = "false") boolean refresh){
        if (refresh){
            mainService.cleanCache();
        }
        return mainService.getApplications();
    }
    @RequestMapping(value = "/stacks/cleanup")
    public List<AppStack> getStacksToCleanUp(){
        return cleanupService.getStacksToCleanUp().collect(Collectors.toList());
    }

}

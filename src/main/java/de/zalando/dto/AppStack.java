package de.zalando.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Value
@Builder(builderClassName = "Builder")
public class AppStack {

    private String             name;
    private String             version;
    private String             stackId;
    private String             hostedZoneId;
    private String             status;
    private List<AppParameter> parameters;
    private List<AppTag> tags;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
    private Date creationTime;


    private int traffic;


    public static Builder buildFrom(AppStack stack) {
        return builder().creationTime(stack.getCreationTime())
                        .hostedZoneId(stack.getHostedZoneId())
                        .name(stack.getName())
                        .parameters(stack.getParameters())
                        .tags(stack.getTags())
                        .stackId(stack.getStackId())
                        .traffic(stack.getTraffic())
                        .version(stack.getVersion())
                        .status(stack.status);
    }
}

package de.zalando.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class Application {

    private String name;
    private List<AppStack> stacks;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
    private Date expirationTime;

    public static Builder buildFrom(Application app) {
        return builder().stacks(app.getStacks())
                        .name(app.getName())
                        .expirationTime(app.getExpirationTime());
    }
}

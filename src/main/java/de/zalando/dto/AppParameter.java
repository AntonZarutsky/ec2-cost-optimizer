package de.zalando.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Value
@AllArgsConstructor
public class AppParameter {

    private String parameterKey;
    private String parameterValue;
    private Boolean usePreviousValue;

}

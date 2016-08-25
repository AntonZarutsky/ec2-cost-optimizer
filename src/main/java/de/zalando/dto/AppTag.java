package de.zalando.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class AppTag {

    private String key;
    private String value;

}

package dev.kozhevnikov.autotests.wiremock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ResponseMapping {
    private Integer status;
    private String body;
    private Map<String, String> headers;
}

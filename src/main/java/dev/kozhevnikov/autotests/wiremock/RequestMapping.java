package dev.kozhevnikov.autotests.wiremock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RequestMapping {
    private String method;
    private String urlPattern;
    private List<Map.Entry<String, String>> bodyPatterns;
}

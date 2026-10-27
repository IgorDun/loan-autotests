package dev.kozhevnikov.autotests.wiremock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CreateMappingRequest {
    private Integer priority;
    private RequestMapping request;
    private ResponseMapping response;
}

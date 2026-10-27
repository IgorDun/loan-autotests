package dev.kozhevnikov.autotests.wiremock;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CreateMappingResponse {
    private String id;
    private RequestMapping request;
    private ResponseMapping response;
    private String uuid;
    private Integer priority;
}

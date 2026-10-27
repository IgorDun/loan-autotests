package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonJsonPaths {
    public static final String DATA = "$.data";
    public static final String DATA_ID = "$.data.id";
    public static final String DATA_0_ID = "$.data[0].id";
    public static final String $ = "$";
}

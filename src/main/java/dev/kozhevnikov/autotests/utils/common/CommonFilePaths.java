package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonFilePaths {
    public final static String TEST_RESOURCES = "src/test/resources";
    public final static String MAIN_RESOURCES = "src/main/resources";
    public final static String TEST_SQL_SCRIPTS = String.format("%s/files/sqlScripts/", TEST_RESOURCES);
    public final static String MAIN_TEMPLATES = String.format("%s/templates/", MAIN_RESOURCES);
    public final static String TEST_TEMPLATES = String.format("%s/templates/", TEST_RESOURCES);
    public final static String TEST_FILES = String.format("%s/files/", TEST_RESOURCES);
}

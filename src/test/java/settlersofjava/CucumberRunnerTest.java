package settlersofjava;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("settlersofjava/features")
@ConfigurationParameter(
        key = "cucumber.glue",
        value = "settlersofjava.stepdefs"
)
public class CucumberRunnerTest {
    // empty uses annotations to configure Cucumber
}
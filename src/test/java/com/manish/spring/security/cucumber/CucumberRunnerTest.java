package com.manish.spring.security.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * CucumberRunnerTest is the JUnit 5 Suite entry-point for all Cucumber scenarios.
 *
 * <p>Why @Suite?
 * Cucumber's JUnit Platform Engine is discovered automatically by the JUnit Platform
 * when using the cucumber-junit-platform-engine dependency.  A @Suite class lets us
 * explicitly configure where feature files live and where the step definitions are,
 * rather than relying purely on convention-based auto-discovery.
 *
 * <p>@IncludeEngines("cucumber"): tells the JUnit Platform to delegate test discovery
 * and execution to the Cucumber engine for this suite.
 *
 * <p>@SelectClasspathResource("features"): instructs Cucumber to load all .feature
 * files found under src/test/resources/features/ on the classpath.
 *
 * <p>@ConfigurationParameter GLUE_PROPERTY_NAME: the "glue" is the package where
 * Cucumber looks for @Given/@When/@Then step definition classes.  Pointing it at
 * our cucumber sub-packages ensures every step definition is found automatically.
 *
 * <p>@ConfigurationParameter PLUGIN_PROPERTY_NAME: configures the output format.
 * "pretty" prints coloured, human-readable scenario output in the console, making
 * it easy to see which steps passed or failed.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.manish.spring.security.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty")
public class CucumberRunnerTest {
    // This class is intentionally empty.
    // JUnit Platform + Cucumber engine handle discovery and execution.
}

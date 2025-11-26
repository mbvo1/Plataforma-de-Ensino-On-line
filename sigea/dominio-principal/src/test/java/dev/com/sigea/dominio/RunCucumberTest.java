package dev.com.sigea.dominio; 

import org.junit.platform.suite.api.*;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("dev/com/sigea/dominio") 
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dev.com.sigea.dominio")
public class RunCucumberTest {}
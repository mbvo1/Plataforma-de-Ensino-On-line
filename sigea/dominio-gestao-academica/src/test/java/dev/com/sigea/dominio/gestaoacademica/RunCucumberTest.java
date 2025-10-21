package dev.com.sigea.dominio.gestaoacademica;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("dev/com/sigea/dominio/gestaoacademica")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dev.com.sigea.dominio.gestaoacademica")
public class RunCucumberTest {

}
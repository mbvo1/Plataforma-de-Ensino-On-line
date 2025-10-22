package dev.com.sigea.dominio.ambientecolaborativo;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("dev/com/sigea/dominio/ambientecolaborativo")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dev.com.sigea.dominio.ambientecolaborativo.turma")
public class RunCucumberTest {

}
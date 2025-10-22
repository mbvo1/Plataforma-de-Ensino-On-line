package dev.com.sigea.dominio.comunicacaogeral;
import org.junit.platform.suite.api.*;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("dev/com/sigea/dominio/comunicacaogeral")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dev.com.sigea.dominio.comunicacaogeral.aviso")
public class RunCucumberTest {}
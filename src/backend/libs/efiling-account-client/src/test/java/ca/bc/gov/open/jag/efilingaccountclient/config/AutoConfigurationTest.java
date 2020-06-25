package ca.bc.gov.open.jag.efilingaccountclient.config;



import ca.bc.gov.open.jag.efilingcommons.model.Clients;
import ca.bc.gov.open.jag.efilingcommons.model.EfilingSoapClientProperties;
import ca.bc.gov.open.jag.efilingcommons.model.SoapProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.ArrayList;

@DisplayName("AutoConfiguration Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutoConfigurationTest {
    private static final String URI = "URI";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(AutoConfiguration.class);


    private AutoConfiguration sut;


    @Test
    @DisplayName("CASE1: Test that beans are created")
    public void testBeansAreGenerated() {
        ArrayList<EfilingSoapClientProperties> soapClientProperties = new ArrayList<>();

        EfilingSoapClientProperties accountProperties = new EfilingSoapClientProperties();
        accountProperties.setClient(Clients.ACCOUNT);
        accountProperties.setUri(URI);
        accountProperties.setUserName(USERNAME);
        accountProperties.setPassword(PASSWORD);
        soapClientProperties.add(accountProperties);


        EfilingSoapClientProperties roleProperties = new EfilingSoapClientProperties();
        roleProperties.setClient(Clients.ROLE);
        roleProperties.setUri(URI);
        roleProperties.setUserName(USERNAME);
        roleProperties.setPassword(PASSWORD);
        soapClientProperties.add(roleProperties);

        SoapProperties soapProperties = new SoapProperties();
        soapProperties.setClients(soapClientProperties);

        sut = new AutoConfiguration(soapProperties);

        Assertions.assertNotNull(sut.accountFacadeBean());
        Assertions.assertNotNull(sut.roleRegistryPortType());
        Assertions.assertNotNull(sut.efilingAccountService(null));
    }
}

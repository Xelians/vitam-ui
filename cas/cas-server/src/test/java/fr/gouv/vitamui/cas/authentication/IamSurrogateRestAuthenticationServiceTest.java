package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import lombok.val;

/**
 * Tests {@link IamSurrogateRestAuthenticationService}.
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class IamSurrogateRestAuthenticationServiceTest {

    private static final String SURROGATE = "surrogate";
    private static final String SU_ID = "id";
    private static final String SU_EMAIL = "superUser";

    private IamSurrogateRestAuthenticationService service;

    private CasExternalRestClient casExternalRestClient;

    @Before
    public void setUp() {
        casExternalRestClient = mock(CasExternalRestClient.class);

        final Utils utils = new Utils(casExternalRestClient, null, 0, null);
        service = new IamSurrogateRestAuthenticationService(casExternalRestClient, mock(ServicesManager.class), utils);
    }

    @Test
    public void testCanAuthenticateOk() {
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID))).thenReturn(Arrays.asList(surrogation()));

        assertTrue(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test
    public void testCanAuthenticateCannotSurrogate() {
        final SubrogationDto subrogation = surrogation();
        subrogation.setSurrogate("anotherUser");
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID)))
            .thenReturn(Arrays.asList(subrogation));

        assertFalse(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test
    public void testCanAuthenticateNotAccepted() {
        final SubrogationDto subrogation = surrogation();
        subrogation.setStatus(SubrogationStatusEnum.CREATED);
        when(casExternalRestClient.getSubrogationsBySuperUserId(any(ExternalHttpContext.class), eq(SU_ID)))
            .thenReturn(Arrays.asList(subrogation));

        assertFalse(service.canAuthenticateAsInternal(SURROGATE, principal(), null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAccounts() {
        when(casExternalRestClient.getSubrogationsBySuperUserEmail(any(ExternalHttpContext.class), eq(SU_EMAIL)))
            .thenReturn(Arrays.asList(surrogation()));

        service.getEligibleAccountsForSurrogateToProxy(SU_EMAIL);
    }

    private Principal principal() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(UserPrincipalResolver.SUPER_USER_ID_ATTRIBUTE, Arrays.asList(SU_ID));

        val factory = new DefaultPrincipalFactory();
        return factory.createPrincipal("x", attributes);
    }

    private SubrogationDto surrogation() {
        final SubrogationDto subrogation = new SubrogationDto();
        subrogation.setSurrogate(SURROGATE);
        subrogation.setSuperUser(SU_EMAIL);
        subrogation.setStatus(SubrogationStatusEnum.ACCEPTED);
        return subrogation;
    }
}

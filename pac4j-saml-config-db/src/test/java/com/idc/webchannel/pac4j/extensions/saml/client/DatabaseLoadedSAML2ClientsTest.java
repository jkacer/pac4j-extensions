package com.idc.webchannel.pac4j.extensions.saml.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.AjaxRequestResolver;
import org.pac4j.core.http.UrlResolver;
import org.pac4j.saml.client.SAML2Client;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;


/**
 * Unit test of {@link DatabaseLoadedSAML2Clients}. Strongly inspired by {@code ClientsTests} from the base PAC4J Core module.
 * Just slightly adapted to suit the tested class.
 * 
 * @author jkacer
 */
public class DatabaseLoadedSAML2ClientsTest {

    private static final String CALLBACK_URL = "http://myappli/callback";
    private static final String TYPE = "type";
    private static final String KEY = "key";

    private DatabaseLoadedSAML2Clients clientsUnderTest;


    // ------------------------------------------------------------------------------------------------------------------------------------


    @Before
    public void initTestedClients() {
    	clientsUnderTest = new DatabaseLoadedSAML2Clients(createSamlClientDaoMock());
    }
	
	
    /**
	 * What happens if no clients are defined? The original version throws an error from {@link org.pac4j.core.client.Clients#init()} if no
	 * explicit client is defined. This version does not - all clients are loaded from the database and it is not an error if no record is
	 * present.
	 */
    @Test
    public void testMissingClient() {
    	DatabaseLoadedSAML2Clients clients = new DatabaseLoadedSAML2Clients(createSamlClientDaoMockWithNoClientsDefined());
    	clients.setCallbackUrl(CALLBACK_URL);
    	clients.init();
        List<Client> clientList = clients.getClients();
        assertNotNull(clientList);
        assertTrue(clientList.isEmpty());
    }

    
    /**
     * When the callback URL is not defined.
     */
    @Test
    public void testNoCallbackUrl() {
    	clientsUnderTest.init();
        Client c = clientsUnderTest.findClient("SamlOne");
        assertNotNull(c);
        assertNull(((SAML2Client)c).getCallbackUrl());
    }

    
    /**
	 * Three different clients. Their callbacks must be OK. Repeated calls to {@link DatabaseLoadedSAML2Clients#findClient(String)} must
	 * return the same client.
	 */
    @Test
    public void testThreeClients() {
    	clientsUnderTest.setClientNameParameter(TYPE);
    	clientsUnderTest.setCallbackUrl(CALLBACK_URL);
        
    	// findClient() involves init()
        Client c1 = clientsUnderTest.findClient("SamlOne");
        Client c2 = clientsUnderTest.findClient("SamlTwo");
        Client c3 = clientsUnderTest.findClient("SamlThree");
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c1.getName(), ((SAML2Client)c1).getCallbackUrl());
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c2.getName(), ((SAML2Client)c2).getCallbackUrl());
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c3.getName(), ((SAML2Client)c3).getCallbackUrl());
        assertEquals(c1, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c1.getName())));
        assertEquals(c2, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c2.getName())));
        assertEquals(c3, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c3.getName())));

        // Explicit init() should not change anything
        clientsUnderTest.init();
        c1 = clientsUnderTest.findClient("SamlOne");
        c2 = clientsUnderTest.findClient("SamlTwo");
        c3 = clientsUnderTest.findClient("SamlThree");
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c1.getName(), ((SAML2Client)c1).getCallbackUrl());
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c2.getName(), ((SAML2Client)c2).getCallbackUrl());
        assertEquals(CALLBACK_URL + "?" + TYPE + "=" + c3.getName(), ((SAML2Client)c3).getCallbackUrl());
        assertEquals(c1, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c1.getName())));
        assertEquals(c2, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c2.getName())));
        assertEquals(c3, clientsUnderTest.findClient(mockWebContextWithSingleTypeParameter(TYPE, c3.getName())));
    }
    


    /**
     * Does {@link DatabaseLoadedSAML2Clients#findAllClients()} return all clients it should return?
     */
    @Test
    public void testAllClients() {
    	clientsUnderTest.setCallbackUrl(CALLBACK_URL);
        final List<Client> clientList = clientsUnderTest.findAllClients();
        assertEquals(3, clientList.size());
        String[] expectedNames = new String[] {"SamlOne", "SamlTwo", "SamlThree"};
        for (String expName: expectedNames) {
        	assertTrue(clientsContain(clientList, expName));
        }
    }

    
	/**
	 * Is the callback URL correct?
	 */
	@Test
    public void testClientWithCallbackUrl() {
		clientsUnderTest.setClientNameParameter(KEY);
		clientsUnderTest.setCallbackUrl(CALLBACK_URL);
		clientsUnderTest.init();
        Client c1 = clientsUnderTest.findClient("SamlOne");
        assertEquals(CALLBACK_URL + "?" + clientsUnderTest.getClientNameParameter() + "=" + c1.getName(), ((SAML2Client) c1).getCallbackUrl());
    }

	
    /**
     * Does it find a client by class name?
     */
    @Test
    public void testByClassFindsSaml() {
        assertNotNull(clientsUnderTest.findClient(SAML2Client.class));
    }
    
    
    /**
     * If we try to get another client type than SAML Client, an exception will be thrown. 
     */
    @Test(expected = TechnicalException.class)
    public void testByClassDoesNotFindAnotherClass() {
        clientsUnderTest.findClient(YetAnotherClient.class);
    }
    
    
    /**
	 * If more clients of the same name are defined in the database, an exception will be thrown on
	 * {@link DatabaseLoadedSAML2Clients#init()}.
	 */
    @Test(expected = TechnicalException.class)
    public void rejectSameName() {
        final DatabaseLoadedSAML2Clients clients = new DatabaseLoadedSAML2Clients(createSamlClientDaoMockWithDuplicateNames());
        clients.init();
    }


	/**
	 * Tests resolvers and authorization generators.
	 */
	@Test
	public void testDefineAjaxCallbackResolverAuthGenerator() {
		final AjaxRequestResolver ajaxRequestResolver = ctx -> false;
		final UrlResolver urlResolver = (url, ctx) -> url;
		final AuthorizationGenerator authorizationGenerator = (ctx, profile) -> profile;

		clientsUnderTest.setAjaxRequestResolver(ajaxRequestResolver);
		clientsUnderTest.setUrlResolver(urlResolver);
		clientsUnderTest.addAuthorizationGenerator(authorizationGenerator);

		clientsUnderTest.init();

		SAML2Client c = (SAML2Client) clientsUnderTest.findClient("SamlOne");

		assertEquals(ajaxRequestResolver, c.getAjaxRequestResolver());
		assertEquals(urlResolver, c.getUrlResolver());
		assertEquals(authorizationGenerator, c.getAuthorizationGenerators().get(0));
	}    

    // ------------------------------------------------------------------------------------------------------------------------------------
   
    
    private boolean clientsContain(List<Client> clients, String expName) {
		for (Client c : clients) {
			if (c.getName().equals(expName))
				return true;
		}
		return false;
	}

    
	private SamlClientDao createSamlClientDaoMock() {
		List<String> fakeNames = Arrays.asList("SamlOne", "SamlTwo", "SamlThree");
		SamlClientDao dao = mock(SamlClientDao.class);
		when(dao.loadClientNames()).thenReturn(fakeNames);
		return dao;
	}

	
	private SamlClientDao createSamlClientDaoMockWithDuplicateNames() {
		List<String> fakeNames = Arrays.asList("SamlOne", "SamlOne");
		SamlClientDao dao = mock(SamlClientDao.class);
		when(dao.loadClientNames()).thenReturn(fakeNames);
		return dao;
	} 

	
	private SamlClientDao createSamlClientDaoMockWithNoClientsDefined() {
		SamlClientDao dao = mock(SamlClientDao.class);
		when(dao.loadClientNames()).thenReturn(Collections.emptyList());
		return dao;
	}
	
	
    private WebContext mockWebContextWithSingleTypeParameter(final String paramName, final String paramValue) {
    	WebContext wc = mock(WebContext.class);
    	
    	Map<String, String[]> m = new HashMap<>();
    	m.put(paramName, new String[] {paramValue});
    	when(wc.getRequestParameter(paramName)).thenReturn(paramValue);
    	when(wc.getRequestParameters()).thenReturn(m);
    	
    	return wc;
    }

}

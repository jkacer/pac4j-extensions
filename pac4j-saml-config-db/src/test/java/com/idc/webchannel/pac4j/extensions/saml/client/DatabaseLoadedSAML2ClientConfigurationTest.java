package com.idc.webchannel.pac4j.extensions.saml.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.io.Resource;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.DbLoadedSamlClientConfigurationDto;
import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;


/**
 * Unit test of {@link DatabaseLoadedSAML2ClientConfiguration}.
 * 
 * @author jkacer
 */
public class DatabaseLoadedSAML2ClientConfigurationTest {

	private static final String[] CLIENT_NAMES = {"SAML_0"};
	private static final String ENVIRONMENT = "Unit_Test_Env";
	private static final String[] KEYSTORE_PASSWORDS = {"Keystore_Pwd_0"};
	private static final String[] KEYSTORE_ALIASES = {"SP0"};
	private static final String[] PRIV_KEY_PASSWORDS = {"Priv_Key_Pwd_0"};
	private static final String[] IDP_ENTITY_IDS = {"https://idp.testshib.org/idp/shibboleth"};
	private static final String[] SP_ENTITY_IDS = {"urn:idc:authentication:saml2:entity:unittest:sp1"};
	private static final int[] MAX_AUTH_LIFETIMES = {3600};
	private static final String[] DEST_BINDING_TYPES = {"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"};
	
	private static final String PATH_TO_TEST_KEYSTORES = "/com/idc/webchannel/pac4j/extensions/saml/client/SP_Keystore_%d.jks";
	private static final String PATH_TO_TEST_METADATA = "/com/idc/webchannel/pac4j/extensions/saml/client/IdP_Metadata_%d.xml";
	
	private DatabaseLoadedSAML2ClientConfiguration configurationUnderTest;
	private WebContext webContextMock;
	
	private SamlClientDao samlClientDaoMock;
	
	
	// ------------------------------------------------------------------------------------------------------------------------------------
	
	
	@Before
	public void setUp() throws IOException {
		samlClientDaoMock = createSamlClientDaoMock();
		configurationUnderTest = new DatabaseLoadedSAML2ClientConfiguration(samlClientDaoMock);
		webContextMock = mock(WebContext.class);
		configurationUnderTest.init(CLIENT_NAMES[0], webContextMock);
	}

	
	private SamlClientDao createSamlClientDaoMock() throws IOException {
		List<DbLoadedSamlClientConfigurationDto> clients = new ArrayList<>();
		DbLoadedSamlClientConfigurationDto client0 = clientDto(0);
		clients.add(client0);
		
		SamlClientDao scd = mock(SamlClientDao.class);
		when(scd.loadClientNames()).thenReturn(Arrays.asList(CLIENT_NAMES));
		when(scd.loadAllClients()).thenReturn(clients);
		when(scd.loadClient(CLIENT_NAMES[0])).thenReturn(client0);
		return scd;
	}
	
	
	private DbLoadedSamlClientConfigurationDto clientDto(int index) throws IOException {
		DbLoadedSamlClientConfigurationDto dto = new DbLoadedSamlClientConfigurationDto();
		
		dto.setClientName(CLIENT_NAMES[index]);
		dto.setEnvironment(ENVIRONMENT);
		dto.setKeystoreBinaryData(loadKeystoreBinaryData(index));
		dto.setKeystorePassword(KEYSTORE_PASSWORDS[index]);
		dto.setKeystoreAlias(KEYSTORE_ALIASES[index]);
		dto.setPrivateKeyPassword(PRIV_KEY_PASSWORDS[index]);
		dto.setIdentityProviderMetadata(loadIdentityProviderMetadata(index));
		dto.setIdentityProviderEntityId(IDP_ENTITY_IDS[index]);
		dto.setServiceProviderEntityId(SP_ENTITY_IDS[index]);
		dto.setMaximumAuthenticationLifetime(MAX_AUTH_LIFETIMES[index]);
		dto.setDestinationBindingType(DEST_BINDING_TYPES[index]);
		
		return dto;
	}

	
	private byte[] loadKeystoreBinaryData(int index) throws IOException {
		final String path = String.format(PATH_TO_TEST_KEYSTORES, index);
		try (InputStream is = DatabaseLoadedSAML2ClientConfigurationTest.class.getResourceAsStream(path)) {
			return IOUtils.toByteArray(is);
		}
	}

	
	private String loadIdentityProviderMetadata(int index) throws IOException {
		final String path = String.format(PATH_TO_TEST_METADATA, index);
		try (InputStream is = DatabaseLoadedSAML2ClientConfigurationTest.class.getResourceAsStream(path)) {
			return IOUtils.toString(is, "UTF-8");
		}
	}
	

	// ------------------------------------------------------------------------------------------------------------------------------------

	
	@Test
	public void basicGettersWork() {
		assertEquals(CLIENT_NAMES[0], configurationUnderTest.getClientName());
		assertNotNull(configurationUnderTest.getKeyStore());
		assertNotNull(configurationUnderTest.getKeystoreResource());
		assertNotNull(configurationUnderTest.getIdentityProviderMetadataResource());
		
		assertEquals(DEST_BINDING_TYPES[0], configurationUnderTest.getDestinationBindingType());
		assertEquals(IDP_ENTITY_IDS[0], configurationUnderTest.getIdentityProviderEntityId());
		assertNull(configurationUnderTest.getIdentityProviderMetadataPath());
		assertEquals(KEYSTORE_ALIASES[0], configurationUnderTest.getKeyStoreAlias());
		assertEquals(KEYSTORE_PASSWORDS[0], configurationUnderTest.getKeystorePassword());
		assertNull(configurationUnderTest.getKeystorePath());
		assertEquals(KeyStore.getDefaultType(), configurationUnderTest.getKeyStoreType());
		assertEquals(MAX_AUTH_LIFETIMES[0], configurationUnderTest.getMaximumAuthenticationLifetime());
		assertEquals(SP_ENTITY_IDS[0], configurationUnderTest.getServiceProviderEntityId());
	}
	
	
	@Test
	public void keystoreIsOk() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		KeyStore ks = configurationUnderTest.getKeyStore();
		assertNotNull(ks);

		Enumeration<String> aliases = ks.aliases();
		assertTrue(aliases.hasMoreElements());
		assertEquals(KEYSTORE_ALIASES[0].toUpperCase(), aliases.nextElement().toUpperCase());
		
		assertTrue(ks.isKeyEntry(KEYSTORE_ALIASES[0]));
		Key key = ks.getKey(KEYSTORE_ALIASES[0], PRIV_KEY_PASSWORDS[0].toCharArray());
		assertTrue(key instanceof PrivateKey);
		
		Certificate cert = ks.getCertificate(KEYSTORE_ALIASES[0]);
		assertNotNull(cert);
		PublicKey publicKey = cert.getPublicKey();
		assertNotNull(publicKey);
	}
	
	
	@Test
	public void keystoreResourceIsOk() throws IOException {
		Resource r = configurationUnderTest.getKeystoreResource();
		assertNotNull(r);
		
		assertTrue(r.exists());
		assertNull(r.getFile());
		assertNull(r.getFilename());
		InputStream is = r.getInputStream();
		assertNotNull(is);
		is.close();
	}

	
	@Test
	public void idpMetadataProvidederResourceIsOk() throws IOException {
		Resource r = configurationUnderTest.getIdentityProviderMetadataResource();
		assertNotNull(r);
		
		assertTrue(r.exists());
		assertNull(r.getFile());
		assertNull(r.getFilename());
		InputStream is = r.getInputStream();
		assertNotNull(is);
		is.close();
	}
	
}

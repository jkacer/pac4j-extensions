package com.idc.webchannel.pac4j.extensions.saml.client;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.DbLoadedSamlClientConfigurationDto;
import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;


/**
 * SAML2 Client configuration intended to be loaded from a database.
 * 
 * An alternative to {@link SAML2ClientConfiguration}. Unlike {@link SAML2ClientConfiguration}, this class does not use paths to resources.
 * Instead, it contains the data directly.
 *
 * @author jkacer
 * @since 1.9.0
 */
public class DatabaseLoadedSAML2ClientConfiguration extends SAML2ClientConfiguration {

	/** DAO to read SAML client configurations. Should be shared by all configuration instances. */
	private final SamlClientDao dao;
	
	/** Binary data of a JKS keystore. */
	private byte[] keystoreBinaryData;

    /** IdP metadata. */
    private String identityProviderMetadata;

	/** SAML Client name. Comes in {@link #init(String, WebContext)}. */
	private String clientName;

    /** The PAC4J resource corresponding to the key store. Just wraps the key store's binary definition. Initialized in {@link #init(String, WebContext)}. */
    private Resource keyStoreResourceFromDatabase;
    
    /** The PAC4J resource corresponding to the IdP metadata. Just wraps the metadata textual definition. Initialized in {@link #init(String, WebContext)}. */
    private Resource identityProviderMetadataResourceFromDatabase;


    // ------------------------------------------------------------------------------------------------------------------------------------


    /**
	 * The constructor. Use setters to initialize properties.
	 * 
	 * @param dao
	 *            DAO loading client configuration from DB.
	 */
	public DatabaseLoadedSAML2ClientConfiguration(final SamlClientDao dao) {
    	super();
    	CommonHelper.assertNotNull("dao", dao);
    	this.dao = dao;
	}

    public String getClientName() {
		return clientName;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}


	/**
	 * {@inheritDoc}
	 * 
	 * Initializes the configuration by loading all its propertied from a database using the DAO provided at creation time.
	 * 
	 * @throws IllegalStateException
	 *             If the context does not contain the client name or if no configuration for the given name exists.
	 */
	@Override
	protected void init(final String clientName, final WebContext context) {
		if (StringUtils.isBlank(clientName)) {
			throw new TechnicalException("The client name must not be null or empty.");
		}

		// Subsequently, the configuration for the name must be loaded using a DAO.
		DbLoadedSamlClientConfigurationDto loaded = dao.loadClient(clientName);
		if (loaded == null || !clientName.equals(loaded.getClientName())) {
			throw new TechnicalException("SAML Client Configuration for name '" + clientName + "' could not be loaded.");
		}
		
		// If everything is OK, we will set the loaded values to the configuration object (itself).
		setClientName(clientName);
		
		// Optional value with a default - only set if not empty
		final String destBinding = loaded.getDestinationBindingType();
		if (StringUtils.isNotBlank(destBinding)) {
			setDestinationBindingType(destBinding);
		}
		
		setIdentityProviderEntityId(loaded.getIdentityProviderEntityId());
		this.identityProviderMetadata = loaded.getIdentityProviderMetadata();
		setKeystoreType(KeyStore.getDefaultType());
		keystoreBinaryData = loaded.getKeystoreBinaryData();
		setKeystorePassword(loaded.getKeystorePassword());
		setKeystoreAlias(loaded.getKeystoreAlias());
		setPrivateKeyPassword(loaded.getPrivateKeyPassword());
		setMaximumAuthenticationLifetime(loaded.getMaximumAuthenticationLifetime());
		setServiceProviderEntityId(loaded.getServiceProviderEntityId());
		
		CommonHelper.assertNotBlank("clientName", this.clientName);
		CommonHelper.assertNotBlank("destinationBindingType", this.getDestinationBindingType());
		CommonHelper.assertNotBlank("identityProviderEntityId", this.getIdentityProviderEntityId());
		CommonHelper.assertNotBlank("identityProviderMetadata", this.identityProviderMetadata);
		CommonHelper.assertNotBlank("keystoreType", this.getKeyStoreType());
		CommonHelper.assertNotNull("keystoreBinaryData", this.keystoreBinaryData);
        CommonHelper.assertNotBlank("keystorePassword", this.getKeystorePassword());
        CommonHelper.assertNotBlank("keystoreAlias", this.getKeyStoreAlias());
        CommonHelper.assertNotBlank("privateKeyPassword", this.getPrivateKeyPassword());
        CommonHelper.assertTrue(this.getMaximumAuthenticationLifetime() > 0, "Maximum authentication lifetime must be positive.");
        CommonHelper.assertNotBlank("serviceProviderEntityId", this.getServiceProviderEntityId());

		initializeKeyStore();
		initializeIdentityProviderMetadata();

		CommonHelper.assertNotNull("keyStoreResourceFromDatabase", this.keyStoreResourceFromDatabase);
		CommonHelper.assertNotNull("identityProviderMetadataResourceFromDatabase", this.identityProviderMetadataResourceFromDatabase);

        final BasicSignatureSigningConfiguration config = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
        setBlackListedSignatureSigningAlgorithms(new ArrayList<>(config.getBlacklistedAlgorithms()));
        setSignatureAlgorithms(new ArrayList<>(config.getSignatureAlgorithms()));
        setSignatureReferenceDigestMethods(new ArrayList<>(config.getSignatureReferenceDigestMethods()));
        getSignatureReferenceDigestMethods().remove("http://www.w3.org/2001/04/xmlenc#sha512");
        setSignatureCanonicalizationAlgorithm(config.getSignatureCanonicalizationAlgorithm());
	}


	@Override
	public Resource getKeystoreResource() {
		return keyStoreResourceFromDatabase;
	}


	@Override
	public Resource getIdentityProviderMetadataResource() {
		return identityProviderMetadataResourceFromDatabase;
	}


	/**
	 * Initializes the key store and the key store resource.
	 */
	private void initializeKeyStore() {
		this.keyStoreResourceFromDatabase = new ByteArrayResource(keystoreBinaryData);
	}
 
	/**
	 * Initializes the IdP resource.
	 */
	private void initializeIdentityProviderMetadata() {
		final InputStream is = new CharSequenceInputStream(identityProviderMetadata, "UTF-8");
		this.identityProviderMetadataResourceFromDatabase = new InputStreamResource(is);
	}

}

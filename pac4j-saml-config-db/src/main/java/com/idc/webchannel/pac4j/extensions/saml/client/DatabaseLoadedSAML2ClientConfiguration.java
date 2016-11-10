package com.idc.webchannel.pac4j.extensions.saml.client;

import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.io.Resource;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.pac4j.saml.exceptions.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.DbLoadedSamlClientConfigurationDto;
import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;
import com.idc.webchannel.pac4j.extensions.saml.io.InMemoryByteArrayResource;
import com.idc.webchannel.pac4j.extensions.saml.io.InMemoryStringResource;


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

	/** SLF4J logger. */
	private final Logger logger = LoggerFactory.getLogger(DatabaseLoadedSAML2ClientConfiguration.class);
	
	/** DAO to read SAML client configurations. Should be shared by all configuration instances. */
	private final SamlClientDao dao;
	
	/** Binary data of a JKS keystore. */
	private byte[] keystoreBinaryData;

    /** IdP metadata. */
    private String identityProviderMetadata;

	/** SAML Client name. Comes in {@link #init(String, WebContext)}. */
	private String clientName;

	/** The key store created from the binary data loaded from the database. Initialized in {@link #init(String, WebContext)}. */
    private KeyStore keyStoreFromDatabase;
    
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


	@Override
    public DatabaseLoadedSAML2ClientConfiguration clone() {
        return (DatabaseLoadedSAML2ClientConfiguration) super.clone();
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
		setDestinationBindingType(loaded.getDestinationBindingType());
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
		
		CommonHelper.assertNotNull("keyStoreFromDatabase", this.keyStoreFromDatabase);
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
	public KeyStore getKeyStore() {
		return keyStoreFromDatabase;
	}

	@Override
	public Resource getKeystoreResource() {
		return keyStoreResourceFromDatabase;
	}

	@Override
	public String getKeystorePath() {
		return null;
	}
	
	@Override
	public String getIdentityProviderMetadataPath() {
		return null;
	}

	@Override
	public Resource getIdentityProviderMetadataResource() {
		return identityProviderMetadataResourceFromDatabase;
	}

	
	/**
	 * Initializes the key store and the key store resource.
	 */
	private void initializeKeyStore() {
        try {
            this.keyStoreResourceFromDatabase = new InMemoryByteArrayResource(keystoreBinaryData);

            String keyStoreAlias = getKeyStoreAlias();
        	String keyStoreType = getKeyStoreType();
        	final String keyStorePassword = getKeystorePassword();
        	
            Security.addProvider(new BouncyCastleProvider());

            if (CommonHelper.isBlank(keyStoreAlias)) {
            	keyStoreAlias = getClass().getSimpleName();
            	setKeystoreAlias(keyStoreAlias);
                logger.warn("Using keystore alias {}.", keyStoreAlias);
            }

            if (CommonHelper.isBlank(keyStoreType)) {
                keyStoreType = KeyStore.getDefaultType();
                logger.warn("Using keystore type {}.", keyStoreType);
            }

            final KeyStore ks = KeyStore.getInstance(keyStoreType);
            final char[] password = keyStorePassword.toCharArray();
            ks.load(this.keyStoreResourceFromDatabase.getInputStream(), password);

            this.keyStoreFromDatabase = ks;
        } catch (final Exception e) {
            throw new SAMLException("Could not create keystore.", e);
        }
	}

 
	/**
	 * Initializes the IdP resource.
	 */
	private void initializeIdentityProviderMetadata() {
		this.identityProviderMetadataResourceFromDatabase = new InMemoryStringResource(identityProviderMetadata);
	}
	
}

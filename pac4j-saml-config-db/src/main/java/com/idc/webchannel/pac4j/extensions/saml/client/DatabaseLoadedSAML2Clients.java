package com.idc.webchannel.pac4j.extensions.saml.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;


/**
 * A special version of {@link Clients} that dynamically loads SAML2 Clients from a database. No other clients are supported.
 * 
 * @author jkacer
 * @since 1.9.0
 */
public class DatabaseLoadedSAML2Clients extends Clients {
    
	private final Logger logger = LoggerFactory.getLogger(DatabaseLoadedSAML2Clients.class);
	
    /** DAO reading definitions of SAML clients from a database. */
    private final SamlClientDao samlClientDao;

    /** Loaded clients. */
	private List<Client> dynamicallyLoadedClients;

    
	
	public DatabaseLoadedSAML2Clients(final SamlClientDao samlClientDao) {
		super();
		this.samlClientDao = samlClientDao;
	}
	
	
    /**
     * {@inheritDoc}
     * 
     * The clients are first loaded from the database, then set up as usual.
     */
    @Override
    protected void internalInit() {
    	final Set<String> names = new HashSet<>();
        final List<SAML2Client> loadedClients = loadClientsInternal();
        CommonHelper.assertNotNull("loadedClients", loadedClients);
    	
        for (final SAML2Client client : loadedClients) {
            final String name = client.getName();
            final String lowerName = name.toLowerCase();
            if (names.contains(lowerName)) {
                throw new TechnicalException("Duplicate name in clients: " + name);
            }
            names.add(lowerName);
            updateCallbackUrlOfIndirectClient(client);

            final BaseClient baseClient = (BaseClient) client;
            final List<AuthorizationGenerator> ag = getAuthorizationGenerators();
            if (!ag.isEmpty()) {
                baseClient.addAuthorizationGenerators(ag);
            }
        }
        
        this.dynamicallyLoadedClients = new ArrayList<>(loadedClients);
    }
	

	/* (non-Javadoc)
	 * @see org.pac4j.core.client.Clients#getClients()
	 */
	@Override
	public List<Client> getClients() {
		init(); // We must assure the clients are loaded from DB first.
		return this.dynamicallyLoadedClients; // Never null, can be an empty list
	}

	
	/**
	 * An internal method that loads client definitions.
	 * 
	 * @return A list of loaded clients. Never {@code null} but an empty list is possible.
	 */
	protected List<SAML2Client> loadClientsInternal() {
		final List<SAML2Client> loaded = new ArrayList<>();

		final List<String> clientNames = samlClientDao.loadClientNames();
		for (final String name: clientNames) {
			final DatabaseLoadedSAML2ClientConfiguration configuration = new DatabaseLoadedSAML2ClientConfiguration(samlClientDao); // AbstractSAML2ClientConfiguration !!! 
			final SAML2Client client = new SAML2Client(configuration);
			client.setName(name);
			loaded.add(client);
		}
		
		logger.info("Dynamically loaded {} SAML clients: {}", loaded.size(), clientNames);
		return loaded;
	}
	
	
	@Override
	public void setClients(List<Client> clients) {
		throw new UnsupportedOperationException("Method setClients(List<Client> clients) is not implemented.");
	}

	@Override
	public void setClients(Client... clients) {
		throw new UnsupportedOperationException("Method setClients(Client... clients) is not implemented.");
	}

}

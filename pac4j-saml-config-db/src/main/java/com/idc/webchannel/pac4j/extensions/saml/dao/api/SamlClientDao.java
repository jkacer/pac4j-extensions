package com.idc.webchannel.pac4j.extensions.saml.dao.api; 

import java.util.List;


/**
 * DAO to manipulate SAML Client Configurations stored in a data source.
 * 
 * @author jkacer
 * @since 1.9.0
 */
public interface SamlClientDao {

	/**
	 * Finds names of all existing clients.
	 * 
	 * @return A list of names.
	 */
	public List<String> loadClientNames();
	
	
	/**
	 * Loads all existing SAML Client Configurations.
	 * 
	 * @return A list of configurations.
	 */
	public List<DbLoadedSamlClientConfigurationDto> loadAllClients();
	
	
	/**
	 * Loads a single SAML Client Configuration.
	 * 
	 * @param clientName
	 *            Name of the client.
	 * 
	 * @return A single configuration for the desired client or {@code null} of no such configuration exists.
	 */
	public DbLoadedSamlClientConfigurationDto loadClient(String clientName);
	
}

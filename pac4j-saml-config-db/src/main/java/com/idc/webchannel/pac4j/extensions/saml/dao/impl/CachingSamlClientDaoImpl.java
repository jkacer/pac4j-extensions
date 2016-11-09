package com.idc.webchannel.pac4j.extensions.saml.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idc.webchannel.pac4j.extensions.saml.dao.api.DbLoadedSamlClientConfigurationDto;
import com.idc.webchannel.pac4j.extensions.saml.dao.api.SamlClientDao;



/**
 * <p>Implementation of {@link SamlClientDao} providing very simple caching.</p>
 * 
 * <p>It bulk-loads all values just once, keeps them in memory forever, then returns the cached values on each call. It delegates bulk-reading
 * to another implementation of the same DAO type.</p>
 * 
 * @author jkacer
 * @since 1.9.0
 */
public class CachingSamlClientDaoImpl implements SamlClientDao {

	/** A DAO that performs actual reading. */
	private final SamlClientDao realDao;

	/** Cached configurations, read just once. */
	private Map<String, DbLoadedSamlClientConfigurationDto> cachedConfigurations;
	

	// ------------------------------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Creates a new caching DAO.
	 * 
	 * @param realDao
	 *            A real DAO, which performs actual read operations.
	 */
	public CachingSamlClientDaoImpl(final SamlClientDao realDao) {
		super();
		if (realDao == null) {
			throw new IllegalArgumentException("Real DAO must not be null.");
		}
		this.realDao = realDao;
		this.cachedConfigurations = null;
	}

	
	/* (non-Javadoc)
	 * @see org.pac4j.saml.dbclient.dao.api.SamlClientDao#loadClientNames()
	 */
	@Override
	public List<String> loadClientNames() {
		checkAndLoadFromRealDao();
		Set<String> keys = cachedConfigurations.keySet();
		return new ArrayList<String>(keys);
	}
	

	/* (non-Javadoc)
	 * @see org.pac4j.saml.dbclient.dao.api.SamlClientDao#loadAllClients()
	 */
	@Override
	public List<DbLoadedSamlClientConfigurationDto> loadAllClients() {
		checkAndLoadFromRealDao();
		Collection<DbLoadedSamlClientConfigurationDto> values = cachedConfigurations.values();
		return new ArrayList<DbLoadedSamlClientConfigurationDto>(values);
	}

	
	/* (non-Javadoc)
	 * @see org.pac4j.saml.dbclient.dao.api.SamlClientDao#loadClient(java.lang.String)
	 */
	@Override
	public DbLoadedSamlClientConfigurationDto loadClient(String clientName) {
		checkAndLoadFromRealDao();
		return cachedConfigurations.get(clientName);
	}

	
	/**
	 * Atomically checks and loads all configurations from the real DAO, if they have not been read yet.
	 * 
	 * Assures that just one reading is done.
	 */
	private synchronized void checkAndLoadFromRealDao() {
		if (cachedConfigurations == null) {
			List<DbLoadedSamlClientConfigurationDto> allLoaded = realDao.loadAllClients();
			cachedConfigurations = new HashMap<String, DbLoadedSamlClientConfigurationDto>();
			for (DbLoadedSamlClientConfigurationDto single: allLoaded) {
				cachedConfigurations.put(single.getClientName(), single);
			}
		}
	}

}

package com.idc.webchannel.pac4j.extensions.saml.dao.api;


/**
 * A simple DTO to carry data from the database to the application.
 * 
 * @author jkacer
 * @since 1.9.0
*/
public class DbLoadedSamlClientConfigurationDto {

	private String clientName;
	private String environment;
	private byte[] keystoreBinaryData;
    private String keystorePassword;
    private String keystoreAlias;
    private String privateKeyPassword;
    private String identityProviderMetadata;
    private String identityProviderEntityId;
    private String serviceProviderEntityId;
    private int maximumAuthenticationLifetime;
    private String destinationBindingType;

    
    public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public byte[] getKeystoreBinaryData() {
		return (keystoreBinaryData == null) ? null : keystoreBinaryData.clone();
	}
	public void setKeystoreBinaryData(byte[] keystoreBinaryData) {
		this.keystoreBinaryData = (keystoreBinaryData == null) ? null : keystoreBinaryData.clone();
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getKeystoreAlias() {
		return keystoreAlias;
	}
	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}
	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}
	public void setPrivateKeyPassword(String privateKeyPassword) {
		this.privateKeyPassword = privateKeyPassword;
	}
	public String getIdentityProviderMetadata() {
		return identityProviderMetadata;
	}
	public void setIdentityProviderMetadata(String identityProviderMetadata) {
		this.identityProviderMetadata = identityProviderMetadata;
	}
	public String getIdentityProviderEntityId() {
		return identityProviderEntityId;
	}
	public void setIdentityProviderEntityId(String identityProviderEntityId) {
		this.identityProviderEntityId = identityProviderEntityId;
	}
	public String getServiceProviderEntityId() {
		return serviceProviderEntityId;
	}
	public void setServiceProviderEntityId(String serviceProviderEntityId) {
		this.serviceProviderEntityId = serviceProviderEntityId;
	}
	public int getMaximumAuthenticationLifetime() {
		return maximumAuthenticationLifetime;
	}
	public void setMaximumAuthenticationLifetime(int maximumAuthenticationLifetime) {
		this.maximumAuthenticationLifetime = maximumAuthenticationLifetime;
	}
	public String getDestinationBindingType() {
		return destinationBindingType;
	}
	public void setDestinationBindingType(String destinationBindingType) {
		this.destinationBindingType = destinationBindingType;
	}

}

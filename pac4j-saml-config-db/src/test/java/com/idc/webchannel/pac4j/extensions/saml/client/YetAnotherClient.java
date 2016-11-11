package com.idc.webchannel.pac4j.extensions.saml.client;

import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.AnonymousProfile;


/**
 * A dummy client just for test purposes.
 * 
 * @author jkacer
 */
public class YetAnotherClient implements Client<AnonymousCredentials, AnonymousProfile> {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public HttpAction redirect(WebContext context) throws HttpAction {
		return null;
	}

	@Override
	public AnonymousCredentials getCredentials(WebContext context) throws HttpAction {
		return null;
	}

	@Override
	public AnonymousProfile getUserProfile(AnonymousCredentials credentials, WebContext context) throws HttpAction {
		return null;
	}

}

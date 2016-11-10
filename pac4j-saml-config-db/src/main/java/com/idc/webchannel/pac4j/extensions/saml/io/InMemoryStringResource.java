package com.idc.webchannel.pac4j.extensions.saml.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.io.Resource;

/**
 * Resource built up on top of a string.
 * 
 * @author jkacer
 * @since 1.9.0
 */
public class InMemoryStringResource implements Resource {

	private final String s;
	
	
	/**
	 * Creates a new resource based on a string.
	 * 
	 * @param s
	 *            A string. Can be {@code null} but the resource will "not exist" then.
	 */
	public InMemoryStringResource(final String s) {
		super();
		this.s = s;
	}

	/**
	 * The resource exists if the supplied string is not {@code null} or empty.
	 * 
	 * @return True if the string is not {@code null} or empty.
	 * 
	 * @see org.pac4j.core.io.Resource#exists()
	 */
	@Override
	public boolean exists() {
		return StringUtils.isNotBlank(s);
	}

	/**
	 * No physical file exists, hence no filename.
	 * 
	 * @return Always {@code null}.
	 * 
	 * @see org.pac4j.core.io.Resource#getFilename()
	 */
	@Override
	public String getFilename() {
		return null;
	}

	/**
	 * Creates a new stream atop the provided string.
	 * 
	 * @return A new input stream with the string as its content.
	 * 
	 * @see org.pac4j.core.io.Resource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (StringUtils.isNotBlank(s)) {
			return new ByteArrayInputStream(s.getBytes("UTF-8"));
		} else {
			throw new IOException("The resource has no backing string and no input stream can be provided.");
		}
	}

	/**
	 * No physical file exists.
	 * 
	 * @return Always {@code null}.
	 * 
	 * @see org.pac4j.core.io.Resource#getFile()
	 */
	@Override
	public File getFile() {
		return null;
	}

}

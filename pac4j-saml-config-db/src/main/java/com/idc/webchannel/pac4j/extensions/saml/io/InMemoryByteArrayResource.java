package com.idc.webchannel.pac4j.extensions.saml.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.pac4j.core.io.Resource;


/**
 * Resource built up on top of a byte array.
 * 
 * @author jkacer
 * @since 1.9.0
 */
public class InMemoryByteArrayResource implements Resource {

	private final byte[] byteArray;
	
	/**
	 * Creates a new resource based on a byte array.
	 * 
	 * @param byteArray
	 *            A byte array. Can be {@code null} but the resource will "not exist" then.
	 */
	public InMemoryByteArrayResource(final byte[] byteArray) {
		super();
		this.byteArray = byteArray;
	}

	/**
	 * The resource exists if the supplied byte array is not {@code null}.
	 * 
	 * @return True if the array is not {@code null}.
	 * 
	 * @see org.pac4j.core.io.Resource#exists()
	 */
	@Override
	public boolean exists() {
		return (byteArray != null);
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
	 * Creates a new stream atop the provided array.
	 * 
	 * @return A new byte array input stream.
	 * 
	 * @see org.pac4j.core.io.Resource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (byteArray != null) {
			return new ByteArrayInputStream(byteArray);
		} else {
			throw new IOException("The resource has no binary data and no input stream can be provided.");
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

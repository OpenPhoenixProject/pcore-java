package com.puppet.pcore.serialization;

import java.io.IOException;

/**
 * An instance capable of deserializing objects from some input source
 */
public interface Deserializer {
	/**
	 * Read the next object from the source
	 * @return the object that was read
	 * @throws IOException
	 */
	Object read() throws IOException;
}
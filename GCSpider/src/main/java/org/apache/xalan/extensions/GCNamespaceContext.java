package org.apache.xalan.extensions;

import org.apache.xalan.extensions.ExtensionNamespaceContext;

public class GCNamespaceContext extends ExtensionNamespaceContext {
    public static final String GC_PREFIX = "gc";
    public static final String GC_URI = "http://kiwiwings.de/gc";
    public static final String GROUNDSPEAK_PREFIX = "groundspeak";
    public static final String GROUNDSPEAK_URI = "http://www.groundspeak.com/cache/1/0/1";

    /**
     * Return the namespace uri for a given prefix
     */
    public String getNamespaceURI(String prefix) {
    	if (GC_PREFIX.equals(prefix)) {
    		return GC_URI;
    	}
    	if (GROUNDSPEAK_PREFIX.equals(prefix)) {
    		return GROUNDSPEAK_URI;
    	}
    	return super.getNamespaceURI(prefix);
    }
    
    /**
     * Return the prefix for a given namespace uri.
     */
    public String getPrefix(String namespace) {
    	if (GC_URI.equals(namespace)) {
    		return GC_PREFIX;
    	}
    	if (GROUNDSPEAK_URI.equals(namespace)) {
    		return GROUNDSPEAK_PREFIX;
    	}
    	return super.getPrefix(namespace);
    }
}

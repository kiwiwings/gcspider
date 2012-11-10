package org.apache.xalan.extensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;

public class GCXPathFunction extends XPathFunctionResolverImpl {
    public XPathFunction resolveFunction(QName qname, int arity) {
    	String uri = (qname == null ? null : qname.getNamespaceURI());
    	if (uri == null || !uri.startsWith("http://kiwiwings.de")) {
        	return super.resolveFunction(qname, arity);
    	}

		String methodName = qname.getLocalPart();
		String className = GCConversions.class.getName();
    
        ExtensionHandler handler = null;
        try {
            ExtensionHandler.getClassForName(className);
            handler = new ExtensionHandlerJavaClass(uri, "javaclass", className);
        } catch (ClassNotFoundException e) {
           return null;
        }
        return new XPathFunctionImpl(handler, methodName);
    
    }
}

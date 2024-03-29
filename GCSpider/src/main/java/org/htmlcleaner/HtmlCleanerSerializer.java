package org.htmlcleaner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>DOM serializer - creates xml DOM.</p>
 */
public class HtmlCleanerSerializer {

    protected CleanerProperties props;
    protected boolean escapeXml = true;

    public HtmlCleanerSerializer(CleanerProperties props, boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    public HtmlCleanerSerializer(CleanerProperties props) {
        this(props, true);
    }

    public Document createDOM(TagNode rootNode) throws ParserConfigurationException {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		String ns = getNamespaceURI(rootNode);
		String name = rootNode.getName();
		
		DOMImplementation domi = factory.newDocumentBuilder().getDOMImplementation();
		Document document = domi.createDocument(ns, name, null);
		
		Element rootElement = document.getDocumentElement();
        setAttributes(rootNode, rootElement);

        createSubnodes(document, rootElement, rootNode.getChildren());

        return document;
    }

    private String getNamespaceURI(TagNode node) {
        String name = node.getName();
        boolean nsAware = props.isNamespacesAware();
        String prefix = Utils.getXmlNSPrefix(name);
        Map<String, String> nsDeclarations = node.getNamespaceDeclarations();
        String nsURI = null;
        if (prefix != null) {
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get(prefix);
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
                if (nsURI == null) {
                    nsURI = prefix;
                }
            } else {
                name = Utils.getXmlName(name);
            }
        } else {
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get("");
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
            }
        }
        return nsURI;
    }
    
    private Element createElement(TagNode node, Document document) {
    	String nsURI = getNamespaceURI(node);
    	String name = node.getName();
    	boolean nsAware = props.isNamespacesAware();
        if (nsAware && nsURI != null) {
            return document.createElementNS(nsURI, name);
        } else {
            return document.createElement(name);
        }
    }

    private void setAttributes(TagNode node, Element element) {
        for (Map.Entry<String, String> entry: node.getAttributes().entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (escapeXml) {
                attrValue = Utils.escapeXml(attrValue, props, true);
            }
            
            boolean isIdAttr = ("id".equals(attrName));
            
            String attPrefix = Utils.getXmlNSPrefix(attrName);
            if (attPrefix != null) {
                if (props.isNamespacesAware()) {
                    String nsURI = node.getNamespaceURIOnPath(attPrefix);
                    if (nsURI == null) {
                        nsURI = attPrefix;
                    }
                    element.setAttributeNS(nsURI, attrName, attrValue);
                    if (isIdAttr) {
                    	element.setIdAttributeNS(nsURI, attrName, true);
                    }
                } else {
                	attrName = Utils.getXmlName(attrName);
                    element.setAttribute(attrName, attrValue);
                    if (isIdAttr) {
                    	element.setIdAttribute(attrName, true);
                    }
                }
            } else {
                element.setAttribute(attrName, attrValue);
                if (isIdAttr) {
                	element.setIdAttribute(attrName, true);
                }
            }
        }
    }

    private void createSubnodes(Document document, Element element, List tagChildren) {
        if (tagChildren != null) {
            Iterator it = tagChildren.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof CommentNode) {
                    CommentNode commentNode = (CommentNode) item;
                    Comment comment = document.createComment( commentNode.getContent().toString() );
                    element.appendChild(comment);
                } else if (item instanceof ContentNode) {
                    String nodeName = element.getNodeName();
                    String content = item.toString();
                    boolean specialCase = props.isUseCdataForScriptAndStyle() &&
                                          ("script".equalsIgnoreCase(nodeName) || "style".equalsIgnoreCase(nodeName));
                    if (escapeXml && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }
                    element.appendChild( specialCase ? document.createCDATASection(content) : document.createTextNode(content) );
                } else if (item instanceof TagNode) {
                    TagNode subTagNode = (TagNode) item;
                    Element subelement = createElement(subTagNode, document);

                    setAttributes(subTagNode, subelement);

                    // recursively create subnodes
                    createSubnodes(document, subelement, subTagNode.getChildren());

                    element.appendChild(subelement);
                } else if (item instanceof List) {
                    List sublist = (List) item;
                    createSubnodes(document, element, sublist);
                }
            }
        }
    }

}
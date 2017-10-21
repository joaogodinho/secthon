package org.owasp.webgoat.plugin;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * Resolver in order to define parameter for XPATH expression.
 *
 */
@SuppressWarnings("static-method")
public class SimpleVariableResolver implements XPathVariableResolver {

	private final Map<QName, Object> vars = new HashMap<QName, Object>();

	public void addVariable(QName name, Object value) {
		vars.put(name, value);
	}

	public Object resolveVariable(QName variableName) {
		return vars.get(variableName);
	}

}
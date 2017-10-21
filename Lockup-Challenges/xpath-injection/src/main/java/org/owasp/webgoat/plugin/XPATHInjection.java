/**
 * 
 */

package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.HtmlColor;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.B;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.PRE;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.LessonAdapter;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.WebSession;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.xpath.XPathExpression;
import org.owasp.webgoat.plugin.SimpleVariableResolver;
import javax.xml.namespace.QName;

public class XPATHInjection extends LessonAdapter {

	private final static Integer DEFAULT_RANKING = new Integer(74);

	private final static String USERNAME = "Username";

	private final static String PASSWORD = "Password";

	protected Element createContent(WebSession s) {

		NodeList nodes = null;
		ElementContainer ec = new ElementContainer();

		try {

			Table t1 = new Table().setCellSpacing(0).setCellPadding(0).setBorder(0).setWidth("90%").setAlign("center");

			TR tr = new TR();
			t1.addElement(tr);

			tr = new TR();
			tr.addElement(new TD().addElement("*Required Fields").setWidth("30%").setColSpan(2).setAlign("left"));
			t1.addElement(tr);

			tr = new TR();
			tr.addElement(new TD().addElement("&nbsp").setWidth("30%").setColSpan(2).setAlign("left"));
			t1.addElement(tr);

			tr = new TR();
			tr.addElement(new TD(new B(new StringElement("*User Name: "))));

			Input input1 = new Input(Input.TEXT, USERNAME, "");
			tr.addElement(new TD(input1));
			t1.addElement(tr);

			tr = new TR();
			tr.addElement(new TD(new B(new StringElement("*Password: "))));

			Input input2 = new Input(Input.PASSWORD, PASSWORD, "");
			tr.addElement(new TD(input2));
			t1.addElement(tr);

			Element b = ECSFactory.makeButton("Submit");
			t1.addElement(new TR(new TD(b)));
			ec.addElement(t1);

			String username = s.getParser().getRawParameter(USERNAME, "");
			if (username == null || username.length() == 0) {
				ec.addElement(new P().addElement(new StringElement("Username is a required field")));
				return ec;
			}

			String password = s.getParser().getRawParameter(PASSWORD, "");
			if (password == null || password.length() == 0) {
				ec.addElement(new P().addElement(new StringElement("Password is a required field")));
				return ec;
			}

			String dir = LessonUtil.getLessonDirectory(s, this) + "/xml/" + "EmployeesData.xml";
			File d = new File(dir);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();
			InputSource inputSource = new InputSource(new FileInputStream(d));

			nodes = defendIt(username, password, xPath, inputSource);
			int nodesLength = nodes.getLength();

			Table t2 = null;
			if (nodesLength > 0) {
				t2 = new Table().setCellSpacing(0).setCellPadding(0).setBorder(1).setWidth("90%").setAlign("center");
				t2.setID("xpathTable");
				tr = new TR();
				tr.setBgColor(HtmlColor.GRAY);
				tr.addElement(new TD().addElement("Username"));
				tr.addElement(new TD().addElement("Account No."));
				tr.addElement(new TD().addElement("Salary"));
				t2.addElement(tr);
			}

			for (int i = 0; i < nodesLength; i++) {
				Node node = nodes.item(i);
				String[] arrTokens = node.getTextContent().split("[\\t\\s\\n]+");

				tr = new TR();
				tr.addElement(new TD().addElement(arrTokens[1]));
				tr.addElement(new TD().addElement(arrTokens[2]));
				tr.addElement(new TD().addElement(arrTokens[4]));
				t2.addElement(tr);

			}
			if (nodes.getLength() > 1) {
				makeSuccess(s);
			}
			if (t2 != null) {
				ec.addElement(new PRE());
				ec.addElement(t2);
			}

		} catch (IOException e) {
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}
		return ec;
	}

	protected Category getDefaultCategory() {
		return Category.INJECTION;
	}

	protected boolean getDefaultHidden() {
		return false;
	}

	protected Integer getDefaultRanking() {
		return DEFAULT_RANKING;
	}

	public String getTitle() {
		return "XPATH Injection";
	}

	// ============================================================================
	// DEFENSE CHALLENGE
	// ============================================================================
	private NodeList defendIt(String username, String password, XPath xPath, InputSource inputSource)
			throws XPathExpressionException {
		// String expression = "/employees/employee[loginID/text()='" + username + "' and passwd/text()='" + password
		// 		+ "']";
		// return (NodeList) xPath.evaluate(expression, inputSource, XPathConstants.NODESET);

		// Create and configure parameter resolver
		SimpleVariableResolver variableResolver = new SimpleVariableResolver();
		variableResolver.addVariable(new QName("username"), username);
		variableResolver.addVariable(new QName("password"), password);

		xPath.setXPathVariableResolver(variableResolver);
		XPathExpression xPathExpression = xPath.compile("/employees/employee[loginID/text()=$username and passwd/text()=$password]");

		// Apply expression on XML document
		return (NodeList) xPathExpression.evaluate(inputSource, XPathConstants.NODESET);
	}

}
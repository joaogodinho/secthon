
package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.B;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.LessonAdapter;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.ParameterNotFoundException;
import org.owasp.webgoat.session.WebSession;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandling extends LessonAdapter {

	/**
	 * Description of the Field
	 */
	protected final static String LOGOUT = "WACLogout";

	/**
	 * Description of the Field
	 */
	protected final static String PASSWORD = "Password";

	/**
	 * Description of the Field
	 */
	protected final static String USERNAME = "Username";

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	protected Element createContent(WebSession s) {
		boolean logout = s.getParser().getBooleanParameter(LOGOUT, false);

		if (logout) {
			s.setMessage("Goodbye!");
			s.eatCookies();

			return (makeLogin(s));
		}

		try {
			return defendit(s);
		} catch (Exception e) {
			s.setMessage("Error generating " + this.getClass().getName());
		}

		return (makeLogin(s));
	}

	public Element defendit(WebSession s) throws Exception {
		String username = "";
		String password = "";
		try {
			username = s.getParser().getRawParameter(USERNAME);
			password = s.getParser().getRawParameter(PASSWORD);

		} catch (ParameterNotFoundException e) {
			s.setMessage("Invalid username and password entered.");
			// if ((username != null) && (username.length() > 0) && username.equals("Smith")) {
			// 	return (makeUser(s, username, ""));
			// }
			return (makeLogin(s));
		}

		if ((username != null) && (username.length() > 0) && username.equals("Smith")) {
			if (password != null && (password.length() > 0) && password.equals("Smith")) {
				return (makeUser(s, username, ""));
			} else {
				s.setMessage("Invalid username and password entered.");
				return (makeLogin(s));
			}

		} else {
			s.setMessage("Invalid username and password entered.");
			return (makeLogin(s));
		}

	}

	/**
	 * Gets the category attribute of the FailOpenAuthentication object
	 * 
	 * @return The category value
	 */
	public Category getDefaultCategory() {
		return Category.ERROR_HANDLING;
	}

	/**
	 * Gets the hints attribute of the AuthenticateScreen object
	 * 
	 * @return The hints value
	 */
	protected List<String> getHints(WebSession s) {
		List<String> hints = new ArrayList<String>();
		return hints;
	}

	/**
	 * Gets the instructions attribute of the FailOpenAuthentication object
	 * 
	 * @return The instructions value
	 */
	public String getInstructions(WebSession s) {
		return "You can safely sign in into our secure (and fail safe) system... have no worries. (only user Smith is available)";
	}

	private final static Integer DEFAULT_RANKING = new Integer(20);

	protected Integer getDefaultRanking() {
		return DEFAULT_RANKING;
	}

	/**
	 * Gets the title attribute of the AuthenticateScreen object
	 * 
	 * @return The title value
	 */
	public String getTitle() {
		return ("Improper Error Handling");
	}

	/**
	 * Description of the Method
	 *
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	protected Element makeLogin(WebSession s) {
		ElementContainer ec = new ElementContainer();

		ec.addElement(new H1().addElement(getLabelManager().get("SignIn")));
		Table t = new Table().setCellSpacing(0).setCellPadding(2).setBorder(0).setWidth("90%").setAlign("center");
		t.setID("improperHandling");

		if (s.isColor()) {
			t.setBorder(1);
		}

		TR tr = new TR();
		tr.addElement(new TH().addElement(getLabelManager().get("WeakAuthenticationCookiePleaseSignIn")).setColSpan(2)
				.setAlign("left"));
		t.addElement(tr);

		tr = new TR();
		tr.addElement(new TD().addElement("*" + getLabelManager().get("RequiredFields")).setWidth("30%"));
		t.addElement(tr);

		tr = new TR();
		tr.addElement(new TD().addElement("&nbsp;").setColSpan(2));
		t.addElement(tr);

		TR row1 = new TR();
		TR row2 = new TR();
		row1.addElement(new TD(new B(new StringElement("*" + getLabelManager().get("UserName")))));
		row2.addElement(new TD(new B(new StringElement("*" + getLabelManager().get("Password")))));

		Input input1 = new Input(Input.TEXT, USERNAME, "");
		Input input2 = new Input(Input.PASSWORD, PASSWORD, "");
		row1.addElement(new TD(input1));
		row2.addElement(new TD(input2));
		t.addElement(row1);
		t.addElement(row2);

		Element b = ECSFactory.makeButton(getLabelManager().get("Login"));
		t.addElement(new TR(new TD(b)));
		ec.addElement(t);

		return (ec);
	}

	/**
	 * Description of the Method
	 *
	 * @param s
	 *            Description of the Parameter
	 * @param user
	 *            Description of the Parameter
	 * @param method
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception Exception
	 *                Description of the Exception
	 */
	protected Element makeUser(WebSession s, String user, String method) throws Exception {
		ElementContainer ec = new ElementContainer();
		ec.addElement(new P().addElement("Welcome " + user));
		ec.addElement(new P().addElement(ECSFactory.makeLink(getLabelManager().get("Logout"), LOGOUT, true)));
		ec.addElement(new P().addElement(ECSFactory.makeLink(getLabelManager().get("Refresh"), "", "")));

		return (ec);
	}

}

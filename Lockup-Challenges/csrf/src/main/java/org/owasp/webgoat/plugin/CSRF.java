
package org.owasp.webgoat.plugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.B;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.LessonAdapter;
import org.owasp.webgoat.session.DatabaseUtilities;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.WebSession;
import org.owasp.webgoat.util.HtmlEncoder;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import javax.servlet.http.HttpSession;

public class CSRF extends LessonAdapter {

	private static final String CSRFTOKEN = "CSRFToken";
	private static final int INVALID_TOKEN = 0;
	private final Random random;

	private final static String MESSAGE = "message";
	private final static int MESSAGE_COL = 3;
	private final static String NUMBER = "Num";
	private final static int NUM_COL = 1;
	private final static String TITLE = "title";
	private final static int TITLE_COL = 2;
	private static int count = 1;
	private final static int USER_COL = 4;

	public CSRF() {
		super();
		random = new SecureRandom();
	}

	/**
	 * Adds a feature to the Message attribute of the MessageBoardScreen object
	 * 
	 * @param s
	 *            The feature to be added to the Message attribute
	 */
	protected void addMessage(WebSession s, String title, String message) {
		try {
			Connection connection = DatabaseUtilities.getConnection(getNameroot(s.getUserName()),
					s.getWebgoatContext());

			String query = "INSERT INTO messages VALUES (?, ?, ?, ?, ? )";

			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, count++);
			statement.setString(2, title);
			statement.setString(3, message);
			statement.setString(4, s.getUserName());
			statement.setString(5, this.getClass().getName());
			statement.execute();

		} catch (Exception e) {
			s.setMessage("Could not add message to database");
		}
	}

	@Override
	protected Element createContent(WebSession s) {
		ElementContainer ec = new ElementContainer();
		HttpSession httpSession = s.getRequest().getSession();
		String title = HtmlEncoder.encode(s.getParser().getRawParameter(TITLE, ""));
		String message = s.getParser().getRawParameter(MESSAGE, "");

		if (!StringUtils.isBlank(title)) {
			// needs to add message
			Integer passedInToken = s.getParser().getIntParameter(CSRFTOKEN, INVALID_TOKEN);
			Integer sessionToken = (Integer) httpSession.getAttribute(CSRFTOKEN);
			if (defendIt(passedInToken, sessionToken)) {
				addMessage(s, title, message);

			} else {
				ec.addElement(new BR());
				ec.addElement(new Div().addElement(new P().addElement("Invalid token").setID("error"))
						.setStyle("color: #5ac4f3; font-weight: bold;"));
			}

		}

		ec.addElement(makeInput(s));
		ec.addElement(new HR());
		ec.addElement(makeCurrent(s));
		ec.addElement(new HR());
		ec.addElement(makeList(s));

		int token = INVALID_TOKEN;
		while (token == INVALID_TOKEN) {
			token = random.nextInt();
		}
		httpSession.setAttribute(CSRFTOKEN, token);
		ec.addElement(new Input(Input.hidden, CSRFTOKEN, token));

		return ec;
	}

	private boolean defendIt(Integer passedInToken, Integer sessionToken) {
		// return true;
		if(passedInToken == sessionToken)
			return true;
		else
			return false;
	}

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	protected Element makeInput(WebSession s) {

		Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);
		TR row1 = new TR();
		TR row2 = new TR();
		row1.addElement(new TD(new StringElement("Title: ")));

		Input inputTitle = new Input(Input.TEXT, TITLE, "");
		row1.addElement(new TD(inputTitle));

		TD item1 = new TD();
		item1.setVAlign("TOP");
		item1.addElement(new StringElement("Message: "));
		row2.addElement(item1);

		TD item2 = new TD();
		TextArea ta = new TextArea(MESSAGE, 12, 60);
		ta.addAttribute("wrap", "soft");
		item2.addElement(ta);
		row2.addElement(item2);
		t.addElement(row1);
		t.addElement(row2);

		Element b = ECSFactory.makeButton("Submit");
		ElementContainer ec = new ElementContainer();
		ec.addElement(t);

		ec.addElement(new P().addElement(b));

		return (ec);
	}

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public Element makeList(WebSession s) {
		Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);

		try {
			Connection connection = DatabaseUtilities.getConnection(getNameroot(s.getUserName()),
					s.getWebgoatContext());

			String query = "SELECT * FROM messages WHERE user_name LIKE ? and lesson_type = ?";
			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, getNameroot(s.getUserName()) + "%");
			statement.setString(2, getClass().getName());
			ResultSet results = statement.executeQuery();

			if ((results != null) && (results.first() == true)) {
				results.beforeFirst();

				for (int i = 0; results.next(); i++) {
					String link = "<a href='" + getLink() + "//" + results.getInt(NUM_COL) + "' style='cursor:hand'>"
							+ results.getString(TITLE_COL) + "</a>";
					TD td = new TD().addElement(link);
					TR tr = new TR().addElement(td);
					t.addElement(tr);
				}
			}
		} catch (Exception e) {
			s.setMessage("Error while getting message list.");
		}

		ElementContainer ec = new ElementContainer();
		ec.addElement(new H1("Message List"));
		ec.addElement(t);
		String transferFunds = s.getParser().getRawParameter("transferFunds", "");
		if (transferFunds.length() != 0) {
			makeSuccess(s);
		}

		return (ec);
	}

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	protected Element makeCurrent(WebSession s) {
		ElementContainer ec = new ElementContainer();

		try {
			int messageNum = s.getParser().getIntParameter(NUMBER, 0);

			Connection connection = DatabaseUtilities.getConnection(getNameroot(s.getUserName()),
					s.getWebgoatContext());

			String query = "SELECT * FROM messages WHERE user_name LIKE ? and num = ? and lesson_type = ?";
			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, getNameroot(s.getUserName()) + "%");
			statement.setInt(2, messageNum);
			statement.setString(3, this.getClass().getName());
			ResultSet results = statement.executeQuery();

			if ((results != null) && results.first()) {
				ec.addElement(new H1("Message Contents For: " + results.getString(TITLE_COL)));
				Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);
				TR row1 = new TR(new TD(new B(new StringElement("Title:"))));
				row1.addElement(new TD(new StringElement(results.getString(TITLE_COL))));
				t.addElement(row1);

				String messageData = results.getString(MESSAGE_COL);
				TR row2 = new TR(new TD(new B(new StringElement("Message:"))));
				row2.addElement(new TD(new StringElement(messageData)));
				t.addElement(row2);

				TR row3 = new TR(new TD(new StringElement("Posted By:")));
				row3.addElement(new TD(new StringElement(results.getString(USER_COL))));
				t.addElement(row3);

				ec.addElement(t);

			} else {
				if (messageNum != 0) {
					ec.addElement(new P().addElement("Could not find message " + messageNum));
				}
			}

		} catch (Exception e) {
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}

		return (ec);
	}

	@Override
	protected Category getDefaultCategory() {
		return Category.CSRF;
	}

	private final static Integer DEFAULT_RANKING = new Integer(121);

	@Override
	protected Integer getDefaultRanking() {

		return DEFAULT_RANKING;
	}

	/**
	 * Gets the title attribute of the MessageBoardScreen object
	 * 
	 * @return The title value
	 */
	public String getTitle() {
		return ("Cross Site Request Forgery (CSRF)");
	}

	private static String getNameroot(String name) {
		String nameroot = name;
		if (nameroot.indexOf('-') != -1) {
			nameroot = nameroot.substring(0, nameroot.indexOf('-'));
		}
		return nameroot;
	}

}

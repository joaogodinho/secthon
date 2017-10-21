
package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.PRE;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.SequentialLessonAdapter;
import org.owasp.webgoat.session.DatabaseUtilities;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.WebSession;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;

public class SqlStringInjection extends SequentialLessonAdapter {
	private final static String ACCT_NAME = "account_name";

	private String accountName;

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	protected Element createContent(WebSession s) {
		return super.createStagedContent(s);
	}

	protected Element doStage1(WebSession s) throws Exception {
		return makequery(s);
	}

	protected Element makequery(WebSession s) {
		ElementContainer ec = new ElementContainer();

		try {
			Connection connection = DatabaseUtilities.getConnection(s);

			ec.addElement(makeAccountLine(s));

			try {
				ResultSet results = defendit(connection);

				if ((results != null) && (results.first() == true)) {
					ResultSetMetaData resultsMetaData = results.getMetaData();
					ec.addElement(DatabaseUtilities.writeTable(results, resultsMetaData, "sqlinjection"));
					results.last();

				} else {
					ec.addElement(new PRE("No Results found!"));
				}
			} catch (SQLException sqle) {
				ec.addElement(new P().addElement(sqle.getMessage()));
				sqle.printStackTrace();
			}
		} catch (Exception e) {
			s.setMessage(getLabelManager().get("ErrorGenerating") + this.getClass().getName());
			e.printStackTrace();
		}

		return (ec);
	}

	private ResultSet defendit(Connection connection) throws SQLException {
		 /* String query = "SELECT * FROM user_data WHERE last_name = '" +
		 accountName + "'";
		
		 Statement statement =
		 connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		 ResultSet.CONCUR_READ_ONLY);
		 return statement.executeQuery(query);
		 */

		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user_data WHERE last_name ='?'");
		stmt.setString(1, accountName);
		return stmt.executeQuery();
	}

	protected Element makeAccountLine(WebSession s) {
		ElementContainer ec = new ElementContainer();
		ec.addElement(new P().addElement(getLabelManager().get("EnterLastName")));

		accountName = s.getParser().getRawParameter(ACCT_NAME, "Your Name");
		Input input = new Input(Input.TEXT, ACCT_NAME, accountName.toString());
		ec.addElement(input);

		Element b = ECSFactory.makeButton(getLabelManager().get("Search"));
		ec.addElement(b);

		return ec;

	}

	/**
	 * Gets the category attribute of the SqNumericInjection object
	 * 
	 * @return The category value
	 */
	protected Category getDefaultCategory() {
		return Category.INJECTION;
	}

	/**
	 * Gets the hints attribute of the DatabaseFieldScreen object
	 * 
	 * @return The hints value
	 */
	protected List<String> getHints(WebSession s) {
		List<String> hints = new ArrayList<String>();
		return hints;
	}

	private final static Integer DEFAULT_RANKING = new Integer(75);

	protected Integer getDefaultRanking() {
		return DEFAULT_RANKING;
	}

	/**
	 * Gets the title attribute of the DatabaseFieldScreen object
	 * 
	 * @return The title value
	 */
	public String getTitle() {
		return ("SQL Injection");
	}

}

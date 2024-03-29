
package org.owasp.webgoat.plugin.crosssitescripting;

import org.owasp.webgoat.plugin.GoatHillsFinancial.DefaultLessonAction;
import org.owasp.webgoat.plugin.GoatHillsFinancial.Employee;
import org.owasp.webgoat.plugin.GoatHillsFinancial.GoatHillsFinancial;
import org.owasp.webgoat.session.ParameterNotFoundException;
import org.owasp.webgoat.session.UnauthenticatedException;
import org.owasp.webgoat.session.UnauthorizedException;
import org.owasp.webgoat.session.ValidationException;
import org.owasp.webgoat.session.WebSession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ViewProfileCrossSiteScripting extends DefaultLessonAction {

	public ViewProfileCrossSiteScripting(GoatHillsFinancial lesson, String lessonName, String actionName) {
		super(lesson, lessonName, actionName);
	}

	public void handleRequest(WebSession s)
			throws ParameterNotFoundException, UnauthenticatedException, UnauthorizedException, ValidationException {
		getLesson().setCurrentAction(s, getActionName());

		if (isAuthenticated(s)) {
			int userId = getIntSessionAttribute(s, getLessonName() + "." + CrossSiteScripting.USER_ID);
			int employeeId = -1;
			try {
				// User selected employee
				employeeId = s.getParser().getIntParameter(CrossSiteScripting.EMPLOYEE_ID);
			} catch (ParameterNotFoundException e) {
				// May be an internally selected employee
				employeeId = getIntRequestAttribute(s, getLessonName() + "." + CrossSiteScripting.EMPLOYEE_ID);
			}

			Employee employee = getEmployeeProfile(s, userId, employeeId);
			setSessionAttribute(s, getLessonName() + "." + CrossSiteScripting.EMPLOYEE_ATTRIBUTE_KEY, employee);

			updateLessonStatus(s, employee);
		} else
			throw new UnauthenticatedException();
	}

	public String getNextPage(WebSession s) {
		return CrossSiteScripting.VIEWPROFILE_ACTION;
	}

	public Employee getEmployeeProfile(WebSession s, int userId, int subjectUserId) throws UnauthorizedException {
		Employee profile = null;

		// Query the database for the profile data of the given employee
		try {
			String query = "SELECT * FROM employee WHERE userid = " + subjectUserId;
			try {
				Statement answer_statement = WebSession.getConnection(s)
						.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet answer_results = answer_statement.executeQuery(query);
				if (answer_results.next()) {

					// Note: Do NOT get the password field.
					profile = new Employee(answer_results.getInt("userid"), answer_results.getString("first_name"),
							answer_results.getString("last_name"), answer_results.getString("ssn"),
							answer_results.getString("title"), answer_results.getString("phone"),
							answer_results.getString("address1"), answer_results.getString("address2"),
							answer_results.getInt("manager"), answer_results.getString("start_date"),
							answer_results.getInt("salary"), answer_results.getString("ccn"),
							answer_results.getInt("ccn_limit"), answer_results.getString("disciplined_date"),
							answer_results.getString("disciplined_notes"),
							answer_results.getString("personal_description"));
					/*
					 * System.out.println("Retrieved employee from db: " +
					 * profile.getFirstName() + " " + profile.getLastName() +
					 * " (" + profile.getId() + ")");
					 */}
			} catch (SQLException sqle) {
				s.setMessage("Error getting employee profile");
				sqle.printStackTrace();
			}
		} catch (Exception e) {
			s.setMessage("Error getting employee profile");
			e.printStackTrace();
		}

		return profile;
	}

	public Employee getEmployeeProfile_BACKUP(WebSession s, int userId, int subjectUserId)
			throws UnauthorizedException {
		// Query the database to determine if this employee has access to this
		// function
		// Query the database for the profile data of the given employee if
		// "owned" by the given
		// user

		Employee profile = null;

		// Query the database for the profile data of the given employee
		try {
			String query = "SELECT * FROM employee WHERE userid = " + subjectUserId;

			try {
				Statement answer_statement = WebSession.getConnection(s)
						.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet answer_results = answer_statement.executeQuery(query);
				if (answer_results.next()) {
					// Note: Do NOT get the password field.
					profile = new Employee(answer_results.getInt("userid"), answer_results.getString("first_name"),
							answer_results.getString("last_name"), answer_results.getString("ssn"),
							answer_results.getString("title"), answer_results.getString("phone"),
							answer_results.getString("address1"), answer_results.getString("address2"),
							answer_results.getInt("manager"), answer_results.getString("start_date"),
							answer_results.getInt("salary"), answer_results.getString("ccn"),
							answer_results.getInt("ccn_limit"), answer_results.getString("disciplined_date"),
							answer_results.getString("disciplined_notes"),
							answer_results.getString("personal_description"));

					/*
					 * System.out.println("Retrieved employee from db: " +
					 * profile.getFirstName() + " " + profile.getLastName() +
					 * " (" + profile.getId() + ")");
					 */}
			} catch (SQLException sqle) {
				s.setMessage("Error getting employee profile");
				sqle.printStackTrace();
			}
		} catch (Exception e) {
			s.setMessage("Error getting employee profile");
			e.printStackTrace();
		}

		return profile;
	}

	private void updateLessonStatus(WebSession s, Employee employee) {
		String stage = getStage(s);
		int userId = -1;
		try {
			userId = getIntSessionAttribute(s, getLessonName() + "." + CrossSiteScripting.USER_ID);
		} catch (ParameterNotFoundException pnfe) {
		}
		if (CrossSiteScripting.STAGE1.equals(stage)) {
			String address1 = employee.getAddress1().toLowerCase();
			if (userId != employee.getId() && address1.indexOf("<script>") > -1 && address1.indexOf("alert") > -1
					&& address1.indexOf("</script>") > -1) {
				setStageComplete(s, CrossSiteScripting.STAGE1);
			}
		}
	}

}

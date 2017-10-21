
package org.owasp.webgoat.plugin.crosssitescripting;

import org.apache.ecs.ElementContainer;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.plugin.GoatHillsFinancial.DeleteProfile;
import org.owasp.webgoat.plugin.GoatHillsFinancial.GoatHillsFinancial;
import org.owasp.webgoat.plugin.GoatHillsFinancial.LessonAction;
import org.owasp.webgoat.plugin.GoatHillsFinancial.ListStaff;
import org.owasp.webgoat.plugin.GoatHillsFinancial.Login;
import org.owasp.webgoat.plugin.GoatHillsFinancial.Logout;
import org.owasp.webgoat.plugin.GoatHillsFinancial.SearchStaff;
import org.owasp.webgoat.session.ParameterNotFoundException;
import org.owasp.webgoat.session.UnauthenticatedException;
import org.owasp.webgoat.session.UnauthorizedException;
import org.owasp.webgoat.session.ValidationException;
import org.owasp.webgoat.session.WebSession;
import org.owasp.webgoat.util.HtmlEncoder;

public class CrossSiteScripting extends GoatHillsFinancial {
	private final static Integer DEFAULT_RANKING = new Integer(100);

	public final static String STAGE1 = "Stored XSS";

	protected void registerActions(String className) {
		registerAction(new ListStaff(this, className, LISTSTAFF_ACTION));
		registerAction(new SearchStaff(this, className, SEARCHSTAFF_ACTION));
		registerAction(new ViewProfileCrossSiteScripting(this, className, VIEWPROFILE_ACTION));
		registerAction(new EditProfileCrossSiteScripting(this, className, EDITPROFILE_ACTION));
		registerAction(new EditProfileCrossSiteScripting(this, className, CREATEPROFILE_ACTION));

		registerAction(new Login(this, className, LOGIN_ACTION, getAction(LISTSTAFF_ACTION)));
		registerAction(new Logout(this, className, LOGOUT_ACTION, getAction(LOGIN_ACTION)));
		registerAction(
				new FindProfileCrossSiteScripting(this, className, FINDPROFILE_ACTION, getAction(VIEWPROFILE_ACTION)));
		registerAction(new UpdateProfileCrossSiteScripting(this, className, UPDATEPROFILE_ACTION,
				getAction(VIEWPROFILE_ACTION)));
		registerAction(new DeleteProfile(this, className, DELETEPROFILE_ACTION, getAction(LISTSTAFF_ACTION)));
	}

	/**
	 * Gets the category attribute of the CrossSiteScripting object
	 * 
	 * @return The category value
	 */
	public Category getDefaultCategory() {
		return Category.XSS;
	}

	/**
	 * Gets the instructions attribute of the ParameterInjection object
	 * 
	 * @return The instructions value
	 */
	public String getInstructions(WebSession s) {
		String instructions = "";

		if (!getLessonTracker(s).getCompleted()) {
			String stage = getStage(s);
			if (STAGE1.equals(stage)) {
				instructions = "Is there any XSS going on? <br>" + "The passwords for the accounts are the lower-case "
						+ "versions of their given names (e.g. the password for Tom Cat is \"tom\").";
			}
		}

		return instructions;
	}

	@Override
	public String[] getStages() {
		if (getWebgoatContext().isCodingExercises())
			return new String[] { STAGE1 };
		return new String[] { STAGE1 };
	}

	public void handleRequest(WebSession s) {
		if (s.getLessonSession(this) == null)
			s.openLessonSession(this);

		String requestedActionName = null;
		try {
			requestedActionName = s.getParser().getStringParameter("action");
		} catch (ParameterNotFoundException pnfe) {
			// Let them eat login page.
			requestedActionName = LOGIN_ACTION;
		}

		if (requestedActionName != null) {
			try {
				LessonAction action = getAction(requestedActionName);

				if (action != null) {
					if (!action.requiresAuthentication() || action.isAuthenticated(s)) {
						action.handleRequest(s);
						// setCurrentAction(s, action.getNextPage(s));
					}
				} else {
					setCurrentAction(s, ERROR_ACTION);
				}
			} catch (ParameterNotFoundException pnfe) {
				// System.out.println("Missing parameter");
				pnfe.printStackTrace();
				setCurrentAction(s, ERROR_ACTION);
			} catch (ValidationException ve) {
				// System.out.println("Validation failed");
				ve.printStackTrace();
				setCurrentAction(s, ERROR_ACTION);
			} catch (UnauthenticatedException ue) {
				s.setMessage("Login failed");
				// System.out.println("Authentication failure");
				ue.printStackTrace();
			} catch (UnauthorizedException ue2) {
				s.setMessage("You are not authorized to perform this function");
				// System.out.println("Authorization failure");
				ue2.printStackTrace();
			} catch (Exception e) {
				// All other errors send the user to the generic error page
				// System.out.println("handleRequest() error");
				e.printStackTrace();
				setCurrentAction(s, ERROR_ACTION);
			}
		}

		// All this does for this lesson is ensure that a non-null content
		// exists.
		setContent(new ElementContainer());
	}

	protected Integer getDefaultRanking() {
		return DEFAULT_RANKING;
	}

	/**
	 * Gets the title attribute of the CrossSiteScripting object
	 * 
	 * @return The title value
	 */
	public String getTitle() {
		return "Cross Site Scripting";
	}

	public String htmlEncode(WebSession s, String text) {
		return HtmlEncoder.encode(text);
	}

}

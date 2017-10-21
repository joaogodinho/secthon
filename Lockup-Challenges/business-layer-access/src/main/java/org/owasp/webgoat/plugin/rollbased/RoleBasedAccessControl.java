
package org.owasp.webgoat.plugin.rollbased;

import org.apache.ecs.ElementContainer;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.plugin.GoatHillsFinancial.DefaultLessonAction;
import org.owasp.webgoat.plugin.GoatHillsFinancial.FindProfile;
import org.owasp.webgoat.plugin.GoatHillsFinancial.GoatHillsFinancial;
import org.owasp.webgoat.plugin.GoatHillsFinancial.ListStaff;
import org.owasp.webgoat.plugin.GoatHillsFinancial.Login;
import org.owasp.webgoat.plugin.GoatHillsFinancial.Logout;
import org.owasp.webgoat.session.ParameterNotFoundException;
import org.owasp.webgoat.session.UnauthenticatedException;
import org.owasp.webgoat.session.UnauthorizedException;
import org.owasp.webgoat.session.ValidationException;
import org.owasp.webgoat.session.WebSession;


public class RoleBasedAccessControl extends GoatHillsFinancial {
    private final static Integer DEFAULT_RANKING = new Integer(125);

    public final static String STAGE1 = "Bypass Business Layer Access Control";

    protected void registerActions(String className) {
        registerAction(new ListStaff(this, className, LISTSTAFF_ACTION));
        registerAction(new ViewProfileRoleBasedAccessControl(this, className, VIEWPROFILE_ACTION));
        registerAction(new EditProfileRoleBasedAccessControl(this, className, EDITPROFILE_ACTION));

        // This action has not yet been implemented. None of the lessons require it.
        registerAction(new EditProfileRoleBasedAccessControl(this, className, CREATEPROFILE_ACTION));

        // These actions are special in that they chain to other actions.
        registerAction(new Login(this, className, LOGIN_ACTION, getAction(LISTSTAFF_ACTION)));
        registerAction(new Logout(this, className, LOGOUT_ACTION, getAction(LOGIN_ACTION)));
        registerAction(new FindProfile(this, className, FINDPROFILE_ACTION, getAction(VIEWPROFILE_ACTION)));
        registerAction(new UpdateProfileRoleBasedAccessControl(this, className, UPDATEPROFILE_ACTION, getAction(VIEWPROFILE_ACTION)));
        registerAction(new DeleteProfileRoleBasedAccessControl(this, className, DELETEPROFILE_ACTION, getAction(LISTSTAFF_ACTION)));
    }

    /**
     * Gets the category attribute of the CommandInjection object
     *
     * @return The category value
     */
    public Category getDefaultCategory() {
        return Category.ACCESS_CONTROL;
    }

    @Override
    public String[] getStages() {
        return new String[]{STAGE1};
    }

    /**
     * Gets the instructions attribute of the ParameterInjection object
     *
     * @return The instructions value
     */
    public String getInstructions(WebSession s) {
        return "Bypass Presentational Layer Access Control.<br />"
                + "As an empployee you can use Delete function.<br />"
                + "The passwords for users are their given names in lowercase (e.g. the password for Tom Cat is \"tom\").";
    }

    public void handleRequest(WebSession s) {
        // Here is where dispatching to the various action handlers happens.
        // It would be a good place verify authorization to use an action.

        if (s.getLessonSession(this) == null) {
            s.openLessonSession(this);
        }

        String requestedActionName = null;
        try {
            requestedActionName = s.getParser().getStringParameter("action");
        } catch (ParameterNotFoundException pnfe) {
            // Let them eat login page.
            requestedActionName = LOGIN_ACTION;
        }

        try {
            DefaultLessonAction action = (DefaultLessonAction) getAction(requestedActionName);
            if (action != null) {
                if (!action.requiresAuthentication()) {
                    // Access to Login does not require authentication.
                    action.handleRequest(s);
                } else {

                    defendIt(s, action);

                }
            } else {
                setCurrentAction(s, ERROR_ACTION);
            }
        } catch (ParameterNotFoundException pnfe) {
            pnfe.printStackTrace();
            setCurrentAction(s, ERROR_ACTION);
        } catch (ValidationException ve) {
            ve.printStackTrace();
            setCurrentAction(s, ERROR_ACTION);
        } catch (UnauthenticatedException ue) {
            s.setMessage("Login failed");
            ue.printStackTrace();
        } catch (UnauthorizedException ue2) {
            s.setMessage("You are not authorized to perform this function");

            setCurrentAction(s, ERROR_ACTION);
            ue2.printStackTrace();
        } catch (Exception e) {
            // All other errors send the user to the generic error page
            e.printStackTrace();
            setCurrentAction(s, ERROR_ACTION);
        }

        // All this does for this lesson is ensure that a non-null content exists.
        setContent(new ElementContainer());
    }

    protected Integer getDefaultRanking() {
        return DEFAULT_RANKING;
    }

    /**
     * Gets the title attribute of the DirectoryScreen object
     *
     * @return The title value
     */
    public String getTitle() {
        return "Bypass Business Layer Access Control";
    }

    private void defendIt(WebSession s, DefaultLessonAction action)
            throws UnauthorizedException, UnauthenticatedException, ParameterNotFoundException, ValidationException {
        
        String requestedActionName = s.getParser().getStringParameter("action");
        if (s.isAuthorizedInLesson(s.getUserIdInLesson(), requestedActionName)) {
            if (action.isAuthenticated(s)) {
                action.handleRequest(s);
            } else {
                throw new UnauthenticatedException();
            }
        }
    }
}

package org.owasp.webgoat.lessons;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Body;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Head;
import org.apache.ecs.html.Html;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.PRE;
import org.apache.ecs.html.Title;
import org.owasp.webgoat.session.ParameterNotFoundException;
import org.owasp.webgoat.session.Screen;
import org.owasp.webgoat.session.WebSession;
import org.owasp.webgoat.session.WebgoatContext;
import org.owasp.webgoat.session.WebgoatProperties;
import org.owasp.webgoat.util.BeanProvider;
import org.owasp.webgoat.util.LabelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

/**
 * ************************************************************************************************
 * <p>
 * This file is part of WebGoat, an Open Web Application Security Project
 * utility. For details, please see http://www.owasp.org/
 * <p>
 * Copyright (c) 2002 - 20014 Bruce Mayhew
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * Getting Source ==============
 * <p>
 * Source for this application is maintained at
 * https://github.com/WebGoat/WebGoat, a repository for free software projects.
 *
 * @author Bruce Mayhew <a href="http://code.google.com/p/webgoat">WebGoat</a>
 * @version $Id: $Id
 * @since October 28, 2003
 */
public abstract class AbstractLesson extends Screen implements Comparable<Object> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractLesson.class);

	/**
	 * Description of the Field
	 */
	public final static String ADMIN_ROLE = "admin";

	/**
	 * Constant <code>CHALLENGE_ROLE="challenge"</code>
	 */
	public final static String CHALLENGE_ROLE = "challenge";

	/**
	 * Description of the Field
	 */
	public final static String HACKED_ADMIN_ROLE = "hacked_admin";

	/**
	 * Description of the Field
	 */
	public final static String USER_ROLE = "user";

	private static int count = 1;

	private Integer id = null;

	final static IMG nextGrey = new IMG("images/right16.gif").setAlt("Next").setBorder(0).setHspace(0).setVspace(0);

	final static IMG previousGrey = new IMG("images/left14.gif").setAlt("Previous").setBorder(0).setHspace(0)
			.setVspace(0);

	private Integer ranking;

	private Category category;

	private boolean hidden;

	private Map<String, String> lessonPlanFileName = new HashMap<String, String>();

	private WebgoatContext webgoatContext;

	private LinkedList<String> availableLanguages = new LinkedList<String>();

	private String defaultLanguage = "en";

	private LabelManager labelManager = null;

	/**
	 * Constructor for the Lesson object
	 */
	public AbstractLesson() {
		// based on the class name derive an id so the screenId is fixed, should
		// not give clashes based on
		// the fact we use the classname and the size is limited
		id = abs(this.getClass().getSimpleName().hashCode());
	}

	/**
	 * <p>
	 * getName.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		String className = getClass().getName();
		return className.substring(className.lastIndexOf('.') + 1);
	}

	/**
	 * <p>
	 * Setter for the field <code>ranking</code>.
	 * </p>
	 *
	 * @param ranking
	 *            a {@link java.lang.Integer} object.
	 */
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	/**
	 * <p>
	 * Setter for the field <code>hidden</code>.
	 * </p>
	 *
	 * @param hidden
	 *            a boolean.
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * <p>
	 * update.
	 * </p>
	 *
	 * @param properties
	 *            a {@link org.owasp.webgoat.session.WebgoatProperties} object.
	 */
	public void update(WebgoatProperties properties) {
		String className = getClass().getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		setRanking(new Integer(
				properties.getIntProperty("lesson." + className + ".ranking", getDefaultRanking().intValue())));
		String categoryRankingKey = "category." + getDefaultCategory().getName() + ".ranking";
		// System.out.println("Category ranking key: " + categoryRankingKey);
		Category tempCategory = Category.getCategory(getDefaultCategory().getName());
		tempCategory.setRanking(new Integer(
				properties.getIntProperty(categoryRankingKey, getDefaultCategory().getRanking().intValue())));
		category = tempCategory;
		setHidden(properties.getBooleanProperty("lesson." + className + ".hidden", getDefaultHidden()));
		// System.out.println(className + " in " + tempCategory.getName() + "
		// (Category Ranking: " + tempCategory.getRanking() + " Lesson ranking:
		// " + getRanking() + ", hidden:" + hidden +")");
	}

	/**
	 * <p>
	 * isCompleted.
	 * </p>
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a boolean.
	 */
	public boolean isCompleted(WebSession s) {
		return getLessonTracker(s, this).getCompleted();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Description of the Method
	 */
	public int compareTo(Object obj) {
		return this.getRanking().compareTo(((AbstractLesson) obj).getRanking());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Description of the Method
	 */
	public boolean equals(Object obj) {
		return this.getScreenId() == ((AbstractLesson) obj).getScreenId();
	}

	/**
	 * Gets the category attribute of the Lesson object
	 *
	 * @return The category value
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * <p>
	 * getDefaultRanking.
	 * </p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	protected abstract Integer getDefaultRanking();

	/**
	 * <p>
	 * getDefaultCategory.
	 * </p>
	 *
	 * @return a {@link org.owasp.webgoat.lessons.Category} object.
	 */
	protected abstract Category getDefaultCategory();

	/**
	 * <p>
	 * getDefaultHidden.
	 * </p>
	 *
	 * @return a boolean.
	 */
	protected abstract boolean getDefaultHidden();

	/**
	 * <p>
	 * getSubmitMethod
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getSubmitMethod();

	/**
	 * Gets the fileMethod attribute of the Lesson class
	 *
	 * @param reader
	 *            Description of the Parameter
	 * @param methodName
	 *            Description of the Parameter
	 * @param numbers
	 *            Description of the Parameter
	 * @return The fileMethod value
	 */
	public static String getFileMethod(BufferedReader reader, String methodName, boolean numbers) {
		int count = 0;
		StringBuffer sb = new StringBuffer();
		boolean echo = false;
		boolean startCount = false;
		int parenCount = 0;

		try {
			String line;

			while ((line = reader.readLine()) != null) {
				if ((line.indexOf(methodName) != -1) && ((line.indexOf("public") != -1)
						|| (line.indexOf("protected") != -1) || (line.indexOf("private") != -1))) {
					echo = true;
					startCount = true;
				}

				if (echo && startCount) {
					if (numbers) {
						sb.append(pad(++count) + "    ");
					}

					sb.append(line + "\n");
				}

				if (echo && (line.indexOf("{") != -1)) {
					parenCount++;
				}

				if (echo && (line.indexOf("}") != -1)) {
					parenCount--;

					if (parenCount == 0) {
						startCount = false;
						echo = false;
					}
				}
			}

			reader.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

		return (sb.toString());
	}

	/**
	 * Reads text from a file into an ElementContainer. Each line in the file is
	 * represented in the ElementContainer by a StringElement. Each
	 * StringElement is appended with a new-line character.
	 *
	 * @param reader
	 *            Description of the Parameter
	 * @param numbers
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public static String readFromFile(BufferedReader reader, boolean numbers) {
		return (getFileText(reader, numbers));
	}

	/**
	 * Gets the fileText attribute of the Screen class
	 *
	 * @param reader
	 *            Description of the Parameter
	 * @param numbers
	 *            Description of the Parameter
	 * @return The fileText value
	 */
	public static String getFileText(BufferedReader reader, boolean numbers) {
		int count = 0;
		StringBuffer sb = new StringBuffer();

		try {
			String line;

			while ((line = reader.readLine()) != null) {
				if (numbers) {
					sb.append(pad(++count) + "  ");
				}
				sb.append(line + System.getProperty("line.separator"));
			}

			reader.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

		return (sb.toString());
	}

	/**
	 * Will this screen be included in an enterprise edition.
	 *
	 * @return The ranking value
	 */
	public boolean isEnterprise() {
		return false;
	}

	/**
	 * Gets the instructions attribute of the AbstractLesson object
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return The instructions value
	 */
	public abstract String getInstructions(WebSession s);

	/**
	 * Gets the lessonPlan attribute of the Lesson object
	 *
	 * @return The lessonPlan value
	 */
	public String getLessonName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Gets the title attribute of the HelloScreen object
	 *
	 * @return The title value
	 */
	public abstract String getTitle();

	/**
	 * Gets the content of lessonPlanURL
	 *
	 * @param s
	 *            The user's WebSession
	 * @return The HTML content of the current lesson plan
	 */
	public String getLessonPlan(WebSession s) {
		StringBuffer src = new StringBuffer();
		String lang = s.getCurrrentLanguage();

		try {
			// System.out.println("Loading lesson plan file: " +
			// getLessonPlanFileName());
			String filename = getLessonPlanFileName(lang);
			if (filename == null) {
				filename = getLessonPlanFileName(getDefaultLanguage());

			}

			src.append(readFromFile(new BufferedReader(new FileReader(filename)), false));

		} catch (Exception e) {
			// s.setMessage( "Could not find lesson plan for " +
			// getLessonName());
			src = new StringBuffer("Could not find lesson plan for: " + getLessonName() + " and language " + lang);

		}
		return src.toString();
	}

	/**
	 * Gets the ranking attribute of the Lesson object
	 *
	 * @return The ranking value
	 */
	public Integer getRanking() {
		if (ranking != null) {
			return ranking;
		} else {
			return getDefaultRanking();
		}
	}

	/**
	 * Gets the hidden value of the Lesson Object
	 *
	 * @return The hidden value
	 */
	public boolean getHidden() {
		return this.hidden;
	}

	/**
	 * Gets the role attribute of the AbstractLesson object
	 *
	 * @return The role value
	 */
	public String getRole() {
		// FIXME: Each lesson should have a role assigned to it. Each
		// user/student
		// should also have a role(s) assigned. The user would only be allowed
		// to see lessons that correspond to their role. Eventually these roles
		// will be stored in the internal database. The user will be able to
		// hack
		// into the database and change their role. This will allow the user to
		// see the admin screens, once they figure out how to turn the admin
		// switch on.
		return USER_ROLE;
	}

	/**
	 * Gets the uniqueID attribute of the AbstractLesson object
	 *
	 * @return The uniqueID value
	 */
	public int getScreenId() {
		return id.intValue();
	}

	/**
	 * <p>
	 * Returns the default "path" portion of a lesson's URL.
	 * </p>
	 * <p>
	 * Legacy webgoat lesson links are of the form
	 * "attack?Screen=Xmenu=Ystage=Z". This method returns the path portion of
	 * the url, i.e., "attack" in the string above.
	 * <p>
	 * Newer, Spring-Controller-based classes will override this method to
	 * return "*.do"-styled paths.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String getPath() {
		return "#attack";
	}

	/**
	 * Get the link that can be used to request this screen.
	 * <p>
	 * Rendering the link in the browser may result in Javascript sending
	 * additional requests to perform necessary actions or to obtain data
	 * relevant to the lesson or the element of the lesson selected by the user.
	 * Thanks to using the hash mark "#" and Javascript handling the clicks, the
	 * user will experience less waiting as the pages do not have to reload
	 * entirely.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLink() {
		StringBuffer link = new StringBuffer(getPath());

		// mvc update:
		return link.append("/").append(getScreenId()).append("/").append(getCategory().getRanking()).toString();
	}

	/**
	 * Get the link to the target servlet.
	 * <p>
	 * Unlike getLink() this method does not require rendering the output of the
	 * request to the link in order to execute the servlet's method with
	 * conventional HTTP query parameters.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getServletLink() {
		StringBuffer link = new StringBuffer("attack");
		return link.append("?Screen=").append(getScreenId()).append("&menu=").append(getCategory().getRanking())
				.toString();
	}

	/**
	 * Get the link to the jsp page used to render this screen.
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getPage(WebSession s) {
		return null;
	}

	/**
	 * Get the link to the jsp template page used to render this screen.
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getTemplatePage(WebSession s) {
		return null;
	}

	/**
	 * <p>
	 * getCurrentAction.
	 * </p>
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getCurrentAction(WebSession s);

	/**
	 * Initiates lesson restart functionality
	 */
	public abstract void restartLesson();

	/**
	 * <p>
	 * setCurrentAction.
	 * </p>
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @param lessonScreen
	 *            a {@link java.lang.String} object.
	 */
	public abstract void setCurrentAction(WebSession s, String lessonScreen);

	/**
	 * Override this method to implement accesss control in a lesson.
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @param functionId
	 *            a {@link java.lang.String} object.
	 * @param employeeId
	 *            a int.
	 * @return a boolean.
	 */
	public boolean isAuthorized(WebSession s, int employeeId, String functionId) {
		return false;
	}

	/**
	 * Override this method to implement accesss control in a lesson.
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @param functionId
	 *            a {@link java.lang.String} object.
	 * @param role
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isAuthorized(WebSession s, String role, String functionId) {
		logger.info("Checking if " + role + " authorized for: " + functionId);
		boolean authorized = false;
		try {
			String query = "SELECT * FROM auth WHERE role = '" + role + "' and functionid = '" + functionId + "'";
			try {
				Statement answer_statement = WebSession.getConnection(s)
						.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet answer_results = answer_statement.executeQuery(query);
				authorized = answer_results.first();
				logger.info("authorized: " + authorized);
			} catch (SQLException sqle) {
				s.setMessage("Error authorizing");
				logger.error("Error authorizing", sqle);
			}
		} catch (Exception e) {
			s.setMessage("Error authorizing");
			logger.error("Error authorizing", e);
		}
		return authorized;
	}

	/**
	 * <p>
	 * getUserId.
	 * </p>
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a int.
	 * @throws org.owasp.webgoat.session.ParameterNotFoundException
	 *             if any.
	 */
	public int getUserId(WebSession s) throws ParameterNotFoundException {
		return -1;
	}

	/**
	 * <p>
	 * getUserName.
	 * </p>
	 *
	 * @param s
	 *            a {@link org.owasp.webgoat.session.WebSession} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.owasp.webgoat.session.ParameterNotFoundException
	 *             if any.
	 */
	public String getUserName(WebSession s) throws ParameterNotFoundException {
		return null;
	}

	/**
	 * Description of the Method
	 *
	 * @param windowName
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public static String makeWindowScript(String windowName) {
		// FIXME: make this string static
		StringBuffer script = new StringBuffer();
		script.append("<script language=\"JavaScript\">\n");
		script.append(" <!--\n");
		script.append("   function makeWindow(url) {\n");
		script.append("\n");
		script.append("       agent = navigator.userAgent;\n");
		script.append("\n");
		script.append("       params  = \"\";\n");
		script.append("       params += \"toolbar=0,\";\n");
		script.append("       params += \"location=0,\";\n");
		script.append("       params += \"directories=0,\";\n");
		script.append("       params += \"status=0,\";\n");
		script.append("       params += \"menubar=0,\";\n");
		script.append("       params += \"scrollbars=1,\";\n");
		script.append("       params += \"resizable=1,\";\n");
		script.append("       params += \"width=500,\";\n");
		script.append("       params += \"height=350\";\n");
		script.append("\n");
		script.append("       // close the window to vary the window size\n");
		script.append("       if (typeof(win) == \"object\" && !win.closed){\n");
		script.append("            win.close();\n");
		script.append("       }\n");
		script.append("\n");
		script.append("       win = window.open(url, '" + windowName + "' , params);\n");
		script.append("\n");
		script.append("           // bring the window to the front\n");
		script.append("       win.focus();\n");
		script.append("   }\n");
		script.append(" //-->\n");
		script.append(" </script>\n");

		return script.toString();
	}

	/**
	 * Simply reads a url into an Element for display. CAUTION: you might want
	 * to tinker with any non-https links (href)
	 *
	 * @param url
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public static Element readFromURL(String url) {
		ElementContainer ec = new ElementContainer();

		try {
			URL u = new URL(url);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(huc.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				ec.addElement(new StringElement(line));
			}

			reader.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

		return (ec);
	}

	/**
	 * Description of the Method
	 *
	 * @param reader
	 *            Description of the Parameter
	 * @param numbers
	 *            Description of the Parameter
	 * @param methodName
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public static Element readMethodFromFile(BufferedReader reader, String methodName, boolean numbers) {
		PRE pre = new PRE().addElement(getFileMethod(reader, methodName, numbers));

		return (pre);
	}

	/**
	 * Description of the Method
	 *
	 * @param s
	 *            Description of the Parameter
	 */
	public void handleRequest(WebSession s) {
		// call createContent first so messages will go somewhere
		Form form = new Form(getFormAction(), Form.POST).setName("form").setEncType("");
		form.addElement(createContent(s));
		setContent(form);
		s.getRequest().getRequestURL();
	}

	/**
	 * <p>
	 * getFormAction.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFormAction() {
		return getLink();
	}

	/**
	 * Description of the Method
	 *
	 * @return Description of the Return Value
	 */
	public String toString() {
		return getTitle();
	}

	/**
	 * <p>
	 * Getter for the field <code>defaultLanguage</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDefaultLanguage() {
		return this.defaultLanguage;
	}

	/**
	 * <p>
	 * Getter for the field <code>lessonPlanFileName</code>.
	 * </p>
	 *
	 * @param lang
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getLessonPlanFileName(String lang) {
		String ret = lessonPlanFileName.get(lang);
		if (ret == null) {
			ret = lessonPlanFileName.get(getDefaultLanguage());
		}
		return ret;
	}

	/**
	 * <p>
	 * Setter for the field <code>lessonPlanFileName</code>.
	 * </p>
	 *
	 * @param lang
	 *            a {@link java.lang.String} object.
	 * @param lessonPlanFileName
	 *            a {@link java.lang.String} object.
	 */
	public void setLessonPlanFileName(String lang, String lessonPlanFileName) {
		this.lessonPlanFileName.put(lang, lessonPlanFileName);
		this.availableLanguages.add(lang);
	}

	/**
	 * <p>
	 * Getter for the field <code>availableLanguages</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getAvailableLanguages() {
		return this.availableLanguages;
	}

	/**
	 * <p>
	 * Getter for the field <code>webgoatContext</code>.
	 * </p>
	 *
	 * @return a {@link org.owasp.webgoat.session.WebgoatContext} object.
	 */
	public WebgoatContext getWebgoatContext() {
		return webgoatContext;
	}

	/**
	 * <p>
	 * Setter for the field <code>webgoatContext</code>.
	 * </p>
	 *
	 * @param webgoatContext
	 *            a {@link org.owasp.webgoat.session.WebgoatContext} object.
	 */
	public void setWebgoatContext(WebgoatContext webgoatContext) {
		this.webgoatContext = webgoatContext;
	}

	/**
	 * <p>
	 * Getter for the field <code>labelManager</code>.
	 * </p>
	 *
	 * @return a {@link org.owasp.webgoat.util.LabelManager} object.
	 */
	protected LabelManager getLabelManager() {
		if (labelManager == null) {
			labelManager = BeanProvider.getBean("labelManager", LabelManager.class);
		}
		return labelManager;
	}

}

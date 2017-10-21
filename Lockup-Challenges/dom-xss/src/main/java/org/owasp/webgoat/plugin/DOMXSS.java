
package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.SequentialLessonAdapter;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.WebSession;

import org.owasp.webgoat.plugin.Encoding;


public class DOMXSS extends SequentialLessonAdapter {

	private final static String PERSON = "person";

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
		ElementContainer ec = new ElementContainer();

		ec.addElement(mainContent(s));

		return (ec);
	}

	protected ElementContainer mainContent(WebSession s) {
		StringBuffer attackString = null;

		ElementContainer ec = new ElementContainer();
		try {
			String inputText = s.getParser().getRawParameter(PERSON, "");

			ec.addElement(new Script().setSrc(LessonUtil.buildJsPath(s, this, "escape.js")));

			defendIt(ec, inputText);
			ec.addElement(new StringElement("Enter your name: "));

			attackString = new StringBuffer(inputText);

			Input input = new Input(Input.TEXT, PERSON, attackString.toString());
			ec.addElement(input);
			ec.addElement(new BR());
			ec.addElement(new BR());

			Element b = ECSFactory.makeButton("Submit Solution");
			ec.addElement(b);

		} catch (Exception e) {
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}
		return ec;

	}

	private void defendIt(ElementContainer ec, String inputText) {
		if (!inputText.isEmpty()) {
			ec.addElement(new H1("Hello, " + Encoding.urlEncode(inputText) + "!"));
		}

	}

	/**
	 * Gets the ranking attribute of the HelloScreen object
	 * 
	 * @return The ranking value
	 */
	private final static Integer DEFAULT_RANKING = new Integer(10);

	protected Integer getDefaultRanking() {
		return DEFAULT_RANKING;
	}

	protected Category getDefaultCategory() {
		return Category.AJAX_SECURITY;
	}

	/**
	 * Gets the title attribute of the HelloScreen object
	 *
	 * @return The title value
	 */
	public String getTitle() {
		return ("DOM-Based cross-site scripting");
	}

	public String getInstructions(WebSession s) {
		String instructions = "";

		if (getLessonTracker(s).getStage() == 1) {
			instructions = "What is your name sir?";
		}
		return (instructions);
	}

}

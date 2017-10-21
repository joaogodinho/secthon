
package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.H3;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.LessonAdapter;
import org.owasp.webgoat.session.WebSession;

import java.io.PrintWriter;
import java.util.HashMap;

public class XMLInjection extends LessonAdapter {

	private final static Integer DEFAULT_RANKING = new Integer(20);

	private final static String ACCOUNTID = "accountID";

	public static HashMap<Integer, Reward> rewardsMap = new HashMap<Integer, Reward>();

	protected static HashMap<Integer, Reward> init() {
		Reward r = new Reward();

		r.setName("Lockout t-shirt");
		r.setPoints(50);
		rewardsMap.put(1001, r);

		r = new Reward();
		r.setName("Lockout Secure Kettle");
		r.setPoints(30);
		rewardsMap.put(1002, r);

		r = new Reward();
		r.setName("Lockout Mug");
		r.setPoints(20);
		rewardsMap.put(1003, r);

		r = new Reward();
		r.setName("Lockout Core Duo Laptop");
		r.setPoints(2000);
		rewardsMap.put(1004, r);

		r = new Reward();
		r.setName("Lockout Hawaii Cruise");
		r.setPoints(3000);
		rewardsMap.put(1005, r);

		return rewardsMap;
	}

	public void handleRequest(WebSession s) {

		try {
			if (s.getParser().getRawParameter("from", "").equals("ajax")) {
				if (s.getParser().getRawParameter(ACCOUNTID, "").equals("836239")) {
					String lineSep = System.getProperty("line.separator");
					String xmlStr = "<root>" + lineSep + "<reward>Lockout Mug 20 Pts</reward>" + lineSep
							+ "<reward>Lockout t-shirt 50 Pts</reward>" + lineSep
							+ "<reward>Lockout Secure Kettle 30 Pts</reward>" + lineSep + "</root>";
					s.getResponse().setContentType("text/xml");
					s.getResponse().setHeader("Cache-Control", "no-cache");
					PrintWriter out = new PrintWriter(s.getResponse().getOutputStream());
					out.print(xmlStr);
					out.flush();
					out.close();
					return;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Form form = new Form(getFormAction(), Form.POST).setName("form").setEncType("");

		form.addElement(createContent(s));

		setContent(form);

	}

	protected Element createContent(WebSession s) {
		ElementContainer ec = new ElementContainer();
		boolean isDone = false;
		init();

		if (s.getParser().getRawParameter("done", "").equals("yes")) {
			isDone = true;
		}

		if (!isDone) {
			ec.addElement(new Script().setSrc(LessonUtil.buildJsPath(s, this, "xmlInjection.js")));
		}
		ec.addElement(new BR().addElement(new H1().addElement("Welcome to Lockout Reward Program.")));
		ec.addElement(new BR());

		ec.addElement(new BR().addElement(new H3().addElement("Rewards available through the program:")));
		ec.addElement(new BR());
		Table t2 = new Table().setCellSpacing(0).setCellPadding(0).setBorder(0).setWidth("90%").setAlign("center");
		TR trRewards = null;

		for (int i = 1001; i < 1001 + rewardsMap.size(); i++) {
			trRewards = new TR();
			Reward r = rewardsMap.get(i);
			trRewards.addElement(new TD("-" + r.getName()));
			trRewards.addElement(new TD(r.getPoints() + " Pts"));
			t2.addElement(trRewards);
		}

		ec.addElement(t2);

		ec.addElement(new BR());

		ec.addElement(new H3().addElement("Redeem your points:"));
		ec.addElement(new BR());

		Table t1 = new Table().setCellSpacing(0).setCellPadding(0).setBorder(0).setWidth("90%").setAlign("center");

		TR tr = new TR();

		tr.addElement(new TD("Please enter your account ID:"));

		Input input1 = new Input(Input.TEXT, ACCOUNTID, "");
		input1.addAttribute("onblur", "getRewards('" + LessonUtil.getXHRLink(s, this) + "');");
		input1.addAttribute("id", ACCOUNTID);
		tr.addElement(new TD(input1));
		t1.addElement(tr);

		ec.addElement(t1);
		ec.addElement(new BR());
		ec.addElement(new BR());
		ec.addElement(new BR());

		Div div = new Div();
		div.addAttribute("name", "rewardsDiv");
		div.addAttribute("id", "rewardsDiv");
		ec.addElement(div);

		Input b = new Input();
		b.setType(Input.SUBMIT);
		b.setValue("Submit");
		b.setName("SUBMIT");
		ec.addElement(b);

		if (s.getParser().getRawParameter("SUBMIT", "") != "") {
			defendIt(s, ec);
		}

		return ec;
	}

	private void defendIt(WebSession s, ElementContainer ec) {
		StringBuffer shipment = new StringBuffer();
		for (int i = 1001; i < 1001 + rewardsMap.size(); i++) {

			if (s.getParser().getRawParameter("check" + i, "") != "") {
				shipment.append(rewardsMap.get(i).getName() + "<br>");
			}
		}
		shipment.insert(0, "<br><br><b>The following items will be shipped to your address:</b><br>");
		ec.addElement(new StringElement(shipment.toString()));
	}

	protected Category getDefaultCategory() {

		return Category.AJAX_SECURITY;
	}

	protected Integer getDefaultRanking() {

		return DEFAULT_RANKING;
	}

	public String getTitle() {
		return "XML Injection";
	}

	static class Reward {

		private String name;

		private int points;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}

	}
}


package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.LessonAdapter;
import org.owasp.webgoat.session.WebSession;

import java.io.PrintWriter;

public class JSONInjection extends LessonAdapter
{

    private final static Integer DEFAULT_RANKING = new Integer(30);

    private final static String TRAVEL_FROM = "travelFrom";

    private final static String TRAVEL_TO = "travelTo";

    private final static String FLIGHT_PRICE_1 = "$600";

    private final static String FLIGHT_PRICE_2 = "$300";

    public void handleRequest(WebSession s)
    {

        try
        {
            if (s.getParser().getRawParameter("from", "").equals("ajax"))
            {
                String lineSep = System.getProperty("line.separator");
                String jsonStr = "{" + lineSep + "\"From\": \"Boston\"," + lineSep + "\"To\": \"Seattle\", " + lineSep
                        + "\"flights\": [" + lineSep
                        + "{\"stops\": \"0\", \"transit\" : \"N/A\", \"price\": \""+FLIGHT_PRICE_1+"\"}," + lineSep
                        + "{\"stops\": \"2\", \"transit\" : \"Newark,Chicago\", \"price\": \""+FLIGHT_PRICE_2+"\"} " + lineSep + "]"
                        + lineSep + "}";
                s.getResponse().setContentType("text/html");
                s.getResponse().setHeader("Cache-Control", "no-cache");
                PrintWriter out = new PrintWriter(s.getResponse().getOutputStream());
                out.print(jsonStr);
                out.flush();
                out.close();
                return;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Form form = new Form(getFormAction(), Form.POST).setName("form").setEncType("");
        form.setOnSubmit("return check();");

        form.addElement(createContent(s));

        setContent(form);

    }

    /**
     * Description of the Method
     *
     * @param s
     *            Current WebSession
     */
    protected Element createContent(WebSession s)
    {
        ElementContainer ec = new ElementContainer();
        ec.addElement(new Script().setSrc(LessonUtil.buildJsPath(s, this, "jsonInjection.js")));

        Table t1 = new Table().setCellSpacing(0).setCellPadding(0).setBorder(0).setWidth("90%").setAlign("center");

        TR tr = new TR();

        tr.addElement(new TD("From: "));
        Input in = new Input(Input.TEXT, TRAVEL_FROM, "");
        in.addAttribute("onblur", "getFlights('" + LessonUtil.getXHRLink(s, this) + "');");
        in.addAttribute("id", TRAVEL_FROM);
        tr.addElement(new TD(in));

        t1.addElement(tr);

        tr = new TR();
        tr.addElement(new TD("To: "));
        in = new Input(Input.TEXT, TRAVEL_TO, "");
        in.addAttribute("onblur", "getFlights('" + LessonUtil.getXHRLink(s, this) + "');");
        in.addAttribute("id", TRAVEL_TO);
        tr.addElement(new TD(in));

        t1.addElement(tr);
        ec.addElement(t1);

        ec.addElement(new BR());
        ec.addElement(new BR());
        Div div = new Div();
        div.addAttribute("name", "flightsDiv");
        div.addAttribute("id", "flightsDiv");
        ec.addElement(div);

        Input b = new Input();
        b.setType(Input.SUBMIT);
        b.setValue("Submit");
        b.setName("SUBMIT");
        ec.addElement(b);

        Input price2Submit = new Input();
        price2Submit.setType(Input.HIDDEN);
        price2Submit.setName("price2Submit");
        price2Submit.setValue("");
        price2Submit.addAttribute("id", "price2Submit");
        ec.addElement(price2Submit);
        defendIt(s);
        return ec;
    }

    private void defendIt(WebSession s) {
        String price = s.getParser().getRawParameter("price2Submit"));
            if (s.getParser().getRawParameter("radio0", "").equals("on") &&
                price == FLIGHT_PRICE_1)
            {
                s.setMessage("Congratulations. You bought it!");
            } else if (s.getParser().getRawParameter("radio1", "").equals("on") &&
                price == FLIGHT_PRICE_2) {
                s.setMessage("Congratulations. You bought it!");
            }
        }
    }

    protected Category getDefaultCategory()
    {
        return Category.AJAX_SECURITY;
    }

    protected Integer getDefaultRanking()
    {
        return DEFAULT_RANKING;
    }

    /**
     * Gets the title attribute of the HelloScreen object
     *
     * @return The title value
     */
    public String getTitle()
    {
        return ("JSON Injection");
    }

}

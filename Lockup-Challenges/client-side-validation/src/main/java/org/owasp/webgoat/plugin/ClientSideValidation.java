
package org.owasp.webgoat.plugin;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Center;
import org.apache.ecs.html.H1;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.lessons.SequentialLessonAdapter;
import org.owasp.webgoat.session.ECSFactory;
import org.owasp.webgoat.session.WebSession;
import org.owasp.webgoat.util.HtmlEncoder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class ClientSideValidation extends SequentialLessonAdapter
{

    private static final String PRICE_1 = "$69.99";

    private static final String PRICE_2 = "$27.99";

    private static final String PRICE_3 = "$1599.99";

    private static final String PRICE_4 = "$299.99";

    private static final Double PLATINUM_DISCOUNT = 0.75;

    protected Element createContent(WebSession s)
    {
        return super.createStagedContent(s);
    }

    protected Element doStage1(WebSession s)
    {
        return stage2Content(s);
    }

    protected Element stage2Content(WebSession s)
    {

        ElementContainer ec = new ElementContainer();

        try
        {

            ec.addElement(new Script().setSrc(LessonUtil.buildJsPath(s, this, "clientSideValidation.js")));

            ec.addElement(new HR().setWidth("90%"));
            ec.addElement(new Center().addElement(new H1().addElement("Shopping Cart")));

            ec.addElement(createQtyTable(s));

            ec.addElement(createTotalTable(s));
            ec.addElement(new BR());
            ec.addElement(new HR().setWidth("90%"));

            // test success
            DecimalFormat money = new DecimalFormat("$0.00");

            String grandTotalString = s.getParser().getStringParameter("GRANDTOT", "0");

            defendIt(s);

        } catch (Exception e)
        {
            s.setMessage("Error generating " + this.getClass().getName());
            e.printStackTrace();
        }
        return (ec);
    }

    private void defendIt(WebSession s) {
        //Change this to validate prices
        if (getTotalQty(s) > 0)
        {
            s.setMessage("Congrats! Your order is for free.");
        }
    }

    protected ElementContainer createTotalTable(WebSession s)
    {

        ElementContainer ec = new ElementContainer();

        String param1 = s.getParser().getRawParameter("field1", "");
        String param2 = HtmlEncoder.encode(s.getParser().getRawParameter("field2", "4128 3214 0002 1999"));

        Table t = new Table().setCellSpacing(0).setCellPadding(2).setBorder(0).setWidth("90%").setAlign("center");

        if (s.isColor())
        {
            t.setBorder(1);
        }

        ec.addElement(new BR());

        TR tr = new TR();
        tr.addElement(new TD().addElement("Total before coupon is applied:"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "SUBTOT", s.getParser()
                                                    .getStringParameter("SUBTOT", "$0.00")).setReadOnly(true)
                                                    .setStyle("border:0px;")).setAlign("right"));
        t.addElement(tr);

        tr = new TR();
        tr.addElement(new TD().addElement("Total to be charged to your credit card:"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "GRANDTOT", s.getParser()
                                                    .getStringParameter("GRANDTOT", "$0.00")).setReadOnly(true)
                                                    .setStyle("border:0px;")).setAlign("right"));
        t.addElement(tr);

        t.addElement(tr);

        tr = new TR();
        tr.addElement(new TD().addElement("&nbsp;").setColSpan(2));
        t.addElement(tr);
        tr = new TR();
        tr.addElement(new TD().addElement("Enter your credit card number:"));
        tr.addElement(new TD().addElement(new Input(Input.TEXT, "field2", param2)));
        t.addElement(tr);
        tr = new TR();
        tr.addElement(new TD().addElement("Enter your coupon code:"));

        Input input = new Input(Input.TEXT, "field1", param1);
        input.setOnKeyUp("isValidCoupon(field1.value)");
        tr.addElement(new TD().addElement(input));
        t.addElement(tr);

        Element b = ECSFactory.makeButton("Purchase");
        tr = new TR();
        tr.addElement(new TD().addElement(b).setColSpan(2).setAlign("center"));
        t.addElement(tr);
        ec.addElement(t);

        return ec;

    }

    protected int getTotalQty(WebSession s)
    {

        int quantity = 0;

        quantity += s.getParser().getFloatParameter("QTY1", 0.0f);
        quantity += s.getParser().getFloatParameter("QTY2", 0.0f);
        quantity += s.getParser().getFloatParameter("QTY3", 0.0f);
        quantity += s.getParser().getFloatParameter("QTY4", 0.0f);

        return quantity;
    }

    protected ElementContainer createQtyTable(WebSession s)
    {

        ElementContainer ec = new ElementContainer();
        Table t = new Table().setCellSpacing(0).setCellPadding(2).setBorder(1).setWidth("90%").setAlign("center");

        if (s.isColor())
        {
            t.setBorder(1);
        }

        TR tr = new TR();
        tr.addElement(new TH().addElement("Shopping Cart Items -- To Buy Now").setWidth("70%"));
        tr.addElement(new TH().addElement("Price").setWidth("10%"));
        tr.addElement(new TH().addElement("Quantity").setWidth("10%"));
        tr.addElement(new TH().addElement("Total").setWidth("10%"));
        t.addElement(tr);

        tr = new TR();
        tr.addElement(new TD().addElement("Studio RTA - Laptop/Reading Cart with Tilting Surface - Cherry "));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "PRC1", s.getParser().getStringParameter("PRC1",
                                                    PRICE_1))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        Input input = new Input(Input.TEXT, "QTY1", s.getParser().getStringParameter("QTY1", "0"));

        input.setOnKeyUp("updateTotals();");
        input.setOnLoad("updateTotals();");
        input.setSize(10);

        tr.addElement(new TD().addElement(input).setAlign("right"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "TOT1", s.getParser().getStringParameter("TOT1",
                                                                                                            "$0.00"))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        t.addElement(tr);
        tr = new TR();
        tr.addElement(new TD().addElement("Dynex - Traditional Notebook Case"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "PRC2", s.getParser().getStringParameter("PRC2",
                                                    PRICE_2))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        input = new Input(Input.TEXT, "QTY2", s.getParser().getStringParameter("QTY2", "0"));

        input.setOnKeyUp("updateTotals();");
        input.setSize(10);
        tr.addElement(new TD().addElement(input).setAlign("right"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "TOT2", s.getParser().getStringParameter("TOT2",
                                                                                                            "$0.00"))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        t.addElement(tr);
        tr = new TR();
        tr.addElement(new TD().addElement("Hewlett-Packard - Pavilion Notebook with Intel� Centrino�"));

        tr.addElement(new TD()
                .addElement(
                            new Input(Input.TEXT, "PRC3", s.getParser().getStringParameter("PRC3", PRICE_3))
                                    .setSize(10).setReadOnly(true).setStyle("border:0px;")).setAlign("right"));

        input = new Input(Input.TEXT, "QTY3", s.getParser().getStringParameter("QTY3", "0"));

        input.setOnKeyUp("updateTotals();");
        input.setSize(10);
        tr.addElement(new TD().addElement(input).setAlign("right"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "TOT3", s.getParser().getStringParameter("TOT3",
                                                                                                            "$0.00"))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        t.addElement(tr);
        tr = new TR();
        tr.addElement(new TD().addElement("3 - Year Performance Service Plan $1000 and Over "));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "PRC4", s.getParser().getStringParameter("PRC4",
                                                                                                            PRICE_4))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        input = new Input(Input.TEXT, "QTY4", s.getParser().getStringParameter("QTY4", "0"));

        input.setOnKeyUp("updateTotals();");
        input.setSize(10);
        tr.addElement(new TD().addElement(input).setAlign("right"));

        tr.addElement(new TD().addElement(
                                            new Input(Input.TEXT, "TOT4", s.getParser().getStringParameter("TOT4",
                                                                                                            "$0.00"))
                                                    .setSize(10).setReadOnly(true).setStyle("border:0px;"))
                .setAlign("right"));

        t.addElement(tr);
        ec.addElement(t);
        return ec;
    }

    protected Category getDefaultCategory()
    {
        return Category.AJAX_SECURITY;
    }

    /**
     * Gets the instructions attribute of the WeakAccessControl object
     * 
     * @return The instructions value
     */
    public String getInstructions(WebSession s)
    {
        return "Use \"platinum\" code as a discount code and try to get your entire order for free.";
    }

    private final static Integer DEFAULT_RANKING = new Integer(120);

    protected Integer getDefaultRanking()
    {
        return DEFAULT_RANKING;
    }

    /**
     * Gets the title attribute of the AccessControlScreen object
     *
     * @return The title value
     */
    public String getTitle()
    {
        return "Insecure Client Storage";
    }

}

<%@ page contentType="text/html; charset=ISO-8859-1" language="java" 
         import="org.owasp.webgoat.session.*, org.owasp.webgoat.lessons.Category, org.owasp.webgoat.lessons.AbstractLesson, org.owasp.webgoat.util.*, java.util.*" 
         errorPage=""  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
    WebSession webSession = ((WebSession) session.getAttribute(WebSession.SESSION));
    Course course = webSession.getCourse();
    AbstractLesson currentLesson = webSession.getCurrentLesson();
%>


<!-- HTML fragment correpsonding to the lesson content -->
<%@page import="org.owasp.webgoat.lessons.RandomLessonAdapter"%>

<div id="lessonContenMaint">
    <%=webSession.getInstructions()%></div>
<div id="message" class="info"><%=webSession.getMessage()%></div>

<%
    if (currentLesson.getTemplatePage(webSession) != null) {
        //System.out.println("Main.jsp - current lesson: " + currentLesson.getName() );
        //System.out.println("         - template Page: " + currentLesson.getTemplatePage(webSession));
%>
<jsp:include page="<%=currentLesson.getTemplatePage(webSession)%>" />
<%
} else {
%>
<div id="lessonContent"><%=currentLesson.getContent()%></div>
<%
    }
%>







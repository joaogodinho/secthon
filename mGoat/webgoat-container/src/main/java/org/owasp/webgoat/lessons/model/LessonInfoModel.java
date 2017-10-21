package org.owasp.webgoat.lessons.model;

import org.owasp.webgoat.lessons.AbstractLesson;
import org.owasp.webgoat.lessons.Category;
import org.owasp.webgoat.session.WebSession;

/**
 * <p>LessonInfoModel class.</p>
 *
 * @author dm
 * @version $Id: $Id
 */
public class LessonInfoModel {

    private String lessonTitle;
    private boolean hasPlan;
    private String submitMethod;

    /**
     * <p>Constructor for LessonInfoModel.</p>
     *
     * @param webSession a {@link org.owasp.webgoat.session.WebSession} object.
     */
    public LessonInfoModel(WebSession webSession) {
        AbstractLesson lesson = webSession.getCurrentLesson();
        //TODO make these first class citizens of the lesson itself; and stop passing the session all over ... and generally tighten the checks up
        this.lessonTitle = lesson.getTitle();
        this.submitMethod = lesson.getSubmitMethod();

        //special challenge case
        if (lesson.getCategory().equals(Category.CHALLENGE)) {
            this.hasPlan = (lesson.isAuthorized(webSession, AbstractLesson.CHALLENGE_ROLE, WebSession.SHOWHINTS)); //assuming we want this to fall in line with source and solutionn
        }
    }

    // GETTERS
    /**
     * <p>Getter for the field <code>lessonTitle</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLessonTitle() {
        return lessonTitle;
    }

    /**
     * <p>isHasPlan.</p>
     *
     * @return a boolean.
     */
    public boolean isHasPlan() {
        return hasPlan;
    }

    /**
     * <p>Getter for the field <code>submitMethod</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSubmitMethod() {
        return submitMethod;
    }

}

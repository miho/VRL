/* 
 * HTMLMenuGenerator.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007–2018 by Michael Hoffer,
 * Copyright (c) 2015–2018 G-CSC, Uni Frankfurt,
 * Copyright (c) 2009–2015 Steinbeis Forschungszentrum (STZ Ölbronn)
 * 
 * This file is part of Visual Reflection Library (VRL).
 *
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
 * 
 * Second, the copyright notice must remain. It must be reproduced in any
 * program that uses VRL.
 *
 * Third, add an additional notice, stating that you modified VRL. A suitable
 * notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Please cite the publication(s) listed below.
 *
 * Publications:
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */

package eu.mihosoft.vrl.visual;

import eu.mihosoft.vrl.reflection.Pair;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class HTMLMenuGenerator {

    private String title;
    private String introText;
    private String metaString = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "    <meta charset=\"utf-8\"/>\n"
            + "    <link type=\"text/css\" rel=\"stylesheet\" href=\"${CSS}\"/>\n"
            + "</head>\n";
    private String cssFile = "resources/css/vrl-documentation.css";
    private Collection<Pair<String, String>> entries =
            new ArrayList<Pair<String, String>>();

    public HTMLMenuGenerator() {
        //
    }

    public HTMLMenuGenerator(String title, String introText) {
        this.title = title;
        this.introText = introText;
    }

    public void addMenuEntry(String text, String link) {
        entries.add(new Pair<String, String>(text, link));
    }

    public String render() {
        String result = metaString.replace("${CSS}", cssFile);

        result += "<body>\n";

        result += "<h1>" + title + "</h1>\n";

        result += "<p>" + introText + "</p>\n";

        result += "<ul>\n";

        for (Pair<String, String> menuEntry : entries) {
            result += "  <li><a href=\"" + menuEntry.getSecond() + "\">"
                    + menuEntry.getFirst()
                    + "</a></li>\n";
        }

        result += "</ul>\n";

        result += "</body>\n";

        return result;
    }

    /**
     * @return the cssFile
     */
    public String getCssFile() {
        return cssFile;
    }

    /**
     * @param cssFile the cssFile to set
     */
    public void setCssFile(String cssFile) {
        this.cssFile = cssFile;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the introText
     */
    public String getIntroText() {
        return introText;
    }

    /**
     * @param introText the introText to set
     */
    public void setIntroText(String introText) {
        this.introText = introText;
    }
}

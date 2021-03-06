/* 
 * PluginConfiguratorGenerator.java
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

package eu.mihosoft.vrl.system;

import eu.mihosoft.vrl.io.VProjectController;
import eu.mihosoft.vrl.lang.VLangUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PluginConfiguratorGenerator {

    public static final String pluginNameRegEx =
            "/\\*\\<VRL_PLUGIN_NAME\\>\\*/.*/\\*\\</VRL_PLUGIN_NAME\\>\\*/";
    public static final String pluginDepsRegEx =
            "/\\*\\<VRL_PLUGIN_DEPS\\>\\*/.*/\\*\\</VRL_PLUGIN_DEPS\\>\\*/";
    public static final String pluginVersionRegEx =
            "/\\*\\<VRL_PLUGIN_VERSION\\>\\*/.*/\\*\\</VRL_PLUGIN_VERSION\\>\\*/";
    public static final String pluginDescRegEx =
            "/\\*\\<VRL_PLUGIN_DESC\\>\\*/.*/\\*\\</VRL_PLUGIN_DESC\\>\\*/";
    public static final String pluginExportRegEx =
            "/\\*\\<VRL_PLUGIN_EXPORTS\\>\\*/.*/\\*\\</VRL_PLUGIN_EXPORTS\\>\\*/";

    public static String generate(VProjectController controller,
            PluginInfo info) {
        String result = readTemplateCode(
                "/eu/mihosoft/vrl/system/PluginConfiguratorTemplateCode");

        result = result.replaceAll(pluginNameRegEx,
                "\"" + VLangUtils.addEscapesToCode(info.getName()) + "\"");
        result = result.replaceAll(pluginVersionRegEx,
                "\"" + VLangUtils.addEscapesToCode(info.getVersion()) + "\"");
        result = result.replaceAll(pluginDescRegEx,
                "\"" + VLangUtils.addEscapesToCode(info.getDescription()) + "\"");


        // PLUGIN DEPS
//        result = result.replaceAll(pluginDepsRegEx, info.getName());

        return result;
    }

    public static String generate(PluginInfo info) {

        return generate(null, info);
    }

    private static String readTemplateCode(String codeName) {

        BufferedReader reader = null;
        String code = "";

        try {
            // load Sample Code
            InputStream iStream =
                    PluginConfiguratorGenerator.class.getResourceAsStream(
                    codeName);

            reader = new BufferedReader(new InputStreamReader(iStream));

            while (reader.ready()) {
                code += reader.readLine() + "\n";
            }
        } catch (Exception ex) {
            Logger.getLogger(PluginConfiguratorGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(PluginConfiguratorGenerator.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
        return code;
    }
}

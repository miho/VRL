/* 
 * SessionEntryFactory.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2012 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2006–2012 by Michael Hoffer
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
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 */

package eu.mihosoft.vrl.io.vrlx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public interface SessionEntryFactory {

    /**
     * Returns a new entry file.
     * @param name file name
     * @return a new entry file
     */
    SessionEntryFile newEntryFile(String name);

    /**
     * Returns a new entry file.
     * @param name file name
     * @param data file data
     * @return a new entry file
     */
    SessionEntryFile newEntryFile(String name, Object data);

    /**
     * Returns a new entry folder.
     * @param name folder name
     * @return a new entry folder
     */
    SessionEntryFolder newEntryFolder(String name);

    /**
     * Returns a new entry folder.
     * @param name folder name
     * @param content content folder
     * @return a new entry folder
     */
    SessionEntryFolder newEntryFolder(
            String name, Collection<SessionEntry> content);

    /**
     * Returns a new session file.
     * @param version file version info
     * @return a new session file
     */
    SessionFile newFile(FileVersionInfo version, String path);

    /**
     * Returns a new session file.
     * @return a new session file
     */
    SessionFile newFile(String path);

    /**
     * Loads a session file from the specified location.
     * @param location location
     * @return session file
     */
    SessionFile loadFile(File f) throws IOException;
    
    /**
     * Saves a session file to the specified location.
     * @param out
     * @param file to save
     * @return session file
     */
    void saveFile(SessionFile file, File f) throws IOException;
}

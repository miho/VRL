/* 
 * RDialog.java
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

package eu.mihosoft.vrl.dialogs;

import eu.mihosoft.vrl.reflection.DefaultMethodRepresentation;
import eu.mihosoft.vrl.reflection.DefaultObjectRepresentation;
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.visual.DialogAnswerListener;
import eu.mihosoft.vrl.visual.PulseIcon;
import eu.mihosoft.vrl.visual.VDialog;
import eu.mihosoft.vrl.visual.VDialog.AnswerType;
import eu.mihosoft.vrl.visual.VSwingUtil;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RDialog {
    
    public static <T extends DialogUIInterface> T showConfirmDialog(
            final VisualCanvas canvas,
            String title, final T o, String okButtonText) {
        return showConfirmDialog(canvas, title, o, okButtonText, true);
    }

    public static <T extends DialogUIInterface> T showConfirmDialog(
            final VisualCanvas canvas,
            String title, final T o, String okButtonText, boolean showCancelBtn) {

        canvas.getInspector().
                addObject(o);

        final DefaultObjectRepresentation oRep = canvas.getInspector().
                generateObjectRepresentation(o);

        String[] buttons = null;
        
        if (showCancelBtn) {
            buttons = new String[]{okButtonText, "Cancel"};
        } else {
            buttons = new String[]{okButtonText};
        }

        VDialog.showConfirmDialog(
                canvas, title,
                oRep,
                new DialogAnswerListener() {

                    @Override
                    public boolean answered(AnswerType answer, int index) {

                        o.setDialogAnswer(answer, index);

                        if (index == 1) {
                            return true;
                        }

                        boolean invocationResult = true;

                        if (index == 0) {

                            // enable pulse effects
                            boolean pulseEffect =
                                    canvas.getEffectPane().
                                    isPulseEffect();
                            canvas.getEffectPane().
                                    enablePulseEffect(true);

                            for (DefaultMethodRepresentation mRep : oRep.getMethods()) {

                                boolean ignoreMethod =
                                        // isVisible() indicator whether mRep
                                        // is hidden, hidden variable only 
                                        // represents annotation request
                                        !mRep.isVisible()
                                        || mRep.getDescription().
                                        getMethodName().
                                        equals("isValid")
                                        || mRep.getDescription().
                                        getMethodName().
                                        equals("setValid")
                                        || mRep.getDescription().
                                        getMethodName().
                                        equals("setDialogAnswer");

                                if (!ignoreMethod) {

                                    try {
                                        mRep.invokeAsCallParentNoNewThread();

                                    } catch (InvocationTargetException ex) {
                                        Logger.getLogger(
                                                RDialog.class.getName()).
                                                log(Level.SEVERE, null, ex);
                                        invocationResult = false;
                                    }
                                }
                            }

                            canvas.getEffectPane().
                                    enablePulseEffect(pulseEffect);
                        }

                        return o.isValid() && invocationResult;
                    }
                },
                buttons);

        // we remove all pulse icons to prevent drawing bugs
        canvas.getInspector().removeObject(oRep.getObjectID());
        Collection<Component> pulseIcons =
                VSwingUtil.getAllChildren(canvas.getEffectPane(),
                PulseIcon.class);
        for (Component pulseIcon : pulseIcons) {
            canvas.getEffectPane().remove(pulseIcon);
        }

        return o;
    }
}

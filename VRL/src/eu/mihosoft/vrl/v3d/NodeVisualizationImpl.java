/* 
 * NodeVisualizationImpl.java
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

package eu.mihosoft.vrl.v3d;

import eu.mihosoft.vrl.animation.LinearInterpolation;
import eu.mihosoft.vrl.v3d.Shape3DArray;
import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Point3f;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class NodeVisualizationImpl implements NodeVisualization {

    private Shape3DArray geometries = new Shape3DArray();
    private ArrayList<Point3f> nodes = new ArrayList<Point3f>();
    private Color min;
    private Color max;
    private double scale;

    public NodeVisualizationImpl(Color min, Color max, double scale) {
        this.min = min;
        this.max = max;
        this.scale = scale;
    }

    @Override
    public void addNode(double x, double y, double z) {
        nodes.add(new Point3f((float) x, (float) y, (float) z));
    }
    
    @Override
    public void clear() {
        geometries.clear();
        nodes.clear();
    }

    @Override
    public Shape3DArray createShapes() {
        LinearInterpolation red =
                new LinearInterpolation(
                min.getRed(), max.getRed());
        LinearInterpolation green =
                new LinearInterpolation(
                min.getGreen(), max.getGreen());
        LinearInterpolation blue =
                new LinearInterpolation(
                min.getBlue(), max.getBlue());

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Point3f n : nodes) {
            minX = Math.min(minX, n.x);
            maxX = Math.max(maxX, n.x);

            minY = Math.min(minY, n.y);
            maxY = Math.max(maxY, n.y);

            minZ = Math.min(minZ, n.z);
            maxZ = Math.max(maxZ, n.z);
        }

        double zRange = Math.abs(maxZ - minZ);

        for (Point3f n : nodes) {
            // create a geometry at x, y, z

            double colorValue = (n.z-minZ) / zRange;

            red.step(colorValue);
            green.step(colorValue);
            blue.step(colorValue);

            Sphere s = new Sphere(0.3 * n.x, 0.3 * n.y, 0.3 * n.z, scale,
                    new Color(
                    (int) red.getValue(),
                    (int) green.getValue(),
                    (int) blue.getValue()));

            // add the geometry
            geometries.addAll(s.generateShape3DArray());
        }
        
        return geometries;
    }
}

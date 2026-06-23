package dev.dankoz.BaseServer.app.board.model;

import java.util.List;

/** A single freehand stroke inside a {@link DrawingElement}. Points are flat [x0,y0,x1,y1,…] relative to element origin. */
public record Stroke(String color, Double width, List<Double> points) {
}

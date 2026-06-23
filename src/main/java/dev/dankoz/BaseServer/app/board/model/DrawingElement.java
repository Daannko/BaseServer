package dev.dankoz.BaseServer.app.board.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elements")
public class DrawingElement extends Element {

    private List<Stroke> strokes = new ArrayList<>();

    public DrawingElement() {}

    public List<Stroke> getStrokes() { return strokes; }
    public void setStrokes(List<Stroke> strokes) { this.strokes = strokes; }
}

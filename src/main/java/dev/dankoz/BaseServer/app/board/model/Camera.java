package dev.dankoz.BaseServer.app.board.model;

/** Last camera/viewport state of a board so reload restores where the user left off. */
public record Camera(Double x, Double y, Double zoom) {
}

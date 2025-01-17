package dev.dankoz.BaseServer.todo.dto;

import java.util.Date;

public record AddToDoRequest(String title, String text, Date expires, Integer priority) {
}

package dev.dankoz.BaseServer.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;

@Builder
public record ExceptionResponse(Date date, String message){
}

package dev.dankoz.BaseServer.auth.dto;

import lombok.Builder;

import java.time.Duration;
import java.util.Date;

@Builder
public record LoginResponseDto(Date jwt, Date refreshToken) {
}

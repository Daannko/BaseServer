package dev.dankoz.BaseServer.auth.dto;

public record LoginResponseDto(String jwt,String refreshToken) {
}

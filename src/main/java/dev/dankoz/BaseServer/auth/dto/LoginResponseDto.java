package dev.dankoz.BaseServer.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(String jwt,String refreshToken) {
}

package dev.dankoz.BaseServer.general.dto;

import lombok.Builder;

@Builder
public record UserDataDTO(
        Integer id,
        String name,
        String email

) {

}

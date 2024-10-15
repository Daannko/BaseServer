package dev.dankoz.BaseServer.general.dto;

import dev.dankoz.BaseServer.general.model.User;
import lombok.Builder;

import java.util.Collection;
import java.util.List;

@Builder
public record UserDataDTO(
        Integer id,
        String name,
        Collection<?> permissions
) {
    public UserDataDTO(User user){
        this(user.getId(),user.getName(),user.getAuthorities());
    }
}

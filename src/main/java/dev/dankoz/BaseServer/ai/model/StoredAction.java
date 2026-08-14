package dev.dankoz.BaseServer.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A proposed {@link BoardAction} plus its resolution status as the user applies/rejects it.
 * {@code status} is one of {@code pending}, {@code applied}, {@code rejected}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredAction {

    private BoardAction action;

    /** pending | applied | rejected. New actions start {@code pending}. */
    private String status;
}

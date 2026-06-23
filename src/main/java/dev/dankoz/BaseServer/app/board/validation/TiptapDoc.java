package dev.dankoz.BaseServer.app.board.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = TiptapDocValidator.class)
public @interface TiptapDoc {

    String message() default "Invalid TipTap document";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

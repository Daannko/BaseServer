package dev.dankoz.BaseServer.app.board.validation;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TiptapDocValidator implements ConstraintValidator<TiptapDoc, Map<String, Object>> {

    @Override
    public boolean isValid(Map<String, Object> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Object type = value.get("type");
        if (!(type instanceof String) || !"doc".equals(type)) {
            return false;
        }

        Object content = value.get("content");
        if (content == null) {
            return true;
        }

        return (content instanceof List<?>);
    }
}

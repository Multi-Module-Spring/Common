package com.wis.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation to mark a field as requiring internationalization (i18n).
 * This can be used on DTO fields where the value is a message key that should
 * be resolved via a message bundle (e.g., message.properties).
 * <p>
 * Usage example:
 *
 * <pre>
 * {@code
 * public class UserDto {
 *
 *     private String name;
 *
 *     @I18n(bundle = "message", args = {"name"})
 *     private String greeting;
 * }
 * }
 * </pre>
 *
 * If the 'greeting' field has value "greeting.hello", and the bundle contains:
 * greeting.hello=Hello, {0}
 * <p>
 * Then the resolved value will be: "Hello, [name]"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18n {

    /**
     * The name of the message bundle (e.g., "message" → message.properties).
     * Default is "message".
     */
    String bundle() default "message";

    /**
     * The list of argument names to be used as parameters when resolving
     * the message. These refer to other field names in the same object.
     * <p>
     * If a value starts with "__", it is treated as a literal constant.
     * For example: {"__Admin"} → will pass "Admin" as a fixed argument.
     */
    String[] args() default {};
}


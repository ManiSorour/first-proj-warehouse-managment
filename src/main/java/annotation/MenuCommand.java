package annotation;

import model.role.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MenuCommand {

    String name() ;

    int rowNumber() default 0;

    Role[] selectedRole() default {};



}

package ShellNightmare.Terminal.CommandHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * annotation indiquant quel est la nature des données fournies à stdin
 * @author Gaëtan Lounes
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileMode {
    CommandFileMode value() default CommandFileMode.Raw;

}

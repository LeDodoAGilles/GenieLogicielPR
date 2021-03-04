package ShellNightmare.Terminal.CommandHandler;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation indiquant que le paramètre d'une option est optionnel
 * @author Gaëtan Lounes
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface isOptional {
}

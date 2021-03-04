package ShellNightmare.Terminal.CommandHandler;

import java.lang.annotation.*;

/**
 * annotation indiquant que la commande doit avoir value() paramètres
 * @author Gaëtan Lounes
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeededParameters {
    String value() default "0";

}


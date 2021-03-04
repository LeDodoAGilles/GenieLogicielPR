package utils.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Une entrée de configuration dont la valeur est booléenne.
 *
 * @author Louri */
public class BooleanConfigItem extends ConfigItem<Boolean> {
    /** Ensemble des Strings évalués à false. Tous les autres sont évalués à true. */
    private static final List<String> FALSE_STRINGS = new ArrayList<>(Arrays.asList("false", "f", "no", "n", "x", "0"));

    public BooleanConfigItem(String key, Boolean value, String comment) {
        super(key, value, comment);
    }

    @Override
    protected void setValue(Object value){ // Overriding
        if(value instanceof Boolean)
            setValue((Boolean) value);
        else if(value instanceof String)
            setValue((String) value);
        else if(value instanceof Integer)
            setValue((Integer) value);
        else if(value instanceof Double)
            setValue((Double) value);
        else
            throw new ClassCastException();
    }

    protected void setValue(Boolean value){ // Overloading
        this.value = value;
    }

    protected void setValue(String value){ // Overloading
        this.value = !FALSE_STRINGS.contains(value.toLowerCase());
    }

    protected void setValue(Integer value){ // Overloading
        this.value = value != 0;
    }

    protected void setValue(Double value){ // Overloading
        this.value = value != 0.0;
    }
}

package utils.config;

/** Une entrée de configuration dont la valeur est de type String.
 *
 * @author Louri */
public class StringConfigItem extends ConfigItem<String> {
    public StringConfigItem(String key, String value, String comment) {
        super(key, value, comment);
    }

    @Override
    protected void setValue(Object value){ // Overriding
        if(value instanceof String)
            setValue((String) value);
        else
            this.value = value.toString();
    }

    protected void setValue(String value){ // Overloading
        this.value = value;
    }
}

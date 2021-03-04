package utils.config;

/** Une entrée de configuration dont la valeur est un entier.
 *
 * @author Louri */
public class IntegerConfigItem extends ConfigItem<Integer> {
    public IntegerConfigItem(String key, Integer value, String comment) {
        super(key, value, comment);
    }

    @Override
    protected void setValue(Object value){ // Overriding
        if(value instanceof Integer)
            setValue((Integer) value);
        else if(value instanceof String)
            setValue((String) value);
        else
            throw new ClassCastException();
    }

    protected void setValue(Integer value){ // Overloading
        this.value = value;
    }

    protected void setValue(String value){ // Overloading
        try{
            this.value = Integer.parseInt(value);
        } catch(NumberFormatException e){
            ClassCastException ex = new ClassCastException();
            ex.addSuppressed(e);
            throw ex;
        }
    }
}

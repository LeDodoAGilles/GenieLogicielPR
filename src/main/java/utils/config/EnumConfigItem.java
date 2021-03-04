package utils.config;

import java.util.List;

/** Une entrée de configuration dont la valeur est prise dans un ensemble d'éléments représentés par des Strings.
 *
 * @author Louri */
public class EnumConfigItem extends ConfigItem<String> {
    /** Liste des valeurs acceptées.
     * On préfère utiliser une List plutôt qu'un Set afin de pouvoir utiliser les indexes comme substituts. */
    private final List<String> available;

    public EnumConfigItem(String key, List<String> available, String value, String comment) {
        super(key, value, comment);
        this.available = available;
    }

    @Override
    protected void setValue(Object value){ // Overriding
        if(value instanceof String)
            setValue((String) value);
        else if(value instanceof Integer)
            setValue((Integer) value);
        else
            throw new ClassCastException();
    }

    protected void setValue(String value){ // Overloading
        if(available.contains(value))
            this.value = value;
        else {
            try{
                int index = Integer.parseInt(value); // on tente de convertir en index
                setValue(index);
            } catch(NumberFormatException e){
                ClassCastException ex = new ClassCastException("Cannot convert config value.");
                ex.addSuppressed(e);
                throw ex;
            }
        }
    }

    protected void setValue(Integer value){ // Overloading
        if(0 <= value && value < available.size())
            this.value = available.get(value);
        else
            throw new ClassCastException();
    }

}

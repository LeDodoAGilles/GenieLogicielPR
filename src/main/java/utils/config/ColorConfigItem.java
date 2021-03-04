package utils.config;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Une entrée de configuration dont la valeur est interprétable comme une couleur.
 *
 * Les patterns acceptés sont :
 * r,g,b
 * fff (3 caractères hexa)
 * aabbcc (6 caractères hexa)
 *
 * @author Louri */
public class ColorConfigItem extends ConfigItem<String> {
    /** Liste des patterns de couleur autorisés.
     *
     * Le pattern regex est matché contre le String à tester.
     * Si c'est bon, alors on passe le même String à la fonction associé qui va s'occuper de le
     * mettre dans une forme interprétable par du css, ou alors retourna null s'il échoue. */
    private static final HashMap<Pattern, Function<String, String>> patterns = new HashMap<>();

    static {
        patterns.put(Pattern.compile("\\d+,\\d+,\\d+"), str -> {
            List<Integer> tab = Arrays.stream(str.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            if(tab.stream().allMatch(i -> 0 <= i && i <= 255))
                return String.format("rgb(%s,%s,%s)", tab.get(0), tab.get(1), tab.get(2));
            else
                return null;
        });
        patterns.put(Pattern.compile("rgb\\(\\d+,\\d+,\\d+\\)"), str -> str);
        patterns.put(Pattern.compile("[0-9a-fA-F]*"), str -> {
            if(str.length() == 3 || str.length() == 6)
                return "#"+str.toLowerCase();
            else
                return null;
        });
        patterns.put(Pattern.compile("#[0-9a-fA-F]*"), str -> {
            if(str.length() == 4 || str.length() == 7)
                return str.toLowerCase();
            else
                return null;
        });
    }

    public ColorConfigItem(String key, String value, String comment) {
        super(key, value, comment);
    }

    @Override
    protected void setValue(Object value){ // Overriding
        if(value instanceof String)
            setValue((String) value);
        else
            throw new ClassCastException();
    }

    protected void setValue(String value){ // Overloading
        this.value = transform(value);
    }

    /** Tente de transformer le String donné en un autre représentant une couleur css valide.
     * Ou none si on ne veut pas écraser la couleur.
     *
     * @param weird le String à transformer */
    public static String transform(String weird) throws ClassCastException {
        Optional<String> ops = patterns.entrySet().stream().map(e -> {
            if(e.getKey().matcher(weird).matches())
                return e.getValue().apply(weird);
            else
                return null;
        }).filter(Objects::nonNull).findAny();

        if(ops.isPresent())
            return ops.get();
        else
            throw new ClassCastException();
    }
}

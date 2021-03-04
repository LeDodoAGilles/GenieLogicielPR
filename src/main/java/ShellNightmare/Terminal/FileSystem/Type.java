package ShellNightmare.Terminal.FileSystem;

/**
 * énumération de la liste des fichiers
 * @author Gaëtan Lounes
 */
public enum Type {
    FOLDER("f", "dossier"),
    SOFTLINK("s", "lien symbolique"),
    DATA("d", "fichier texte"),
    BINARY("b", "fichier binaire");


    public final String shortName;
    public final String longName;

    Type(String s, String lo){
        shortName=s;
        longName = lo;
    }

    public static Type getTypeByLongName(String lo){
        for (Type t : Type.values())
            if (lo.equals(t.longName))
                return t;
        return null;
    }
}

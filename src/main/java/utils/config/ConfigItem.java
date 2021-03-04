package utils.config;

import java.util.Arrays;

/** Objet représentant une entrée dans une configuration.
 *
 * @author Louri */
public class ConfigItem<T> {
    /** Index de l'entrée dans le fichier de configuration.
     * Il est affecté automatiquement par {@link Config} lors de la définition de l'entrée par défaut.
     * C'est index n'est utilisé que lors de l'écriture du fichier de configuration. */
    protected int index;

    /** Clé de l'entrée. */
    public String key;

    /** Valeur de l'entrée. */
    public T value;

    /** Valeur par défaut de l'entrée.
     * Elle est affectée automatiquement par {@link Config} lors de la définition de l'entrée par défaut. */
    protected T defaultValue;

    /** Commentaire de l'entrée.
     * Le commentaire d'une entrée est écrit juste au dessus de celle-ci.
     * Chaque ligne commencera automatiquement par un <code>#</code> suivi d'un espace (pas besoin de l'écrire soi-même).
     * Si deux <code>\n</code> se suivent, alors une nouvelle ligne vide sera ajoutée (sans écrire de <code>#</code>).
     * Il est ainsi possible de rajouter une ou plusieurs lignes vides entre deux entrées.
     * Si vous souhaitez écrire un <code>#</code> tout seul sur une ligne, il faudra mettre un espace entre les deux
     * caractères de retour à la ligne. */
    public String comment;

    public ConfigItem(String key, T value, String comment) {
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    protected T getValue(){
        return value;
    }

    /** Essaye de mettre à jour la valeur de l'entrée.
     * À override dans les entrées de type spécifique pour automatiser les conversions.
     *
     * @param value la nouvelle valeur
     * @throws ClassCastException la valeur n'a pas pu être convertie */
    protected void setValue(Object value) throws ClassCastException {
        this.value = (T) value;
    }

    /** Donne la représentation de l'entrée sous la forme un String, prêt à être écrit dans un fichier. */
    public String toString(){
        StringBuilder sb = new StringBuilder();

        Arrays.stream(comment.split("\n", -1)) // garde les trailing spaces
                .sequential().forEachOrdered(s ->
        {
            if(!s.isEmpty())
                sb.append("# ").append(s);
            sb.append("\n");
        });

        sb.append(key).append("=").append(value).append("\n");

        return sb.toString();
    }
}

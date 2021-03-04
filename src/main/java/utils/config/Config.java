package utils.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Need anything optional of little or no importance? Let's go overkill!

/** Une configuration provenant d'un fichier de configuration.
 * Propose des méthodes utilitaires pour charger et sauvegarder une configuration.
 *
 * Chaque entrée du fichier doit avoir une clé unique.
 * Si ce n'est pas le cas, alors c'est la dernière des entrées qui est prise en compte (car elle écrase les précédentes)
 *
 * Un fichier de configuration de EscapeTheShell répond au format suivant :
 * - une entrée est de la forme <code>key=value</code> sur une seule ligne.
 *   Pas besoin de guillemets pour entourer key ou value.
 *   Le premier caractère = qui apparaît est considéré comme le séparateur, les autres seront intégrés à value.
 *   <code>key</code> est un String, <code>value</code> est interprétée par la configuration par défaut si l'entrée y
 *   apparaît, sinon elle est interprétée comme un String et il reviendra au programme d'en faire la conversion.
 * - un <code>#</code> comme premier caractère d'une ligne dénote un commentaire.
 *   Il est impossible de commenter une entrée sur la même ligne, en ayant un <code>#</code> à la suite.
 * - les lignes vides sont ignorées
 * - un mode paragraphe permet d'intégrer facilement des retours à la ligne dans les values.
 *   Un paragraphe commence à la ligne suivant <code>key=§</code> et termine à la ligne précédant le prochain
 *   <code>§</code> (1er caractère). Une ligne vide avant ce caractère de fin dénote un <code>\n</code>.
 *   Les caractères suivant ces deux types de <code>key=§</code> sur la même ligne sont ignorés.
 *
 * Le mode paragraphe a été ajouté afin de ne pas avoir à différencier le traitement des antislashes dans une seule ligne.
 * Cela permet aussi de garder une mise en forme du texte plus évidente.
 *
 * @author Louri */
public class Config {
    /** Séparateur indiquant le début d'un paragraphe.
     * N'est ni static ni final afin de pouvoir être changé au besoin dans les classes héritées. */
    protected String BEGIN = "§";

    /** Séparateur indiquant la fin d'un paragraphe.
     * N'est ni static ni final afin de pouvoir être changé au besoin dans les classes héritées. */
    protected String END = "§";

    /** Registre des entrées du fichier de configuration.
     * C'est une HashMap afin d'avoir un accès rapide (et non séquentiel) aux entrées à partir de leur clé.
     * Les clés de la HashMap sont les clés des entrées qu'elle contient. */
    private final HashMap<String, ConfigItem<?>> items = new HashMap<>();

    /** Revient à la configuration par défaut.
     * Les entrées qui n'étaient pas là par défaut sont enlevées, tandis que celles qui l'étaient reprennent leur
     * valeur par défaut. */
    public void revertToDefaults(){
        items.values().forEach(item -> {
            if(item.defaultValue == null)
                items.remove(item.key);
            else
                item.setValue(item.defaultValue);
        });
    }

    /** Ajoute une entrée par défaut.
     * Afin de sauvegarder l'ordre d'apparition de l'entrée dans le fichier, un index lui est automatiquement attribué.
     * Ces indexes commencent à 0.
     *
     * Si une entrée de même clé existe déjà, essaye de la remplacer.
     *
     * @param item l'entrée à rajouter à la configuration par défaut
     * @throws ClassCastException si une entrée de même clé existe déjà et que le type de leur valeur est différent */
    protected <T> void addDefaultItem(ConfigItem<T> item) throws ClassCastException {
        item.defaultValue = item.value;
        this.setItem(item);
    }

    /** Récupère l'entrée associée à la clé donnée.
     *
     * @param key la clé de l'entrée
     * @return l'entrée associée, ou null si aucune entrée associée n'existe */
    public Optional<ConfigItem<?>> getItem(String key){
        return Optional.ofNullable(items.get(key));
    }

    /** Récupère l'entrée associée à l'index donné
     *
     * @param index l'index de l'entrée
     * @return l'entrée associée, ou null si aucune entrée ne possède cet index */
    public Optional<ConfigItem<?>> getItem(int index){
        if(0 <= index && index < items.size())
            return items.values().stream().parallel().filter(item -> item.index == index).findFirst();
        else
            return Optional.empty();
    }

    /** Récupère la valeur associée à la clé donnée.
     *
     * @param key la clé de l'entrée
     * @return la valeur associée, ou null si aucune value associée n'existe */
    public Optional<?> getValue(String key){
        return getItem(key).map(item -> item.value);
    }

    /** Met à jour l'entrée si elle existe ou en créé une autre le cas échéant
     *
     * @param item l'entrée à rajouter
     * @throws ClassCastException le type de l'entrée est différent du type précédent */
    public <T> void setItem(ConfigItem<T> item) throws ClassCastException {
        ConfigItem<?> saved = items.get(item.key);
        if(saved != null){ // l'entrée existe déjà
            assert saved.key.equals(item.key);
            saved.setValue(item.value); // erreur si types incompatibles
        }
        else { // c'est une nouvelle entrée
            item.index = items.size(); // affectation de l'index
            items.put(item.key, item);
        }
    }

    /** Charge la configuration avec le contenu du fichier de configuration donné.
     * Le fichier de configuration doit être encodé en UTF-8.
     * Si le fichier n'existe pas encore, le crée et utilise la configuration par défaut.
     *
     * Si une entrée est dans la la configuration précédemment chargée ou par défaut,
     * alors seule sa valeur est mis à jour (et pas son commentaire).
     * Si une entrée n'est pas dans la configuration précédemment chargée ou par défaut,
     * alors elle est créée avec son commentaire, et le type de sa valeur est String (car on ne peut pas le déterminer).
     *
     * Le fichier de configuration est accédé par son URL afin de pouvoir tout aussi bien
     * charger un fichier à l'extérieur du .jar et dans celui-ci.
     * De plus, la lecture en est facilitée.
     *
     * @param src le chemin menant au fichier de configuration
     * @throws IOException si une erreur I/O survient ou si le fichier est corrompu */
    public void load(String src) throws IOException {
        File file = new File(src);
        if(Files.notExists(Path.of(file.toURI()))){
            // si le fichier n'existe pas encore, on le crée avec la configuration par défaut.
            save(src);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.toURI().toURL().openStream(), StandardCharsets.UTF_8))) {
            StringBuilder commentBuilder = new StringBuilder(); // réservé à l'élaboration du commentaire
            StringBuilder paragraphBuilder = new StringBuilder(); // réservé à l'élaboration du paragraphe
            String key = "";
            String value;

            boolean paragraph = false; // si la ligne lue est dans un paragraphe
            int equalIndex;
            String line; // ligne lue, sans le \n
            int lineno = 0;
            while ((line = reader.readLine()) != null) {
                if(paragraph){
                    if(line.startsWith(END)){ // fin de paragraphe, le reste de la ligne est ignoré
                        if(paragraphBuilder.length() != 0)
                            paragraphBuilder.deleteCharAt(paragraphBuilder.length()-1); // élimine le dernier \n qui est de trop
                        value = paragraphBuilder.toString(); // value est de type String

                        assert !key.isBlank();
                        ConfigItem<String> newItem = new ConfigItem<>(key, value, commentBuilder.toString());
                        try{
                            this.setItem(newItem);
                        } catch(ClassCastException e){
                            throw new IOException("Entrée de type incompatible (ligne " + lineno + ") : " + newItem);
                        }

                        // reset pour la prochaine entrée
                        commentBuilder = new StringBuilder();
                        paragraphBuilder = new StringBuilder();
                        key = null;
                        value = null;
                        paragraph = false;
                    }
                    else {
                        paragraphBuilder.append(line).append("\n"); // on met la ligne telle quelle
                    }
                }
                else {
                    if(line.isBlank())
                        commentBuilder.append("\n");
                    else if(line.startsWith("# "))
                        commentBuilder.append(line.substring(2)).append("\n");
                    else if(line.startsWith("#"))
                        commentBuilder.append(line.substring(1)).append("\n");
                    else {
                        equalIndex = line.indexOf("=");
                        if(equalIndex >= 0){
                            key = line.substring(0, equalIndex).strip();
                            value = line.substring(equalIndex+1).strip();
                            if(key.isEmpty())
                                throw new IOException("Entrée sans clé (ligne " + lineno + ") : " + line);
                            else if(value.isEmpty())
                                throw new IOException("Entrée sans valeur (ligne " + lineno + ") : " + line);
                            else if(value.startsWith(BEGIN)){ // début de paragraphe, le reste de la ligne est ignoré
                                value = null;
                                paragraph = true;
                            }
                            else {
                                ConfigItem<String> newItem = new ConfigItem<>(key, value, commentBuilder.toString());
                                try{
                                    this.setItem(newItem);
                                } catch(ClassCastException e){
                                    throw new IOException("Entrée de type incompatible (ligne " + lineno + ") : " + newItem);
                                }

                                // reset pour la prochaine entrée
                                commentBuilder = new StringBuilder();
                                key = null;
                                value = null;
                            }
                        }
                        else
                            throw new IOException("Entrée sans paire clé-valeur (ligne " + lineno + ") : " + line);
                    }
                }
                lineno++;
            }

            if(paragraph)
                throw new IOException("EOF atteint sans avoir fermé le paragraphe.");
        }
    }

    /** Sauvegarde la configuration dans le fichier de chemin donné.
     * Le fichier doit être extérieur au .jar
     *
     * @param dest le chemin du fichier dans lequel sauvegarder la configuration
     * @throws IOException si une erreur I/O survient ou si le fichier est corrompu */
    public void save(String dest) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), StandardCharsets.UTF_8))){
            final IOException[] toRethrow = new IOException[1]; // nécessaire pour être mis à jour par le lambda

            // écrit les entrées de configuration dans le fichier dans l'ordre de leurs indexes.
            items.values().stream().sorted(Comparator.comparingInt(item -> item.index)).map(ConfigItem::toString).forEachOrdered(s -> {
                try {
                    writer.append(s);
                } catch (IOException e) {
                    // try-catch car on ne peut pas throw depuis un lambda
                    // mais on veut rethrow l'erreur, d'où l'utilisation du tableau de une case toRethrow
                    if(toRethrow[0] != null)
                        e.addSuppressed(toRethrow[0]); // on prend toutes les erreurs
                    toRethrow[0] = e;
                }
            });

            if(toRethrow[0] != null)
                throw toRethrow[0];
        }
    }
}

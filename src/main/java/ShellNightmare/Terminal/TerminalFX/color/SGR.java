package ShellNightmare.Terminal.TerminalFX.color;

import ShellNightmare.Terminal.TerminalFX.DodoStyle;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO code nouveau ou réutilisé pour le dashed-line (overlined) ?

// formules pour xterm : https://tintin.mudhalla.net/info/256color/

// On peut chaîner les fonctions facilement

/* Modifie le style depuis une séquence de codes.
 * Utilise le code SGR (Select Graphic Rendition) https://en.wikipedia.org/wiki/ANSI_escape_code#SGR */

/* Classe aidant à l'utilisation du code SGR (Select Graphic Rendition)
 * Liste des codes : https://en.wikipedia.org/wiki/ANSI_escape_code#SGR */

/** Utilitaire pour insérer des codes SGR à un string et à les appliquer à un style.
 * Un objet SGR encapsule un string auquel sera ajouté des codes SGR.
 * Un objet SGR est immuable, un nouveau sera créé à chaque changement.
 *
 * Lorsqu'un style est appliqué par une fonction utilitaire, un objet SGR est retourné
 * afin de pouvoir chaîner les fonctions. Le résultat final est obtenu avec toString.
 *
 * Il suffira dans un string d'avoir \e[code1;code2;...m our combiner les effets des codes SGR.
 *
 * Usage :
 * "Ce texte est en \e[3mitalique\e[0m n'est-ce pas."
 * "Ce texte est en \e[1;3mgras et en italique\e[0m n'est-ce pas."
 * "Ce texte \e[4mest souligné \e[9met parfois\e[29m barré\e[0m c'est évident."
 * "Ce texte est \e[31;44mmoche\e[0m et \e[38;5;226;48;2;255;0;255mcelui-ci\e[39;49m n'est pas mieux."
 *
 * Codes SGR supportés :
 * 0/aucun : réinitialisation au style par défaut.
 * 1/21 : met en gras / reset le gras
 * 2/22 : assombrit / reset l'assombrissement
 * 3/23 : italique / reset l'italic
 * 4/24 : souligner / reset le soulignage
 * 5/25 : clignottement / reset le clignottement
 * 7/27 : échange foreground et background / reset l'échange
 * 8/28 : cache le texte (remplace le foreground avec le background) / reset le foreground avec celui par défaut
 * 9/29 : barré / reset la barre
 *
 * fonts : (doivent être pré-définis à l'index 10-code dans FontRegister)
 * 10 : font par défaut
 * 11-19 : font alternatif
 * 20 : fraktur (lettres 'germaniques'). Dans notre implémentation, est géré par un font et est donc annulé par le code 10.
 *
 * foreground = couleur de texte :
 * 38;2;R;G;B : couleur RGB
 * 38;5;K : K code couleur prédéfini
 *
 * background :
 * 48;2;R;G;B : couleur RGB
 * 48;5;K : K code couleur prédéfini
 *
 * foreground / background : couleurs usuelles
 * 39/49 : reset à la couleur du style par défaut
 * 30/40 : black
 * 31/41 : red
 * 32/42 : green
 * 33/43 : yellow
 * 34/44 : blue
 * 35/45 : magenta
 * 36/46 : cyan
 * 37/47 : white
 * 90/100 : bright black = gray
 * 91/101 : bright red
 * 92/102 : bright green
 * 93/103 : bright yellow
 * 94/104 : bright blue
 * 95/105 : bright magenta
 * 96/106 : bright cyan
 * 97/107 : bright white */
public class SGR {
    private static final String fraktur = "Fette classic UNZ Fraktur"; // font à utiliser pour l'effet fraktur

    private static final boolean IS_DEFAULT_BOUNDED = true; // si, par défaut, un code d'annulation doit être ajouté à la fin du string

    private String codedText; // le string encapsulé

    /** Crée un objet SGR avec le string donné.
     * @param codedText le string à encapsuler */
    private SGR(String codedText){
        this.codedText = codedText;
    }

    public SGR hide(boolean bounded){
        if(bounded)
            codedText = String.format("\\e[8m%s\\e[28m", codedText);
        else
            codedText = String.format("\\e[8m%s", codedText);
        return this;
    }

    public SGR hide(){
        return hide(IS_DEFAULT_BOUNDED);
    }

    /** Échange le foreground et le background du string encapsulé.
     * Ajoute le code 7 en début de string.
     * Si bounded, ajoute le code 27 en fin de string pour annuler l'échange. */
    public SGR reverse(boolean bounded){
        if(bounded)
            codedText = String.format("\\e[7m%s\\e[27m", codedText);
        else
            codedText = String.format("\\e[7m%s", codedText);
        return this;
    }

    public SGR reverse(){
        return reverse(IS_DEFAULT_BOUNDED);
    }

    public static SGR reverse(String s, boolean bounded){
        return new SGR(s).reverse(bounded);
    }

    public static SGR reverse(String s){
        return reverse(s, IS_DEFAULT_BOUNDED);
    }

    /** Efface tous les styles précédents.
     * Ajoute le code 0 de reset complet à la fin du string. */
    public SGR flush(){
        codedText = codedText + "\\e[0m";
        return this;
    }

    /** Efface tous les styles précédents.
     * Ajoute le code 0 de reset complet à la fin du string. */
    public static SGR flush(String s){
        return new SGR(s).flush();
    }

    /**
     * @return le string encapsulé, avec les codes de séquence adéquats. */
    @Override
    public String toString() {
        return codedText;
    }

    /**
     * @param codes des entiers
     * @return un string contenant ces entiers dans l'ordre donné espacés chacun d'un espace */
    private static String sequenceToString(Integer... codes){
        return Arrays.stream(codes).map(Object::toString).collect(Collectors.joining(" "));
    }

    /** Calcule et renvoie la couleur 8bit indiquée par le code SGR approprié.
     * Le code est relié aux composantes R G B de la couleur associée par la formule suivante :
     * code = 16 + 36*R + 6*G + B (avec R, G et B entre 0 et 5, en posant 5 le maximum d'intensité)
     * @param code code SGR entre 16 et 231
     * @return la couleur indiquée par le code
     * @exception IllegalArgumentException si code n'est pas entre 16 et 231 */
    public static Color get8bitColor(int code){
        if(!(16 <= code && code <= 231))
            throw new IllegalArgumentException(String.format("code %d must be between 16 and 231.", code));

        code -= 16;
        int b = code % 6;
        code = (code - b) / 6;
        int g = code % 6;
        int r = (code - g) / 6;

        // r g b entre 0 et 5, avec 5 le maximum d'intensité, d'où la division par 5
        return Color.color(r/5., g/5., b/5.);
    }

    /** Calcule et renvoie la couleur de niveau de gris indiquée par le code SGR approprié.
     * Le code est entre 232 et 255. Le premier correspond à rgb(8,8,8), les suivants incrémente le niveau de 10 en 10.
     * @param code code SGR entre 232 et 255
     * @return la couleur de niveau de gris indiquée par le code
     * @exception IllegalArgumentException si code n'est pas entre 232 et 255 */
    public static Color get8bitGrayscaleColor(int code){
        if(!(232 <= code && code <= 255))
            throw new IllegalArgumentException(String.format("code %d must be between 232 and 255.", code));

        int k = 8 + 10 * (code - 232);
        return Color.rgb(k, k, k);
    }

    /** Applique la couleur en foreground du style donné à partir de son code SGR.
     * @param style le style à modifier
     * @param code le code SGR de la couleur, compris entre 0 et 255
     * @return le style modifié
     * @exception IllegalArgumentException si le code n'est pas entre 0 et 255 */
    public static DodoStyle setForegroundBy8bitCode(DodoStyle style, int code){
        if(0 <= code && code <= 15) // code couleur aixterm
            return style.setForeground(Aixterm_Color.ALL.get(code));
        else if(16 <= code && code <= 231) // code couleur 8bit
            return style.setForeground(get8bitColor(code));
        else if(code <= 255) // code couleur en niveau de gris
            return style.setForeground(get8bitGrayscaleColor(code));
        else
            throw new IllegalArgumentException(String.format("code %d must be between 0 and 255.", code));
    }

    /** Applique la couleur en background du style donné à partir de son code SGR.
     * @param style le style à modifier
     * @param code le code SGR de la couleur, compris entre 0 et 255
     * @return le style modifié
     * @exception IllegalArgumentException si le code n'est pas entre 0 et 255 */
    public static DodoStyle setBackgroundBy8bitCode(DodoStyle style, int code){
        if(0 <= code && code <= 15) // code couleur aixterm
            return style.setBackground(Aixterm_Color.ALL.get(code));
        else if(16 <= code && code <= 231) // code couleur 8bit
            return style.setBackground(get8bitColor(code));
        else if(code <= 255) // code couleur en niveau de gris
            return style.setBackground(get8bitGrayscaleColor(code));
        else
            throw new IllegalArgumentException(String.format("code %d must be between 0 and 255.", code));
    }

    /** Modifie le style avec la séquence de codes SGR donnée.
     * Il est aussi nécessaire de fournir un style par défaut afin de pouvoir annuler
     * les changements de couleur du background et du foreground.
     * @param current le style à modifier
     * @param defaultStyle le style par défaut. Doit posséder un background et un foreground.
     * @param codes la séquence de codes SGR
     * @return le style modifié
     * @exception IllegalArgumentException si le style par défaut est invalide ou si un code n'est pas entre 0 de 255
     * @exception IllegalColorSequenceException si un code de la séquence est invalide à sa position */
    public static DodoStyle modifyStyleFromSequence(DodoStyle current, DodoStyle defaultStyle, Integer... codes){
        if(defaultStyle.foreground.isEmpty() || defaultStyle.background.isEmpty())
            throw new IllegalArgumentException("Le style par défaut doit posséder un background et un foreground.");

        DodoStyle style = current;

        if(codes.length == 0){ // Si aucun code, même effet qu'avec le code 0
            return defaultStyle;
        }

        boolean foregroundMode = false; // mode dans lequel on rentre à partir du code 38
        boolean backgroundMode = false; // mode dans lequel on rentre à partir du code 48

        boolean waiting8bitColor = false;
        int waiting24bitColor = -1; // nombre de composantes encore attendues, moins 1
        int[] bgr = {0, 0, 0}; // ordre inverse : blue-green-red

        for(int code : codes){
            if(waiting8bitColor){
                if(foregroundMode){
                    style = setForegroundBy8bitCode(style, code);
                    foregroundMode = false;
                }
                else if(backgroundMode){
                    style = setBackgroundBy8bitCode(style, code);
                    backgroundMode = false;
                }
                waiting8bitColor = false;
                continue;
            }
            else if(waiting24bitColor > -1){
                if(0 <= code && code <= 255)
                    bgr[waiting24bitColor--] = code;
                else
                    throw new IllegalArgumentException(String.format("code %d must be between 0 and 255.", code));

                if(waiting24bitColor == -1){ // toutes les composantes ont été récupérées
                    if(foregroundMode){
                        style = style.setForeground(Color.rgb(bgr[2], bgr[1], bgr[0]));
                        foregroundMode = false;
                    }
                    else if(backgroundMode){
                        style = style.setBackground(Color.rgb(bgr[2], bgr[1], bgr[0]));
                        backgroundMode = false;
                    }

                    // waiting24bitColor est déjà à -1
                }

                continue;
            }

            if(foregroundMode || backgroundMode){
                if(code == 2){
                    waiting24bitColor = 2; // on attend les 3 composantes RGB (moins 1)
                }
                else if(code == 5){
                    waiting8bitColor = true;
                }
                else {
                    throw new IllegalColorSequenceException(String.format("Invalid foreground/background code %d, must be 2 or 5.", code));
                }
                continue;
            }

            switch(code){
                // default
                case 0:  return defaultStyle; // arrête si on trouve un code 0

                // text style
                case 1:  style = style.setBold(true); break;
                case 21: style = style.setBold(false); break; // doubly underline unsupported
                case 2:  style = style.setDim(true); break;
                case 22: style = style.setDim(false); break;
                case 3:  style = style.setItalic(true); break;
                case 23: style = style.setItalic(false); break; // not fraktur unsupported
                case 4:  style = style.setUnderline(true); break;
                case 24: style = style.setUnderline(false); break;
                case 5:  style = style.setBlink(true); break;
                case 25: style = style.setBlink(false); break;
                case 6:  throw new IllegalColorSequenceException("Rapid blink is unsupported.");
                case 7:  style = style.setReverse(true); break;
                case 27: style = style.setReverse(false); break;
                case 8:  style = style.setHidden(true); break;
                case 28: style = style.setHidden(false); break;
                case 9:  style = style.setStrikethrough(true); break;
                case 29: style = style.setStrikethrough(false); break;

                case 26: throw new IllegalColorSequenceException("Proportional spacing is unsupported.");
                case 50: System.err.println("Warning: proportional spacing is unsupported."); break; // désactivation : pas besoin d'erreur
                case 51: throw new IllegalColorSequenceException("Framed is unsupported.");
                case 52: throw new IllegalColorSequenceException("Encircled is unsupported.");
                case 53: throw new IllegalColorSequenceException("Overlined is unsupported.");
                case 54: System.err.println("Warning: Framed and Encircled are unsupported."); break; // désactivation : pas besoin d'erreur
                case 55: System.err.println("Warning: Overlined is unsupported."); break; // désactivation : pas besoin d'erreur

                case 58: throw new IllegalColorSequenceException("Underline color is unsupported."); // not standard. TODO ?
                case 59: System.err.println("Warning: Underline color is unsupported."); break; // désactivation : pas besoin d'erreur

                case 60: // ideogram underline or right side line
                case 61: // ideogram double underline or double line on the right edge
                case 62: // ideogram overline or left side edge
                case 63: // ideogram double overline or double line on the left edge
                case 64: throw new IllegalColorSequenceException("Ideogram lines are unsupported."); // ideogram stress marking
                case 65: System.err.println("Warning: Ideogram lines are unsupported."); break; // désactivation : pas besoin d'erreur

                // fonts
                case 10: // font par défaut
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19: // 11-19 : fonts alternatifs
                    Optional<String> family = FontRegister.INSTANCE.get(code-10);
                    if(family.isPresent())
                        style = style.setFont(family.get());
                    else if(code == 10)
                        throw new IllegalColorSequenceException("Default font family not found.");
                    else
                        throw new IllegalColorSequenceException("Alternative font families not found.");
                    break;
                case 20: style = style.setFont(fraktur); break; // lettres germaniques

                // foreground (text color)
                case 38: foregroundMode = true; break; // marqueur de début de séquence d'un foreground personalisé.
                case 39: style = style.setForeground(defaultStyle.foreground.get()); break;

                case 30: style = style.setForeground(Aixterm_Color.BLACK); break;
                case 31: style = style.setForeground(Aixterm_Color.RED); break;
                case 32: style = style.setForeground(Aixterm_Color.GREEN); break;
                case 33: style = style.setForeground(Aixterm_Color.YELLOW); break;
                case 34: style = style.setForeground(Aixterm_Color.BLUE); break;
                case 35: style = style.setForeground(Aixterm_Color.MAGENTA); break;
                case 36: style = style.setForeground(Aixterm_Color.CYAN); break;
                case 37: style = style.setForeground(Aixterm_Color.WHITE); break;

                case 90: style = style.setForeground(Aixterm_Color.BRIGHT_BLACK); break;
                case 91: style = style.setForeground(Aixterm_Color.BRIGHT_RED); break;
                case 92: style = style.setForeground(Aixterm_Color.BRIGHT_GREEN); break;
                case 93: style = style.setForeground(Aixterm_Color.BRIGHT_YELLOW); break;
                case 94: style = style.setForeground(Aixterm_Color.BRIGHT_BLUE); break;
                case 95: style = style.setForeground(Aixterm_Color.BRIGHT_MAGENTA); break;
                case 96: style = style.setForeground(Aixterm_Color.BRIGHT_CYAN); break;
                case 97: style = style.setForeground(Aixterm_Color.BRIGHT_WHITE); break;

                // background
                case 48: backgroundMode = true; break; // marqueur de début de séquence d'un background personalisé.
                case 49: style = style.setBackground(defaultStyle.background.get()); break;

                case 40: style = style.setBackground(Aixterm_Color.BLACK); break;
                case 41: style = style.setBackground(Aixterm_Color.RED); break;
                case 42: style = style.setBackground(Aixterm_Color.GREEN); break;
                case 43: style = style.setBackground(Aixterm_Color.YELLOW); break;
                case 44: style = style.setBackground(Aixterm_Color.BLUE); break;
                case 45: style = style.setBackground(Aixterm_Color.MAGENTA); break;
                case 46: style = style.setBackground(Aixterm_Color.CYAN); break;
                case 47: style = style.setBackground(Aixterm_Color.WHITE); break;

                case 100: style = style.setBackground(Aixterm_Color.BRIGHT_BLACK); break;
                case 101: style = style.setBackground(Aixterm_Color.BRIGHT_RED); break;
                case 102: style = style.setBackground(Aixterm_Color.BRIGHT_GREEN); break;
                case 103: style = style.setBackground(Aixterm_Color.BRIGHT_YELLOW); break;
                case 104: style = style.setBackground(Aixterm_Color.BRIGHT_BLUE); break;
                case 105: style = style.setBackground(Aixterm_Color.BRIGHT_MAGENTA); break;
                case 106: style = style.setBackground(Aixterm_Color.BRIGHT_CYAN); break;
                case 107: style = style.setBackground(Aixterm_Color.BRIGHT_WHITE); break;

                default:
                    throw new IllegalColorSequenceException(String.format("Unknown code %d in sequence %s.", code, sequenceToString(codes)));
            }
        }

        if(foregroundMode)
            throw new IllegalColorSequenceException(String.format("Foreground needs more parameters in sequence %s.", sequenceToString(codes)));

        if(backgroundMode)
            throw new IllegalColorSequenceException(String.format("Background needs more parameters in sequence %s.", sequenceToString(codes)));

        return style;
    }
}

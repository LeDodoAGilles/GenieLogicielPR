package ShellNightmare.Terminal.TerminalFX;

import ShellNightmare.Terminal.TerminalFX.color.Aixterm_Color;
import ShellNightmare.Terminal.TerminalFX.color.DodoColor;
import javafx.scene.paint.Color;

import java.util.*;

// inspiré de : https://github.com/FXMisc/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/richtext/TextStyle.java
// tous les champs sont final afin de ne pas perturber JavaFX

// TODO des Optional dans le reste du code dans les retour et params de méthode quand ça peut êrte nul

public class DodoStyle {
    public static final DodoStyle EMPTY = makeCssStyle("");

    public final Collection<String> classStyles; // Collections.empty si aucun
    private final String cssStyle;   // "" si aucun

    // Seront ajoutés à cssStyle si non null
    public final Optional<Boolean> bold;
    public final Optional<Boolean> dim;
    public final Optional<Boolean> italic;
    public final Optional<Boolean> underline;
    public final Optional<Boolean> blink; // TODO
    public final Optional<Boolean> reverse; // inversera foreground et background
    public final Optional<Boolean> hidden;
    public final Optional<Boolean> strikethrough; // barré
    public final Optional<Integer> size; // font size
    public final Optional<String> font; // font family
    public final Optional<DodoColor> foreground; // text color
    public final Optional<DodoColor> background;

    public static DodoStyle makeClassStyles(Collection<String> styles){
        return new DodoStyle(styles, "");
    }

    public static DodoStyle makeSingleClassStyle(String style){
        return new DodoStyle(Collections.singleton(style), "");
    }

    // TODO https://linuxhint.com/ls_colors_bash/
    public static DodoStyle getStyleFromClassCode(int code){
        switch(code){
            case 128: return makeSingleClassStyle("ls-folder");
            default: return EMPTY;
        }
    }

    public static DodoStyle makeCssStyle(String style){
        return new DodoStyle(Collections.<String>emptyList(), style);
    }

    public DodoStyle(Collection<String> classStyles, String cssStyle,
                     Optional<Boolean> bold,
                     Optional<Boolean> dim,
                     Optional<Boolean> italic,
                     Optional<Boolean> underline,
                     Optional<Boolean> blink,
                     Optional<Boolean> reverse,
                     Optional<Boolean> hidden,
                     Optional<Boolean> strikethrough,
                     Optional<Integer> size,
                     Optional<String> font,
                     Optional<DodoColor> foreground,
                     Optional<DodoColor> background){
        this.classStyles = classStyles;
        this.cssStyle = cssStyle;

        this.bold = bold;
        this.dim = dim;
        this.italic = italic;
        this.underline = underline;
        this.blink = blink;
        this.reverse = reverse;
        this.hidden = hidden;
        this.strikethrough = strikethrough;
        this.size = size;
        this.font = font;
        this.foreground = foreground;
        this.background = background;
    }

    public DodoStyle(Collection<String> classStyles, String cssStyle){
        this(classStyles, cssStyle,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DodoStyle) {
            DodoStyle that = (DodoStyle) other;
            return Objects.equals(this.classStyles,   that.classStyles) &&
                   Objects.equals(this.cssStyle,      that.cssStyle) &&
                   Objects.equals(this.bold,          that.bold) &&
                   Objects.equals(this.dim,           that.dim) &&
                   Objects.equals(this.italic,        that.italic) &&
                   Objects.equals(this.underline,     that.underline) &&
                   Objects.equals(this.blink,         that.blink) &&
                   Objects.equals(this.reverse,       that.reverse) &&
                   Objects.equals(this.hidden,        that.hidden) &&
                   Objects.equals(this.strikethrough, that.strikethrough) &&
                   Objects.equals(this.size,          that.size) &&
                   Objects.equals(this.font,          that.font) &&
                   Objects.equals(this.foreground,    that.foreground) &&
                   Objects.equals(this.background,    that.background);
        } else {
            return false;
        }
    }

    public DodoStyle addCssStyle(String another){
        String newStyle = cssStyle;
        if(!newStyle.isEmpty() && !newStyle.endsWith(";"))
            newStyle += ";";
        newStyle += another;
        return new DodoStyle(classStyles, newStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    /** Retourne un nouveau style possédant les mêmes attribus, sauf bold qui est donné. le paramètre peut être null. */
    public DodoStyle setBold(Boolean bold){
        return new DodoStyle(classStyles, cssStyle, Optional.ofNullable(bold), dim, italic, underline, blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setDim(Boolean dim){
        return new DodoStyle(classStyles, cssStyle, bold, Optional.ofNullable(dim), italic, underline, blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setItalic(Boolean italic){
        return new DodoStyle(classStyles, cssStyle, bold, dim, Optional.ofNullable(italic), underline, blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setUnderline(Boolean underline){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, Optional.ofNullable(underline), blink, reverse, hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setBlink(Boolean blink){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, Optional.ofNullable(blink), reverse, hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setReverse(Boolean reverse){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, Optional.ofNullable(reverse), hidden, strikethrough, size, font, foreground, background);
    }

    public DodoStyle setHidden(Boolean hidden){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, Optional.ofNullable(hidden), strikethrough, size, font, foreground, background);
    }

    public DodoStyle setStrikethrough(Boolean strikethrough){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, Optional.ofNullable(strikethrough), size, font, foreground, background);
    }

    public DodoStyle setSize(Integer size){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, Optional.ofNullable(size), font, foreground, background);
    }

    public DodoStyle setFont(String font){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, size, Optional.ofNullable(font), foreground, background);
    }

    public Optional<DodoColor> getForeground(){
        return foreground;
    }

    public DodoStyle setForeground(DodoColor foreground){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, size, font, Optional.ofNullable(foreground), background);
    }

    public DodoStyle setForeground(String foreground){
        return setForeground(new DodoColor(foreground));
    }

    public DodoStyle setForeground(Color foreground){
        return setForeground(new DodoColor(foreground));
    }

    public DodoStyle setForeground(Aixterm_Color foreground){
        return setForeground(foreground.toString());
    }

    public Optional<DodoColor> getBackground(){
        return background;
    }

    public DodoStyle setBackground(DodoColor background){
        return new DodoStyle(classStyles, cssStyle, bold, dim, italic, underline, blink, reverse, hidden, strikethrough, size, font, foreground, Optional.ofNullable(background));
    }

    public DodoStyle setBackground(String background){
        return setBackground(new DodoColor(background));
    }

    public DodoStyle setBackground(Color background){
        return setBackground(new DodoColor(background));
    }

    public DodoStyle setBackground(Aixterm_Color background){
        return setBackground(background.toString());
    }

    public String getCssStyle(){
        StringBuilder builder = new StringBuilder(cssStyle);
        if(!cssStyle.isEmpty() && !cssStyle.endsWith(";"))
            builder.append(";");

        if(bold.isPresent()) {
            if (bold.get())
                builder.append("-fx-font-weight: bold;");
            else
                builder.append("-fx-font-weight: normal;");
        }

        if(italic.isPresent()) {
            if (italic.get())
                builder.append("-fx-font-style: italic;");
            else
                builder.append("-fx-font-style: normal;");
        }

        if(underline.isPresent()) {
            if (underline.get())
                builder.append("-fx-underline: true;");
            else
                builder.append("-fx-underline: false;");
        }

        // blink : pas de style css

        if(strikethrough.isPresent()) {
            if (strikethrough.get())
                builder.append("-fx-strikethrough: true;");
            else
                builder.append("-fx-strikethrough: false;");
        }

        size.ifPresent(integer -> builder.append("-fx-font-size: ").append(integer).append(";"));
        font.ifPresent(str -> builder.append("-fx-font-family: '").append(str).append("';"));

        Optional<DodoColor> back, fore;

        if(reverse.isPresent() && reverse.get()){
            back = foreground;
            fore = background;
            if(back.isEmpty() || fore.isEmpty())
                System.err.println("Warning: inversion background-foreground et l'un des deux est null.");
        }
        else {
            back = background;
            fore = foreground;
        }

        if(hidden.isPresent()){
            if(back.isPresent()){
                if(hidden.get())
                    fore = back;
            }
            else
                System.err.println("Warning : hidden est fourni sans connaitre le background");
        }
        else if(dim.isPresent()){
            if(fore.isPresent()){
                if(dim.get())
                    fore = Optional.of(fore.get().dim());
            }
            else
                System.err.println("Warning : dim est fourni sans connaitre le foreground");
        }

        fore.ifPresent(dodo -> builder.append("-fx-fill: ").append(dodo.toString()).append(";"));
        back.ifPresent(dodo -> builder.append("-rtfx-background-color: ").append(dodo.toString()).append(";"));

        return builder.toString();
    }
}

package utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.ColorConverter;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

//import javafx.css.converters.ColorConverter; // remplacé par import javafx.css.converter.ColorConverter;

/* Sert à déterminer une couleur nommée dans un fichier css.
Cette classe ne provient pas de notre projet.
lien original : https://stackoverflow.com/questions/32625212/convert-color-from-css-to-javafx-color-object
Cette classe a cependant été modifiée par nos soins par soucis de facilité d'utilisation.
*/

/** Utilitaire pour récupérer la valeur d'une couleur depuis une variable css.
 * L'instance HELPER doit d'abord être ajoutée aux enfants de root.
 * Cette classe doit être considérée comme une librairie externe. Son code est disponible en suivant le lien ci-dessous :
 * https://stackoverflow.com/questions/32625212/convert-color-from-css-to-javafx-color-object
 * Le code a cependant été un peu adapté.
 *
 * Usage : HELPER.getNamedColor(variable_name) */
public class CssToColorHelper extends Parent{
    public static final CssToColorHelper HELPER = new CssToColorHelper(); // Ajouté

    /** Prend le style css de la variable indiquée et récupère la valeur de la couleur utilisée.
     * @return la couleur JavaFX de la variable css indiquée */
    public static Color getNamedColor(String name) { // Ajouté, vient de l'exemple d'utilisation fourni par l'auteur
        HELPER.setStyle("-named-color: " + name + ";");
        HELPER.applyCss();
        return HELPER.getNamedColor();
    }


    public static final Color DEFAULT_NAMED_COLOR = null;

    private ObjectProperty<Color> namedColor;

    public ObjectProperty<Color> namedColorProperty() {
        if(namedColor == null) {
            namedColor = new StyleableObjectProperty<Color>(DEFAULT_NAMED_COLOR) {

                @Override
                protected void invalidated() {
                    super.invalidated();
                }

                @Override
                public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                    return StyleableProperties.NAMED_COLOR;
                }

                @Override
                public Object getBean() {
                    return CssToColorHelper.this;
                }

                @Override
                public String getName() {
                    return "namedColor";
                }
            };
        }
        return namedColor;
    }

    public Color getNamedColor() {
        return namedColorProperty().get();
    }

    public CssToColorHelper() {
        setFocusTraversable(false);
        getStyleClass().add("css-to-color-helper");
    }

    private static class StyleableProperties {
        private static final CssMetaData<CssToColorHelper, Color> NAMED_COLOR =
                new CssMetaData<CssToColorHelper, Color>("-named-color", ColorConverter.getInstance(),
                        DEFAULT_NAMED_COLOR) {

                    @Override
                    public boolean isSettable(CssToColorHelper n) {
                        return n.namedColor == null || !n.namedColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Color> getStyleableProperty(CssToColorHelper n) {
                        return (StyleableProperty<Color>) (WritableValue<Color>) n.namedColorProperty();
                    }

                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Parent.getClassCssMetaData());
            styleables.add(NAMED_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}

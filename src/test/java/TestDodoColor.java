import ShellNightmare.Terminal.TerminalFX.color.DodoColor;
import javafx.scene.paint.Color;
import org.assertj.core.api.Assertions;

public class TestDodoColor {
    /* Teste qu'un DodoColor encapsulant un nom de variable css redonne bien ce nom avec toString. */
    @org.junit.Test
    public void testHoldName(){
        String name = "nom-de-variable";
        DodoColor color = new DodoColor(name);
        Assertions.assertThat(color.toString()).isEqualTo(name);
    }

    /* Teste qu'un DodoColor encapsulant une couleur JavaFX redonne bien les mêmes composantes avec toString. */
    @org.junit.Test
    public void testHoldColor(){
        int r = 32;
        int g = 64;
        int b = 128;
        String s = String.format("rgb(%d,%d,%d)", r, g, b);

        Color color = Color.rgb(r,g,b);
        DodoColor dodo = new DodoColor(Color.rgb(r,g,b));
        Assertions.assertThat(dodo.getFXColor()).isEqualTo(color);
    }

    /* Teste qu'un DodoColor assombrit bien la couleur JavaFX qu'il contient. */
    @org.junit.Test
    public void testDimColor(){
        int r = 32;
        int g = 64;
        int b = 128;

        Color color = Color.rgb(r,g,b);
        DodoColor dodo = new DodoColor(color);
        Color darker = dodo.dim().getFXColor();
        Assertions.assertThat(darker.getBrightness()).isLessThan(color.getBrightness());
    }
}

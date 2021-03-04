import ShellNightmare.Terminal.TerminalFX.DodoStyle;
import ShellNightmare.Terminal.TerminalFX.color.SGR;
import javafx.scene.paint.Color;
import org.assertj.core.api.Assertions;

public class TestSGR {
    private final DodoStyle defaultStyle = DodoStyle.EMPTY.setForeground(Color.WHITE).setBackground(Color.BLACK);

    private void applyTest(DodoStyle style, DodoStyle expectedStyle, Integer... codes){
        DodoStyle newStyle = SGR.modifyStyleFromSequence(style, defaultStyle, codes);
        Assertions.assertThat(newStyle).isEqualTo(expectedStyle);
    }

    /* Teste différents codes SGR */
    @org.junit.Test
    public void testSeveralCodes(){
        applyTest(defaultStyle, defaultStyle.setBold(true), 1);
        applyTest(defaultStyle, defaultStyle.setBold(false), 21);
        applyTest(defaultStyle, defaultStyle.setDim(true), 2);
        applyTest(defaultStyle, defaultStyle.setDim(false), 22);
        applyTest(defaultStyle, defaultStyle.setItalic(true), 3);
        applyTest(defaultStyle, defaultStyle.setItalic(false), 23);
        applyTest(defaultStyle, defaultStyle.setUnderline(true), 4);
        applyTest(defaultStyle, defaultStyle.setUnderline(false), 24);
        applyTest(defaultStyle, defaultStyle.setBlink(true), 5);
        applyTest(defaultStyle, defaultStyle.setBlink(false), 25);
        applyTest(defaultStyle, defaultStyle.setReverse(true), 7);
        applyTest(defaultStyle, defaultStyle.setReverse(false), 27);
        applyTest(defaultStyle, defaultStyle.setHidden(true), 8);
        applyTest(defaultStyle, defaultStyle.setHidden(false), 28);
        applyTest(defaultStyle, defaultStyle.setStrikethrough(true), 9);
        applyTest(defaultStyle, defaultStyle.setStrikethrough(false), 29);

        // tout en même temps
        applyTest(defaultStyle, defaultStyle.setBold(true).setDim(true).setItalic(true).setUnderline(true).setBlink(true).setReverse(true).setHidden(true).setStrikethrough(true), 9, 8, 7, 5, 1, 2, 3, 4);

        // reset de plusieurs codes
        applyTest(defaultStyle, defaultStyle, 1, 2, 3, 0, 7, 4, 5, 1337);
    }
}

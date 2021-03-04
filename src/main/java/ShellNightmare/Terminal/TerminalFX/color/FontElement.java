package ShellNightmare.Terminal.TerminalFX.color;

import javafx.scene.text.Font;

public class FontElement {
    public String fontFamily;
    public java.io.File tempFile; // nul si c'est le font par défaut car le fichier est dans l'archive

    public FontElement(String fontFamily, java.io.File tempFile){
        this.fontFamily = fontFamily;
        this.tempFile = tempFile;
    }

    public void dispose(){
        if(tempFile != null)
            tempFile.delete();
    }
}

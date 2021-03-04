package ShellNightmare.Terminal.TerminalFX.color;

import javafx.scene.text.Font;

import java.util.Optional;

public class FontRegister {
    public static final FontRegister INSTANCE = new FontRegister();

    private static final int REGISTER_SIZE = 10;
    private final FontElement[] register = new FontElement[REGISTER_SIZE]; // index = code SGR - 10 (donc 0 = default font, 1-9 = alternative fonts)

    private FontRegister(){}

    /* renvoie le font-family */
    public Optional<String> get(int index){
        return register[index] != null ? Optional.of(register[index].fontFamily) : Optional.empty();
    }

    public void set(int index, String fontFamily, java.io.File tempFile){
        if(register[index] != null){
            register[index].dispose();
        }

        register[index] = new FontElement(fontFamily, tempFile);
    }

    public void remove(int index){
        if(register[index] != null){
            register[index].dispose();
            register[index] = null;
        }
    }

    public void removeAll(){
        for(int i=0 ; i<REGISTER_SIZE ; i++){
            if(register[i] != null){
                register[i].dispose();
                register[i] = null;
            }
        }
    }

    public void removeAlternatives(){
        for(int i=1 ; i<REGISTER_SIZE ; i++){
            if(register[i] != null){
                register[i].dispose();
                register[i] = null;
            }
        }
    }
}

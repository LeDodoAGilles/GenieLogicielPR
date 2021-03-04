package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.TerminalFX.color.FontRegister;
import javafx.application.Platform;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/** Usage : font --load 5 --size 12 filename.ttf
 * avec l'index 0 pour set le font par défaut
 * avec un index de 1 à 9 pour un font alternatif.
 * avec une taille en pt
 *
 * Le font pourra ensuite être utilisé avec les codes SGR, grâce au code 10+index
 * Avec l'exemple plus haut : \e[15m */

/**
 * commande voir man pour le détail
 * @author Louri Noël
 */
@Permission
@FileMode(CommandFileMode.Read)
public class Font extends Command {
    String index = null;
    String size = null;

    @Override
    public void processCommand() {
        if(index == null){
            stderr.add("Un index doit être précisé.");
            return;
        }

        if(size == null){
            stderr.add("Une taille doit être précisée.");
            return;
        }

        int i;
        try {
            i = Integer.parseInt(index);
        } catch (NumberFormatException ignore){
            stderr.add("L'index " + index + " est invalide : un nombre entre 0 et 9 inclus est attendu.");
            return;
        }

        if(!(0 <= i && i <= 9)){
            stderr.add("L'index " + index + " est invalide : un nombre entre 0 et 9 inclus est attendu.");
            return;
        }

        int s;
        try {
            s = Integer.parseInt(size);
        } catch (NumberFormatException ignore){
            stderr.add("La taille " + size + " est invalide : un nombre strictement positif est attendu.");
            return;
        }

        if(s <= 0){
            stderr.add("La taille " + size + " est invalide : un nombre strictement positif est attendu.");
            return;
        }

        if(InputFiles.isEmpty()){
            stderr.add("Un fichier doit être donné.");
            return;
        }

        File<?> f = InputFiles.get(0);

        if (f.getType() != Type.BINARY){
            stderr.add("Le fichier doit être binaire.");
            return;
        }

        File<BinaryData> binaryFile = File.ConvertFile(f, BinaryData.class);

        Platform.runLater(() -> processInRightThread(binaryFile, i, s));
    }

    private void processInRightThread(File<BinaryData> binaryFile, int index, int size){
        java.io.File temp;
        try {
            temp = Files.createTempFile("EscapeTheShell", "").toFile();

            try(OutputStream outputStream = new FileOutputStream(temp)){
                binaryFile.getInodeData().writeToStream(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(temp.toURI().toString(), size);
        FontRegister.INSTANCE.set(index, font.getFamily(), temp);
    }

    public void LONGOPT_l_load(String i){index = i;}
    public void LONGOPT_s_size(String s){size = s;}
}

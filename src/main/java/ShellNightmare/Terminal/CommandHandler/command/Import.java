package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.MetaContext;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * commande voir man pour le détail
 * @author Louri Noêl
 */

@Permission
@FileMode(CommandFileMode.Write)
public class Import extends Command {
    boolean forceText = false;
    boolean forceBinary = false;

    @Override
    public void processCommand() {
        /*if(MetaContext.mainDaemon.t == null || !MetaContext.mainDaemon.t.debug) // commande valide uniquement depuis l'éditeur
            return;*/

        if(forceText && forceBinary) // pas les deux à la fois
            return;

        PseudoPath p;

        if(InputPseudoPath.size() == 0){
            p = new PseudoPath(context.currentPath); //On récupère le chemin du fichier
            p.newFile = null;
        }
        else{
            p = InputPseudoPath.get(0);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier");
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        Platform.runLater(() -> {
            java.io.File file = fileChooser.showOpenDialog(MetaContext.mainDaemon.t.getScene().getWindow());
            if(file != null){
                if(!forceBinary && (forceText || file.getName().endsWith(".txt")))
                    importText(file, p);
                else
                    importBinary(file, p);
            }
        });
    }

    private void importText(java.io.File file, PseudoPath p){
        if(p.newFile == null)
            p.newFile = file.getName();

        File<Data> data;

        if (!p.isFileExist()) {
            data = new File<>(p.newFile, Data.class, context.currentUser);
            addErrorMessages(data.addToFolder(p.getFolder(), context.fs, context.currentUser));
        }
        else {
            data = File.ConvertFile(p.getChildFile(), Data.class);
        }

        try{
            String s = Files.readString(Paths.get(file.toURI()), StandardCharsets.UTF_8);
            s = s.replaceAll("\r", "");
            data.getInodeData().setData(s);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void importBinary(java.io.File file, PseudoPath p){
        if(p.newFile == null)
            p.newFile = file.getName();

        File<BinaryData> data;

        if (!p.isFileExist()) {
            data = new File<>(p.newFile, BinaryData.class, context.currentUser);
            addErrorMessages(data.addToFolder(p.getFolder(), context.fs, context.currentUser));
        }
        else {
            data = File.ConvertFile(p.getChildFile(), BinaryData.class);
        }

        data.getInodeData().loadFromImportedFile(file);
    }

    public void LONGOPT_t_text(){forceText = true;}
    public void LONGOPT_b_binary(){forceBinary = true;}
}

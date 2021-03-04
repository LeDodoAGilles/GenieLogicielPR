package Interface;

import ShellNightmare.Terminal.FileSystem.Type;
import ShellNightmare.Terminal.MetaContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreateFileGUI extends GUI {
    public static final String FXML = "/FXML/CreateFile.fxml";

    private static final String LOADED_BINARY_PROMPT = "Ce type de fichier ne stocke pas de contenu, ou ce dernier n'est pas affichable.";
    public static final Pattern LOOSE_FOCUS_PERMISSION_PATTERN = Pattern.compile("[0-7][0-7][0-7]");
    private static final Pattern EDITING_PERMISSION_PATTERN = Pattern.compile("[0-7]{0,3}");

    private static final String INVALID_NAME_STYLE = "-fx-text-fill: red;";
    private static final String VALID_NAME_STYLE = "-fx-text-fill: black;";

    @FXML
    private ComboBox<String> folderCB;
    @FXML
    private TextField nameTF;
    @FXML
    private ComboBox<String> typeCB;
    @FXML
    private ComboBox<String> userCB;
    @FXML
    private ComboBox<String> groupCB;
    @FXML
    private TextField permissionTF;
    @FXML
    private CheckBox textModeCheckbox;
    @FXML
    private TextArea contentArea;

    public boolean toProcess = false; // si appui sur OK et que donc doit interpréter les entrées

    public CreateFileOptions defaultData; // données d'entrée
    public CreateFileOptions resultData; // données à retourner

    private String savedContent = ""; // lors du changement de type, pour reload si jamais on revient à Type.DATA

    public BooleanProperty nameIsInvalid = new SimpleBooleanProperty(false);

    @Override
    public void reset(Object[] args) {}

    @Override
    public void dispose() {}

    @Override
    public void init(Object[] args) {
        assert args.length == 1;
        defaultData = (CreateFileOptions) args[0];
        resultData = (CreateFileOptions) defaultData.clone();

        folderCB.getItems().addAll(defaultData.allFolders);
        folderCB.getSelectionModel().select(defaultData.folder);
        folderCB.setEditable(defaultData.editableFolder);
        folderCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> {
            resultData.folder = n;
            verifyNameTF(); // car c'est peut être un nom de fichier qui était valide dans le dossier précédent mais pas celui là
        });

        nameTF.setText(defaultData.name);
        nameTF.setEditable(defaultData.editableName);
        nameTF.textProperty().addListener((e, o, n) -> {
            nameTF.setText(n.replace(" ", "_"));
            verifyNameTF();
        });

        nameIsInvalid.addListener((e, o, n) -> {
            nameTF.setStyle(n ? INVALID_NAME_STYLE : VALID_NAME_STYLE);
        });

        typeCB.getItems().addAll(Arrays.stream(Type.values()).map(t -> t.longName).collect(Collectors.toList()));
        typeCB.getSelectionModel().select(defaultData.type.longName);
        typeCB.setEditable(defaultData.editableType);
        typeCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> {
            resultData.type = Type.getTypeByLongName(n);
            if(resultData.type == Type.DATA){
                contentArea.setPromptText("");
                contentArea.setText(savedContent);
                textModeCheckbox.setSelected(true);
            }
            else if(Type.getTypeByLongName(o) == Type.DATA){ // car sinon pas besoin d'enregistrer ailleurs
                savedContent = contentArea.getText();
                contentArea.setPromptText(LOADED_BINARY_PROMPT);
                contentArea.setText("");
            }
        });

        textModeCheckbox.selectedProperty().addListener((e, o, n) -> {
            if(!n && typeCB.getSelectionModel().getSelectedItem().equals(Type.DATA.longName))
                textModeCheckbox.setSelected(true);
        });

        userCB.getItems().addAll(defaultData.allUsers);
        userCB.getSelectionModel().select(defaultData.user);
        userCB.setEditable(defaultData.editableUser);
        userCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> resultData.user = n); // todo invalidation

        groupCB.getItems().addAll(defaultData.allGroups);
        groupCB.getSelectionModel().select(defaultData.group);
        groupCB.setEditable(defaultData.editableGroup);
        groupCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> resultData.group = n); // todo invalidation

        permissionTF.setText(defaultData.permission);
        permissionTF.setEditable(defaultData.editablePermission);
        permissionTF.textProperty().addListener((e, o, n) -> {
            if(!n.equals(resultData.permission)){
                if(!EDITING_PERMISSION_PATTERN.matcher(n).matches()) // pendant qu'on édite, il peut y avoir moins de 3 chiffres
                    permissionTF.setText(o); // reset
            }
        });
        permissionTF.focusedProperty().addListener((e, o, n) -> {
            if(!n && !permissionTF.getText().equals(resultData.permission)){
                if(LOOSE_FOCUS_PERMISSION_PATTERN.matcher(permissionTF.getText()).matches())
                    resultData.permission = permissionTF.getText();
                else // invalidate
                    permissionTF.setText(resultData.permission); // reset
            }
        });

        savedContent = defaultData.content;
        contentArea.setWrapText(false);
        contentArea.setText(defaultData.content);

        nameTF.requestFocus();
    }

    private void verifyNameTF(){
        String n = nameTF.getText();

        if(!n.isEmpty() && (defaultData.allPaths.contains(resultData.folder + n)
                || n.equals(".") || n.equals("..") || n.equals("~") || n.contains("/") || n.contains("\\")
                || !MetaContext.VALID_FILE_NAME.matcher(n).matches())){
            // blacklisted : ce nom est déjà utilisé dans ce dossier (path déjà pris)
            //nameTF.setText(resultData.name); // non ! car empêche d'écrire dès qu'on a un préfixe
            nameIsInvalid.set(true);
        }
        else {
            resultData.name = n;
            nameIsInvalid.set(false);
        }
    }

    @FXML
    private void selectImport(){
        Type type = Type.getTypeByLongName(typeCB.getSelectionModel().getSelectedItem());
        if(!(type == Type.DATA || type == Type.BINARY)){
            Framework.showError("Importation impossible", "Le fichier doit être de type texte ou binaire.");
            return;
        }

        // TODO factorisation avec la commande import

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier");
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        java.io.File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            try{
                if(textModeCheckbox.isSelected() || file.getName().endsWith(".txt")){
                    resultData.content = Files.readString(Paths.get(file.toURI()), StandardCharsets.UTF_8);
                    resultData.binaryContent = new byte[]{};
                    contentArea.setText(resultData.content);
                    typeCB.getSelectionModel().select(Type.DATA.longName);
                }
                else{
                    resultData.binaryContent = Files.readAllBytes(file.toPath());
                    resultData.content = "";
                    contentArea.clear();
                    typeCB.getSelectionModel().select(Type.BINARY.longName);
                }
            } catch(IOException e){
                Framework.showError("Importation impossible", "Impossible d'ouvrir le fichier.");
                return;
            }
        }
    }

    @FXML
    private void selectOK(){
        toProcess = true;
        stage.hide();
    }

    @FXML
    private void selectCancel(){
        stage.hide();
    }
}

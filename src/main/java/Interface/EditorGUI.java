package Interface;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import ShellNightmare.Terminal.challenge.Challenge;
import ShellNightmare.Terminal.challenge.ChallengeHeader;
import ShellNightmare.Terminal.interpreter.ExplicitCommandInterpreter;
import com.mifmif.common.regex.Generex;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.codec.digest.DigestUtils;
import utils.Utils;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ShellNightmare.Terminal.MetaContext.mainDaemon;
import static ShellNightmare.Terminal.challenge.ChallengeHeader.SHAPER_USERNAME;

/** GUI de l'éditeur de challenges.
 *
 * @author Louri
 * @author Marc
 * @author Gaëtan Lounes
*/
public class EditorGUI extends GUI {
    public static final String FXML = "/FXML/Editor.fxml";
    private static final String HTML_HELP = "help/index.html";

    private static final String HASHED_MASTER_PASSWORD_REPLACEMENT = "$$$ hashed $$$"; // remplace un mot de passe hashé

    // caractères possibles pour le nom du fichier .zip du challenge
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_0-9 .-]*");

    // noms des fichiers requis par défaut
    private static final String COMMAND_SCRIPT = "commandScript.sh";
    private static final String DOCS = "docs";
    private static final String END_SCRIPT = "endScript.sh";
    private static final String HOME = "home";
    private static final String LAUNCH = "launch";
    private static final String INIT_SCRIPT = "initScript.sh";
    private static final String MAIN_SCRIPT = "mainScript.sh";
    private static final String README = "readme.md";
    private static final String RESOURCES = "resources";
    private static final String SCRIPTS = "scripts";

    private static final String COMMAND_SCRIPT_TOOLTIP = "";
    private static final String DOCS_TOOLTIP = "Contient les documents importants du challenge.";
    private static final String END_SCRIPT_TOOLTIP = "Exécuté lors de la victoire.";
    private static final String HOME_TOOLTIP = "Répertoire courant de jeu.";
    private static final String LAUNCH_TOOLTIP = "À exécuter avec bash pour lancer le challenge facilement.";
    private static final String INIT_SCRIPT_TOOLTIP = "Exécuté pour lancer le challenge.";
    private static final String MAIN_SCRIPT_TOOLTIP = "Script pouvant être lancé par l'utilisateur directement.";
    private static final String README_TOOLTIP = "";
    private static final String RESOURCES_TOOLTIP = "Contient les ressources du challenges (musiques, fonts, textes, ...)";
    private static final String SCRIPTS_TOOLTIP = "Contient les scripts utilitaires lancés au démarrage du challenge.";

    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField challengeNameTF;
    @FXML
    private TextField difficultyTF;
    @FXML
    private TextField usernameTF;
    @FXML
    private TextField masterPasswordTF;
    @FXML
    private TextField rootPasswordTF;
    @FXML
    private TextField nbPartTF;
    @FXML
    private BorderPane terminalPane;
    @FXML
    private VBox commandsPane;
    @FXML
    private ImageView thumbnailView;
    @FXML
    private TreeView<FileLabel> hierarchy;

    // https://stackoverflow.com/questions/32335015/get-a-list-of-all-treecell-objects-that-currently-exist-in-a-treeview
    // ensemble des items du treeview hierarchy
    // weak reference pour être garbage-collecté quand il le faudra
    private final Set<TreeItem<FileLabel>> existingItems = Collections.newSetFromMap(new WeakHashMap<>()); // et idem ici pour avoir tous les noeuds

    private Terminal terminal;

    private Challenge challenge; // challenge créé ou édité
    private Context contextShaper;
    private User shaper; // sauvegarde du Shaper
    private User playerUser; // sauvegarde du User du joueur
    private ExplicitCommandInterpreter eci; // pour lancer des commandes depuis le GUI
    private Map.Entry<String, Long> dataPassword; // données concernant les mots de passe aléatoires
    private final HashMap<String, CheckBox> cmdCheckboxes = new HashMap<>(); // associe un nom de commande à sa checkbox

    private FileLabel cutbuffer = null;
    private boolean cutbufferCut = false; // false si Copy, true si Cut

    @Override
    public void reset(Object[] args) { // TODO reload header
        assert !challenge.filename.isEmpty();

        ChallengeHeader header;
        try {
            header = Challenge.readSimpleField(ChallengeHeader.class, Challenge.CHALLENGES_FOLDER+"/"+challenge.filename, Challenge.HEADER);
        } catch (IOException e) {
            Framework.showError("Impossible de récupérer l'état de validation du challenge.", "Le challenge ne peut pas être re-chargé.");
            return;
        }

        MetaContext.win = false;

        if(header.validated)
            framework.previous(); // sort de l'éditeur si le challenge a été validé, sinon continue l'édition
        else
            mainDaemon.setData(terminal,this.challenge.context);
    }

    @Override
    public void init(Object[] args) {
        MetaContext.win=false;
        assert args.length == 1;

        FileLabel.Init(this);

        // mettra à jour le mot de passe root à chaque perte de focus du textfield
        rootPasswordTF.focusedProperty().addListener((e, o, isFocused) -> {if(!isFocused) setRootHash();});

        initHierarchy(); // traitements spéciaux pour les fichiers par défaut

        initCommands();

        populateFromChallenge((Challenge) args[0]);

        initTerminal();
    }

    /** Initialise la hiérarchie de fichiers afin de rendre compte de comportements spéciaux pour les fichiers par défaut.
     * Les fichiers par défaut sont dits "requis" : il ne peuvent être ni renommés, ni déplacés, ni supprimés
     * (du moins depuis l'interface graphique. Un utilisateur ayant les droits Root dans le Terminal peut le faire) */
    private void initHierarchy(){
        hierarchy.setCellFactory(tree -> {
            FileTreeCell cell = new FileTreeCell();

            cell.createCallback = this::onCreateMenu;
            cell.renameCallback = this::onRenameMenu;
            cell.permissionCallback = this::onPermissionMenu;
            cell.editCallback = this::onEditMenu;
            cell.importCallback = this::onImportMenu;
            cell.deleteCallback = this::onDeleteMenu;
            cell.copyCallback = this::onCopyMenu;
            cell.cutCallback = this::onCutMenu;
            cell.pasteCallback = this::onPasteMenu;

            return cell;
        });

        // root de l'arbre, pas du file system. ne sera pas affiché
        TreeItem<FileLabel> rootItem = new TreeItem<>(new FileLabel(null, true, "", null, ""));
        rootItem.setExpanded(true);
        hierarchy.setRoot(rootItem);

        TreeItem<FileLabel> root = new TreeItem<>(new FileLabel(null, true, "/", Type.FOLDER, ""));
        rootItem.getChildren().add(root);

        TreeItem<FileLabel> commandScript = new TreeItem<>(new FileLabel(null, true, COMMAND_SCRIPT, Type.DATA, COMMAND_SCRIPT_TOOLTIP));
        TreeItem<FileLabel> docs = new TreeItem<>(new FileLabel(null, true, DOCS, Type.FOLDER, DOCS_TOOLTIP));
        TreeItem<FileLabel> endScript = new TreeItem<>(new FileLabel(null, true, END_SCRIPT, Type.DATA, END_SCRIPT_TOOLTIP));

        TreeItem<FileLabel> home = new TreeItem<>(new FileLabel(null, true, HOME, Type.FOLDER, HOME_TOOLTIP));
        TreeItem<FileLabel> launch = new TreeItem<>(new FileLabel(null, true, LAUNCH, Type.SOFTLINK, LAUNCH_TOOLTIP));
        home.getChildren().add(launch);
        home.setExpanded(true);

        TreeItem<FileLabel> initScript = new TreeItem<>(new FileLabel(null, true, INIT_SCRIPT, Type.DATA, INIT_SCRIPT_TOOLTIP));
        TreeItem<FileLabel> mainScript = new TreeItem<>(new FileLabel(null, true, MAIN_SCRIPT, Type.DATA, MAIN_SCRIPT_TOOLTIP));
        TreeItem<FileLabel> readme = new TreeItem<>(new FileLabel(null, true, README, Type.DATA, README_TOOLTIP));
        TreeItem<FileLabel> resources = new TreeItem<>(new FileLabel(null, true, RESOURCES, Type.FOLDER, RESOURCES_TOOLTIP));
        TreeItem<FileLabel> scripts = new TreeItem<>(new FileLabel(null, true, SCRIPTS, Type.FOLDER, SCRIPTS_TOOLTIP));

        // Dans un ordre qui nous est pratique, plutôt qu'alphabétique
        root.getChildren().addAll(home, docs, resources, scripts, commandScript, initScript, endScript, mainScript, readme);
        root.setExpanded(true);

        // save tout
        existingItems.addAll(Arrays.asList(root, home, launch, docs, resources, scripts, commandScript, initScript, endScript, mainScript, readme));

    }

    /** Ajoute une checkbox par commande existante. */
    private void initCommands(){
        commandsPane.getChildren().clear();

        // Pour chaque commande existante (dans l'ordre alphabétique), créer une checkbox correspondante
        MetaContext.registerC.getCommandsString().stream().sorted().forEach(cmdName -> {
            CheckBox checkbox = new CheckBox(cmdName);
            commandsPane.getChildren().add(checkbox);
            cmdCheckboxes.put(cmdName, checkbox);
        });
    }

    /** Initialise le GUI avec le challenge donné ou crée le challenge par défaut, et enfin ajoute les listeners.
     *
     * @param challenge le challenge à éditer, null si doit être créé */
    public void populateFromChallenge(Challenge challenge){

        if(challenge != null){ // édition d'un challenge existant
            this.challenge = challenge;
            playerUser = challenge.context.currentUser;
            challenge.context.getUser(SHAPER_USERNAME).ifPresentOrElse(u -> shaper = u, () -> {
                Framework.showError("Challenge corrompu", "L'Architecte / le Shaper n'existe pas.");
                framework.previous();
            });

            contextShaper = challenge.context.clone();
            contextShaper.currentUser=challenge.context.getUser(SHAPER_USERNAME).get();
            eci = new ExplicitCommandInterpreter(contextShaper);

            challengeNameTF.setText(challenge.header.name);
            descriptionArea.setText(challenge.header.description);
            difficultyTF.setText(challenge.header.difficulty);

            thumbnailView.setImage(SwingFXUtils.toFXImage(challenge.image, null));

            usernameTF.setText(playerUser.name);
            rootPasswordTF.setText(challenge.header.rootPasswordGenerator);
            createPasswordTooltip();
            masterPasswordTF.setText(challenge.header.hashedMasterPassword.isEmpty() ? "" : HASHED_MASTER_PASSWORD_REPLACEMENT);

            challenge.context.rootUser.hash = challenge.header.hashedRootPassword = DigestUtils.sha512Hex(dataPassword.getKey());
            contextShaper.setEnvar("__password", dataPassword.getKey());
            var sa = Utils.stringSplitter(dataPassword.getKey(),challenge.header.numberCutKey<=0?1:challenge.header.numberCutKey);
            int k=0;
            for (String s: sa){
                contextShaper.setEnvar(String.format("__passwordPart%d",k++),s);
            }
            cmdCheckboxes.forEach((cmd, box) -> box.setSelected(!this.challenge.context.blackListCommand.contains(cmd)));
        }
        else { // création d'un nouveau challenge
            challenge = new Challenge();
            this.challenge = challenge;

            Context c = challenge.context;
            createPasswordTooltip();
            playerUser = c.currentUser;
            playerUser.name = "";

            // challenge header : name == description == difficulty == rootPassword == hashedMasterPassword == "" par défaut

            challenge.image = SwingFXUtils.fromFXImage(thumbnailView.getImage(), null);

            // par défaut, toutes les checkboxes sont dé-sélectionnées, donc toutes les commandes sont interdites au joueur.
            cmdCheckboxes.forEach((cmd, box) -> c.blackListCommand.add(cmd));

            // crée et prend temporairement l'identité du Shaper pour initialiser le challenge
            Generex generex = new Generex("*");
            shaper = c.addNewUser(SHAPER_USERNAME,generex.random(15,20));
            c.currentUser= shaper;
            c.addUserToAdminGroup(shaper);

            contextShaper = challenge.context.clone();
            contextShaper.currentUser=challenge.context.getUser(SHAPER_USERNAME).get();

            eci = new ExplicitCommandInterpreter(c);
            // créé la hiérarchie par défaut de tous les challenges
            eci.processCommand("touch",INIT_SCRIPT,MAIN_SCRIPT,END_SCRIPT,COMMAND_SCRIPT);
            eci.processCommand("chmod","700",".");
            eci.processCommand("chmod","701",MAIN_SCRIPT);
            eci.processCommand("touch",README);
            eci.processCommand("mkdir",SCRIPTS,RESOURCES,DOCS);
            eci.processCommand("mkdir",HOME);
            eci.processCommand("chmod","777",HOME);
            eci.processCommand("cd",HOME);
            eci.processCommand("ln","-s","../"+MAIN_SCRIPT,LAUNCH);
            eci.processCommand("chmod","777",LAUNCH);

            c.currentUser = playerUser;
        }

        if(challenge.context.currentUser.name.isEmpty())
            challenge.context.currentUser.name = MetaContext.username; // mais reste vide dans le GUI

        // auto-update du lors d'un changement dans le GUI
        challengeNameTF.textProperty().addListener((e, o, name) -> this.challenge.header.name = name );
        descriptionArea.textProperty().addListener((e, o, desc) -> this.challenge.header.description = desc);
        difficultyTF.textProperty().addListener((e, o, difficulty) -> this.challenge.header.difficulty = difficulty);

        thumbnailView.imageProperty().addListener((e, o, fximg) -> this.challenge.image = SwingFXUtils.fromFXImage(fximg, null));

        // d'où la sauvegarde de playerUser, au cas où le currentUser aurait changé
        usernameTF.textProperty().addListener((e, o, name) -> playerUser.name = name);

        ChangeListener<Object> passwordGenerate = (e, o, password) -> {
            this.challenge.header.rootPasswordGenerator = (String) password;
            createPasswordTooltip();
            this.challenge.context.rootUser.hash =this.challenge.header.hashedRootPassword = DigestUtils.sha512Hex(dataPassword.getKey());
            contextShaper.listEnvar().stream().filter(s->s.indexOf("__password")==0).forEach(s->contextShaper.removeEnvar(s));
            contextShaper.setEnvar("__password", dataPassword.getKey());
            var sa = Utils.stringSplitter(dataPassword.getKey(),Math.min(this.challenge.header.numberCutKey,dataPassword.getKey().length()));
            int k=0;
            for (String s: sa){
                contextShaper.setEnvar(String.format("__passwordPart%d",k++),s);
            }
        };
        rootPasswordTF.textProperty().addListener(passwordGenerate);

        // TODO renseigner le TF : nbPartTF.setText();
        nbPartTF.textProperty().addListener((e, o, n) -> {
            try{
                this.challenge.header.numberCutKey = Integer.parseInt(n);
                passwordGenerate.changed(e,o,this.challenge.header.rootPasswordGenerator);
            } catch(NumberFormatException ex){
                nbPartTF.setText(o);
                return;
            }

            // TODO set le nb de parties du mdp
        });

        cmdCheckboxes.forEach((cmd, box) -> box.selectedProperty().addListener((e, o, allowed) -> {
            if (allowed) {
                this.challenge.context.blackListCommand.remove(cmd);
            } else {
                this.challenge.context.blackListCommand.add(cmd);
            }
        }));

        fileCrawling(hierarchy.getRoot(), challenge.context.fs.root);
    }

    /** Initialise le terminal avec le context du challenge, et l'ajoute au GUI. */
    private void initTerminal(){
        terminal = new Terminal(false);
        terminal.waitingUserCommand.addListener((e, o, isWaiting) -> {
            // mise à jour automatique de la hiérarchie lorsque la commande de l'utilisateur a fini d'être traitée (et qu'il s'apprête à en faire une autre).
            if(isWaiting)
                fileCrawling(hierarchy.getRoot(), challenge.context.fs.root);
        });
        mainDaemon.setData(terminal,this.challenge.context);
        terminal.init();
        terminalPane.setCenter(terminal);
        terminal.getBody().requestFocus(); // afin de pouvoir écrire directement
    }

    @Override
    public void dispose(){
        existingItems.clear(); // pour être sûr
    }

    /** Sélectionne toutes les checkboxes de commande.
     * Appelé lors du clic sur le bouton Toutes. */
    @FXML
    private void selectAllCommands(){
        setAllCommandsState(true);
    }

    /** Dé-sélectionne toutes les checkboxes de commande.
     * Appelé lors du clic sur le bouton Aucune. */
    @FXML
    private void unselectAllCommands(){
        setAllCommandsState(false);
    }

    /** Coche ou décoche toutes les checkboxes de commande.
     * L'état <i>cochée</i> correspond à une commande autorisée, l'état <i>décochée</i> à une commande interdite au joueur.
     *
     * @param state nouvel état de toutes les checkboxes : true pour cochée, false pour décochée */
    public void setAllCommandsState(boolean state){
        for(var checkbox : commandsPane.getChildren()){
            ((CheckBox) checkbox).setSelected(state);
        }
    }

    /** Demande à l'utilisateur de choisir une image à utiliser comme vignette du challenge. */
    @FXML
    private void askThumbnail(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une image");
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        java.io.File file = fileChooser.showOpenDialog(stage);
        if(file != null){ // un fichier a été choisi
            try{
                thumbnailView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e){
                Framework.showError("Impossible de charger la vignette", e.getMessage());
            }
        }
    }

    @FXML
    private void save(){
        if(challenge.filename.isEmpty()){
            String filename = askSave();
            if(filename == null) // cancel
                return;

            if(!validateFilename(filename))
                return; // nom invalide : on annule

            challenge.filename = filename;
        }

        String masterPassword = masterPasswordTF.getText();
        if(!masterPassword.equals(HASHED_MASTER_PASSWORD_REPLACEMENT))
            challenge.header.hashedMasterPassword = (!masterPassword.isBlank()) ? DigestUtils.sha512Hex(masterPasswordTF.getText()) : "";

        setRootHash();

        // Sauvegardes temporaire

        String name = challenge.context.currentUser.name;
        User currentUser = challenge.context.currentUser;
        challenge.context.currentUser = playerUser;

        List<String> history = challenge.context.history;
        List<String> simplehistory = challenge.context.simplehistory;

        // vide l'historique avant de sauvegarder
        challenge.context.history = new ArrayList<>();
        challenge.context.simplehistory = new ArrayList<>();
        challenge.context.sweepVar();

        challenge.header.validated = false; // le challenge devra être re-validé
        challenge.scores.clear(); // les scores ne sont plus valides

        if(canSave())
            challenge.saveOnDisk(Challenge.CHALLENGES_FOLDER + "/" + challenge.filename);

        // On remet la sauvegarde
        challenge.context.currentUser = currentUser;
        challenge.context.history = history;
        challenge.context.simplehistory = simplehistory;
        challenge.context.currentUser.name = name;
    }

    /** Demande à l'utilisateur de choisir un nom de fichier .zip sous lequel sauvegarder le challenge.
     *
     * @return le nom de fichier choisi
     *         null si l'utilisateur a annulé la saisie */
    private String askSave(){

        ChallengeFilenamePane dialog = new ChallengeFilenamePane();
        Stage stage = framework.makeDialog(dialog, "Sauvegarder");

        stage.showAndWait();

        return dialog.getFilename();
    }

    /** Vérifie si le nom de fichier est valide.
     * Affiche une erreur sinon.
     *
     * @param filename le nom de fichier
     * @return true si le nom de fichier est valide. */
    private boolean validateFilename(String filename){
        String msg = "Le nom de fichier ";

        if(filename == null || filename.isBlank())
            msg += "est vide.";
        else if(!NAME_PATTERN.matcher(filename).matches())
            msg += "contient au moins un caractère invalide.";
        else {
            java.io.File[] files = new java.io.File(Challenge.CHALLENGES_FOLDER).listFiles((f, n) -> n.equals(filename));
            if(files != null && files.length != 0)
                msg += "existe déjà.";
            else
                return true;
        }

        Framework.showError("Nom de fichier invalide", msg);

        return false;
    }

    /** Vérifie que le challenge est correct avant de le sauvegarder. */
    private boolean canSave(){
        String H = "Impossible de sauvegarder.";
        String msg;

        if(challenge.header.name.isBlank())
            msg = "Le challenge n'a pas de nom.";
        else if(challenge.header.description.isBlank())
            msg = "Le challenge n'a pas de description.";
        else if(challenge.header.difficulty.isBlank())
            msg = "Le challenge n'a pas de difficulté.";
        else if(challenge.context.currentUser.name.equals(User.ROOT_USERNAME))
            msg = "L'utilisateur ne peut pas s'appeler " + User.ROOT_USERNAME;
        else if(challenge.context.currentUser.name.equals(SHAPER_USERNAME))
            msg = "L'utilisateur ne peut pas s'appeler " + SHAPER_USERNAME;
        else if(challenge.header.rootPasswordGenerator.isBlank())
            msg = "Il n'y a pas de mot de passe Root (celui que l'utilisateur doit trouver).";
        else if(challenge.image == null)
            msg = "Le challenge n'a pas de vignette.";
        else
            return true;

        Framework.showError(H, msg);

        return false;
    }

    /** Demande à l'utilisateur de réussir son challenge afin de le valider.
     * S'il réussit, alors on quittera l'éditeur. Sinon, l'édition continuera. */
    @FXML
    private void validate(){
        save();

        framework.launchGameFromFile(challenge.filename);
    }

    /** Remplace le hash du mot de passe Root dans le context. */
    private void setRootHash(){
        challenge.header.hashedRootPassword = DigestUtils.sha512Hex(dataPassword.getKey());
        challenge.context.rootUser.hash = challenge.header.hashedRootPassword;
    }

    private void createPasswordTooltip(){
        dataPassword = challenge.header.generatePassword();
        rootPasswordTF.setTooltip(new Tooltip(String.format("%s  (%d)", dataPassword.getKey(), dataPassword.getValue())));
    }

    /** Parcourt le {@link FileSystem} et met à jour l'arbre de hiérarchie des fichiers.
     * Fonction récursive.
     * Chaque cellule est marquée "visited" lorsqu'on la rencontre. Elle est crée si elle n'existe pas.
     * Si <code>file</code> est un dossier alors :
     *   appel récursif sur chacun de ses fichiers (sauf . et ..)
     *   une fois cela fait, supprime toutes les cellules enfant de cette cellule qui n'ont pas été visitées (car n'existent plus dans le FileSystem)
     *   reset l'état "visited" à false chez les cellules enfant restantes
     *
     * @param parentCell la cellule de l'arbre qui serait parente de la cellule du fichier d'origine
     * @param file le fichier d'origine, duquel part le crawler */
    private void fileCrawling(TreeItem<FileLabel> parentCell, File<?> file){
        clearCutbuffer();

        Optional<TreeItem<FileLabel>> found = parentCell.getChildren().stream().filter(item -> item.getValue().is(file)).findFirst();
        TreeItem<FileLabel> thisCell;

        if(found.isPresent()){
            thisCell = found.get();
            thisCell.getValue().file = file; // surtout pour initialiser les fichiers requis avec ceux du FileSystem
        }
        else {
            thisCell = new TreeItem<>(new FileLabel(file));
            parentCell.getChildren().add(thisCell);
            existingItems.add(thisCell);
        }

        thisCell.getValue().visited = true;

        if(file.getType() == Type.FOLDER){
            File<Folder> folder = File.ConvertFile(file, Folder.class);

            for(File<?> child : folder.getInodeData().getExistantFiles(true, shaper)){
                if(!child.isInternFile()){ // vérifier "." ou ".." est suffisant car se sont les seuls "dossiers" (en fait hardlinks) qui ramènent en arrière ou qui bouclent.
                    fileCrawling(thisCell, child);
                }
            }

            List<TreeItem<FileLabel>> toRemove = thisCell.getChildren().stream().filter(item -> !item.getValue().visited).collect(Collectors.toList());

            // avant car possède des weak references
            existingItems.removeAll(toRemove);

            // supprime de l'arbre tous les éléments qui ne sont plus dans le file system
            thisCell.getChildren().removeAll(toRemove);
            // reset
            thisCell.getChildren().forEach(item -> item.getValue().visited = false);
        }
    }

    @FXML
    private void selectUsers(){
        Framework.showError("Not Implemented", "TODO");
    }

    @FXML
    private void selectGroups(){
        Framework.showError("Not Implemented", "TODO");
    }

    // return le path du dossier actuel ou parent si c'était pas un dossier, se terminant par '/'
    private String transformFilePath(FileLabel f){
        String path = f.file.getPath();
        if(f.type != Type.FOLDER) // non dossier : donne le chemin du dossier parent
            path = path.substring(0, path.lastIndexOf("/")+1);
        else if(!path.equals("/")) // dossier non root : ajoute '/'
            path += "/";
        return path;
    }

    /** affichage du menu de création de fichier */
    private void onCreateMenu(FileLabel cell) {
        CreateFileOptions options = new CreateFileOptions();

        options.setFolder(transformFilePath(cell), false);

        options.setAllFolders(existingItems.stream().map(TreeItem::getValue).filter(f -> f.type == Type.FOLDER).map(this::transformFilePath).sorted().collect(Collectors.toList()));

        // TODO plutot renvoyer eci et verifier par pseudopath si le fichier existe déjà ou non
        // sans les . et ..
        options.setAllPaths(existingItems.stream().map(TreeItem::getValue).map(c -> c.file.getPath()).collect(Collectors.toList()));
        // édition : ne pas blacklist son propre nom

        options.setName("", true); // creation : pas de nom
        // edition : options.setName(cell.name, true);

        options.setType(Type.DATA, false);

        // édition : options.setType(cell.type, true);

        User user = shaper;
        options.setUser(user.name, false);
        options.setAllUsers(challenge.context.getUsers().stream().map(u -> u.name).collect(Collectors.toList()));

        options.setGroup(user.mainGroup.name, false);
        options.setAllGroups(challenge.context.getGroups().stream().map(g -> g.name).collect(Collectors.toList()));

        CreateFileGUI dialog;
        try {
            dialog = framework.makeGUIdialog(CreateFileGUI.FXML, "Créer un fichier", true, StageStyle.DECORATED, new Object[]{options});
        } catch (IOException e) {
            e.printStackTrace(); // FXML non trouvé
            return;
        }
        dialog.stage.showAndWait();

        if(!dialog.toProcess) // annulation
            return;

        if(dialog.resultData.equals(dialog.defaultData)) // aucun changement
            return;

        String fullpath = dialog.resultData.folder + dialog.resultData.name;
        if(dialog.nameIsInvalid.get()){
            fullpath = dialog.resultData.folder + " INVALID FILENAME " + dialog.resultData.name; // TODO pour l'instant
        }

        switch(dialog.resultData.type){
            case DATA:
                eci.processCommand("touch", fullpath);
                break;
            case FOLDER:
                eci.processCommand("mkdir",fullpath);
                break;
            case SOFTLINK:
                eci.processCommand("ln","-s", cell.file.getPath(), fullpath);
                break;
            case BINARY:
                eci.processCommand("touch", "--binary", fullpath);
                break;
        }

        PseudoPath p = new PseudoPath(challenge.context.currentPath);
        p.setPseudoPath(fullpath);
        if(!p.isFileExist()){
            Framework.showError("Le fichier n'a pas été créé.", "Avez-vous activé les commandes ?");
            return;
        }

        File<?> file = p.getChildFile();
        if(file.getType() == Type.DATA)
            File.ConvertFile(file, Data.class).getInodeData().setData(dialog.resultData.content);
        if(file.getType() == Type.BINARY)
            File.ConvertFile(file, BinaryData.class).getInodeData().setData(dialog.resultData.binaryContent);

        eci.processCommand("chmod",dialog.resultData.permission, fullpath);
        // alternative //file.getInodeData().setPermission(Integer.valueOf(dialog.resultData.permission).shortValue());

        Optional<User> owner = challenge.context.getUser(dialog.resultData.user);
        assert owner.isPresent();
        file.getInodeData().setOwner(owner.get());

        Optional<Group> group = challenge.context.getGroup(dialog.resultData.group);
        assert group.isPresent();
        file.getInodeData().setGroup(group.get());

        fileCrawling(hierarchy.getRoot(), challenge.context.fs.root); // mise à jour de la hiérarchie
        if(cell.type == Type.FOLDER) // s'assure que le dossier soit déployé
            existingItems.stream().filter(item -> item.getValue() == cell).findFirst().ifPresent(item -> item.setExpanded(true));
    }

    private void onRenameMenu(FileLabel cell){
        TextInputDialog alert = new TextInputDialog();

        alert.setTitle("Renommer");
        alert.setHeaderText("Renommer "+cell.name+" en :");
        alert.setContentText("");

        alert.showAndWait().ifPresent(newName -> {
            String n = newName.replace(" ", "_");
            if(!(!n.isEmpty() && (n.equals(".") || n.equals("..") || n.equals("~") || n.contains("/") || n.contains("\\")
                    || !MetaContext.VALID_FILE_NAME.matcher(n).matches()))){
                PseudoPath p = new PseudoPath(challenge.context.currentPath);
                String s = cell.file.getPath();
                s = s.substring(0, s.lastIndexOf("/")+1) + n;
                p.setPseudoPath(s);
                if(!p.isFileExist()){
                    eci.processCommand("mv",cell.file.getPath(), s);
                    fileCrawling(hierarchy.getRoot(), challenge.context.fs.root);
                    return;
                }
            }

            Framework.showError("Le renommage a échoué.", "Nom invalide");
        });
    }

    private void onPermissionMenu(FileLabel cell){
        var inode = cell.file.getInode();
        PermissionPane permPane = new PermissionPane(inode.getOwner().name, inode.getGroup().name, String.valueOf(inode.getPermission()), challenge.context);
        Stage stage = framework.makeDialog(permPane, "Permissions");
        stage.showAndWait();

        if(!permPane.isOK)
            return; // annulé

        if(CreateFileGUI.LOOSE_FOCUS_PERMISSION_PATTERN.matcher(permPane.perm).matches()){
            inode.setPermission(Integer.valueOf(permPane.perm).shortValue());
        }
        else {
            Framework.showError("Permissions invalides", "Il faut un nombre en octal : de 000 à 777");
        }

        challenge.context.getUser(permPane.user).ifPresent(inode::setOwner);
        challenge.context.getGroup(permPane.group).ifPresent(inode::setGroup);
    }

    private void onEditMenu(FileLabel cell){
        var inode = File.ConvertFile(cell.file, Data.class).getInodeData();

        TextArea area = new TextArea();
        area.setText(inode.getData(challenge.context.rootUser));
        area.setMinSize(400, 600);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Édition");
        alert.setGraphic(null);
        alert.getDialogPane().setContent(area);

        alert.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                inode.setData(area.getText());
            }
        });
    }

    private void onImportMenu(FileLabel cell){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier");
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        java.io.File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            try{
                byte[] data = Files.readAllBytes(file.toPath());
                File.ConvertFile(cell.file, BinaryData.class).getInodeData().setData(data);
            } catch(IOException e){
                Framework.showError("Importation impossible", "Impossible d'ouvrir le fichier.");
            }
        }
    }

    private void onDeleteMenu(FileLabel cell){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Suppression");
        alert.setHeaderText("Souhaitez-vous VRAIMENT supprimer ce fichier ?");
        alert.setContentText("Cette opération est IRRÉVERSIBLE !");

        alert.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                if(cell.type == Type.FOLDER)
                    eci.processCommand("rmdir","-r",cell.file.getPath());
                else
                    eci.processCommand("rm",cell.file.getPath());

                fileCrawling(hierarchy.getRoot(), challenge.context.fs.root);
            }
        });
    }

    /* gestion de la copie/coupe/colle des fichiers. Le cutbuffer sauvegarde quel fichier est visé */

    private void clearCutbuffer(){
        if(cutbuffer == null)
            return;

        cutbuffer.setEffect(null);

        cutbuffer = null;
    }

    private void onCopyMenu(FileLabel cell){
        cutbuffer = cell;
        cutbufferCut = false;
        cell.setEffect(new GaussianBlur(3.0));
    }

    private void onCutMenu(FileLabel cell){
        cutbuffer = cell;
        cutbufferCut = true;
        cell.setEffect(new GaussianBlur(3.0));
    }

    private void onPasteMenu(FileLabel cell){
        if(cutbuffer == null)
            return;

        String src = cutbuffer.file.getPath();
        String dest = transformFilePath(cell) + cutbuffer.name;

        if(src.equals(dest)){
            clearCutbuffer();
            return;
        }

        PseudoPath p = new PseudoPath(challenge.context.currentPath);
        p.setPseudoPath(dest);
        if(p.isFileExist()){
            if(existingItems.stream().filter(item -> item.getValue().required).anyMatch(item -> item.getValue().file == p.getChildFile())) {
                Framework.showError("Impossible de coller ici", "Un fichier du même nom et nécessaire y existe déjà.");
                clearCutbuffer();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Coller");
            alert.setHeaderText("Un fichier du même nom existe déjà à cet emplacement.");
            alert.setContentText("Continuer et écraser ce fichier ?");

            var button = alert.showAndWait();
            if(button.isEmpty() || button.get() != ButtonType.OK) {
                clearCutbuffer();
                return;
            }

            if(p.getChildFile().getType() == Type.FOLDER)
                eci.processCommand("rmdir","-r",dest);
            else
                eci.processCommand("rm",cell.file.getPath());
        }

        if(cutbufferCut)
            eci.processCommand("mv",src, dest);
        else
            eci.processCommand("cp",src, dest);

        clearCutbuffer();

        fileCrawling(hierarchy.getRoot(), challenge.context.fs.root);
    }

    /** Affiche l'aide de l'application.
     * Appelé lors de l'appui sur le bouton Information . */
    @FXML
    private void selectInfo(){
        framework.showInfo(HTML_HELP);
    }

    /** Affiche les options.
     * Appelé lors de l'appui sur le bouton Option . */
    @FXML
    private void selectOption() throws IOException {
        framework.showOption();
    }

    /** Revient au menu précédent.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectReturn(){
        framework.previous();
    }
}

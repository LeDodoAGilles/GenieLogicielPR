package ShellNightmare.Terminal.TerminalFX.nano;

// https://linuxcommand.org/lc3_man_pages/nano1.html

// todo ? titlecolor, statuscolor, keycolor, functioncolor, numbercolor, and/or selectedcolor

public class NanoConfig {
    // option: +LINE,COLUMN      avec LINE et COLUMN des entiers positifs
    // ATTENTION : LINE et COLUMN commencent à 1 (1ere ligne, 1ere colonne)
    public int line = 1; // ligne du curseur à l'ouverture
    public int column = 1; // colonne du curseur à l'ouverture

    public boolean smarthome = false; // −A, −−smarthome : déplacement en début de ligne (après les whitespaces)
    public boolean backup = false; // −B, −−backup : avant de sauvegarder, crée une copie nommée avec ~ // TODO
    public boolean tabtospaces = false; // −E, −−tabstospaces : remplace les \t tapés par des espaces
    public boolean nonewlines = false; // −L, −−nonewlines : ne pas rajouter automatiquement de '\n' en fin de fichier
    //public boolean restricted = false; // −R, −−restricted // ~ TODO
    public int tabsize = 8; // −T number, −−tabsize=number : >0, TODO juste utilisé avec tabtospaces pour l'instant
    //public String syntax = ""; // −Y name, −−syntax=name // TODO
    //public boolean constantshow= false; // −c, −−constantshow // TODO
    public boolean emptyline= false; // −e, −−emptyline : ligne vide sous le header
    public boolean autoindent= false; // −i, −−autoindent : indentation automatique lors de la création d'une nouvelle ligne (indentation basée sur la précédente ligne)
    public boolean linenumbers = false; // −l, −−linenumbers : afficher les numéros de lignes (le wrap ne compte pas comme de nouvelles lignes)
    public boolean preserve = false; // −p, −−preserve // TODO
    public boolean saveonexit = false; // −t, −−saveonexit : sauvegarde automatique lorsqu'un fichier est fermé
    public boolean view = false; // −v, −−view : non éditable, lecture seule
    public boolean nowrap = false; // −w, −−nowrap : pas de wrap des longues lignes
    public boolean nohelp = false; // −x, −−nohelp : ne pas afficher l'aide des raccourcis en bas de nano
    //public boolean afterends = false; // −y, −−afterends // TODO
}

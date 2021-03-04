package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

/**
 * gestion des erreurs dans la construiction et le lancement de l'arbre sémantique
 * @author Gaëtan Lounes
 */
public class ASTException extends RuntimeException{
    public ASTException(String msg){
        super(msg);
    }
}

package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;

import java.util.List;

/**
 * objet représentatn une liste de token
 * @author Gaëtan Lounes
 */

public class GroupToken extends Token {
    List<Token> subToken;

    public GroupToken(SemanticWord type, List<Token> subToken) {
        super(type, null);
        this.subToken = subToken;
    }

    @Override
    public String toString() {
        return subToken.toString();
    }
}

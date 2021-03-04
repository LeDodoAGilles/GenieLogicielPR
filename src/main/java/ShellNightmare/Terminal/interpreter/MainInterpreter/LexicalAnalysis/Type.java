package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;

/**
 * énumération représentant une partie des objets interprétables
 * @author Gaëtan Lounes
 */
public enum Type implements SemanticWord {
    ATOM,RBRACKET,LBRACKET,RBRACES,LBRACES,FUNCTION,
    SQLBRACKET, SQRBRACKET,

    k_$,k_equal, nextInstruction,ATOMS, caseEndClause
}

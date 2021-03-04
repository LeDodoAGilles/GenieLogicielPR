package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

import java.io.Serializable;
/**
 * enumération des patterns utilisables par l'ast
 * @author Gaëtan Lounes
 */
public enum Pattern implements Serializable {
    IF, CASE,SUBCOMMAND,SEQCOMMAND,SILENTSEQCOMMAND, COMMAND, PLAINTEXT, COMPRESSEDPLAINTEXT, BINARYOPERATOR, FOR, WHILE, VARASSIGNEMENT,
    VARGET, NOTHING,
    FUNCTION,
}

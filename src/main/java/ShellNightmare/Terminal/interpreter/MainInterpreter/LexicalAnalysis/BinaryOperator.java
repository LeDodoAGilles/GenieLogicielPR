package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;

/**
 * énumération représentant les opérateurs binaires
 * @author Gaëtan Lounes
 */

public enum BinaryOperator implements SemanticWord{
    and("&&",true),or("||",true),
    k_pipe("|",true),
    k_rediIn("<",false),
    k_rediErr("2>",false),k_rediOutEnd(">>",false),k_rediOut(">",false);

    private final String representation;
    private final Boolean command;
    BinaryOperator(String data,Boolean command){
        representation = data;
        this.command = command;
    }

    public String getRepresentation() {
        return representation;
    }

    public Boolean getCommand() {
        return command;
    }
}

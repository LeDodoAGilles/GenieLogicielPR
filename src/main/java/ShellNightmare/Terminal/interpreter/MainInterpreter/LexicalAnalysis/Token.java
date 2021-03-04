package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * objet représentant les briques élémentaires de l'interpréteur
 * @author Gaëtan Lounes
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 4442452786L;

    public SemanticWord type;
    public String data;

    public Token(SemanticWord type, String data){
        this.type = type;
        this.data = data;
    }
    public static Token Token(SemanticWord type){
        return new Token(type,null);
    }
    public static Token Token(SemanticWord type, String data){
        return new Token(type,data);
    }



    public void addTokenToInterpreterWord(List<String> al){
        al.add(data);
    }

    @Override
    public String toString() {
        return data==null?type.toString():data;
    }
}


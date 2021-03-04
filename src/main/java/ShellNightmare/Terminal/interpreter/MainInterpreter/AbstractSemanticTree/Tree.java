package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tree implements Serializable {
    private static final long serialVersionUID = 77741587186L;

    public List<Tree> child;
    public Pattern type;
    public Token t;

    public Tree(Pattern type){
        this.type = type;
        child = new ArrayList<>();
    }

    public Tree(Token t){
        this.t = t;
    }

    public void addToParent(Tree t){
        if (type == Pattern.COMMAND && child.size() == 0)
            return;
        if (t.child!=null)
            t.child.add(this);

        if (t.type==Pattern.SILENTSEQCOMMAND && type == Pattern.SEQCOMMAND) //une commande silencieuse
            type = Pattern.SILENTSEQCOMMAND;
    }


}

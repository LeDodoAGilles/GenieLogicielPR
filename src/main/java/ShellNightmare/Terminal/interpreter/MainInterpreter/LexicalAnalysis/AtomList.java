package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;

import java.util.ArrayList;
import java.util.List;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Type.ATOMS;

/**
 * objet représentant une liste d'atomes
 * @author Gaëtan Lounes
 */
public class AtomList extends Token{
    public List<String> arrayData;

    public AtomList(List<String> as) {
        super(ATOMS, null);
        arrayData = as;
    }

    @Override
    public void addTokenToInterpreterWord(List<String> al){
        al.addAll(arrayData);
    }
}

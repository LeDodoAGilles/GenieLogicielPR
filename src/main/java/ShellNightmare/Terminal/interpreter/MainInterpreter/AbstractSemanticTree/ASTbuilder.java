package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.*;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Type.*;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Type.FUNCTION;

/**
 * construction de l'arbre sémantique abstrait
 * @author Gaëtan Lounes
 */

public class ASTbuilder {

    public ASTbuilder(){
    }

    public void build(Tree currentBranch,List<Token> array) throws Exception {
        build(currentBranch,array,COMMAND);
    }
    public void build(Tree currentBranch,List<Token> array,Pattern p) throws Exception{
        Tree currentCommand = new Tree(p);

        boolean newWord = true;
        boolean binaryMode = false;
        int k=-1;
        while(++k< array.size()){
            Token t = array.get(k);

            if (t.type != ATOM && t.type != k_$)
                newWord=true;

            if (t.type instanceof KeyWord){
                switch ((KeyWord)t.type){
                    case IF:
                    {
                        int[] value = getIfStatement(array,k);

                        if (value[0]==-1 || value[2]== -1)
                            throw new ASTException("invalid if branchment");

                        Tree tr = new Tree(IF);
                        ArrayList<Token> subArray = new ArrayList<>();
                        for (int l=0; l<3; l++){
                            if (value[l]==-1) //pas de else
                                continue;

                            for (int j = k+1; j<value[l];j++){
                                subArray.add(array.get(j));
                            }
                            k=value[l]; //on recommence a la suite
                            if (l>0){
                                Tree t2 = new Tree(SEQCOMMAND);
                                build(t2,subArray);
                                t2.addToParent(tr);
                            }
                            else
                                build(tr,subArray); //dans la condition du if que une commande

                            subArray.clear();
                        }

                        currentCommand = tr;
                        k=value[2];
                        break;
                    }
                    case THEN:
                    case ELSE:
                    case FI:
                        throw new ASTException("invalid if loop");

                    case FOR: {
                        int[] value = getForStatement(array, k);

                        if (value[0] == -1 || value[1] == -1 || value[2] == -1)
                            throw new ASTException("invalid for loop");

                        Tree tr = new Tree(FOR);
                        ArrayList<Token> subArray = new ArrayList<>();
                        for (int l = 0; l < 3; l++) {

                            for (int j = k + 1; j < value[l]; j++)
                                subArray.add(array.get(j));

                            k = value[l]; //on recommence a la suite
                            if (l == 0)
                                build(tr, subArray, PLAINTEXT);
                            else if (l==1){
                                if (subArray.stream().noneMatch(e -> e.type == LBRACKET || e.type == RBRACKET))
                                    build(tr, subArray, PLAINTEXT);
                                else{
                                    Tree t2 = new Tree(SILENTSEQCOMMAND); //forcément synchrone, on veut la voir.
                                    build(t2, subArray);//sequence de commandes
                                    t2.addToParent(tr);
                                }
                            }
                             else {
                                Tree t2 = new Tree(SEQCOMMAND);
                                build(t2, subArray);//sequence de commandes
                                t2.addToParent(tr);
                            }

                            subArray.clear();
                        }

                        tr.addToParent(currentBranch);
                        currentCommand = new Tree(COMMAND);
                        k = value[2];
                        break;
                    }

                    case WHILE:{
                        int[] value = getWhileStatement(array, k);
                        if (value[0] == -1 || value[1] == -1)
                            throw new ASTException("invalid while loop");

                        Tree tr = new Tree(WHILE);
                        ArrayList<Token> subArray = new ArrayList<>();
                        for (int l = 0; l < 2; l++) {
                            for (int j = k + 1; j < value[l]; j++)
                                subArray.add(array.get(j));

                            k = value[l]; //on recommence a la suite
                            if (l == 0) {
                                build(tr, subArray);
                            } else {
                                Tree t2 = new Tree(SEQCOMMAND);
                                build(t2, subArray);//sequence de commandes
                                t2.addToParent(tr);
                            }

                            subArray.clear();
                        }

                        tr.addToParent(currentBranch);
                        currentCommand = new Tree(COMMAND);
                        k = value[1];
                        break;

                    }

                    case CASE: {
                        var value = getCaseStatement(array, k);
                        if (value[0] == -1 || value[1] == -1)
                            throw new ASTException("invalid case statement");
                        Tree tr = new Tree(CASE);
                        ArrayList<Token> subArray = new ArrayList<>();

                        for (int j = k + 1; j < value[0]; j++)
                            subArray.add(array.get(j));

                        build(tr, subArray, PLAINTEXT);
                        subArray.clear();


                        for (int j = value[0]+1; j < value[1]; j++){
                            Token t2 = array.get(j);
                            if (t2.type == caseEndClause){
                                var f = IntStream.range(0,subArray.size()).filter(e -> subArray.get(e).type == RBRACKET).findFirst();
                                if (f.isEmpty())
                                    throw new ASTException("Invalid Case Clause");
                                int firstValue = f.getAsInt();

                                ArrayList<Token> subArray1 = new ArrayList<>();
                                for (int l = 0; l < firstValue; l++)
                                    subArray1.add(subArray.get(l));
                                build(tr, subArray1, PLAINTEXT);

                                ArrayList<Token> subArray2 = new ArrayList<>();
                                for (int l = firstValue+1; l < subArray.size(); l++)
                                    subArray2.add(subArray.get(l));
                                Tree sub = new Tree(SEQCOMMAND);
                                build(sub, subArray2);//sequence de commandes
                                sub.addToParent(tr);

                                subArray.clear();
                            }
                            else{
                                if (subArray.size()==0 && t2.type == nextInstruction)
                                    continue;
                                subArray.add(t2);
                            }


                        }
                        tr.addToParent(currentBranch);
                        currentCommand = new Tree(COMMAND);
                        k=value[1];
                        break;
                    }
                }
            }
            else if (t.type instanceof BinaryOperator){
                if (binaryMode) {
                    Tree t2 = currentBranch.child.remove(currentBranch.child.size() - 1);
                    currentCommand.addToParent(t2);
                    currentCommand = t2;
                }

                binaryMode = true;
                Tree bo = new Tree(BINARYOPERATOR);
                bo.t = t;
                if (currentCommand.child.isEmpty()) { //permet de lancer des commandes du type: >testet les sous commandes
                    if (!currentBranch.child.isEmpty()){
                        Tree t2 = currentBranch.child.get(currentBranch.child.size() - 1);
                        if (t2.type==SUBCOMMAND){
                            currentBranch.child.remove(t2);
                            currentCommand = t2;
                        }
                    }

                    currentCommand.type= (currentCommand.type==COMMAND)? NOTHING : currentCommand.type;
                }
                currentCommand.addToParent(bo);
                bo.addToParent(currentBranch);
                currentCommand = new Tree(((BinaryOperator)t.type).getCommand()?COMMAND:PLAINTEXT); //on garde une commande à droite que pour les pipes
            }
            else
                switch ((Type)t.type){
                    case LBRACKET:{ //sous commande
                        int v = getMatchedBracket(k,array);
                        if (v==-1)
                            throw new ASTException("invalid bracket");
                        Tree tr = new Tree(SUBCOMMAND);


                        ArrayList<Token> subArray = new ArrayList<>();
                        for (int x= k+1; x<v+1; x++)
                            subArray.add(array.get(x));


                        if (!currentCommand.child.isEmpty()) //c'est une commande qui la précède

                            tr.addToParent(currentCommand);
                        else
                            tr.addToParent(currentBranch);
                        Tree ssc = new Tree(SILENTSEQCOMMAND);
                        ssc.addToParent(tr);
                        build(ssc,subArray);
                        k=v+1;
                        break;
                    }

                    case SQLBRACKET:{
                        int v = getMatchedSBracket(k,array);
                        if (v==-1)
                            throw new ASTException("invalid sqbracket");
                        array.set(k,new Token(ATOM,"test"));
                        array.remove(v+1);
                        k--;
                        break;
                    }

                    case SQRBRACKET: throw new ASTException("invalid sqbracket");
                    case RBRACKET: throw new ASTException("invalid bracket");

                    case FUNCTION:{
                        if (k<1)
                            throw new ASTException("pas de nom de fonction!");
                        Token node = array.get(k-1);
                        int beginFunction = -1, endFunction = -1;
                        var data =getBracesIndex(array,k);
                        beginFunction=data[0];
                        endFunction=data[1];
                        if (beginFunction == -1 || endFunction == -1 || endFunction<beginFunction)
                            throw new ASTException("invalid fonction");

                        Tree trFunction = new Tree(Pattern.FUNCTION);
                        (new Tree(node)).addToParent(trFunction);
                        ArrayList<Token> subArray = new ArrayList<>();

                        for (int x= beginFunction+1; x<endFunction; x++)
                            subArray.add(array.get(x));

                        Tree t2 = new Tree(SEQCOMMAND);
                        build(t2, subArray);//sequence de commandes
                        t2.addToParent(trFunction);
                        trFunction.addToParent(currentBranch);
                        currentCommand = new Tree(COMMAND);
                        k = endFunction+1;
                        break;
                    }


                    case ATOM:{
                        if (array.size()>k+1 && array.get(k+1).type == FUNCTION)
                            continue;
                        newWord = t.data.charAt(t.data.length() - 1) == '\t' || t.data.charAt(0) == '\t';

                        var e = new ArrayList<>(List.of(t.data.split("\t")));
                        e = e.stream().filter(f -> !f.equals("")).collect(Collectors.toCollection(ArrayList::new));

                        Tree command;
                        if (e.size()>1) {
                            String last = e.remove(e.size()-1);
                            command = new Tree(new AtomList(e));
                            command.addToParent(currentCommand);
                            (new Tree(new Token(ATOM,last))).addToParent(currentCommand);
                        }
                        else
                        if (!e.isEmpty()){
                            command = new Tree(new Token(ATOM, e.get(0)));

                            if (!newWord && !currentCommand.child.isEmpty()) {
                                Tree t2 = currentCommand.child.get(currentCommand.child.size() - 1);
                                if (t2.type == COMPRESSEDPLAINTEXT) {
                                    command.addToParent(t2);
                                } else {
                                    Tree plainText = new Tree(COMPRESSEDPLAINTEXT);

                                    currentCommand.child.remove(currentCommand.child.size() - 1);
                                    t2.addToParent(plainText);

                                    command.addToParent(plainText);
                                    plainText.addToParent(currentCommand);

                                }
                            }
                            else
                                command.addToParent(currentCommand);
                        }
                        break;
                    }
                    case nextInstruction:{
                        if (binaryMode){
                            binaryMode=false;
                            Tree binaryOp = currentBranch.child.get(currentBranch.child.size()-1);

                            if (currentCommand.child.isEmpty())
                                throw new ASTException("Invalid argument");
                            currentCommand.addToParent(binaryOp);
                        }
                        else
                            currentCommand.addToParent(currentBranch);
                        currentCommand = new Tree(COMMAND);
                        break;
                    }

                    case k_$:{
                        Tree e = new Tree(VARGET);
                        if (!newWord){
                            Tree t2 = (currentCommand.child.isEmpty())?null:currentCommand.child.get(currentCommand.child.size()-1);
                            if (t2 !=null && t2.type==COMPRESSEDPLAINTEXT){
                                e.addToParent(t2);
                            }
                            else {
                                Tree plainText = new Tree(COMPRESSEDPLAINTEXT);
                                if (!currentCommand.child.isEmpty()){
                                    currentCommand.child.remove(currentCommand.child.size()-1);
                                    assert t2 != null;
                                    t2.addToParent(plainText);
                                }
                                e.addToParent(plainText);
                                plainText.addToParent(currentCommand);

                            }
                        }
                        else
                            e.addToParent(currentCommand);

                        if (k==array.size())
                            throw new ASTException("invalid use of $");


                        if (t.type == ATOM)
                            throw new ASTException("invalid use of $");

                        if (t.data.equals("")){ // de la forme ${e}
                            var index=getBracesIndex(array,k);
                            if (index[0]!=k+1||index[1]!=k+3)
                                throw new ASTException("invalid brace use");
                            t=array.get(k+2);
                            k=k+3;
                        }
                        Tree child = new Tree(t);
                        child.addToParent(e);
                        newWord=false;
                        break;
                    }


                    case k_equal:{
                        int v = getNextInstructionIndex(k,array);
                        if (v==k)
                            throw new ASTException("missing assignement after =");

                        Tree e = new Tree(VARASSIGNEMENT);
                        Tree varname = currentCommand.child.remove(currentCommand.child.size()-1);
                        varname.addToParent(e);
                        e.addToParent(currentBranch);

                        ArrayList<Token> subArray = new ArrayList<>();
                        for (int x= k+1; x<=v; x++)
                            subArray.add(array.get(x));
                        build(e,subArray,PLAINTEXT);
                        k=v;
                        break;
                    }


                    default:
                        throw new ASTException("invalid expression");
                }
        }
        currentCommand.addToParent(currentBranch);
    }

    private int getMatchedSBracket(int k, List<Token> array){
        int bracketCount=0;
        for (int i = k; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof Type))
                continue;
            switch ((Type)t.type){
                case SQRBRACKET: bracketCount--; break;
                case SQLBRACKET: bracketCount++; break;
                default: break;
            }
            if (bracketCount==0)
                return i-1;
        }
        return -1;
    }

    private int getMatchedBracket(int k, List<Token> array){
        int bracketCount = 1;
        for (int i = k+1; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof Type))
                continue;
            switch ((Type)t.type){
                case RBRACKET: bracketCount--; break;
                case LBRACKET: bracketCount++; break;
                default: break;
            }
            if (bracketCount==0)
                return i-1;
        }
        return -1;
    }

    private int getNextInstructionIndex(int k, List<Token> array){
        for (int i = k+1; i<array.size(); i++){
            Token t = array.get(i);

            if (t.type == LBRACKET) // on ne regarde pas les nextInstruction des sous commandes.
                i=getMatchedBracket(i,array);

            if (t.type == nextInstruction)
                return i;

        }
        return array.size()-1;
    }

    private int[] getIfStatement(List<Token> array, int index){
        int ifCount = 1;
        int fiIndex = -1;
        int thenIndex = -1;
        int elseIndex = -1;
        for (int i = index+1; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof KeyWord))
                continue;
            switch ((KeyWord)t.type){
                case THEN: thenIndex=(thenIndex==-1)?i:thenIndex; break;
                case ELSE: elseIndex = (ifCount == 1)?i: elseIndex; break;
                case IF: ifCount++; break;
                case FI: ifCount--; break;
                default: break;
            }
            if (ifCount==0) {
                fiIndex = i;
                break;
            }
        }
        return new int[]{thenIndex,elseIndex,fiIndex};
    }


    private int[] getWhileStatement(List<Token> array, int index){
        int WhileCount = 1;
        int doneIndex = -1;
        int doIndex = -1;
        for (int i = index+1; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof KeyWord))
                continue;
            switch ((KeyWord)t.type){
                case DO: doIndex = (doIndex == -1)?i:doIndex; break;
                case DONE: WhileCount--; break;
                case FOR: WhileCount++; break;
                default: break;
            }
            if (WhileCount==0) {
                doneIndex = i;
                break;
            }
        }
        return new int[]{doIndex,doneIndex};
    }


    private int[] getForStatement(List<Token> array, int index){
        int ForCount = 1;
        int doneIndex = -1;
        int doIndex = -1;
        int inIndex = -1;
        for (int i = index+1; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof KeyWord))
                continue;
            switch ((KeyWord)t.type){
                case IN: inIndex=(inIndex==-1)?i:inIndex; break;
                case DO: doIndex = (doIndex == -1)?i:doIndex; break;
                case DONE: ForCount--; break;
                case FOR: ForCount++; break;
                default: break;
            }
            if (ForCount==0) {
                doneIndex = i;
                break;
            }
        }
        return new int[]{inIndex,doIndex,doneIndex};
    }

    private int[] getCaseStatement(List<Token> array, int index){
        int CaseCount = 1;
        int esacIndex = -1;
        int inIndex = -1;
        for (int i = index+1; i<array.size(); i++){
            Token t = array.get(i);
            if (!(t.type instanceof KeyWord))
                continue;
            switch ((KeyWord)t.type){
                case IN: inIndex=(inIndex==-1)?i:inIndex; break;
                case ESAC: CaseCount--; break;
                case CASE: CaseCount++; break;
                default: break;
            }
            if (CaseCount==0) {
                esacIndex = i;
                break;
            }
        }
        return new int[]{inIndex,esacIndex};
    }

    private int[] getBracesIndex(List<Token> array,int k){
        int beginVar = -1, endVar = -1;
        int bracesCount=0;
        for (int i = k+1; i<array.size();i++){
            Token t2 = array.get(i);
            if (t2.type instanceof Type)
                switch ((Type)t2.type){
                    case LBRACES:
                        bracesCount--;
                        break;
                    case RBRACES:
                            beginVar=(beginVar==-1)?i:beginVar;
                            bracesCount++;

                        break;
                }
            if (bracesCount==0){
                endVar=i;
                break;
            }
        }
        return new int[]{beginVar,endVar};
    }


}

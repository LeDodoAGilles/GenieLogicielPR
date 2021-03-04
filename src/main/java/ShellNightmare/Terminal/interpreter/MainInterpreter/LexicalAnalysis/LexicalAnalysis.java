package ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis;


import java.util.ArrayList;
import java.util.List;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Token.*;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Type.FUNCTION;
/**
 * analyseur syntaxique de l'interpréteur
 * @author Gaëtan Lounes
 */

public class LexicalAnalysis {
    public StringBuilder script;
    public List<Token> tokened,tokened2,tokened3;

    public LexicalAnalysis(StringBuilder script){
        this.script = script;
        tokened = new ArrayList<>();
        tokened2 = new ArrayList<>();
        tokened3 = new ArrayList<>();
    }

    int commandIndex = 0;
    int commandSize = 0;

    private void endToken(){
        if (commandSize==0)
            return;
        tokened.add(Token(Type.ATOM,script.substring(commandIndex,commandIndex+commandSize)));
        commandIndex = commandSize = 0;
    }

    private void AtomUp(int i){
        commandIndex = commandSize==0? i : commandIndex;
        commandSize++;
    }

    private KeyWord getAKeyWord(int i){
        for (KeyWord kw : KeyWord.values()){
            String word = kw.toString();
            word = word.toLowerCase();
            int size = word.length();
            if (script.length()>i+size-1 && script.substring(i,i+size).equals(word)){
                if (script.length()==i+size)
                    return kw;
                char c =  script.charAt(i+size);
                if (c == ';' || c == ' '|| c == '\n')
                    return kw;
            }

        }
        return null;
    }

    public Boolean Structuralanalyze(){
        boolean escaped =false, quote = false, comment = false;
        char quoteType = ' ';
        int i=-1;
        while(++i<script.length()){
            char c = script.charAt(i);
            if (escaped){
                escaped=false;
                AtomUp(i);
                continue;
            }
            if (quote && c!=quoteType){
                AtomUp(i);
                continue;
            }

            if (!quote) //detection des mots cles
                if (i==0 || (i>0 && !(script.charAt(i-1)<'z' && script.charAt(i-1)>'a'))){
                var keyWord = getAKeyWord(i);
                if (keyWord!=null){
                    endToken(); tokened.add(Token(keyWord));
                    i+=keyWord.toString().length()-1;
                    continue;
                }
            }

            if (comment && c!='\n')
                continue;

            switch (c){
                case '#':
                    if (i>0 && script.charAt(i-1)=='$')
                        AtomUp(i);
                    else{
                        endToken();comment=true;} break;
                case '\\': escaped =true; AtomUp(i); break;
                case '(':endToken();
                if (script.length()>i && script.charAt(i+1)==')'){
                    tokened.add(Token(FUNCTION));
                    i++;
                }
                else
                    tokened.add(Token(Type.LBRACKET));break;
                case ')':endToken();tokened.add(Token(Type.RBRACKET));break;
                case '{':endToken();tokened.add(Token(Type.RBRACES));break;
                case '}':endToken();tokened.add(Token(Type.LBRACES));break;
                case '[': endToken();tokened.add(Token(Type.SQLBRACKET));break;
                case ']': endToken();tokened.add(Token(Type.SQRBRACKET));break;
                case '\"':
                case '\'':
                    if (!quote){
                        quote = true;
                        quoteType=c;
                    }
                    else
                        quote = false;
                    AtomUp(i);
                    break;

                case '\n':
                    comment=false;
                case';' :{
                    endToken();
                    if (i+1<script.length() && script.charAt(i+1)==';') { // caractère de fin de clause case.
                        tokened.add(Token(Type.caseEndClause));
                        i=i+1;
                    }
                    else
                        tokened.add(Token(Type.nextInstruction));
                    break;
                }

                default :{
                    AtomUp(i);
                }
            }
        }
        endToken();
        tokened.add(Token(Type.nextInstruction));
        return tokened.parallelStream().filter(e -> e.type == Type.LBRACKET).count() == tokened.parallelStream().filter(e ->e.type == Type.RBRACKET).count();
    }




    int subCommandIndex = 0;
    int subCommandSize = 0;
    String command;
    char quoteMode = ' ';
    int subQuoteSection = -1;
    boolean trim =true;
    ArrayList<Token> sousToken = new ArrayList<>();
    private void endsubToken(){
        if (subCommandSize==0 || subCommandIndex+subCommandSize>command.length())
            return;
        String subsection = command.substring(subCommandIndex,subCommandIndex+subCommandSize);

        sousToken.add(Token(Type.ATOM,subsection));
        subCommandIndex = subCommandSize = 0;
    }

    private void endsubTokenNotBlank(){
        if (subCommandSize==0 || subCommandIndex+subCommandSize>command.length())
            return;
        String subsection = command.substring(subCommandIndex,subCommandIndex+subCommandSize);

        if (!subsection.equals("\t"))
            sousToken.add(Token(Type.ATOM,subsection));
        subCommandIndex = subCommandSize = 0;
    }



    private BinaryOperator getBinaryOperator(int i){
        for (BinaryOperator bo : BinaryOperator.values()){
            String word = bo.getRepresentation();
            word = word.toLowerCase();
            int size = word.length();
            if (command.length()>i+size-1 && command.substring(i,i+size).equals(word)){
                return bo;
            }

        }
        return null;
    }

    private void SubStructuralanalyze(Token t){
        sousToken.clear();
        command = t.data;
        boolean containBlank=false;
        int firstBlankIndex = -1;
        int $index = -1;
        int $size = 0;
        boolean escaped=false,processWord=true;



        int i=-1;
        while(++i<command.length()+1){
            char c;
            if (i==command.length()){ //cas où l'allocation de la variable est en fin de chaines de caractères
                if (escaped){
                    subCommandSize++;
                    continue;
                }


                if ($index==-1)
                    continue;
                c=' ';
            }
            else
                c = command.charAt(i);


            if (escaped) {
                c = '0'; //pas un caractère spécial...
                escaped=false;
            }


            if ($index>-1){
                if (c == ' ' || c == '$' || c=='\"' || c == '\'' || c == '\\'){ // fin de la déclaration d'une variable avec un espace, un " ou une autre variable

                    sousToken.get(sousToken.size()-1).data = command.substring($index+1,$index+1+$size);
                    subCommandSize = 0;
                    $index=-1;
                    $size=0;
                }
                else
                    $size++;
            }

            if (c!=' ' && firstBlankIndex>-1) {
                command = command.substring(0,firstBlankIndex)+'\t'+command.substring(i);
                i = firstBlankIndex + 1;
                firstBlankIndex = -1;
                subCommandSize++; // le \t
            }

            if (c=='\\' && i+1<command.length() && !escaped
                    && command.charAt(i+1)!='x'
                    && command.charAt(i+1)!='e'
                    && command.charAt(i+1)!='t'
                    && command.charAt(i+1)!='n'
                    && command.charAt(i+1)!='0') //ne pas echappé x e 0 pour la détection des codes couleurs

            {
                escaped=true;
                command = command.substring(0,i)+command.substring(i+1);
                i--;
                continue;
            }

            if (c=='\"' || c == '\''){
                if (trim) {
                    quoteMode = c;
                    trim = false;
                    subQuoteSection = i;
                    command = command.substring(0,i)+command.substring(i+1);
                    subCommandSize-=1;
                    i--;
                }
                else
                if (c == quoteMode) {
                    command = command.substring(0,i)+command.substring(i+1);
                    subCommandSize-=1;
                    trim = true;
                    i--;
                    processWord=false;
                }

            }




            if (c=='=' && containBlank) {
                c = '0';
            }


            if (c == ' '){
                containBlank=true;
                if (trim) {
                    if (firstBlankIndex == -1)
                        firstBlankIndex = i;
                    subCommandIndex = subCommandSize==0? i : subCommandIndex;
                    continue; // evite de decaler l'index de subCommand ...
                }
            }





            if (!trim)
                c='$'==c?c:'0'; //pas de traitement des caractéres spéciaux lorsque l'on fait une quote sauf le $
            else
            {
                if (processWord) {
                    var binWord = getBinaryOperator(i);
                    if (binWord != null) {
                        endsubTokenNotBlank();
                        sousToken.add(Token(binWord));
                        i += binWord.getRepresentation().length() - 1;
                        continue;
                    }
                }
                else
                    processWord=true;

            }

            switch (c){
                case '$':endsubToken();sousToken.add(Token(Type.k_$)); $index=i; break;
                case '=':endsubToken(); sousToken.add(Token(Type.k_equal)); break;

                default:{
                    subCommandIndex = subCommandSize==0? i : subCommandIndex;
                    subCommandSize++;
                }
            }
        }

        endsubToken();
        tokened2.add(new GroupToken(t.type,new ArrayList<>(sousToken)));
    }


    public void subStructureAnalysis(){
        for (Token t : tokened){
            if (t.type==Type.ATOM){
                SubStructuralanalyze(t);
            }
            else
                tokened2.add(t);
        }

    }


    public void flatten(){
        for (int k=0; k<tokened2.size();k++){ //TODO: trim les nextInstructions
            Token t =  tokened2.get(k);
            if (t.type == Type.ATOM){
                GroupToken a = (GroupToken) t;
                tokened3.addAll(a.subToken);
            }
            else
                tokened3.add(t);
        }

    }


}

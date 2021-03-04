package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.interpreter.ExplicitCommandInterpreter;
import ShellNightmare.Terminal.interpreter.E_InterpreterStatus;
import ShellNightmare.Terminal.interpreter.InterpreterStack;
import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.BinaryOperator;
import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Token;
import utils.Clonage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ShellNightmare.Terminal.CommandHandler.Command.getErrorMessage;
import static ShellNightmare.Terminal.DaemonMessage.*;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.NOT_EXIST;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.OK;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.*;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.BinaryOperator.*;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.Type.*;

/**
 * lancement de l'arbre sémantique abstrait
 * @author Gaëtan Lounes
 */

public class ASTLaunch {
    private Tree ast;
    private Context c;
    private boolean silent, compressed,addEndFile;
    private static final String pipeName = ".unknamedPipe";

    public ASTLaunch(Tree ast, Context c){
        this.ast = ast;
        this.c = c;
        silent = MetaContext.mainDaemon ==null;
        compressed = addEndFile = false;
    }

    protected boolean addErrorMessages(IOStack io){
        if (io.status==OK)
            return false;
        if (stderr==null)
            stderr = new ArrayList<>();
        stderr.add(getErrorMessage(io));
        return true;
    }


    List<String> stdout,stderr;

    public void setStderr(List<String> stderr) {
        this.stderr = stderr;
    }

    public void setStdout(List<String> stdout) {
        this.stdout = stdout;
    }

    public List<String> getStdout(){
        return stdout;
    }

    public String compressStdout(){
        if (stdout == null)
            return "";
        return String.join("\n",stdout);
    }

    public String compressStderr(){
        return String.join("\n",stderr);
    }

    public List<String> getStderr(){
        return stderr;
    }


    private void addPartialStdOut(List<String> out){
        if (!silent) {
            stdout.clear();
            stdout.addAll(out);
            if (stdout.isEmpty())
                return;
            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(STDOUT, compressStdout()));
            stdout.clear();
        }
        else
            stdout.addAll(out);
    }

    private void addPartialStdErr(List<String> err){
        if (!silent) {
            stderr.clear();
            stderr.addAll(err);
            if (stderr.isEmpty())
                return;
            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(STDERR, compressStderr()));
            stderr.clear();
        }
        else
            stderr.addAll(err);
    }
    public void run(boolean forceSilent) throws  Exception{
        silent=forceSilent||silent;
        run(null);
    }

    public void run() throws Exception{
        run(null);
    }
    public void run(Set<String> aliasBlacklist) throws Exception {
        List<Tree> at = ast.child;
        switch (ast.type){
            case CASE:{
                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                int size = at.size();
                if (size==0)
                    return;
                Tree condition = at.get(0);
                ASTLaunch astCondition = new ASTLaunch(condition,c);
                astCondition.run();
                String cond = astCondition.compressStdout();
                for (int k=0;k<size/2;k++){
                    Tree match = at.get(2*k+1);
                    ASTLaunch astMatch = new ASTLaunch(match,c);
                    astMatch.run();
                    String matched = astMatch.compressStdout();
                    if (FnMatch.fnmatch(matched,cond))
                    {
                        Tree selected = at.get(2*k+2);
                        ASTLaunch astSelected = new ASTLaunch(selected,c);
                        astSelected.run();

                        stdout.addAll(astSelected.stdout);
                        stderr.addAll(astSelected.stderr);
                        break;
                    }

                }
                break;
            }

            case FUNCTION:{
                if (at.size()!=2)
                    throw new ASTException("invalid function def");
                Tree name = at.get(0);
                Tree instructions = at.get(1);
                c.setFunction(name.t.data.replace("\t",""), instructions);

                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                break;
            }


            case IF: {
                int size = at.size();
                Tree condition = at.get(0);
                ASTLaunch astCondition = new ASTLaunch(condition,c);
                astCondition.run();
                if (!astCondition.getStderr().isEmpty())
                {
                    stdout = new ArrayList<>();
                    stderr = astCondition.getStderr();
                    return;
                }
                ASTLaunch astIf = null;
                if (c.getEnvar("?").equals("0"))
                    astIf = new ASTLaunch(at.get(1),c);
                else
                if (size == 3)
                    astIf = new ASTLaunch(at.get(2),c);


                if (astIf == null){
                    stdout = new ArrayList<>();
                    stderr = new ArrayList<>();
                }
                else {
                    astIf.run();
                    stdout = astIf.getStdout();
                    stderr = astIf.getStderr();
                }
            }
                break;


            case FOR:{
                ASTLaunch astVariable = new ASTLaunch(at.get(0),c);
                astVariable.run();
                String s = astVariable.getStdout().get(0);


                ASTLaunch iterateur = new ASTLaunch(at.get(1),c);
                iterateur.run();

                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                for (String st : iterateur.getStdout()){
                    Tree clonage = at.get(2);
                    ASTLaunch loop = new ASTLaunch(clonage,c);
                    c.setEnvar(s,st);
                    loop.run();
                    addPartialStdOut(loop.stdout);
                    addPartialStdErr(loop.stderr);
                }
                break;}

            case WHILE: {


                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                boolean loopC=true;
                while (loopC){
                    ASTLaunch astCondition = new ASTLaunch(at.get(0),c);
                    astCondition.run();
                    loopC = c.getEnvar("?").equals("0");
                    if (!loopC)
                        continue;
                    Tree clonage = at.get(1);
                    ASTLaunch loop = new ASTLaunch(clonage,c);
                    loop.run();
                    addPartialStdOut(loop.stdout);
                    addPartialStdErr(loop.stderr);
                }
                break;
            }




            case NOTHING:
                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                break;

            case SILENTSEQCOMMAND:
                silent = true;
            case SEQCOMMAND:
                stdout = new ArrayList<>();
                stderr = new ArrayList<>();
                for (Tree a : at){
                    ASTLaunch ast = new ASTLaunch(a,c);
                    ast.run(silent);
                    addPartialStdOut(ast.getStdout());
                    addPartialStdErr(ast.getStderr());
                }
                break;

            case SUBCOMMAND:
                Tree commandLaunch = at.get(0);
                ASTLaunch ast1 = new ASTLaunch(commandLaunch,c);
                ast1.run();
                this.stdout=ast1.getStdout();
                this.stderr=ast1.getStderr();
                break;

            case COMMAND:
                ArrayList<String> commandPart = new ArrayList<>();

                stdout = new ArrayList<>();
                stderr = new ArrayList<>();

                for (Tree a : at){
                    if (a.type!=null) {
                        ASTLaunch ast = new ASTLaunch(a, c);
                        ast.run();
                        if (a.type==SUBCOMMAND) {
                            a.t = (a.t==null)?new Token(ATOM, ""):a.t;
                            a.t.data=ast.compressStdout();
                        }
                    }

                    a.t.addTokenToInterpreterWord(commandPart);
                }

                String commandNam = commandPart.get(0);

                commandPart=commandPart.stream().map(s->s.replace("~",c.getEnvar("HOME"))).collect(Collectors.toCollection(ArrayList::new));

                if (c.getFunction(commandNam)!=null){
                    Tree e = Clonage.cloneObject(c.getFunction(commandNam));//à retirer a terme
                    for (int k=1; k<commandPart.size();k++){
                        c.setEnvar(String.format("%d",k),commandPart.get(k));
                    }
                    c.setEnvar("#",String.format("%d",commandPart.size()));
                    ASTLaunch ast = new ASTLaunch(e,c);
                    ast.run(silent);

                    for (int k=1; k<commandPart.size();k++){
                        c.setEnvar(String.format("%d",k),"");
                    }
                    c.setEnvar("#","");

                    stdout = ast.stdout;
                    stderr = ast.stderr;
                    return;
            }


                if (c.alias.containsKey(commandNam) && (aliasBlacklist==null || !aliasBlacklist.contains(commandNam))) {
                    Tree t = Clonage.cloneObject(c.alias.get(commandNam));
                    ASTLaunch ast = new ASTLaunch(t, c);
                    Tree lastChild = t.child.remove(t.child.size() - 1);

                    Set<String> blackListAlias = (aliasBlacklist == null) ? new HashSet<>() : aliasBlacklist;
                    blackListAlias.add(commandNam);
                    ast.run(blackListAlias);
                    stdout.addAll(ast.getStdout());
                    stderr.addAll(ast.getStderr());

                    commandPart.remove(0);

                    ArrayList<String> als = new ArrayList<>();
                    for (Tree a : lastChild.child) {
                        if (a.type != null) {
                            ASTLaunch ast4 = new ASTLaunch(a, c);
                            ast4.run();
                        }
                        a.t.addTokenToInterpreterWord(als);
                    }
                    commandPart.addAll(0, als);
                }




                ExplicitCommandInterpreter bi = new ExplicitCommandInterpreter(c);
                InterpreterStack is = bi.processCommand(commandPart);

                if (bi.stdout != null)
                    stdout.addAll(bi.stdout);
                if (bi.stderr !=null)
                    stderr.addAll(bi.stderr);

                if (is.status!= E_InterpreterStatus.OK){
                    throw new ASTException(is.message!=null?is.message:String.join("\n",stderr));
                }


                break;


            case VARGET:
                Tree g = ast.child.get(0);
                ast.t = Token.Token(ATOM,c.getEnvar(g.t.data));
                break;

            case VARASSIGNEMENT:
            {
                Tree variable = at.get(0);
                String variableName = variable.t.data;


                Tree value = at.get(1);
                String variableValue;
                ASTLaunch ast = new ASTLaunch(value,c);
                ast.run();
                variableValue = String.join("",ast.stdout);
                stderr = ast.getStderr();


                c.setEnvar(variableName,variableValue);

                stdout = new ArrayList<>();
                break;
            }

            case COMPRESSEDPLAINTEXT:
                compressed=true;
            case PLAINTEXT:
            {
                stderr = new ArrayList<>();
                stdout = new ArrayList<>();
                for (Tree t : at){
                    if (t.type == VARGET || t.type == COMPRESSEDPLAINTEXT ||(t.t.type != ATOM && t.t.type != ATOMS)) {
                        ASTLaunch ast = new ASTLaunch(t, c);
                        ast.run();

                        if (t.type == COMPRESSEDPLAINTEXT){
                            t.t=new Token(ATOM,ast.compressStdout());
                        }
                    }
                        if (t.t.data == null)
                            t.t.addTokenToInterpreterWord(stdout);
                        else
                            stdout.add(t.t.data);
                    }
                if (compressed){
                    String output = String.join("",stdout);
                    stdout.clear();
                    stdout.add(output);
                    ast.t = new Token(ATOM,output);
                }

                break;

            }

            case BINARYOPERATOR:
                Token t = ast.t;
                BinaryOperator bo = (BinaryOperator)t.type;
                if (ast.child.size()<2)
                    throw new ASTException("pas assez d'arguments pour :"+bo.getRepresentation());
                switch(bo){
                    case k_rediOutEnd:
                        addEndFile=true;
                    case k_rediOut: {
                        stderr = new ArrayList<>();
                        stdout = new ArrayList<>();
                        ASTLaunch ast2 = new ASTLaunch(ast.child.get(0), c);
                        ast2.run();
                        PseudoPath p = new PseudoPath(c.currentPath);
                        ASTLaunch ast3 = new ASTLaunch(ast.child.get(1), c);
                        ast3.run();
                        String path = ast3.compressStdout();
                        p.setPseudoPath(path);
                        File<Folder> currentFolder = p.getFolder();
                        if (currentFolder==null){
                            stderr.add("le dossier n'existe pas");
                            break;
                        }
                        String name = p.newFile;
                        File<Data> output;
                        if (p.isFileExist()){
                            File<?> f = p.getChildFile();
                            if (f.getType()==Type.DATA)
                                output = File.ConvertFile(f,Data.class);
                            else
                                break;
                        }
                        else{
                            if (addEndFile)
                            {
                                addErrorMessages(IOStack.interpreterStack(NOT_EXIST,name));
                                break;
                            }
                            output = new File<>(name, Data.class, c.currentUser);
                        }



                        if (!addEndFile && currentFolder.getInodeData().isFileNameInDirectory(name))
                            if (addErrorMessages(currentFolder.getInodeData().getFileByName(name).removeFile(c.fs, c.currentUser)))
                                    break;


                        if (!addEndFile && addErrorMessages(output.addToFolder(currentFolder, c.fs, c.currentUser)))
                        {
                            break;
                        }

                        String data = ast2.compressStdout();
                        output.getInodeData().setData(addEndFile?output.getInodeData().getData(c.currentUser)+data:data);
                        ast2.stdout.clear();
                        stderr = ast2.stderr;
                        stdout = new ArrayList<>();
                        break;
                    }

                    case k_rediIn:
                        ASTLaunch ast5 = new ASTLaunch(ast.child.get(1), c);
                        ast5.run();
                        String path = ast5.compressStdout();
                        PseudoPath fileInput = new PseudoPath(c.currentPath);
                        fileInput.setPseudoPath(path);
                        File<Folder> currentFolder = fileInput.getFolder();
                        if (currentFolder==null){
                            stderr.add("le dossier n'existe pas");
                            break;
                        }

                        String name = fileInput.newFile;
                        if (fileInput.getFolder().getInodeData().isFileNameInDirectory(name))
                            fileInput.setPseudoPath(path);
                        else
                            throw new ASTException("ce fichier n'existe pas");

                        String fileInputPath = fileInput.getChildFile().getPath();
                        Tree commandTree = ast.child.get(0);
                        Tree e = new Tree(Pattern.PLAINTEXT);
                        e.t = new Token(ATOM,fileInputPath);
                        e.addToParent(commandTree);
                        ASTLaunch ast6 = new ASTLaunch(commandTree, c);
                        ast6.run();

                        stderr = ast6.stderr;
                        stdout =  ast6.stdout;

                        break;

                    case k_pipe:
                        Tree commandIn = ast.child.get(0);
                        Tree commandOut = ast.child.get(1);
                        stderr = new ArrayList<>();
                        stdout = new ArrayList<>();

                        Tree part1 = new Tree(Pattern.BINARYOPERATOR);
                        part1.t = new Token(k_rediOut,null);

                        Tree part1b = new Tree(Pattern.PLAINTEXT);
                        Tree part1b2 = new Tree(new Token(ATOM,pipeName));
                        part1b2.addToParent(part1b);
                        commandIn.addToParent(part1);
                        part1b.addToParent(part1);

                        ASTLaunch ast7 = new ASTLaunch(part1, c);
                        ast7.run();
                        stderr.addAll(ast7.stderr);
                        if (!stderr.isEmpty())
                            break;

                        Tree part2 = new Tree(Pattern.BINARYOPERATOR);
                        part2.t = new Token(k_rediIn,null);

                        Tree part2b = new Tree(Pattern.PLAINTEXT);
                        Tree part2b2 = new Tree(new Token(ATOM,pipeName));
                        part2b2.addToParent(part2b);
                        commandOut.addToParent(part2);
                        part2b.addToParent(part2);

                        ASTLaunch ast8 = new ASTLaunch(part2, c);
                        ast8.run();

                        PseudoPath p4 = new PseudoPath(c.currentPath);
                        File<Folder> currentFolder4 = p4.getChildFolder();
                        currentFolder4.getInodeData().getFileByName(pipeName).removeFile(c.fs,c.currentUser);

                        stderr.addAll(ast8.stderr);
                        stdout.addAll(ast8.stdout);
                        break;


                    case and:
                        Tree conditionTree = ast.child.get(0);
                        Tree sucess = ast.child.get(1);

                        ASTLaunch astConditionCommand = new ASTLaunch(conditionTree, c);
                        astConditionCommand.run();
                        stdout=astConditionCommand.stdout;
                        stderr=astConditionCommand.stderr;

                        if (c.getEnvar("?").equals("0")) {
                            ASTLaunch astTrueCommand = new ASTLaunch(sucess, c);
                            astTrueCommand.run();
                            stderr.addAll(astTrueCommand.stderr);
                            stdout.addAll(astTrueCommand.stdout);
                        }

                        break;

                    case or:
                        Tree conditionTreeOR = ast.child.get(0);
                        Tree fail = ast.child.get(1);

                        ASTLaunch astConditionORCommand = new ASTLaunch(conditionTreeOR, c);
                        astConditionORCommand.run();
                        stdout=astConditionORCommand.stdout;
                        stderr=astConditionORCommand.stderr;

                        if (!c.getEnvar("?").equals("0")) {
                            ASTLaunch astFalseCommand = new ASTLaunch(fail, c);
                            astFalseCommand.run();
                            stderr.addAll(astFalseCommand.stderr);
                            stdout.addAll(astFalseCommand.stdout);
                        }
                        break;

                    default:

                }

        }


    }


}

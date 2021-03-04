

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.interpreter.Tester;
import org.junit.BeforeClass;

import static ShellNightmare.Terminal.Daemon.colluminize;
public class Tests {
    Context c = new Context();
    Tester t = new Tester(c);


    @BeforeClass
    public static void initMetaContext(){
        MetaContext.Init();
        MetaContext.mainDaemon = null;
    }

    @org.junit.Test
    public void testNoParameterCommand() throws Exception {
        t.noError("history");
        t.noError("pwd");
        t.noError("help");
        t.noError("whoami");
    }
    @org.junit.Test
    public void testRedirection() throws Exception {
        t.noError("mkdir e; cd e");
        t.noError("history | wc -w > ../ln");
        t.expectedResult("cd ..; ls|clean","e\nln");
    }

    @org.junit.Test
    public void testEasyCommand() throws Exception {
        t.noError("cd");
        t.noError("ls -la");
        t.noError("mkdir e");
        t.errorExpected("mkdir e");
        t.noError("cd e");
        t.noError("cd ..");
        t.noError("rmdir e");
        t.errorExpected("cd e");
        t.noError("cd ..");
        t.noError("touch k");
        t.errorExpected("cd k");
        t.noError("rm k");
        t.errorExpected("cd -k");

        //vérifier que echo passe bien sans paramètres
        t.noError("echo");

        //vérifier qu'on ne peut pas déplacer le dossier courant ni supprimer un dossier sans l'option -r
        t.noError("mkdir e");
        t.noError("cd e");
        t.errorExpected("mv e ..");
        t.noError("cd ..");
        t.errorExpected("rm e");
        t.noError("rmdir e");

        //se déplacer dans soi-même
        t.noError("mkdir e");
        t.errorExpected("mv e e");
        t.noError("rmdir e");

        //déplacer plusieurs dossiers
        t.noError("mkdir e");
        t.noError("mkdir f");
        t.noError("mkdir g");
        t.errorExpected("mv e f g");
        t.noError("rmdir g");

        //concatenation
        t.expectedResult("echo \"a b c\" > fd; cat fd","a b c");

        //find
        t.noError("find");
    }
    @org.junit.Test
    public void testEasyCommand2(){
        t.noError("touch a b c d e");
        t.noError("cp a f");
        t.noError("mkdir folder");
        t.noError("mkdir g");
        t.noError("cp -r g folder");
        t.noError("cp b c folder");
        t.noError("cp a e");
        t.errorExpected("rmdir .");

        t.noError("mkdir truc; mkdir cible; cd truc; ");
        t.noError("touch a b c; mv a c; mv b ../cible");
        t.noError("mkdir f; touch f/t;");
        t.errorExpected("mv f ../cible");
        t.noError("mv -r f ../cible");

        //permission
        t.noError("touch h");
        t.noError("chmod 100 h");
        t.errorExpected("wc h");
        t.errorExpected("chmod");
        t.errorExpected("chmod h h");
        t.errorExpected("rm *");
        t.noError("chmod 777 h");
        t.errorExpected("rmdir h");
        t.noError("rm h");
    }

    @org.junit.Test
    public void testInterpreter() throws Exception {
        t.expectedResult("if true then if false then echo cheval; else echo 'ah la  la'; fi; fi;","ah la  la");
        t.expectedResult("echo \" e\""," e");
        t.expectedResult("e=abc;echo $e","abc");
        t.expectedResult("e='machin';echo $e","machin");
        t.expectedResult("k=(help | wc -l |cut -d \"\\t\" -f 0;); echo $k;",String.format("%d",MetaContext.registerC.getCommands().size()));

        t.expectedResult("for k in 1 2 3 4 5 do echo $k; done;","1\n2\n3\n4\n5");

        //subroutine

        t.expectedResult("e=(echo canard);echo $e;","canard");
        t.expectedResult("echo \" Pigeon Marrant   VROUM \" | wc | wc","25\t1\t169\t1\t/.unknamedPipe");

        t.expectedResult("false && echo a","");
        t.expectedResult("true || echo a", "");
        t.expectedResult("true && echo a","a");
        t.expectedResult("false || echo a", "a");
        t.errorExpected("if end");
        t.noError("echo \" 'citation' \" # test de commentaire ");
        t.noError(">e #test de commentaire");
        t.noError("for k in 1 2 3 do for i in 1 2 do echo $k$i; done; done;");
        t.expectedResult("(echo a)|wc -l","1\t/.unknamedPipe");
        t.expectedResult("echo (echo (echo e))","e");
        t.expectedResult("echo e > f; echo f >> f; cat f","ef");
        t.expectedResult("e=a;f=${e}b;g=$e$f; echo $g","aab");
        t.expectedResult("for k in (seq 0 2) do echo a;done","a\na\na");
        t.expectedResult("while false do echo a done;","");
        t.expectedResult("help | wc -l | cut -d \\t -f 0",String.format("%d",MetaContext.registerC.getCommands().size()));
        t.expectedResult("(echo \"a b c\"; echo d e)| sed \"s/a/b/g;\"","b b c");
    }



    @org.junit.Test
    public void testCase() throws Exception {

        t.expectedResult("e=5; case $e in \n" +
                "a) echo e ;;" +
                "*) echo f ;; esac;","f");
    }

    @org.junit.Test
    public void testFunction() throws Exception {
        t.expectedResult("fk(){echo \"rip\";}; fk;","rip");
        t.expectedResult("a(){echo a;}; b(){echo b;}; a;b","a\nb");
    }

    @org.junit.Test
    public void testTestShortWritting() throws Exception {
        t.expectedResult("if [ -n $randomvar ] then echo a; else echo b; fi;","a");
        t.expectedResult("if [ -f e ] then echo t else echo f; fi","f");
        t.expectedResult("touch e; if [ -f e ] then echo t else echo f; fi","t");
        t.expectedResult("if [ -f e/hgf/hgfh/hgf ] then echo t else echo f; fi","f");
    }

    @org.junit.Test
    public void testTest() throws Exception {
        t.expectedResult("test -z a && echo t || echo f ","t");
        t.expectedResult("test -n $randomVar && echo t || echo f ","t");
        t.expectedResult("test e = f && echo t || echo f ","f");
        t.expectedResult("test e != f && echo t || echo f ","t");
        t.errorExpected("test e -q f");
        t.errorExpected("test -q u");
    }

    @org.junit.Test
    public void testUnvar()  {
        t.expectedResult("a=5; unset a ; test -z a && echo t || echo f","t");
    }

    @org.junit.Test
    public void testAlias() throws Exception {
        t.noError("alias t='echo e';");
        t.expectedResult("t","e");
    }

    @org.junit.Test
    public void testBash() throws Exception {
        t.noError("echo \"echo a b c\" > k");
        t.expectedResult("bash k","a\nb\nc");
    }

    @org.junit.Test
    public void testAlignementColonnes() throws Exception {
        String e= "a\nbf\nc\ncgrtgr\nfrewgtrc\nc\nfereyhd\ne\nf5353wtr\nabc\nfe\ngeloltrh\ndeprego\npgkpkg\npg\nfeteryer";
        String f =colluminize(e);
        System.out.println(f);
    }




    @org.junit.Test
    public void testAutreMv() throws Exception {
        t.noError("mkdir cible s; cd s;touch a b c; mv b ../cible");
    }




    @org.junit.Test
    public void testMvPerms() throws Exception {
        t.noError("mkdir e; cd e; chmod 0 ..; mkdir f; cd f; cd ..");
    }

}
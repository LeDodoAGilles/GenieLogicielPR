package ShellNightmare.Terminal.FileSystem;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.*;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;


/**
 * classe représentant un chemin d'accès à un fichier
 * @author Gaëtan Lounes
 */
public class Path implements Serializable {
    private static final long serialVersionUID = 11135841515L;

    private String path = "";
    public User u;
    public FileSystem fs;

    public Path(Path p2){
        this(p2.fs,p2.u);
        this.path = p2.path;
    }

    public Path(FileSystem fs,User u){
        path ="/";
        this.fs = fs;
        this.u = u;
    }

    public IOStack setPath(String fs){
        if (fs.length()>0){
            List<IOStack> a = setFilePath(fs,u);
            if (a.isEmpty())
                return interpreterStack(OK);
            return a.get(0);
        }
        return interpreterStack(EMPTY);
    }

    /**
     * retourne le fichier associé au chemin absolu fourni
     * @return File<?></>
     */
    public File<?> getFile(){
        String[] folder = path.split("/");
        folder= Arrays.stream(folder).filter(s -> !s.isEmpty()).toArray(String[]::new);
        File<Folder> f2 = fs.root;
        Folder r = f2.getInodeData();
        for (String fold : folder){
                f2 = r.getFileByName(fold,Folder.class);
                if (f2.isAFolder())
                    r = f2.getInodeData();
                else
                    return f2;

            }
        return f2;
    }

    /**
     * retourne le dossier associé au chemin
     * ex: /test/voiture.txt --> null
     *     /test/voitures/canard --> canard
     * @return File<Folder></>
     */
    public File<Folder> getFolder(){
        File<?> f2 = getFile();
        if (f2 == null) // le dossier n'existe pas i.e le chemin est incorrect
            return null;
        if (f2.getType()==Type.FOLDER)
            return File.ConvertFile(f2,Folder.class);
        return null;

    }

    public Set<String> showRelatives(String relativePath, Boolean hiddenFiles,boolean absolutePath, List<IOStack> errorCatched){
        HashSet<String> paths = new HashSet<>();
        File<Folder> f2;
        String pathr = relativePath;
        int i=0;
        if (relativePath.charAt(0)=='/'){ //simplification des chemins absolue avec ceux que l'on as
            if (relativePath.indexOf(path)==0)
                if (path.length()<relativePath.length() && relativePath.charAt(path.length())=='/')
                    pathr=relativePath.substring(path.length()); //simplification du chemin
            if (pathr.equals(relativePath)){
                f2 = fs.root;
                if (pathr.equals("/"))
                    i=0;
                else
                    i=pathr.split("/").length-1;
            }
            else
                f2 = getFolder();
        }
        else
            f2 = getFolder(); // chemin relatif

        if (f2==null)
            return null;

        String[] folder = pathr.split("/");
        ArrayList<String> e = Arrays.stream(folder).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));



        showRelativesSubRoutine(e, i, f2,paths,hiddenFiles,errorCatched);

        if (!absolutePath)
            paths = paths.stream().map(s-> {
                if (s.length()>path.length()&& s.contains(path))
                    return s.substring(path.length()==1?1:path.length()+1); // path
                else
                    return s;
            }).collect(Collectors.toCollection(HashSet::new));
        return paths;
    }

    private void showRelativesSubRoutine(ArrayList<String> filter,int i, File<Folder>  f, HashSet<String> output, boolean HiddenFiles, List<IOStack> errorCatched){
            if (filter.isEmpty()){
                    output.add(f.path);
                    return;
            }

            String currentFilter = filter.get(0);
            filter.remove(0);
            if (i<=0)
            if (!f.getInode().getPermission(u).contains(Permission.EXECUTION)){//faut l'éxecution pour accéder aux informations dans l'inode du fichier
                errorCatched.add(interpreterStack(PERMISSION,"execution"));
                return;
            }
            if (f.getType()!=Type.FOLDER){
                errorCatched.add(interpreterStack(NOT_A_FOLDER,f.name));
                return;
            }

            boolean FindMatch = false;
            for (String fileName : f.getInodeData().getExistantFilesNames(HiddenFiles,i>0?new User(0):u)){
                if (currentFilter.equals(fileName) || FnMatch.fnmatch(currentFilter,fileName)){
                    FindMatch = true;
                    if (!currentFilter.equals(fileName) && !f.getInode().getPermission(u).contains(Permission.READ)){ // pas de regex sans la permission de lecture
                        if (i<=0) {
                            errorCatched.add(interpreterStack(PERMISSION, "lecture"));
                            continue;
                        }
                }

                    File<Folder> f2 = f.getInodeData().getFileByName(fileName,Folder.class);
                    showRelativesSubRoutine(filter,--i,f2,output,HiddenFiles,errorCatched);
                }
            }
            if (!FindMatch)
                errorCatched.add(interpreterStack(NOT_EXIST,currentFilter));

    }

    public List<IOStack> setFilePath(String RelativePath, User u){
        this.u=u;
        ArrayList<IOStack> errorCatched = new ArrayList<>();
        Set<String> relative = showRelatives(RelativePath,true,true, errorCatched);
        if (relative.isEmpty())
            return errorCatched;
        this.path = relative.iterator().next();
        return errorCatched;
    }

    public String getFileName(){
        return path.substring(path.lastIndexOf('/'));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path1 = (Path) o;

        return path.equals(path1.path);
    }

    public boolean equals(String path){
        return path.equals(this.path);
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getPath() {
        return path;
    }
}

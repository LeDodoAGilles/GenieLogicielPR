package Interface;

import ShellNightmare.Terminal.FileSystem.Type;
import ShellNightmare.Terminal.TerminalFX.DodoStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CreateFileOptions implements Cloneable {
    public String folder = "";
    public boolean editableFolder = true;
    public List<String> allFolders = new ArrayList<>(); // choose
    public String name = "";
    public List<String> allPaths = new ArrayList<>(); // tous les paths existants afin de blacklist blacklist
    public boolean editableName = true;
    public Type type = Type.DATA;
    public boolean editableType = true;
    public String user;
    public boolean editableUser = true;
    public List<String> allUsers = new ArrayList<>();
    public String group;
    public boolean editableGroup = true;
    public List<String> allGroups = new ArrayList<>();
    public String permission = "777";
    public boolean editablePermission = true;
    public String content = "";
    public byte[] binaryContent = new byte[]{}; // not displayed

    public CreateFileOptions setFolder(String folder, boolean editable){
        this.folder = folder;
        this.editableFolder = editable;
        return this;
    }

    public CreateFileOptions setAllFolders(List<String> allFolders){
        this.allFolders = allFolders;
        return this;
    }

    public CreateFileOptions setName(String name, boolean editable){
        this.name = name;
        this.editableName = editable;
        return this;
    }

    public CreateFileOptions setAllPaths(List<String> allPaths){
        this.allPaths = allPaths;
        return this;
    }

    public CreateFileOptions setType(Type type, Boolean editable){
        this.type = type;
        this.editableType = editable;
        return this;
    }

    public CreateFileOptions setUser(String user, boolean editable){
        this.user = user;
        this.editableUser = editable;
        return this;
    }

    public CreateFileOptions setAllUsers(List<String> allUsers){
        this.allUsers = allUsers;
        return this;
    }

    public CreateFileOptions setGroup(String group, boolean editable){
        this.group = group;
        this.editableGroup = editable;
        return this;
    }

    public CreateFileOptions setAllGroups(List<String> allGroups){
        this.allGroups = allGroups;
        return this;
    }

    public CreateFileOptions setPermission(String permission, boolean editable){
        this.permission = permission;
        this.editablePermission = editable;
        return this;
    }

    public CreateFileOptions setContent(String content){
        this.content = content;
        return this;
    }

    public CreateFileOptions setBinaryContent(byte[] binaryContent){
        this.binaryContent = binaryContent;
        return this;
    }

    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch(CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        return o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(folder, name, type, user, group, permission, content, binaryContent);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof CreateFileOptions) {
            CreateFileOptions that = (CreateFileOptions) other;
            return Objects.equals(this.folder,        that.folder) &&
                   Objects.equals(this.name,          that.name) &&
                   Objects.equals(this.type,          that.type) &&
                   Objects.equals(this.user,          that.user) &&
                   Objects.equals(this.group,         that.group) &&
                   Objects.equals(this.permission,    that.permission) &&
                   Objects.equals(this.content,       that.content) &&
                    Arrays.equals(this.binaryContent, that.binaryContent);
        } else {
            return false;
        }
    }

}

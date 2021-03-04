package ShellNightmare.Terminal.FileSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * représente une donnée binaire notamment musique
 * @author Louri Noël
 */

public class BinaryData extends Inode<BinaryData> {
    private static final long serialVersionUID = 306586949445437585L;

    byte[] binary;

    @Override
    public int getSize() {
        return binary.length;
    }

    public BinaryData(User u, Group g) {
        super(u,g);
        permission=644;
        type = Type.BINARY;
        color=34;
    }

    public void setData(byte[] data) {
        this.binary = data;
    }

    public void loadFromImportedFile(java.io.File file){
        try {
            setData(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToStream(OutputStream stream) throws IOException {
        stream.write(binary);
    }

    public byte[] getData(User u) {
        if (getPermission(u).contains(Permission.READ))
            return binary;
        return null;
    }
}

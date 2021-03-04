package ShellNightmare.Terminal.challenge;

import Monitoring.SerializableImage;
import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.MetaContext;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// serialisation et zip :
// https://www.baeldung.com/java-compress-and-uncompress
// https://codereview.stackexchange.com/questions/171680/exporting-an-object-to-a-zip-file
// ou encore https://stackoverflow.com/questions/5465511/java-reading-multiple-images-from-a-single-zip-file-and-eventually-turning-the
// utilisation des try pour fermer correctement les streams

// https://stackoverflow.com/questions/33074774/javafx-image-serialization

public class Challenge implements Serializable { // Serializable : juste pour le serveur
    public static final String CHALLENGES_FOLDER = "challenges"; // dossier où se trouvent tous les challenges en .zip
    public static final String HEADER = "header.bin";
    public static final String IMAGE = "image.png";
    public static final String SCORES = "scores.bin";
    public static final String CONTEXT = "context.bin";

    public transient String filename = ""; // de la forme nom.zip (sans spécifier le dossier challenges/)

    public ChallengeHeader header;
    public transient BufferedImage image; // transient pour pas oublier si on rend Challenge serializable
    public Context context;
    public transient ScoreList scores;

    public Challenge(){
        header = new ChallengeHeader();
        context = new Context();
        scores = new ScoreList();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(new SerializableImage(image));
        oos.flush();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        SerializableImage ser = (SerializableImage) ois.readObject();
        image = ser.image;
        scores = new ScoreList();
    }

    private void writeField(ZipOutputStream stream, String name, Object obj) throws IOException {
        if(obj == null)
            return;

        stream.putNextEntry(new ZipEntry(name));
        ObjectOutputStream objectStream = new ObjectOutputStream(stream);
        objectStream.writeObject(obj); // do not close
    }

    // modification sans tout re-sauvegarder : https://stackoverflow.com/questions/11502260/modifying-a-text-file-in-a-zip-archive-in-java
    public void updateScoresOnDisk(){
        if(filename == null) return;

        Path zipFilePath = Paths.get(CHALLENGES_FOLDER + "/" + filename);
        try (java.nio.file.FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Path path = fs.getPath("/" + SCORES);
            Files.delete(path); // car existe déjà

            try(ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))){
                output.writeObject(scores);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateHeaderOnDisk(){
        if(filename == null) return;

        Path zipFilePath = Paths.get(CHALLENGES_FOLDER + "/" + filename);
        try (java.nio.file.FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Path path = fs.getPath("/" + HEADER);
            Files.delete(path); // car existe déjà

            try(ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))){
                output.writeObject(header);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOnDisk(String path){
        File zipfile = new File(path);

        if(context.currentUser.name.equals(MetaContext.username))
            context.currentUser.name = "";

        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipfile)))) {
            writeField(zipStream, HEADER, header);

            if(image != null){
                zipStream.putNextEntry(new ZipEntry(IMAGE));
                ImageIO.write(image, "png", zipStream);
            }

            writeField(zipStream, SCORES, scores);

            writeField(zipStream, CONTEXT, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // peut renvoyer null si le fichier n'est pas trouvé
    private static Object readField(String zipPath, String name, Function<InputStream, Object> converter) throws IOException {
        Object obj = null;
        File zipfile = new File(zipPath);

        // pour décompresser
        File temp = Files.createTempFile("EscapeTheShell", "todelete").toFile();

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipfile)));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(temp))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                if(zipEntry.getName().equals(name)){
                    IOUtils.copy(zis, bos); // copy input to output
                    bos.close();
                    obj = converter.apply(bis);
                    break;
                }
                zipEntry = zis.getNextEntry();
            }
        }

        temp.delete();

        return obj;
    }

    public static <T> T readSimpleField(Class<T> type, String zipPath, String name) throws IOException {
        Object result = readField(zipPath, name, (in) -> {
            Object obj = null;
            try {
                ObjectInputStream ois = new ObjectInputStream(in);
                obj = ois.readObject();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return obj;
        });

        return type.cast(result);
    }

    public static BufferedImage readImageField(String zipPath) throws IOException {
        return (BufferedImage) readField(zipPath, IMAGE, (in) -> {
            BufferedImage img = null;
            try {
                img = ImageIO.read(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return img;
        });
    }

    public Challenge loadFromDisk(String path) { // challenges/nom.zip
        try {
            header = readSimpleField(ChallengeHeader.class, path, HEADER);
            image = readImageField(path);
            scores = readSimpleField(ScoreList.class, path, SCORES); // intérêt du ScoreList au lieu de ArrayList<Score> : pouvoir en déterminer la classe
            context = readSimpleField(Context.class, path, CONTEXT);
        }catch(Exception e){
            return null;
        }
        return this;
    }
}

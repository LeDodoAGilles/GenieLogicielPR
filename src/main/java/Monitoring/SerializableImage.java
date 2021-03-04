package Monitoring;

import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

public class SerializableImage implements Serializable {
    private static final long serialVersionUID = 1743914665L;

    public transient BufferedImage image;

    public SerializableImage(BufferedImage src){
        image = src;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteStream);

        byte[] size = ByteBuffer.allocate(4).putInt(byteStream.size()).array();

        byte[] buffer = byteStream.toByteArray();

        out.write(size);
        out.write(buffer); // fill all array
        out.flush();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int len = 0;
        int part = 0;

        byte[] size = new byte[4];
        while(len != 4 && (part = in.read(size)) > 0) // 4 octets, c'est petit mais on sait jamais
            len += part;

        int N = ByteBuffer.wrap(size).asIntBuffer().get();

        len = 0;
        part = 0;

        byte[] buffer = new byte[N];
        while(len != N && (part = in.read(buffer, len, N-len)) > 0)
            len += part;

        image = ImageIO.read(new ByteArrayInputStream(buffer));
    }
}

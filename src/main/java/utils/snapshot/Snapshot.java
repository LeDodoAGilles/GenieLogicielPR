package utils.snapshot;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;
import org.apache.commons.lang3.SystemUtils;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

// si Robot : multi-screen: https://docs.oracle.com/javase/6/docs/api/java/awt/GraphicsConfiguration.html

/* ImageMagick avec maven :
<dependency>
<groupId>org.im4java</groupId>
<artifactId>im4java</artifactId>
<version>1.4.0</version>
</dependency>*/

/** Classe utilitaire pour faire la capture d'écran d'un node, typiquement du terminal. */
public class Snapshot {
    // L'exécutable magick.exe doit être renommé/copié en convert.exe
    //private static final String imageMagicFolder = "C:\\Program Files\\ImageMagick-7.0.8-Q16";

    /** Prends une capture d'écran du node donné. (la souris n'est pas visible)
     *
     * Usage recommandé :
     *   Qu'importe : toutes les options à true
     *     capture(node, useHiDPI=true, replaceBorders=true, useSharpen=true, useBrighten=true)
     *   Windows + application sur l'écran principal + terminal au premier plan :
     *     capture(node, useHiDPI=true, replaceBorders=false, useSharpen=false, useBrighten=true)
     *   Autre :
     *     capture(node, useHiDPI=false, replaceBorders=true, useSharpen=true, useBrighten=false)
     *
     * 'node' est le node dont l'on veut la capture d'écran.
     *
     * L'option useHiDPI peut être activée pour avoir une meilleure résolution de l'image.
     *   Cette option prend en compte la véritable apparence du node à l'écran, dont la résolution peut différer
     *   de la dimension (ce qui est souvent le cas sur les ordinateurs récents).
     *   Autrement, la capture aura une résolution plus faible, et sera floue car apparaîtra agrandie.
     *   La capture renvoyée avec cette option activée est trop grande pour être utilisable telle quelle,
     *   elle devra être réduite dans le canvas.
     *   Cette option nécessite Java 12 et JNA 5.4.0 .
     *   Cette option nécessite que l'application soit lancée sur l'écran principal, et que le node soit au premier plan.
     *   Cette option n'est disponible que sur Windows, elle sera ignorée sur les autres OS.
     *
     * Avec (notamment) l'option useHiDPI, la capture peut paraître plus sombre.
     *   L'option useBrighten permet de rendre la capture plus claire.
     *   Si useHiDPI est désactivée, cette option est ignorée si useSharpen est activée.
     *
     * Sans l'option useHiDPI, la capture sera gérée par JavaFx directement.
     *   Cependant, il est possible que le bord inférieur et/ou droit de la capture soit extérieur au node
     *   (c'est-à-dire qui appartient probablement à son parent, qui a possiblement une autre couleur de fond).
     *   L'option replaceBorders permet de remplacer les pixels possiblements affectés par la couleur donnée
     *   (même s'il n'y a pas eu de problème). Attention, cela efface le bord inférieur et le bord droit
     *   sur une ligne de 1 pixel de large, et recopie les pixels intérieurs voisins.
     *   Si replaceBorders est null, les bords inférieur et droit de la capture ne seront pas remplacés.
     *   Cette option est ignorée si useHiDPI est activée.
     *
     * Sans l'option useHiDPI, la capture sera floue.
     *   L'option useSharpen permet de rendre la capture plus nette.
     *     Cette option est ignorée si useHiDPI est activée. */
    public static Image capture(Node node, boolean useHiDPI, boolean replaceBorders, /*boolean useImageMagick,*/ boolean useSharpen, boolean useBrighten){
        if(SystemUtils.IS_OS_WINDOWS && useHiDPI){
            BufferedImage image = windows_hidpi(node); // /!\ Image plus grande qu'il ne faudrait.

            if(useBrighten){
                BufferedImage output = Snapshot.brighten(image, 1.1f);
                return SwingFXUtils.toFXImage(output, null);
            }

            return SwingFXUtils.toFXImage(image, null);
        }
        else {
            WritableImage image = node.snapshot(new SnapshotParameters(), null);

            // avant les autres traitements
            if(replaceBorders){
                replaceBottomLeftBorders(image);
            }

            /*if(useImageMagick){
                try{
                    BufferedImage output = imagemagick_sharpen(SwingFXUtils.fromFXImage(image, null));
                    return SwingFXUtils.toFXImage(output, null);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }*/

            if(useSharpen){
                BufferedImage output = Snapshot.sharpen(SwingFXUtils.fromFXImage(image, null));
                return SwingFXUtils.toFXImage(output, null);
            }

            if(useBrighten){
                BufferedImage output = Snapshot.brighten(SwingFXUtils.fromFXImage(image, null), 1.1f);
                return SwingFXUtils.toFXImage(output, null);
            }

            return image; // default capture
        }
    }

    public static Image capture(Node node){
        //return capture(node, true, true, true, true, true);
        return capture(node, true, true, true, true);
    }

    /** Rend l'image nette par convolution. */
    private static BufferedImage sharpen(BufferedImage input){
        // matrice : https://en.wikipedia.org/wiki/Kernel_(image_processing)#Details
        // code Kernel + example sharpen : https://www.informit.com/articles/article.aspx?p=1013851&seqNum=5
        float[] sharpenConvo = new float[] { // /!\ la somme des coefficients d'une matrice de convolution vaut 1.
                0.0f, -1.0f, 0.0f,
                -1.0f, 5.0f, -1.0f,
                0.0f, -1.0f, 0.0f
        };

        Kernel kernel = new Kernel(3, 3, sharpenConvo);
        ConvolveOp op = new ConvolveOp(kernel);
        return op.filter(input, null);
    }

    /** Éclaircit l'image par convolution.
     * coef doit être > 1 */
    private static BufferedImage brighten(BufferedImage input, float coef){
        // code Kernel : https://www.informit.com/articles/article.aspx?p=1013851&seqNum=5
        // Ici coef > 1, donc la somme des coefficients est > 1, d'où l'éclaircissement.
        float[] brightenConvo = new float[] {
                0.0f, 0.0f, 0.0f,
                0.0f, coef, 0.0f,
                0.0f, 0.0f, 0.0f
        };

        Kernel kernel = new Kernel(3, 3, brightenConvo);
        ConvolveOp op = new ConvolveOp(kernel);
        return op.filter(input, null);
    }

    private static void replaceBottomLeftBorders(WritableImage image){
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = image.getPixelWriter();
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        if(h > 1){ // Si plus de 1 pixel de haut
            for(int x=0 ; x<w ; x++){
                pixelWriter.setColor(x, h-1, pixelReader.getColor(x, h-2)); // remplace la dernière ligne par la précédente
            }
        }

        if(w > 1){ // Si plus de 1 pixel de long
            for(int y=0 ; y<h ; y++){
                pixelWriter.setColor(w-1, y, pixelReader.getColor(w-2, y)); // remplace la dernière colonne par la précédente
            }
        }
    }

    /** Renvoie une capture du node avec les vraies dimensions. */
    private static BufferedImage windows_hidpi(Node node){
        // code hidpi : https://stackoverflow.com/questions/48140881/how-to-capture-a-screenshot-in-native-hdpi-resolution#answer-57614537
        com.sun.jna.platform.win32.User32 user32 = com.sun.jna.platform.win32.User32.INSTANCE;
        com.sun.jna.platform.win32.WinDef.HWND hwnd = user32.GetDesktopWindow();
        BufferedImage screenshot = com.sun.jna.platform.win32.GDI32Util.getScreenshot(hwnd); // screenshot de tout l'écran

        Bounds userNodeBounds = node.localToScreen(node.getBoundsInLocal()); // dimensions "retrécies", dans "l'espace utilisateur"
        double k = screenshot.getWidth() / Screen.getPrimary().getBounds().getWidth(); // facteur d'échelle, résolution

        // Coordonnées et dimensions du node dans le screenshot.
        int x = (int) Math.round(k * userNodeBounds.getMinX());
        int y = (int) Math.round(k * userNodeBounds.getMinY());
        int w = (int) Math.round(k * userNodeBounds.getWidth());
        int h = (int) Math.round(k * userNodeBounds.getHeight());

        return screenshot.getSubimage(x, y, w, h);
    }

    /*private static BufferedImage imagemagick_sharpen(BufferedImage input) throws InterruptedException, IOException, IM4JavaException {
        // Voir TestCase 12 et 13, sources ici : https://sourceforge.net/projects/im4java/
        // code (usage + output BufferedImage) http://im4java.sourceforge.net/docs/dev-guide.html , section "Using BufferedImages"

        ConvertCmd cmd = new ConvertCmd();
        cmd.setSearchPath(imageMagicFolder); // lui dit où trouver l'exécutable. Évite de changer le classpath.

        Stream2BufferedImage s2b = new Stream2BufferedImage();
        cmd.setOutputConsumer(s2b); // Afin de convertir stdout en BufferedImage

        IMOperation op = new IMOperation();
        op.addImage(); // placeholder pour l'input
        op.sharpen(2.0); // plus c'est grand, mieux c'est, mais plus c'est aussi coûteux et long.
        op.addImage("png:-"); // output en png (sans perte), sur stdout au lieu d'enregistrer dans un fichier

        cmd.run(op, input); // lance la commande en utilisant l'exécutable de ImageMagick
        return s2b.getImage(); // récupère l'output
    }*/
}

package utils.snapshot;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;

/** Canvas contenant la capture d'écran d'un node, typiquement du terminal.
 *
 * En fait, c'est un Pane qui encapsule un Canvas, afin de pouvoir resize le canvas. */
public class SnapshotCanvas extends Pane {
    private final Paint backgroundColor; // couleur de fond initiale du terminal.
    private final Canvas canvas;
    private final Image snapshot; // capture d'écran / du terminal
    private final double imageW, imageH; // Dimensions initiales de la capture d'écran, à conserver.

    /**
     * width, height : dimensions initiales, typiquement les mêmes que le terminal.
     * backgroundColor : couleur de fond initiale du terminal. */
    public SnapshotCanvas(double width, double height, Image snapshot, Paint backgroundColor){
        imageW = width;
        imageH = height;
        setWidth(imageW);
        setHeight(imageH);

        this.snapshot = snapshot;
        this.backgroundColor = backgroundColor;

        canvas = new Canvas(imageW, imageH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(snapshot, 0, 0, imageW, imageH); // Dessine la capture aux dimensions du terminal.
        getChildren().add(canvas);

        // Les dimensions du canvas changeront automatiquement lorsque le Pane sera resize.
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        // Redessine le canvas à chaque resize.
        canvas.widthProperty().addListener(e -> redraw());
        canvas.heightProperty().addListener(e -> redraw());
    }

    /** Redessine le canvas lors d'un resize.
     *
     * Cela est nécessaire lorsque la fenêtre est agrandie afin que :
     * - l'image placée en (0,0) reste dans le coin haut-gauche.
     * - il n'y ait pas d'espace transparent laissant apparaitre le terminal dessous, car ce dernier subira des
     *   changements qui ne devront pas être vus. Il est donc nécessaire de combler ce vide avec la couleur de fond
     *   qu'avait le terminal lors de la capture d'écran. */
    private void redraw(){
        // Nouvelles dimensions du canvas.
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h); // Efface tout le contenu du canvas.
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, w, h); // Rempli tout le fond du canvas avec la couleur prédéfinie.

        gc.drawImage(snapshot, 0, 0, imageW, imageH); // Dessine la capture aux dimensions du terminal.
    }
}

package ShellNightmare.Terminal.TerminalFX;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

// TODO https://www.howtogeek.com/howto/44997/how-to-use-bash-history-to-improve-your-command-line-productivity/

/** Historique des commandes rentrées dans un terminal.
 *
 * Les commandes sont stockées en tant que String afin de pouvoir ressortir tel quel ce qu'a rentré l'utilisateur.
 * Le stockage est circulaire : si le maximum d'éléments est dépassé lorqsqu'une insertion est demandée,
 *   alors l'élément le plus ancien est supprimé pour faire de la place. */
public class CommandHistory {

    /** Un "Iterator" sur un historique de commandes.
     *
     * Permet de ne pas rendre disponible les méthodes add(), remove() et set() .
     * Change le comportement de previous() et next() pour ne pas renvoyer le même élément si elles sont alternées.
     * Ajoute une méthode peek() qui renvoit le précédent élément retourné, sans se déplacer. */
    static class PseudoListIterator<E>{
        private final List<E> array;
        private int index;

        public PseudoListIterator(List<E> array){
            this.array = array;
            this.index = array.size();
        }

        public boolean hasPrevious(){
            return index > 0;
        }

        public boolean hasNext(){
            return index < array.size()-1;
        }

        public E previous(){
            index--;
            return array.get(index);
        }

        public E next(){
            index++;
            return array.get(index);
        }

        public E peek(){
            if(index == array.size())
                throw new IllegalStateException("There has been no iteration yet.");

            return array.get(index);
        }
    }

    // Structure de stockage des commandes.
    private final List<String> fifo;

    // Taille max de l'historique des commandes.
    // Peut normalement être affichée avec echo $HISTSIZE, et même être modifiée avec HISTSIZE=...
    private int histsize = 500;

    public CommandHistory(List<String> fifo){
        this.fifo = fifo;
    }

    /** Renvoit un "Iterator" pointant sur la dernière commande rentrée. */
    public PseudoListIterator<String> iterator(){
        return new PseudoListIterator<>(fifo);
    }

    public void add(String cmd){
        if(histsize == 0) return;

        if(fifo.size() == histsize){
            fifo.remove(0);
        }
        fifo.add(cmd);
    }

    public String get(int i){
        return fifo.get(i);
    }

    /** history -c */
    public void clear(){
        fifo.clear();
    }

    /** $HISTSIZE */
    public int maxSize(){
        return histsize;
    }

    /** HISTSIZE=size */
    public void setMaxSize(int size){
        if(size < 0) throw new IllegalArgumentException("Size is negative");

        histsize = size;

        if(histsize == 0){
            fifo.clear();
        }

        while(fifo.size() > histsize){
            fifo.remove(0);
        }
    }

    /** Retourne la taille réelle de l'historique */
    public int size(){
        return fifo.size();
    }

    /** history */
    @Override
    public String toString(){
        if(fifo.size() == 0) return "";

        StringBuilder buffer = new StringBuilder();

        int indexLength = Math.max(5, (int) Math.log10(histsize)); // arrondi à l'inférieur. plus performant que String.valueOf(histsize).length()
        String pattern = "%" + indexLength + "d  %s\n";

        for(int i=0 ; i<fifo.size() ; i++){
            buffer.append(String.format(pattern, i+1, fifo.get(i))); // TODO format dans une fonction à part pour mettre en page des données en colonnes
        }

        return buffer.toString(); // se termine par '\n'
    }
}

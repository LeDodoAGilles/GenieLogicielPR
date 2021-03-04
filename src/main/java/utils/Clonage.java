package utils;

import java.io.*;

/**
 * Classe utilitaire permettant de cloner un objet sérialisable
 *
 * @author Lounes Gaëtan
 */
public class Clonage {
    /**
     * @param e l'objet cloné
     * @param <E> l'objet doit être sérialisable!
     * @see Serializable
     * @return un copie profonde de e.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Serializable> E cloneObject(E e){
        E newE=null;
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(e);
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            newE = (E) is.readObject();
            os.close();
            is.close();
        }
        catch(Exception ignored){
            ignored.printStackTrace();
        }
        return newE;
    }
}

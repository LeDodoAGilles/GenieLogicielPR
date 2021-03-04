package ShellNightmare.Terminal.TerminalFX;

import javafx.scene.control.IndexRange;

/** Encapsule diverses informations sur une sélection dans un DodoTextArea. */
public class TermSelection {
    public int limit;
    public int caret;
    public int start;
    public int end;
    public String text;

    public TermSelection(int limit, int caret, int start, int end, String text){
        this.limit = limit;
        this.caret = caret;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public TermSelection(int limit, int caret, IndexRange range, String text){
        this(limit, caret, range.getStart(), range.getEnd(), text);
    }

    public boolean isEmpty(){
        return start == end;
    }

    public IndexRange range(){
        return new IndexRange(start, end);
    }

    public boolean isEditable(){
        return start >= limit;
    }
}

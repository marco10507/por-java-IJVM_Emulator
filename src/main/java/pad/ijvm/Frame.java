package pad.ijvm;

import java.util.HashMap;
import java.util.Map;

public class Frame {
    private int programCounter;
    private StackWord stackWord;
    private Map<Integer, Integer> localVariablesMap;

    Frame() {
        programCounter = 0;
        stackWord = new StackWord();
        localVariablesMap = new HashMap<>();
    }

    public StackWord getStackWord() {
        return stackWord;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public Map<Integer, Integer> getLocalVariablesMap() {
        return localVariablesMap;
    }
}

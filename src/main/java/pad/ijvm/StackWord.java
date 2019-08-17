package pad.ijvm;

public class StackWord extends Stack<Word> {
    public StackWord() {
    }

    public int[] toIntArray() {
        int[] intArray = new int[super.size()];

        int intArrayIndex = 0;
        Word word;

        for(Object object : super.toArray()){
            word = (Word) object;
            intArray[intArrayIndex] = word.getIntValue();

            ++intArrayIndex;
        }

        return  intArray;
    }
}

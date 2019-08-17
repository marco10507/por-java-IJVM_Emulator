package pad.ijvm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Word {
    private byte[] wordByteArray;
    private int data;

    Word(int data) {
        wordByteArray = constructWordByteArray(data);
        this.data = IJVMUtils.parseToInt(wordByteArray);
    }

    Word(byte[] dataByteArray) {
        wordByteArray = constructWordByteArray(dataByteArray);
        this.data = IJVMUtils.parseToInt(wordByteArray);
    }

    public int getIntValue() {
        return data;
    }

    public byte[] getDataByteArray() {
        return wordByteArray;
    }

    public Word getClone() {
        return new Word(getDataByteArray().clone());
    }

    private byte[] constructWordByteArray(int data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(IJVMUtils.WORD_BYTE_SIZE).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(data);
        return byteBuffer.array();
    }

    private byte[] constructWordByteArray(byte[] dataByteArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(IJVMUtils.WORD_BYTE_SIZE).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(IJVMUtils.parseToInt(dataByteArray));
        return byteBuffer.array();
    }
}

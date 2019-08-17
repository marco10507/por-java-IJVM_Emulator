package pad.ijvm;

import java.nio.ByteBuffer;

public class IJVMUtils {
    public static final int WORD_BYTE_SIZE = 4;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray(); // took it from the internet

    public static int parseToInt(byte[] dataByteArray) {
        int result;

        if (dataByteArray.length == 1) {
            result = ByteBuffer.wrap(dataByteArray).get();
        } else if (dataByteArray.length == 2) {
            byte[] toParseByteArray = new byte[]{(byte) 0x00, (byte) 0x00, dataByteArray[0], dataByteArray[1]};
            result = ByteBuffer.wrap(toParseByteArray).getInt();
        } else {
            result = ByteBuffer.wrap(dataByteArray).getInt();
        }

        return result;
    }

    // took it from the internet
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

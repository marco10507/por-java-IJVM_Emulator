package pad.ijvm;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class IJVMBinaries {
    private static final int MAGIC_NUMBER_BYTES = 4;
    private static final int BLOCK_ADDRESS_BYTES = 4;
    private static final int DATA_LENGTH_BYTES = 4;

    private List<BinaryBlock> binaryBlockList;
    private byte[] magicNumberArray;
    private File binaries;
    private byte[] bytesArray;

    IJVMBinaries(File binaries) {
        this.binaries = binaries;
        binaryBlockList = new ArrayList<>();
        magicNumberArray = null;
        bytesArray = null;
        build();
    }

    byte[] getMagicNumberArray() {
        return magicNumberArray;
    }

    List<BinaryBlock> getBinaryBlockList() {
        return binaryBlockList;
    }

    private void setMagicNumber(byte[] bytesArray) {
        if (magicNumberArray == null) {
            magicNumberArray = new byte[MAGIC_NUMBER_BYTES];
            System.arraycopy(bytesArray, 0, magicNumberArray, 0, MAGIC_NUMBER_BYTES);
        }
    }

    private void setBinaryBlockList(byte[] bytesArray) {
        int blockIndex = MAGIC_NUMBER_BYTES;
        int dataLength;

        while (blockIndex < bytesArray.length) {
            BinaryBlock block = new BinaryBlock();
            block.setAddressArray(blockIndex, bytesArray);
            blockIndex += DATA_LENGTH_BYTES;
            block.setDataLengthArray(blockIndex, bytesArray);
            dataLength = IJVMUtils.parseToInt(block.getDataLengthArray());
            blockIndex += DATA_LENGTH_BYTES;
            block.setDataArray(blockIndex, dataLength, bytesArray);
            binaryBlockList.add(block);
            blockIndex += dataLength;
        }
    }

    private void build() {
        bytesArray = getBytesArray(binaries);
        setMagicNumber(bytesArray);
        setBinaryBlockList(bytesArray);
    }

    private byte[] getBytesArray(File binaries) {
        byte[] bytes = new byte[(int) binaries.length()];
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(binaries);
            fileInputStream.read(bytes);
        } catch (Exception e) {
            // todo
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception e) {
                // todo
            }
        }

        return bytes;
    }

    class BinaryBlock {
        private byte[] addressArray;
        private byte[] dataLengthArray;
        private byte[] dataArray;

        BinaryBlock() {
            addressArray = null;
            dataLengthArray = null;
            dataArray = null;
        }

        void setAddressArray(int startAddressIndex, byte[] bytesArray) {
            if (addressArray == null) {
                addressArray = new byte[BLOCK_ADDRESS_BYTES];
                System.arraycopy(bytesArray, startAddressIndex, addressArray, 0, BLOCK_ADDRESS_BYTES);
            }
        }

        void setDataLengthArray(int startDataLengthIndex, byte[] bytesArray) {
            if (dataLengthArray == null) {
                dataLengthArray = new byte[DATA_LENGTH_BYTES];
                System.arraycopy(bytesArray, startDataLengthIndex, dataLengthArray, 0, DATA_LENGTH_BYTES);
            }
        }

        void setDataArray(int startDataIndex, int dataLength, byte[] bytesArray) {
            if (dataArray == null && dataLength > 0) {
                dataArray = new byte[dataLength];
                System.arraycopy(bytesArray, startDataIndex, dataArray, 0, dataLength);
            }
        }

        byte[] getAddressArray() {
            return addressArray;
        }

        byte[] getDataLengthArray() {
            return dataLengthArray;
        }

        byte[] getDataArray() {
            return dataArray;
        }
    }
}

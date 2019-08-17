package pad.ijvm;

import pad.ijvm.interfaces.IJVMInterface;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

class IJVM implements IJVMInterface {
    private static final int METHOD_BLOCK_INDEX = 1;
    private static final int CONSTANT_POOL_BLOCK_INDEX = 0;

    private byte[] textArray;
    private IJVMBinaries ijvmBinaries;
    private Map<Short, Integer> constantPoolMap;
    private StackFrame stackFrame;
    private boolean machineActive;
    private Socket networkConnection;
    private List<List<Integer>> memoryHeapList;

    PrintStream out;
    InputStream in;

    IJVM(File binary) {
        ijvmBinaries = new IJVMBinaries(binary);
        textArray = getTextByteArray(ijvmBinaries);
        constantPoolMap = Collections.unmodifiableMap(constructConstantPoolMap(ijvmBinaries)); // todo: move it to ENUM
        stackFrame = new StackFrame(new Frame());
        machineActive = true;
        networkConnection = null;
        memoryHeapList = new ArrayList<>();
        out = System.out;
        in = System.in;
    }

    public StackFrame getStackFrame() {
        return stackFrame;
    }

    @Override
    public int topOfStack() {
        return stackFrame.top().getStackWord().top().getIntValue();
    }

    @Override
    public int[] getStackContents() {
        return getStackWord().toIntArray();
    }

    @Override
    public byte[] getText() {
        return textArray;
    }

    @Override
    public int getProgramCounter() {
        return stackFrame.top().getProgramCounter();
    }

    @Override
    public int getLocalVariable(int i) {
        // todo: local vars: instructions say that constant index is a byte. Why using int to find the value?
        // todo: I am using a Frame class so it make sense to have getLocalVariable() in Frame not in IJVM. Is this
        // todo: method only used for testing? My code looks ugly if I keep this method in IJVM and not in Fame
        return stackFrame.top().getLocalVariablesMap().get(i);
    }

    @Override
    public int getConstant(int i) {
        // todo: getConstant casting the index to short because the paramenter is int. correct?
        return constantPoolMap.get((short) i);
    }

    @Override
    public void step() {
        if(machineActive){
            String instructionName = ISA.map.get(getInstruction());

            if (isJUnitTest()) {
                System.err.printf("%s %s\n", instructionName, IJVMUtils.bytesToHex(new byte[]{getInstruction()}));
            }
            ISA.valueOf(instructionName).runInstruction(this);
        }
    }

    @Override
    public void run() {
        while (machineActive && stackFrame.top().getProgramCounter() < textArray.length) {
            step();
        }
    }

    @Override
    public byte getInstruction() {
        return textArray[stackFrame.top().getProgramCounter()];
    }

    @Override
    public void setOutput(PrintStream out) {
        this.out = out;
    }

    @Override
    public void setInput(InputStream in) {
        this.in = in;
    }

    public Frame getFrame() {
        return stackFrame.top();
    }

    public StackWord getStackWord() {
        return stackFrame.top().getStackWord();
    }

    public void setProgramCounter(int programCounter) {
        stackFrame.top().setProgramCounter(programCounter);
    }

    public void addLocalVariable(int localVarIndex, int newLocalVarValue) {
        stackFrame.top().getLocalVariablesMap().put(localVarIndex, newLocalVarValue);
    }

    public void setMachineActive(boolean machineActive) {
        this.machineActive = machineActive;
    }

    private Map<Short, Integer> constructConstantPoolMap(IJVMBinaries ijvmBinaries) {
        Map<Short, Integer> constantPoolMap = new HashMap<>();
        byte[] constantPoolByteArray = getConstantPoolByteArray(ijvmBinaries);
        byte[] constantByteArray;
        short index = 0;

        if (getConstantPoolByteArray(ijvmBinaries) != null) {
            for (int i = 0; i < constantPoolByteArray.length; i++) {
                constantByteArray = new byte[IJVMUtils.WORD_BYTE_SIZE];
                System.arraycopy(constantPoolByteArray, i, constantByteArray, 0, IJVMUtils.WORD_BYTE_SIZE);
                constantPoolMap.put(index, ByteBuffer.wrap(constantByteArray).getInt());
                ++index;
                i += 3;
            }
        }

        return constantPoolMap;
    }

    private byte[] getTextByteArray(IJVMBinaries ijvmBinaries) {
        return ijvmBinaries.getBinaryBlockList().get(METHOD_BLOCK_INDEX).getDataArray();
    }

    private byte[] getConstantPoolByteArray(IJVMBinaries ijvmBinaries) {
        return ijvmBinaries.getBinaryBlockList().get(CONSTANT_POOL_BLOCK_INDEX).getDataArray();
    }

    public Socket getNetworkConnection() {
        return networkConnection;
    }

    public void setNetworkConnection(Socket networkConnection) {
        this.networkConnection = networkConnection;
    }

    public List<List<Integer>> getMemoryHeapList() {
        return memoryHeapList;
    }


    // todo: change it a bit, from the internet
    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}

package pad.ijvm;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public enum ISA {
    BIPUSH((byte) 0x10, 1) {
        @Override
        void runInstruction(IJVM ijvm) {
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            ijvm.getStackWord().push(new Word(operandByteArray));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IADD((byte) 0x60, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int term1 = ijvm.getStackWord().pop().getIntValue();
            int term2 = ijvm.getStackWord().pop().getIntValue();
            int result = term2 + term1;

            ijvm.getStackWord().push(new Word(result));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    ISUB((byte) 0x64, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int term1 = ijvm.getFrame().getStackWord().pop().getIntValue();
            int term2 = ijvm.getFrame().getStackWord().pop().getIntValue();
            int result = term2 - term1;

            ijvm.getStackWord().push(new Word(result));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    DUP((byte) 0x59, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word topOfTheStackWord = ijvm.getStackWord().top().getClone();

            ijvm.getStackWord().push(topOfTheStackWord);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    GOTO((byte) 0xA7, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            // todo: the offset is the only one that does not support the int conversion (parseToInt()), why?
            int offSet = ByteBuffer.wrap(operandByteArray).getShort();
            int nextInstructionIndex = ijvm.getProgramCounter() + offSet;
            ijvm.setProgramCounter(nextInstructionIndex);
        }
    },
    IAND((byte) 0x7E, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int term1 = ijvm.getStackWord().pop().getIntValue();
            int term2 = ijvm.getStackWord().pop().getIntValue();
            int result = term2 & term1;

            ijvm.getStackWord().push(new Word(result));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IFEQ((byte) 0x99, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word word = ijvm.getStackWord().pop();
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            if (word.getIntValue() == 0) {
                int offSet = IJVMUtils.parseToInt(operandByteArray);
                int nextInstructionIndex = ijvm.getProgramCounter() + offSet;
                ijvm.setProgramCounter(nextInstructionIndex);
            } else {
                ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
            }
        }
    },
    IFLT((byte) 0x9B, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word word = ijvm.getStackWord().pop();
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            if (isNegativeNumber(word.getIntValue())) {
                short offSet = ByteBuffer.wrap(operandByteArray).getShort();
                int nextInstructionIndex = ijvm.getProgramCounter() + offSet;
                ijvm.setProgramCounter(nextInstructionIndex);
            } else {
                ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
            }
        }
    },
    IF_ICMPEQ((byte) 0x9F, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word word1 = ijvm.getStackWord().pop();
            Word word2 = ijvm.getStackWord().pop();
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            if (word1.getIntValue() == word2.getIntValue()) {
                int offSet = IJVMUtils.parseToInt(operandByteArray);
                int nextInstructionIndex = ijvm.getProgramCounter() + offSet;
                ijvm.setProgramCounter(nextInstructionIndex);
            } else {
                ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
            }
        }
    },
    IINC((byte) 0x84, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            byte localVarIndex = ijvm.getText()[ijvm.getProgramCounter() + 1];
            byte termValue = ijvm.getText()[ijvm.getProgramCounter() + 2];

            int localVarValue = ijvm.getLocalVariable(localVarIndex);
            int newLocalVarValue = localVarValue + termValue;

            ijvm.addLocalVariable(localVarIndex, newLocalVarValue);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    ILOAD((byte) 0x15, 1) {
        @Override
        void runInstruction(IJVM ijvm) {
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            int localVarIndex;
            int localVarValue;

            if (operandByteSize == 1) {
                localVarIndex = IJVMUtils.parseToInt(operandByteArray);
            } else {
                localVarIndex = IJVMUtils.parseToInt(operandByteArray);
            }

            localVarValue = ijvm.getLocalVariable(localVarIndex);

            ijvm.getStackWord().push(new Word(localVarValue));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    INVOKEVIRTUAL((byte) 0xB6, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            int constantIndex = ByteBuffer.wrap(operandByteArray).getShort();
            int methodAreaAddress = ijvm.getConstant(constantIndex);

            int numberOfArguments = getNumberOfArguments(ijvm.getText(), methodAreaAddress);
            int numberOfObjectReferences = 1;
            int localVariablesLength = getLocalVariablesLength(ijvm.getText(), methodAreaAddress);

            Frame frame = new Frame();
            frame.setProgramCounter(methodAreaAddress + 4);

            numberOfArguments = numberOfArguments - numberOfObjectReferences;

            for (int i = numberOfArguments; i > 0; i--) {
                frame.getLocalVariablesMap().put(i, ijvm.getStackFrame().top().getStackWord().pop().getIntValue());
            }

            frame.getLocalVariablesMap().put(0, 0x00);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));

            ijvm.getStackFrame().push(frame);
        }
    },
    IOR((byte) 0xB0, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int term1 = ijvm.getStackWord().pop().getIntValue();
            int term2 = ijvm.getStackWord().pop().getIntValue();
            int result = term2 | term1;

            ijvm.getFrame().getStackWord().push(new Word(result));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IRETURN((byte) 0xAC, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            if (ijvm.getStackWord().size() == 0) {
                ijvm.getStackFrame().pop();
            } else {
                int returnValue = ijvm.getStackWord().pop().getIntValue();
                Word word = new Word(returnValue);

                ijvm.getStackFrame().pop();
                ijvm.getStackWord().pop();
                ijvm.getStackWord().push(word);
            }
        }
    },
    ISTORE((byte) 0x36, 1) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word word = ijvm.getStackWord().pop();
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            int localVarIndex;
            int localVarValue;

            if (operandByteSize == 1) {
                localVarIndex = IJVMUtils.parseToInt(operandByteArray);
            } else {
                localVarIndex = IJVMUtils.parseToInt(operandByteArray);
            }

            localVarValue = word.getIntValue();

            ijvm.addLocalVariable(localVarIndex, localVarValue);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    LDC_W((byte) 0x13, 2) {
        @Override
        void runInstruction(IJVM ijvm) {
            int operandIndex = ijvm.getProgramCounter() + 1;
            byte[] operandByteArray = getByteArrayChunk(ijvm.getText(), operandIndex, operandByteSize);

            short constantIndex = ByteBuffer.wrap(operandByteArray).getShort();
            int constantValue = ijvm.getConstant(constantIndex);

            ijvm.getStackWord().push(new Word(constantValue));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    NOP((byte) 0x00, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    POP((byte) 0x57, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            ijvm.getStackWord().pop();

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    SWAP((byte) 0x5F, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            Word word1 = ijvm.getStackWord().pop();
            Word word2 = ijvm.getStackWord().pop();

            ijvm.getStackWord().push(word1);
            ijvm.getStackWord().push(word2);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    WIDE((byte) 0xC4, 0) {
        // todo: task4(important): implement WIDE, skipping this for now because no test for WIDE until advanced tests
        @Override
        void runInstruction(IJVM ijvm) {
            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
            byte instruction = ijvm.getText()[ijvm.getProgramCounter()];
            String instructionName = ISA.map.get(instruction);

            ISA isa = ISA.valueOf(instructionName);
            isa.operandByteSize = 2;
            isa.runInstruction(ijvm);
            isa.operandByteSize = 1;
        }
    },
    OUT((byte) 0xFD, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int intValue = ijvm.getStackWord().pop().getIntValue();
            ijvm.out.print((char) intValue);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IN((byte) 0xFC, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int i;
            char c = 0x00;

            try {
                if ((i = ijvm.in.read()) != -1) {
                    c = (char) i;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijvm.getStackWord().push(new Word(c));

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    HALT((byte) 0xff, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            //todo: halt the emulator. What to run here?:
            ijvm.setMachineActive(false);
            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    NETBIND((byte) 0xE1, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int port = ijvm.getStackWord().pop().getIntValue();

            try {
                ServerSocket server = new ServerSocket(port);
                Socket socket = server.accept();

                ijvm.setNetworkConnection(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    NETCONNECT((byte) 0xE2, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int host = ijvm.getStackWord().pop().getIntValue();
            int port = ijvm.getStackWord().pop().getIntValue();

            String ipAddress;
            boolean connectionSucceeded = false;

            try {
                ipAddress = toIpAddress(host);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(ipAddress, port);

                Socket socket = new Socket();
                socket.connect(inetSocketAddress);

                connectionSucceeded = socket.isConnected();

                ijvm.setNetworkConnection(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Word word = (connectionSucceeded) ? new Word(1) : new Word(0);

            ijvm.getStackWord().push(word);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    NETIN((byte) 0xE3, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            InputStream netInputStream;

            try {
                netInputStream = ijvm.getNetworkConnection().getInputStream();

                int i;
                char c = 0x00;

                if ((i = netInputStream.read()) != -1) {
                    c = (char) i;
                }

                ijvm.getStackWord().push(new Word(c));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    NETOUT((byte) 0xE4, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int intValue = ijvm.getStackWord().pop().getIntValue();
            char charValue = (char) intValue;

            try {
                OutputStream outputStream = ijvm.getNetworkConnection().getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);

                printStream.print(charValue);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));

        }
    },
    NETCLOSE((byte) 0xE5, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            if (ijvm.getNetworkConnection() != null) {
                try {
                    ijvm.getNetworkConnection().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    },
    NEWARRAY((byte) 0xD1, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            ijvm.getMemoryHeapList().add(new ArrayList<>());
            int intArrayReference = ijvm.getMemoryHeapList().size() - 1;

            Word word = new Word(intArrayReference);
            ijvm.getStackWord().push(word);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IALOAD((byte) 0xD2, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int arrayref = ijvm.getStackWord().pop().getIntValue();
            int index = ijvm.getStackWord().pop().getIntValue();
            int value = ijvm.getMemoryHeapList().get(arrayref).get(index);

            Word word = new Word(value);
            ijvm.getStackWord().push(word);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    },
    IASTORE((byte) 0xD3, 0) {
        @Override
        void runInstruction(IJVM ijvm) {
            int arrayref = ijvm.getStackWord().pop().getIntValue();
            int index = ijvm.getStackWord().pop().getIntValue();
            int value = ijvm.getStackWord().pop().getIntValue();

            ijvm.getMemoryHeapList().get(arrayref).add(index, value);

            ijvm.setProgramCounter(getNextInstructionIndex(ijvm.getProgramCounter(), operandByteSize));
        }
    };
    // todo: change the name of the to instructionNameByHexValueMap (for example)
    public static final Map<Byte, String> map = Collections.unmodifiableMap(buildISAMap());
    private byte opcode;
    public int operandByteSize;

    ISA(byte opcode, int operandSize) {
        this.opcode = opcode;
        this.operandByteSize = operandSize;
    }

    abstract void runInstruction(IJVM ijvm);

    private static Map<Byte, String> buildISAMap() {
        Map<Byte, String> map = new HashMap<>();

        for (ISA instruction : ISA.values()) {
            map.put(instruction.getOpcode(), instruction.name());
        }

        return map;
    }

    private byte getOpcode() {
        return opcode;
    }

    private static byte[] getByteArrayChunk(byte[] srcByteArray, int copyFromIndex, int elements) {
        byte[] result = new byte[elements];
        System.arraycopy(srcByteArray, copyFromIndex, result, 0, elements);

        return result;
    }

    private static int getNextInstructionIndex(int currentInstructionIndex, int operandByteSize) {
        return currentInstructionIndex + operandByteSize + 1;
    }

    private static int getNumberOfArguments(byte[] textByteArray, int methodAreaAddress) {
        int numberOfArguments;
        int numberOfArgumentsByteSize = 2;
        byte[] numberOfArgumentsByteArray = getByteArrayChunk(
                textByteArray,
                methodAreaAddress,
                numberOfArgumentsByteSize
        );
        numberOfArguments = ByteBuffer.wrap(numberOfArgumentsByteArray).getShort();

        return numberOfArguments;
    }

    private static int getLocalVariablesLength(byte[] textByteArray, int methodAreaAddress) {
        int localVariablesLength;
        int localVariablesLengthByteSize = 2;
        int localVariableLengthAddress = methodAreaAddress + localVariablesLengthByteSize;
        byte[] localVariablesLengthByteArray = getByteArrayChunk(
                textByteArray,
                localVariableLengthAddress,
                localVariablesLengthByteSize
        );


        localVariablesLength = IJVMUtils.parseToInt(localVariablesLengthByteArray);

        return localVariablesLength;
    }

    private static boolean isNegativeNumber(int number) {
        return number <= -1;
    }

    private static String toIpAddress(int numericIpAddress) throws UnknownHostException {
        String ipAddress;

        InetAddress inetAddress = InetAddress.getByName(String.valueOf(numericIpAddress));
        ipAddress = inetAddress.getHostAddress();

        return ipAddress;
    }
}

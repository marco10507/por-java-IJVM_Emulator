package pad.ijvm;

public class StackFrame extends Stack<Frame> {
    StackFrame(Frame frame) {
        push(frame);
    }
}

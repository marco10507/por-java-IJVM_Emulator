package pad.ijvm;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

public class Stack<T> {
    private Deque<T> stack;

    public Stack() {
        stack = new ArrayDeque<>();
    }

    public void push(T element) {
        stack.push(element);
    }

    public T top() {
        return stack.getFirst();
    }

    public int size() {
        return stack.size();
    }

    public T pop() {
        return stack.pop();
    }

    Object[] toArray() {
        return stack.toArray();
    }
}

package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.heap.SimpleHeap;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction.Instruction;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class SimulatorMemory {
    private final Deque<Long> operandStack = new ArrayDeque<>();
    private final List<Long> globalMemory = new ArrayList<>();
    private final List<Instruction> instructions;
    private long programCounter = 0;
    private long stackPointer = 0;
    private final Deque<Frame> callStack = new ArrayDeque<>();
    private final SimpleHeap heap = new SimpleHeap();
    private final Scanner scanner;
    private final PrintStream printStream;

    public static class Frame {
        public long savedSp;
        public long savedPc;
    }

    public SimulatorMemory(List<Instruction> instructions, InputStream inputStream, PrintStream printStream) {
        this.instructions = instructions;
        this.scanner = new Scanner(inputStream);
        this.printStream = printStream;
    }

    public Deque<Long> getOperandStack() {
        return operandStack;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public long getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(long programCounter) {
        this.programCounter = programCounter;
    }

    public void instructionDone() {
        this.programCounter += 8;
    }

    public long getStackPointer() {
        return stackPointer;
    }

    public void setStackPointer(long stackPointer) {
        this.stackPointer = stackPointer;
    }

    public SimpleHeap getHeap() {
        return heap;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void pushCallFrame(long savedSp, long savedPc) {
        Frame frame = new Frame();
        frame.savedSp = savedSp;
        frame.savedPc = savedPc;
        callStack.push(frame);
    }

    public Frame popCallFrame() {
        if (callStack.isEmpty()) {
            throw new IllegalStateException("Call stack underflow");
        }
        return callStack.pop();
    }

    // 全局变量动态扩容访问
    public long readGlobal(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Global variable index negative: " + index);
        }
        if (index >= globalMemory.size()) {
            return 0L;
        }
        return (int) (long) globalMemory.get(index);
    }

    public void writeGlobal(int index, long value) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Global variable index negative: " + index);
        }
        while (index >= globalMemory.size()) {
            globalMemory.add(0L);
        }
        globalMemory.set(index, value & 0xFFFFFFFFL);
    }

    // 局部变量通过 SP + offset * 4 访问堆内存
    public long readLocal(int offset) {
        long address = stackPointer + (long) offset * 4L;
        return (int) heap.read(address);
    }

    public void writeLocal(int offset, long value) {
        long address = stackPointer + (long) offset * 4L;
        heap.write(address, value);
    }
}

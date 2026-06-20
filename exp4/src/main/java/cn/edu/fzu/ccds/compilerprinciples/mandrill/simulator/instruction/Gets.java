package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * gets: 从 stdin 读入一个连续非空白字符构成的字符串，
 * 在堆上按 UTF-32（4字节/字符，0结尾）存储，将地址压入栈。
 */
public class Gets extends Instruction {
    public Gets(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        String str = vm.getScanner().next();
        int[] codePoints = str.codePoints().toArray();
        int len = codePoints.length;
        int bytes = (len + 1) * 4;
        long address = vm.getHeap().allocate(bytes);
        for (int i = 0; i < len; i++) {
            vm.getHeap().write(address + (long) i * 4L, codePoints[i]);
        }
        // C 风格结尾 0
        vm.getHeap().write(address + (long) len * 4L, 0);
        vm.getOperandStack().push(address);
        vm.instructionDone();
    }
}

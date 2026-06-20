package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * puts x: 弹出栈顶元素作为地址，从该地址按 C 风格 UTF-32 字符串读取并输出到 stdout。
 */
public class PutS extends Instruction {
    public PutS(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long address = vm.getOperandStack().pop();
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        while (true) {
            long codePoint = vm.getHeap().read(address + (long) offset * 4L);
            if (codePoint == 0) {
                break;
            }
            sb.appendCodePoint((int) codePoint);
            offset++;
        }
        vm.getPrintStream().print(sb.toString());
        vm.instructionDone();
    }
}

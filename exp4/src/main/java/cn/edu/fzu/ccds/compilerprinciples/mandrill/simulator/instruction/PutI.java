package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * puti x: 弹出栈顶元素，以十进制形式输出到 stdout。
 */
public class PutI extends Instruction {
    public PutI(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long value = vm.getOperandStack().pop();
        vm.getPrintStream().print((int) value);
        vm.instructionDone();
    }
}

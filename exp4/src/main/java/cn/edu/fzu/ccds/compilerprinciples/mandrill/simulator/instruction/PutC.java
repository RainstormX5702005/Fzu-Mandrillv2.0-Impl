package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * putc x: 弹出栈顶元素，转换为字符输出到 stdout。
 */
public class PutC extends Instruction {
    public PutC(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long value = vm.getOperandStack().pop();
        vm.getPrintStream().print(Character.toChars((int) value));
        vm.instructionDone();
    }
}

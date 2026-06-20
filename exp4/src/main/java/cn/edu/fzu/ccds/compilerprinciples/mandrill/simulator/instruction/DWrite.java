package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * dwrite x: 将操作数栈顶元素弹出，写入全局变量 x。
 */
public class DWrite extends Instruction {
    public DWrite(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long value = vm.getOperandStack().pop();
        vm.writeGlobal((int) operand, value);
        vm.instructionDone();
    }
}

package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * dawrite x: 从栈顶获取地址，次栈顶获取值，将值写入对应地址。
 */
public class DAWrite extends Instruction {
    public DAWrite(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long address = vm.getOperandStack().pop();
        long value = vm.getOperandStack().pop();
        vm.getHeap().write(address, value);
        vm.instructionDone();
    }
}

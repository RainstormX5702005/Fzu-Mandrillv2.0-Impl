package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * dstore x: 将立即数 x 压入操作数栈。
 */
public class DStore extends Instruction {
    public DStore(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        vm.getOperandStack().push(operand);
        vm.instructionDone();
    }
}

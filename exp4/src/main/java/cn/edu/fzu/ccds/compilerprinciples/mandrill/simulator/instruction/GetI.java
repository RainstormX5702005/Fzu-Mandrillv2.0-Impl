package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * geti x: 从 stdin 读入一个十进制整数，压入操作数栈。
 */
public class GetI extends Instruction {
    public GetI(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        try {
            long value = vm.getScanner().nextLong();
            vm.getOperandStack().push(value);
        } catch (Exception e) {
            vm.getOperandStack().push(0L);
        }
        vm.instructionDone();
    }
}

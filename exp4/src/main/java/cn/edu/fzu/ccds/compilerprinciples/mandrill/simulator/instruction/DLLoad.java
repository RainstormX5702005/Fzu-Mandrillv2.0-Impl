package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * dlload x: 从局部变量区（SP + x*4）读取数据压入操作数栈。
 */
public class DLLoad extends Instruction {
    public DLLoad(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long value = vm.readLocal((int) operand);
        vm.getOperandStack().push(value);
        vm.instructionDone();
    }
}

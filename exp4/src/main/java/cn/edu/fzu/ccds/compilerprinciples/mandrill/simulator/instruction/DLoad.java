package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * dload x: 从全局变量 x 读取数据压入操作数栈。
 */
public class DLoad extends Instruction {
    public DLoad(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long value = vm.readGlobal((int) operand);
        vm.getOperandStack().push(value);
        vm.instructionDone();
    }
}

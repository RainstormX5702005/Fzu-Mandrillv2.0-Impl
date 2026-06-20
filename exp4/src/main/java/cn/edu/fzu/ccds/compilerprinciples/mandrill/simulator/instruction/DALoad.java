package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * daload x: 从栈顶获取地址，从该地址读取数据并存入操作数栈。
 */
public class DALoad extends Instruction {
    public DALoad(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long address = vm.getOperandStack().pop();
        long value = vm.getHeap().read(address);
        vm.getOperandStack().push(value);
        vm.instructionDone();
    }
}

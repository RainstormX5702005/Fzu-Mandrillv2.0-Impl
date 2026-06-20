package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * malloc x: 从栈顶获取所需字节数，申请堆内存，将起始地址压入栈。
 */
public class Malloc extends Instruction {
    public Malloc(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        long size = vm.getOperandStack().pop();
        long address = vm.getHeap().allocate((int) size);
        vm.getOperandStack().push(address);
        vm.instructionDone();
    }
}

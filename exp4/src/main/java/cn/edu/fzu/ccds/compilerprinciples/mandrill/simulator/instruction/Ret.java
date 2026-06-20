package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * ret x: 从调用栈帧恢复 SP 和 PC。
 */
public class Ret extends Instruction {
    public Ret(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        SimulatorMemory.Frame frame = vm.popCallFrame();
        vm.setStackPointer(frame.savedSp);
        vm.setProgramCounter(frame.savedPc);
    }
}

package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

public class Nop extends Instruction {
    public Nop(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        vm.instructionDone();
    }
}

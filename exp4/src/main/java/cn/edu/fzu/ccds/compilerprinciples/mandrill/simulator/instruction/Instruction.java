package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

public abstract class Instruction {
    protected final long operand;

    public Instruction(long operand) {
        this.operand = operand;
    }

    public long getOperand() {
        return operand;
    }

    public abstract void execute(SimulatorMemory vm);
}

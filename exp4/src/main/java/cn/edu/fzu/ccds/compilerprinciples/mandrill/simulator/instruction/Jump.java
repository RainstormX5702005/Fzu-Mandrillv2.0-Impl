package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * jump x: 将 PC 直接设置为 x。
 */
public class Jump extends Instruction {
    public Jump(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        vm.setProgramCounter(operand);
    }
}

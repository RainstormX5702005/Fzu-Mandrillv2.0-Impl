package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.Constants;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;

/**
 * eval x: 根据子操作码 x 执行运算或条件跳转。
 */
public class Eval extends Instruction {
    public Eval(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        int op = (int) operand;
        if (op == Constants.EVAL_CONDITION) {
            long cond = vm.getOperandStack().pop();
            long trueTarget = vm.getOperandStack().pop();
            long falseTarget = vm.getOperandStack().pop();
            if (cond != 0) {
                vm.setProgramCounter(trueTarget);
            } else {
                vm.setProgramCounter(falseTarget);
            }
        } else {
            long right = vm.getOperandStack().pop();
            long left = vm.getOperandStack().pop();
            long result = switch (op) {
                case Constants.EVAL_ADD -> left + right;
                case Constants.EVAL_MINUS -> left - right;
                case Constants.EVAL_MUL -> left * right;
                case Constants.EVAL_DIV -> left / right;
                case Constants.EVAL_MOD -> left % right;
                case Constants.EVAL_GREATER -> left > right ? 1L : 0L;
                case Constants.EVAL_LESS -> left < right ? 1L : 0L;
                case Constants.EVAL_GREATER_OR_EQUAL -> left >= right ? 1L : 0L;
                case Constants.EVAL_LESS_OR_EQUAL -> left <= right ? 1L : 0L;
                case Constants.EVAL_EQUAL -> left == right ? 1L : 0L;
                case Constants.EVAL_NOT_EQUAL -> left != right ? 1L : 0L;
                default -> throw new IllegalStateException("Unexpected eval operand: 0x" + Integer.toHexString(op));
            };
            vm.getOperandStack().push(result);
            vm.instructionDone();
        }
    }
}

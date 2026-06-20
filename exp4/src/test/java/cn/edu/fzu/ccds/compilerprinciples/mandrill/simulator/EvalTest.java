package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction.Eval;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction.Instruction;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvalTest {

    private SimulatorMemory createMemory(List<Instruction> instructions) {
        return new SimulatorMemory(instructions,
                new ByteArrayInputStream(new byte[0]),
                new PrintStream(new ByteArrayOutputStream()));
    }

    @Test
    void testAdd() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(3L);
        vm.getOperandStack().push(5L);
        new Eval(Constants.EVAL_ADD).execute(vm);
        assertEquals(8L, vm.getOperandStack().pop());
    }

    @Test
    void testSub() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(10L);
        vm.getOperandStack().push(4L);
        new Eval(Constants.EVAL_MINUS).execute(vm);
        assertEquals(6L, vm.getOperandStack().pop());
    }

    @Test
    void testMul() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(7L);
        vm.getOperandStack().push(6L);
        new Eval(Constants.EVAL_MUL).execute(vm);
        assertEquals(42L, vm.getOperandStack().pop());
    }

    @Test
    void testDiv() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(20L);
        vm.getOperandStack().push(4L);
        new Eval(Constants.EVAL_DIV).execute(vm);
        assertEquals(5L, vm.getOperandStack().pop());
    }

    @Test
    void testMod() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(17L);
        vm.getOperandStack().push(5L);
        new Eval(Constants.EVAL_MOD).execute(vm);
        assertEquals(2L, vm.getOperandStack().pop());
    }

    @Test
    void testGreater() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(5L);
        vm.getOperandStack().push(3L);
        new Eval(Constants.EVAL_GREATER).execute(vm);
        assertEquals(1L, vm.getOperandStack().pop());
    }

    @Test
    void testEqual() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        vm.getOperandStack().push(5L);
        vm.getOperandStack().push(5L);
        new Eval(Constants.EVAL_EQUAL).execute(vm);
        assertEquals(1L, vm.getOperandStack().pop());
    }

    @Test
    void testConditionTrue() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        // push order: falseTarget, trueTarget, cond (cond must be on top)
        vm.getOperandStack().push(32L);   // false target
        vm.getOperandStack().push(16L);   // true target
        vm.getOperandStack().push(1L);    // cond = true
        new Eval(Constants.EVAL_CONDITION).execute(vm);
        assertEquals(16L, vm.getProgramCounter());
    }

    @Test
    void testConditionFalse() {
        List<Instruction> instructions = new ArrayList<>();
        SimulatorMemory vm = createMemory(instructions);
        // push order: falseTarget, trueTarget, cond
        vm.getOperandStack().push(32L);   // false target
        vm.getOperandStack().push(16L);   // true target
        vm.getOperandStack().push(0L);    // cond = false
        new Eval(Constants.EVAL_CONDITION).execute(vm);
        assertEquals(32L, vm.getProgramCounter());
    }
}

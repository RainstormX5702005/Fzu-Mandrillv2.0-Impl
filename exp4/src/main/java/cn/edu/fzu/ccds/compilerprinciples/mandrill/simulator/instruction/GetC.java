package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMemory;
import java.util.Scanner;

/**
 * getc x: 从 stdin 读入一个字符，将其 ASCII/Unicode 码点值压入操作数栈。
 */
public class GetC extends Instruction {
    public GetC(long operand) {
        super(operand);
    }

    @Override
    public void execute(SimulatorMemory vm) {
        try {
            Scanner sc = vm.getScanner();
            sc.useDelimiter("");
            if (sc.hasNext()) {
                String first = sc.next();
                int ch;
                if (!first.isEmpty()
                        && Character.isHighSurrogate(first.charAt(0))
                        && sc.hasNext()) {
                    String second = sc.next();
                    ch = Character.toCodePoint(first.charAt(0), second.charAt(0));
                } else {
                    ch = first.codePointAt(0);
                }
                vm.getOperandStack().push((long) ch);
            } else {
                vm.getOperandStack().push(0L);
            }
        } catch (Exception e) {
            vm.getOperandStack().push(0L);
        }
        vm.instructionDone();
    }
}

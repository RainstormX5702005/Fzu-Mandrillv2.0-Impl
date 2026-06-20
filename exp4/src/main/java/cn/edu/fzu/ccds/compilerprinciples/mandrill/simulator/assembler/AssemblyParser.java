package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.assembler;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本汇编解析器。
 * 每行一条指令，格式：<指令名> [操作数]
 * 支持 # 开头或 // 开头的注释，忽略空行。
 */
public class AssemblyParser {

    public static List<Instruction> parse(InputStream inputStream) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                Instruction instruction = parseLine(line);
                instructions.add(instruction);
            }
        }
        return instructions;
    }

    private static Instruction parseLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Empty instruction line");
        }
        String name = parts[0].toLowerCase();
        long operand = 0;
        if (parts.length >= 2) {
            operand = parseOperand(parts[1]);
        }

        return switch (name) {
            case "nop" -> new Nop(operand);
            case "dstore" -> new DStore(operand);
            case "dload" -> new DLoad(operand);
            case "dlload" -> new DLLoad(operand);
            case "daload" -> new DALoad(operand);
            case "dwrite" -> new DWrite(operand);
            case "dlwrite" -> new DLWrite(operand);
            case "dawrite" -> new DAWrite(operand);
            case "eval" -> new Eval(operand);
            case "jump" -> new Jump(operand);
            case "jal" -> new Jal(operand);
            case "ret" -> new Ret(operand);
            case "malloc" -> new Malloc(operand);
            case "geti" -> new GetI(operand);
            case "getc" -> new GetC(operand);
            case "gets" -> new Gets(operand);
            case "puti" -> new PutI(operand);
            case "putc" -> new PutC(operand);
            case "puts" -> new PutS(operand);
            default -> throw new IllegalArgumentException("Unknown instruction: " + name);
        };
    }

    private static long parseOperand(String s) {
        s = s.trim();
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Long.parseLong(s.substring(2), 16);
        }
        return Long.parseLong(s);
    }
}

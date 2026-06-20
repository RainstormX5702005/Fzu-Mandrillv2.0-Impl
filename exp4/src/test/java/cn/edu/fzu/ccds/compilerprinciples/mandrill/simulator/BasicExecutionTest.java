package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.assembler.AssemblyParser;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.instruction.Instruction;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicExecutionTest {

    private String runAssembly(String assembly, String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        try {
            List<Instruction> instructions = AssemblyParser.parse(
                    new ByteArrayInputStream(assembly.getBytes(StandardCharsets.UTF_8)));
            SimulatorMemory memory = new SimulatorMemory(instructions,
                    new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out);
            while (memory.getProgramCounter() != 0xFFFFFFFFL) {
                int index = (int) (memory.getProgramCounter() / 8);
                if (index < 0 || index >= instructions.size()) {
                    break;
                }
                instructions.get(index).execute(memory);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test
    void testAddAndPrint() {
        // Push 3 and 5 onto the operand stack, add them (EVAL_ADD = 0x00010001 = 65537),
        // print the result, then halt (jump to 0xFFFFFFFF).
        String asm = """
                dstore 3
                dstore 5
                eval 65537
                puti 0
                jump 4294967295
                """;
        assertEquals("8", runAssembly(asm, ""));
    }

    @Test
    void testGlobalVariable() {
        // Store immediate 7 into global[0], load it back, print, then halt.
        String asm = """
                dstore 7
                dwrite 0
                dload 0
                puti 0
                jump 4294967295
                """;
        assertEquals("7", runAssembly(asm, ""));
    }

    @Test
    void testInputOutput() {
        // Read an integer from stdin, add 1, print the result.
        // EVAL_ADD (65537) computes: read_value + 1 = 6.
        String asm = """
                geti 0
                dstore 1
                eval 65537
                puti 0
                jump 4294967295
                """;
        assertEquals("6", runAssembly(asm, "5"));
    }

    @Test
    void testLocalVariable() {
        // Call a function with 0 args and 1 local variable (4 bytes).
        // main: push localSize=4, call func at addr 24, halt.
        // func: push 42, store to local[0], load local[0], print, return.
        String asm = """
                dstore 4
                jal 24
                jump 4294967295
                dstore 42
                dlwrite 0
                dlload 0
                puti 0
                ret 0
                """;
        assertEquals("42", runAssembly(asm, ""));
    }

    @Test
    void testMallocAndArrayAccess() {
        // Allocate 12 bytes (3 ints) on the heap, store the address in global[0].
        // Write 99 to array index 1 (byte offset = 1 * 4 = 4).
        // Read back from index 1 and print.
        String asm = """
                dstore 12
                malloc 0
                dwrite 0
                dstore 99
                dload 0
                dstore 1
                dstore 4
                eval 65539
                eval 65537
                dawrite 0
                dload 0
                dstore 1
                dstore 4
                eval 65539
                eval 65537
                daload 0
                puti 0
                jump 4294967295
                """;
        assertEquals("99", runAssembly(asm, ""));
    }

    @Test
    void testFunctionCall0Params() {
        // main: push localSize=4, call noargs() at addr 32, print return value, halt.
        // noargs: push 42 as return value, return.
        String asm = """
                dstore 4
                jal 32
                puti 0
                jump 4294967295
                dstore 42
                ret 0
                """;
        assertEquals("42", runAssembly(asm, ""));
    }

    @Test
    void testFunctionCall1Param() {
        // main: push arg=21, push localSize=4, call double(x) at addr 40, print, halt.
        // double: pop arg into local[0], load it, multiply by 2, return.
        String asm = """
                dstore 21
                dstore 4
                jal 40
                puti 0
                jump 4294967295
                dlwrite 0
                dlload 0
                dstore 2
                eval 65539
                ret 0
                """;
        assertEquals("42", runAssembly(asm, ""));
    }

    @Test
    void testFunctionCall2Params() {
        // main: push args b=5, a=3 (reverse order so local[0]=a, local[1]=b),
        // push localSize=8, call add(a,b) at addr 48, print, halt.
        // add: pop args to local[0] and local[1], add them, return.
        String asm = """
                dstore 5
                dstore 3
                dstore 8
                jal 48
                puti 0
                jump 4294967295
                dlwrite 0
                dlwrite 1
                dlload 0
                dlload 1
                eval 65537
                ret 0
                """;
        assertEquals("8", runAssembly(asm, ""));
    }

    @Test
    void testFunctionCall3Params() {
        // main: push args c=3, b=2, a=1, push localSize=12,
        // call sum3(a,b,c) at addr 56, print, halt.
        // sum3: pop args to locals, compute a+b+c, return.
        String asm = """
                dstore 3
                dstore 2
                dstore 1
                dstore 12
                jal 56
                puti 0
                jump 4294967295
                dlwrite 0
                dlwrite 1
                dlwrite 2
                dlload 0
                dlload 1
                eval 65537
                dlload 2
                eval 65537
                ret 0
                """;
        assertEquals("6", runAssembly(asm, ""));
    }

    @Test
    void testFunctionCall4Params() {
        // main: push args d=4, c=3, b=2, a=1, push localSize=16,
        // call sum4(a,b,c,d) at addr 64, print, halt.
        // sum4: pop args to locals, compute a+b+c+d, return.
        String asm = """
                dstore 4
                dstore 3
                dstore 2
                dstore 1
                dstore 16
                jal 64
                puti 0
                jump 4294967295
                dlwrite 0
                dlwrite 1
                dlwrite 2
                dlwrite 3
                dlload 0
                dlload 1
                eval 65537
                dlload 2
                eval 65537
                dlload 3
                eval 65537
                ret 0
                """;
        assertEquals("10", runAssembly(asm, ""));
    }

    @Test
    void testRecursion3Levels() {
        // Tail-recursive factorial(3) with ~3 levels of recursion.
        // The function fact(n, acc) computes n! by accumulating the result.
        // main: push acc=1, n=3, localSize=8, call fact at addr 48, print, halt.
        // fact(n, acc):
        //   addr 48: pop n to local[0], pop acc to local[1]
        //   addr 64: push false target=136 (else), push true target=120 (then)
        //   addr 80: load n, push 1, eval LESS_OR_EQUAL (65545)
        //   addr 96: eval CONDITION (65548)
        //   addr 120 (then): load acc, return
        //   addr 136 (else): load n, load acc, eval MUL (65539)
        //   addr 160: load n, push 1, eval MINUS (65538)
        //   addr 184: push new acc, push n-1, push localSize=8, recursive jal 48
        //   addr 200: ret (unwinds intermediate frames in tail recursion)
        String asm = """
                dstore 1
                dstore 3
                dstore 8
                jal 48
                puti 0
                jump 4294967295
                dlwrite 0
                dlwrite 1
                dlload 0
                dstore 136
                dstore 120
                dlload 0
                dstore 1
                eval 65545
                eval 65548
                dlload 1
                ret 0
                dlload 0
                dlload 1
                eval 65539
                dlload 0
                dstore 1
                eval 65538
                dstore 8
                jal 48
                ret 0
                """;
        assertEquals("6", runAssembly(asm, ""));
    }

    @Test
    void testLoop() {
        // Compute sum of 1..5 using a loop.
        // Initialization: global[0] = i = 1, global[1] = sum = 0.
        // Loop condition check at addr 32:
        //   push false target = exit at addr 152, push true target = body at addr 80
        //   load i, push 5, eval LESS_OR_EQUAL (65545) -> pushes cond
        //   eval CONDITION (65548)
        // Loop body at addr 80:
        //   sum = sum + i; i = i + 1; jump back to condition.
        // Exit at addr 152: load sum, print, halt.
        String asm = """
                dstore 1
                dwrite 0
                dstore 0
                dwrite 1
                dstore 152
                dstore 80
                dload 0
                dstore 5
                eval 65545
                eval 65548
                dload 1
                dload 0
                eval 65537
                dwrite 1
                dload 0
                dstore 1
                eval 65537
                dwrite 0
                jump 32
                dload 1
                puti 0
                jump 4294967295
                """;
        assertEquals("15", runAssembly(asm, ""));
    }
}

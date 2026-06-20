package cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMain;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortCircuitTest {

    @Test
    void andShouldNotEvaluateRightSideWhenLeftIsFalse() throws Exception {
        File tempDir = Files.createTempDirectory("mandrill-short-circuit").toFile();
        File srcFile = new File(tempDir, "short-circuit.mds");
        File asmFile = new File(tempDir, "short-circuit.asm");
        File inFile = new File(tempDir, "short-circuit.in");
        File outFile = new File(tempDir, "short-circuit.out");

        String source = """
                if (0 && (1 / 0)) {
                    write = 1;
                } else {
                    write = 2;
                }
                put = '\n';
                """;
        try (FileOutputStream fos = new FileOutputStream(srcFile)) {
            fos.write(source.getBytes(StandardCharsets.UTF_8));
        }
        try (FileOutputStream fos = new FileOutputStream(inFile)) {
            fos.write(new byte[0]);
        }

        CompilerMain.main(new String[]{srcFile.getAbsolutePath(), asmFile.getAbsolutePath()});

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            SimulatorMain.main(new String[]{asmFile.getAbsolutePath(), inFile.getAbsolutePath(), outFile.getAbsolutePath()});
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        String output = Files.readString(outFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("2\n", output);

        srcFile.delete();
        asmFile.delete();
        inFile.delete();
        outFile.delete();
        tempDir.delete();
    }
}

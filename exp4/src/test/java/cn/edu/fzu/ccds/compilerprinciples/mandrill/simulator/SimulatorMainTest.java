package cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatorMainTest {

    @Test
    void testAddProgram() throws Exception {
        File tempDir = Files.createTempDirectory("mandrill-sim-test").toFile();
        File asmFile = new File(tempDir, "add.mas");
        File outFile = new File(tempDir, "add.out");

        String asm = """
                geti 0
                geti 0
                eval 65537
                puti 0
                jump 4294967295
                """;
        try (FileOutputStream fos = new FileOutputStream(asmFile)) {
            fos.write(asm.getBytes(StandardCharsets.UTF_8));
        }

        ByteArrayInputStream in = new ByteArrayInputStream("3 5".getBytes(StandardCharsets.UTF_8));
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setIn(in);
        System.setOut(new PrintStream(outFile));

        try {
            SimulatorMain.main(new String[]{asmFile.getAbsolutePath()});
        } finally {
            System.setIn(System.in);
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        String output = Files.readString(outFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("8", output);

        asmFile.delete();
        outFile.delete();
        tempDir.delete();
    }
}

package cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 编译器 CLI 入口。
 *
 * 用法: CompilerMain [--backend direct|tac] <input.mds> [output.asm]
 *   - --backend direct: 使用 AST → 汇编直接生成
 *   - --backend tac:    使用 AST → TAC → 汇编（默认）
 *   - input.mds:        Mandrill 源文件
 *   - output.asm:       可选，输出汇编文件路径。若省略，输出到 stdout。
 */
public class CompilerMain {

    public static void main(String[] args) {
        String inputPath = null;
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                default -> {
                    if (inputPath == null) {
                        inputPath = args[i];
                    } else if (outputPath == null) {
                        outputPath = args[i];
                    } else {
                        System.err.println("Usage: CompilerMain <input.mds> [output.asm]");
                        System.exit(1);
                    }
                }
            }
        }

        if (inputPath == null) {
            System.err.println("Usage: CompilerMain <input.mds> [output.asm]");
            System.exit(1);
        }

        try {
            String assembly = compileFile(inputPath);
            if (outputPath != null) {
                Files.writeString(Path.of(outputPath), assembly, StandardCharsets.UTF_8);
            } else {
                System.out.print(assembly);
            }
        } catch (Exception e) {
            System.err.println("Compilation error: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }

    public static String compileFile(String path) throws IOException {
        try (InputStream in = new FileInputStream(path)) {
            Compiler backend = new CompilerImpl(); // CompilerImpl换成自己实现的编译器类，返回值为汇编代码，参考test中给出的样例
            return backend.compile(in);
        }
    }
}

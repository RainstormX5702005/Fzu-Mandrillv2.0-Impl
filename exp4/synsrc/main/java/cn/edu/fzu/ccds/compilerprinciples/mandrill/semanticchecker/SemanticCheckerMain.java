package cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillLexer;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 语义检查器 CLI 入口。
 *
 * 用法: SemanticCheckerMain [input.mds]
 *   - 无参数时从标准输入读取 Mandrill 源程序
 *   - 有参数时从第一个参数指定的文件读取
 *   - 输出 Pass 或 Error 到标准输出
 */
public class SemanticCheckerMain {

    public static void main(String[] args) {
        try {
            InputStream input = args.length == 0 ? System.in : Files.newInputStream(Path.of(args[0]));
            String result = check(input);
            System.out.println(result);
            if (input != System.in) {
                input.close();
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public static String check(InputStream inputStream) throws IOException {
        try {
            CharStream charStream = CharStreams.fromStream(inputStream);
            MandrillLexer lexer = new MandrillLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MandrillParser parser = new MandrillParser(tokens);
            MandrillParser.ProgramContext tree = parser.program();

            SymbolTable table = new SymbolTable();
            // Phase 1: collect function signatures into the symbol table.
            SymbolCollector.collect(tree, table);
            // Phase 2: semantic checks using the populated table.
            new SemanticChecker(table).visit(tree);
            return "Pass";
        } catch (SemanticException e) {
            return "Error";
        }
    }
}

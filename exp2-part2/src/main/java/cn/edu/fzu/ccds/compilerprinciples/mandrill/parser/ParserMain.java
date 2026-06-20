package cn.edu.fzu.ccds.compilerprinciples.mandrill.parser;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.HandcraftLexer;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens.Token;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class ParserMain {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = args.length > 0 && !args[0].equals("-")
                ? new FileInputStream(args[0]) : System.in;
        PrintStream printStream = args.length > 1 && !args[1].equals("-")
                ? new PrintStream(args[1]) : System.out;

        try (inputStream; printStream) {
            // 1. 词法分析
            HandcraftLexer lexer = new HandcraftLexer(inputStream);
            List<Token> tokens = lexer.scanTokens();

            // 2. 语法分析
            // 学生需要实现 HandcraftParser，此处也可替换为基于 ANTLR 的 AntlrParser：
            // Parser parser = new AntlrParser(tokens);
            Parser parser = new HandcraftParser(tokens);
            boolean success = parser.parse();

            // 3. 输出结果：文法正确输出 Pass，错误输出 Error
            printStream.println(success ? "Pass" : "Error");
        } catch (Exception e) {
            // 发生异常时视为语法错误
            printStream.println("Error");
        }
    }
}

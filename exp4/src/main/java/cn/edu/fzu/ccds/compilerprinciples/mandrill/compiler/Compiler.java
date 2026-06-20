package cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillLexer;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.SemanticChecker;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.SymbolCollector;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.SymbolTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Mandrill 编译器接口。
 *
 * <p>提供统一的编译入口，将 Mandrill 源程序转换为汇编代码。
 * 具体实现可以选择直接生成（AST → 汇编）或经由中间表示生成（AST → TAC → 汇编）。</p>
 */
public interface Compiler {

    /**
     * 编译输入流中的 Mandrill 源代码，返回汇编文本。
     *
     * @param inputStream 源代码输入流
     * @return 生成的汇编代码
     * @throws IOException 读取输入失败
     */
    String compile(InputStream inputStream) throws IOException;

    /**
     * 解析源代码并执行前端分析（词法/语法/语义），返回解析树与符号表。
     * 供各实现类复用。
     */
    static CompileContext frontend(InputStream inputStream) throws IOException {
        CharStream charStream = CharStreams.fromStream(inputStream);
        MandrillLexer lexer = new MandrillLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MandrillParser parser = new MandrillParser(tokens);
        MandrillParser.ProgramContext tree = parser.program();

        SymbolTable table = new SymbolTable();
        SymbolCollector.collect(tree, table);
        new SemanticChecker(table).visit(tree);

        return new CompileContext(tree, table);
    }

    /**
     * 前端分析结果上下文。
     */
    record CompileContext(MandrillParser.ProgramContext tree, SymbolTable table) {}
}

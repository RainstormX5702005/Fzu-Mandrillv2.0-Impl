package cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillBaseVisitor;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SymbolCollector extends MandrillBaseVisitor<Void> {

    private final SymbolTable table;

    private SymbolCollector(SymbolTable table) {
        this.table = table;
    }

    public static void collect(MandrillParser.ProgramContext tree, SymbolTable table) {
        new SymbolCollector(table).visit(tree);
    }

    @Override
    public Void visitProgram(MandrillParser.ProgramContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof MandrillParser.FunctionDefContext functionDef) {
                visitFunctionDef(functionDef);
            }
        }
        return null;
    }

    /** 处理单个函数定义：提取签名 → 校验 → 写入符号表。 */
    @Override
    public Void visitFunctionDef(MandrillParser.FunctionDefContext ctx) {
        String name = ctx.Identifier().getText();
        MandrillType returnType = ctx.arraySuffix() != null ? MandrillType.ARRAY : MandrillType.INT;
        List<String> paramNames = getParameterNames(ctx);
        List<MandrillType> paramTypes = getParameterTypes(ctx);

        checkDuplicateParameters(ctx, paramNames);
        table.defineFunction(ctx, name, returnType, paramTypes, paramNames);
        return null;
    }


    /**
     * 通用的参数列表收集方法，实际调用时可以改成
     * 只负责遍历参数节点，真正的提取逻辑由 {@code mapper} 决定。
     *
     * @param mapper 将单个 ParameterContext 映射为目标类型 T 的函数
     */
    private static <T> List<T> collectParameters(
            MandrillParser.FunctionDefContext ctx,
            Function<MandrillParser.ParameterContext, T> mapper) {
        List<T> result = new ArrayList<>();
        if (ctx.parameterList() != null) {
            for (MandrillParser.ParameterContext p : ctx.parameterList().parameter()) {
                result.add(mapper.apply(p));
            }
        }
        return result;
    }


    // 参数名获取方法，提取函数列表中所有的参数名
    private List<String> getParameterNames(MandrillParser.FunctionDefContext ctx) {
        return collectParameters(ctx, p -> p.Identifier().getText());
    }

    // 推导每个参数的类型：有 arraySuffix → ARRAY，否则 → INT
    private List<MandrillType> getParameterTypes(MandrillParser.FunctionDefContext ctx) {
        return collectParameters(ctx, p -> p.arraySuffix() != null ? MandrillType.ARRAY : MandrillType.INT);
    }

    // 检查并警告重复的参数
    private void checkDuplicateParameters(MandrillParser.FunctionDefContext ctx, List<String> names) {
        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                if (names.get(i).equals(names.get(j))) {
                    int line = names.isEmpty()
                        ? ctx.getStart().getLine()
                        : ctx.parameterList().parameter(i).getStart().getLine();
                    int col = names.isEmpty()
                        ? ctx.getStart().getCharPositionInLine()
                        : ctx.parameterList().parameter(i).getStart().getCharPositionInLine();
                    throw new SemanticException(line, col, "Duplicate parameter: " + names.get(i));
                }
            }
        }
    }
}

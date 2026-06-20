package cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.MandrillType;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.SymbolTable;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.Constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实验四核心编译器。
 *
 * 这个文件是编译器的核心后端，它主要执行如下的功能：
 * 
 * 1. 收集变量/函数布局
 * 2. 生成 TAC 风格的内部项
 * 3. 回填标签地址并输出虚拟机汇编
 */
public class TacCompiler {
    public String compile(Compiler.CompileContext context) {
        return compile(context.tree(), context.table());
    }

    private String compile(MandrillParser.ProgramContext program, SymbolTable symbols) {
        LayoutCollector collector = new LayoutCollector(program);
        collector.collect();

        TacProgram tacProgram = new TacProgram();
        TacBuilder builder = new TacBuilder(program, collector, tacProgram, symbols);
        builder.build();

        return new AssemblyEmitter(tacProgram).emit();
    }

    // 做静态布局：全局变量分配全局编号，函数参数和局部变量分配局部槽位。
    private static final class LayoutCollector {
        private static final String TEMP_STRING_SLOT = "__tmp_string_slot";

        private final MandrillParser.ProgramContext program;
        private final Map<String, Integer> globals = new LinkedHashMap<>();
        private final Map<String, FunctionLayout> functions = new LinkedHashMap<>();
        private final List<MandrillParser.StatementContext> mainStatements = new ArrayList<>();

        private LayoutCollector(MandrillParser.ProgramContext program) {
            this.program = program;
        }

        private void collect() {
            for (int i = 0; i < program.getChildCount(); i++) {
                if (program.getChild(i) instanceof MandrillParser.StatementContext stmt) {
                    mainStatements.add(stmt);
                    collectGlobals(stmt);
                } else if (program.getChild(i) instanceof MandrillParser.FunctionDefContext fn) {
                    int parameterCount = fn.parameterList() == null ? 0 : fn.parameterList().parameter().size();
                    FunctionLayout layout = new FunctionLayout(fn.Identifier().getText(), parameterCount);
                    if (fn.parameterList() != null) {
                        for (MandrillParser.ParameterContext param : fn.parameterList().parameter()) {
                            layout.locals.putIfAbsent(param.Identifier().getText(), layout.locals.size());
                        }
                    }
                    collectFunctionLocals(fn.stmtBlock(), layout);
                    functions.put(layout.name, layout);
                }
            }
            globals.putIfAbsent(TEMP_STRING_SLOT, globals.size());
        }

        private int tempStringIndex() {
            return globals.get(TEMP_STRING_SLOT);
        }

        private void collectGlobals(MandrillParser.StatementContext stmt) {
            if (stmt.assignStatement() != null) {
                MandrillParser.AssignStatementContext assign = stmt.assignStatement();
                if (assign.lvalue() instanceof MandrillParser.TargetVariableContext target) {
                    globals.putIfAbsent(target.Identifier().getText(), globals.size());
                } else if (assign.Identifier() != null) {
                    globals.putIfAbsent(assign.Identifier().getText(), globals.size());
                }
            } else if (stmt.declarationStmt() != null && stmt.declarationStmt().scope.getText().equals("global")) {
                globals.putIfAbsent(stmt.declarationStmt().Identifier().getText(), globals.size());
            } else if (stmt.stmtBlock() != null) {
                for (MandrillParser.StatementContext inner : stmt.stmtBlock().statement()) {
                    collectGlobals(inner);
                }
            }
        }

        private void collectFunctionLocals(MandrillParser.StmtBlockContext block, FunctionLayout layout) {
            for (MandrillParser.StatementContext stmt : block.statement()) {
                collectFunctionLocals(stmt, layout);
            }
        }

        private void collectFunctionLocals(MandrillParser.StatementContext stmt, FunctionLayout layout) {
            if (stmt.declarationStmt() != null) {
                layout.locals.putIfAbsent(stmt.declarationStmt().Identifier().getText(), layout.locals.size());
            }
            if (stmt.assignStatement() != null) {
                MandrillParser.AssignStatementContext assign = stmt.assignStatement();
                if (assign.lvalue() instanceof MandrillParser.TargetVariableContext target) {
                    String name = target.Identifier().getText();
                    if (!globals.containsKey(name)) {
                        layout.locals.putIfAbsent(name, layout.locals.size());
                    }
                    if (target.expression() != null) {
                        collectExpressionNames(target.expression(), layout);
                    }
                }
                collectRvalueNames(assign.rvalue(), layout);
            } else if (stmt.loopStatement() != null) {
                collectExpressionNames(stmt.loopStatement().expr, layout);
                collectFunctionLocals(stmt.loopStatement().stmtBlock(), layout);
            } else if (stmt.conditionStatement() != null) {
                collectExpressionNames(stmt.conditionStatement().expr, layout);
                collectFunctionLocals(stmt.conditionStatement().thenStatement, layout);
                if (stmt.conditionStatement().elseStatement != null) {
                    collectFunctionLocals(stmt.conditionStatement().elseStatement, layout);
                }
            } else if (stmt.stmtBlock() != null) {
                collectFunctionLocals(stmt.stmtBlock(), layout);
            }
        }

        private void collectRvalueNames(MandrillParser.RvalueContext rvalue, FunctionLayout layout) {
            collectExpressionNames(rvalue.expression(), layout);
        }

        /**
         * 递归收集表达式里的变量名。
         *
         * 这样能在生成指令前就确定局部槽位规模，后面发射时就不用边看边猜。
         */
        private void collectExpressionNames(MandrillParser.ExpressionContext expr, FunctionLayout layout) {
            if (expr == null) return;
            if (expr instanceof MandrillParser.SourceVariableContext source) {
                String name = source.Identifier().getText();
                if (!globals.containsKey(name)) {
                    layout.locals.putIfAbsent(name, layout.locals.size());
                }
                if (source.expression() != null) {
                    collectExpressionNames(source.expression(), layout);
                }
                return;
            }
            if (expr instanceof MandrillParser.FunctionCallContext call) {
                if (call.argumentList() != null) {
                    for (MandrillParser.ExpressionContext arg : call.argumentList().expression()) {
                        collectExpressionNames(arg, layout);
                    }
                }
                return;
            }
            if (expr instanceof MandrillParser.SubgroupExpressionContext subgroup) {
                collectExpressionNames(subgroup.expression(), layout);
                return;
            }
            if (expr instanceof MandrillParser.MulDivModExpressionContext bin) {
                collectExpressionNames(bin.expression(0), layout);
                collectExpressionNames(bin.expression(1), layout);
                return;
            }
            if (expr instanceof MandrillParser.AddSubExpressionContext bin) {
                collectExpressionNames(bin.expression(0), layout);
                collectExpressionNames(bin.expression(1), layout);
                return;
            }
            if (expr instanceof MandrillParser.ComparingExpressionContext bin) {
                collectExpressionNames(bin.expression(0), layout);
                collectExpressionNames(bin.expression(1), layout);
                return;
            }
            if (expr instanceof MandrillParser.EqualityExpressionContext bin) {
                collectExpressionNames(bin.expression(0), layout);
                collectExpressionNames(bin.expression(1), layout);
            }
        }

        private List<MandrillParser.FunctionDefContext> orderedFunctions() {
            List<MandrillParser.FunctionDefContext> list = new ArrayList<>();
            for (int i = 0; i < program.getChildCount(); i++) {
                if (program.getChild(i) instanceof MandrillParser.FunctionDefContext fn) {
                    list.add(fn);
                }
            }
            return list;
        }
    }

    /**
     * 一个函数的静态布局。
     *
     * parameterCount 只用于函数入口弹参数；locals 记录局部槽位。
     */
    private static final class FunctionLayout {
        private final String name;
        private final int parameterCount;
        private final LinkedHashMap<String, Integer> locals = new LinkedHashMap<>();

        private FunctionLayout(String name, int parameterCount) {
            this.name = name;
            this.parameterCount = parameterCount;
        }

        private int localBytes() {
            return locals.size() * 4;
        }
    }

    private record TacItem(Type type, String name, long value, String labelRef) {
        private enum Type { LABEL, LINE }

        private static TacItem label(String name) {
            return new TacItem(Type.LABEL, name, 0, null);
        }

        private static TacItem line(String name, long value) {
            return new TacItem(Type.LINE, name, value, null);
        }

        private static TacItem lineRef(String name, String labelRef) {
            return new TacItem(Type.LINE, name, 0, labelRef);
        }
    }

    /**
     *  在这个方法中，items 保持原始顺序；render 时先统计标签地址，再把引用替换成数字地址。
     */
    private static final class TacProgram {
        private final List<TacItem> items = new ArrayList<>();

        private void add(TacItem item) {
            items.add(item);
        }

        private String render() {
            Map<String, Long> addresses = new LinkedHashMap<>();
            long pc = 0;
            for (TacItem item : items) {
                if (item.type == TacItem.Type.LABEL) {
                    addresses.put(item.name, pc);
                } else {
                    pc += 8;
                }
            }

            StringBuilder out = new StringBuilder();
            for (TacItem item : items) {
                if (item.type == TacItem.Type.LABEL) {
                    continue;
                }
                long operand = item.value;
                if (item.labelRef != null) {
                    Long resolved = addresses.get(item.labelRef);
                    if (resolved == null) {
                        throw new IllegalStateException("Unknown label: " + item.labelRef);
                    }
                    operand = resolved;
                }
                out.append(item.name).append(' ').append(operand).append('\n');
            }
            return out.toString();
        }
    }

    private static final class TacBuilder {

        // 这个类是最核心的实现，会把 AST 转化为若干的 TAC

        private final MandrillParser.ProgramContext program;
        private final LayoutCollector collector;
        private final TacProgram out;
        private final SymbolTable symbols;
        private final LabelAllocator labels = new LabelAllocator();

        private TacBuilder(MandrillParser.ProgramContext program, LayoutCollector collector, TacProgram out, SymbolTable symbols) {
            this.program = program;
            this.collector = collector;
            this.out = out;
            this.symbols = symbols;
        }

        private void build() {
            emitMain();
            out.add(TacItem.line("jump", 0xFFFFFFFFL));
            for (MandrillParser.FunctionDefContext fn : collector.orderedFunctions()) {
                emitFunction(fn);
            }
        }

        private void emitMain() {
            for (MandrillParser.StatementContext stmt : collector.mainStatements) {
                emitStatement(stmt, null);
            }
        }

        private void emitFunction(MandrillParser.FunctionDefContext fn) {
            FunctionLayout layout = collector.functions.get(fn.Identifier().getText());
            out.add(TacItem.label(functionLabel(fn.Identifier().getText())));
            for (int i = 0; i < layout.parameterCount; i++) {
                out.add(TacItem.line("dlwrite", i));
            }
            emitBlock(fn.stmtBlock(), layout);
            out.add(TacItem.line("dstore", 0));
            out.add(TacItem.line("ret", 0));
        }

        /**
         * 递归处理语句块，语句块本质上只是一个按顺序执行的语句列表，这里直接顺序遍历即可
         */
        private void emitBlock(MandrillParser.StmtBlockContext block, FunctionLayout layout) {
            for (MandrillParser.StatementContext stmt : block.statement()) {
                emitStatement(stmt, layout);
            }
        }

        private void emitStatement(MandrillParser.StatementContext stmt, FunctionLayout layout) {
            if (stmt.assignStatement() != null) {
                emitAssign(stmt.assignStatement(), layout);
                return;
            }
            if (stmt.loopStatement() != null) {
                emitLoop(stmt.loopStatement(), layout);
                return;
            }
            if (stmt.conditionStatement() != null) {
                emitCondition(stmt.conditionStatement(), layout);
                return;
            }
            if (stmt.jumpStmt() != null) {
                emitJump(stmt.jumpStmt(), layout);
                return;
            }
            if (stmt.stmtBlock() != null) {
                emitBlock(stmt.stmtBlock(), layout);
            }
        }

        private void emitJump(MandrillParser.JumpStmtContext ctx, FunctionLayout layout) {
            if (ctx.Return() != null) {
                emitExpression(ctx.expression(), layout);
                out.add(TacItem.line("ret", 0));
            }
        }

        private void emitCondition(MandrillParser.ConditionStatementContext ctx, FunctionLayout layout) {
            String thenLabel = labels.next("if_then");
            String elseLabel = labels.next("if_else");
            String endLabel = labels.next("if_end");
            out.add(TacItem.lineRef("dstore", elseLabel));
            out.add(TacItem.lineRef("dstore", thenLabel));
            emitExpression(ctx.expr, layout);
            out.add(TacItem.line("eval", Constants.EVAL_CONDITION));
            out.add(TacItem.label(thenLabel));
            emitBlock(ctx.thenStatement, layout);
            out.add(TacItem.lineRef("jump", endLabel));
            out.add(TacItem.label(elseLabel));
            if (ctx.elseStatement != null) {
                emitBlock(ctx.elseStatement, layout);
            }
            out.add(TacItem.label(endLabel));
        }

        private void emitLoop(MandrillParser.LoopStatementContext ctx, FunctionLayout layout) {
            String beginLabel = labels.next("while_begin");
            String bodyLabel = labels.next("while_body");
            String endLabel = labels.next("while_end");
            out.add(TacItem.label(beginLabel));
            out.add(TacItem.lineRef("dstore", endLabel));
            out.add(TacItem.lineRef("dstore", bodyLabel));
            emitExpression(ctx.expr, layout);
            out.add(TacItem.line("eval", Constants.EVAL_CONDITION));
            out.add(TacItem.label(bodyLabel));
            emitBlock(ctx.stmtBlock(), layout);
            out.add(TacItem.lineRef("jump", beginLabel));
            out.add(TacItem.label(endLabel));
        }

        private void emitAssign(MandrillParser.AssignStatementContext ctx, FunctionLayout layout) {
            if (ctx.lvalue() instanceof MandrillParser.PrintIntegerContext) {
                if (isStringLike(ctx.rvalue().expression())) {
                    emitExpression(ctx.rvalue().expression(), layout);
                    out.add(TacItem.line("puts", 0));
                    return;
                }
                emitExpression(ctx.rvalue().expression(), layout);
                out.add(TacItem.line("puti", 0));
                return;
            }
            if (ctx.lvalue() instanceof MandrillParser.PrintCharContext) {
                emitExpression(ctx.rvalue().expression(), layout);
                out.add(TacItem.line("putc", 0));
                return;
            }

            if (ctx.lvalue() == null) {
                String name = ctx.Identifier().getText();
                if (ctx.rvalue().expression() instanceof MandrillParser.StringLiteralContext stringLiteral) {
                    emitStringLiteral(stringLiteral.getText());
                    storeVariable(name, layout, true);
                    return;
                }
                emitExpression(ctx.rvalue().expression(), layout);
                storeVariable(name, layout, true);
                return;
            }

            MandrillParser.TargetVariableContext target = (MandrillParser.TargetVariableContext) ctx.lvalue();
            if (target.expression() == null) {
                emitExpression(ctx.rvalue().expression(), layout);
                storeVariable(target.Identifier().getText(), layout, false);
                return;
            }

            emitExpression(ctx.rvalue().expression(), layout);
            emitArrayAddress(target.Identifier().getText(), target.expression(), layout);
            out.add(TacItem.line("dawrite", 0));
        }

        private void emitExpression(MandrillParser.ExpressionContext expr, FunctionLayout layout) {
            if (expr instanceof MandrillParser.IntLiteralContext intLiteral) {
                out.add(TacItem.line("dstore", Long.parseLong(intLiteral.getText())));
                return;
            }
            if (expr instanceof MandrillParser.CharLiteralContext charLiteral) {
                out.add(TacItem.line("dstore", parseChar(charLiteral.getText())));
                return;
            }
            if (expr instanceof MandrillParser.StringLiteralContext stringLiteral) {
                emitStringLiteral(stringLiteral.getText());
                return;
            }
            if (expr instanceof MandrillParser.InputIntContext) {
                out.add(TacItem.line("geti", 0));
                return;
            }
            if (expr instanceof MandrillParser.InputChatContext) {
                out.add(TacItem.line("getc", 0));
                return;
            }
            if (expr instanceof MandrillParser.SubgroupExpressionContext subgroup) {
                emitExpression(subgroup.expression(), layout);
                return;
            }
            if (expr instanceof MandrillParser.SourceVariableContext source) {
                if (source.expression() != null) {
                    emitArrayLoad(source.Identifier().getText(), source.expression(), layout);
                } else {
                    loadVariable(source.Identifier().getText(), layout);
                }
                return;
            }
            if (expr instanceof MandrillParser.FunctionCallContext call) {
                emitFunctionCall(call, layout);
                return;
            }
            if (expr instanceof MandrillParser.MulDivModExpressionContext bin) {
                emitExpression(bin.expression(0), layout);
                emitExpression(bin.expression(1), layout);
                out.add(TacItem.line("eval", evalOp(bin.op.getText())));
                return;
            }
            if (expr instanceof MandrillParser.AddSubExpressionContext bin) {
                emitExpression(bin.expression(0), layout);
                emitExpression(bin.expression(1), layout);
                out.add(TacItem.line("eval", evalOp(bin.op.getText())));
                return;
            }
            if (expr instanceof MandrillParser.ComparingExpressionContext bin) {
                emitExpression(bin.expression(0), layout);
                emitExpression(bin.expression(1), layout);
                out.add(TacItem.line("eval", evalOp(bin.op.getText())));
                return;
            }
            if (expr instanceof MandrillParser.EqualityExpressionContext bin) {
                emitExpression(bin.expression(0), layout);
                emitExpression(bin.expression(1), layout);
                out.add(TacItem.line("eval", evalOp(bin.op.getText())));
            }
        }

        private void emitFunctionCall(MandrillParser.FunctionCallContext call, FunctionLayout callerLayout) {
            List<MandrillParser.ExpressionContext> args = call.argumentList() == null ? List.of() : call.argumentList().expression();
            for (int i = args.size() - 1; i >= 0; i--) {
                emitExpression(args.get(i), callerLayout);
            }
            FunctionLayout callee = collector.functions.get(call.Identifier().getText());
            out.add(TacItem.line("dstore", callee.localBytes()));
            out.add(TacItem.lineRef("jal", functionLabel(call.Identifier().getText())));
        }

        // TODO: 目前仅支持一维数组，可以尝试扩展为支持多维数组的方式
        // 计算数组地址方法：使用龙书式地址计算
        private void emitArrayAddress(String name, MandrillParser.ExpressionContext indexExpr, FunctionLayout layout) {
            emitExpression(indexExpr, layout);
            out.add(TacItem.line("dstore", 4));
            out.add(TacItem.line("eval", Constants.EVAL_MUL));
            loadVariable(name, layout);
            out.add(TacItem.line("eval", Constants.EVAL_ADD));
        }

        private void emitArrayLoad(String name, MandrillParser.ExpressionContext indexExpr, FunctionLayout layout) {
            emitArrayAddress(name, indexExpr, layout);
            out.add(TacItem.line("daload", 0));
        }

        private void emitStringLiteral(String literal) {
            String body = unescapeString(literal.substring(1, literal.length() - 1));
            int[] codePoints = body.codePoints().toArray();
            int bytes = (codePoints.length + 1) * 4;
            out.add(TacItem.line("dstore", bytes));
            out.add(TacItem.line("malloc", 0));
            out.add(TacItem.line("dwrite", collector.tempStringIndex()));
            for (int i = 0; i < codePoints.length; i++) {
                out.add(TacItem.line("dstore", codePoints[i]));
                out.add(TacItem.line("dload", collector.tempStringIndex()));
                out.add(TacItem.line("dstore", i * 4L));
                out.add(TacItem.line("eval", Constants.EVAL_ADD));
                out.add(TacItem.line("dawrite", 0));
            }
            out.add(TacItem.line("dload", collector.tempStringIndex()));
        }

        /**
         * 用于决定 print 该走 `puti` 还是 `puts`。
         *
         * 这不是完整类型系统，只是够用的表达式类型推断。
         */
        private boolean isStringLike(MandrillParser.ExpressionContext expr) {
            if (expr == null) {
                return false;
            }
            if (expr instanceof MandrillParser.StringLiteralContext) {
                return true;
            }
            if (expr instanceof MandrillParser.SubgroupExpressionContext subgroup) {
                return isStringLike(subgroup.expression());
            }
            if (expr instanceof MandrillParser.FunctionCallContext call) {
                SymbolTable.FunctionSymbol fn = symbols.getFunction(call.Identifier().getText());
                return fn != null && fn.getReturnType() == MandrillType.ARRAY;
            }
            if (expr instanceof MandrillParser.SourceVariableContext source && source.expression() == null) {
                return symbols.resolve(source, source.Identifier().getText()).getType() == MandrillType.ARRAY;
            }
            return false;
        }

        private void storeVariable(String name, FunctionLayout layout, boolean allowGlobalCreate) {
            if (layout != null && layout.locals.containsKey(name)) {
                out.add(TacItem.line("dlwrite", localIndex(layout, name)));
                return;
            }
            // warning: 需要特别区分变量的定义域，特别是对全局变量而言
            if (layout == null || collector.globals.containsKey(name) || allowGlobalCreate) {
                out.add(TacItem.line("dwrite", globalIndex(name)));
                return;
            }
            out.add(TacItem.line("dwrite", globalIndex(name)));
        }

        private void loadVariable(String name, FunctionLayout layout) {
            if (layout != null && layout.locals.containsKey(name)) {
                out.add(TacItem.line("dlload", localIndex(layout, name)));
                return;
            }
            if (layout == null || collector.globals.containsKey(name)) {
                out.add(TacItem.line("dload", globalIndex(name)));
                return;
            }
            out.add(TacItem.line("dload", globalIndex(name)));
        }

        private int localIndex(FunctionLayout layout, String name) {
            Integer index = layout.locals.get(name);
            if (index == null) {
                index = layout.locals.size();
                layout.locals.put(name, index);
            }
            return index;
        }

        private int globalIndex(String name) {
            Integer index = collector.globals.get(name);
            if (index == null) {
                index = collector.globals.size();
                collector.globals.put(name, index);
            }
            return index;
        }

        private String functionLabel(String name) {
            return "func_" + name;
        }

        private long evalOp(String op) {
            return switch (op) {
                case "+" -> Constants.EVAL_ADD;
                case "-" -> Constants.EVAL_MINUS;
                case "*" -> Constants.EVAL_MUL;
                case "/" -> Constants.EVAL_DIV;
                case "%" -> Constants.EVAL_MOD;
                case ">" -> Constants.EVAL_GREATER;
                case "<" -> Constants.EVAL_LESS;
                case ">=" -> Constants.EVAL_GREATER_OR_EQUAL;
                case "<=" -> Constants.EVAL_LESS_OR_EQUAL;
                case "==" -> Constants.EVAL_EQUAL;
                case "!=" -> Constants.EVAL_NOT_EQUAL;
                default -> throw new IllegalStateException("Unexpected operator: " + op);
            };
        }

        private long parseChar(String text) {
            String body = text.substring(1, text.length() - 1);
            if (body.startsWith("\\") && body.length() >= 2) {
                return switch (body.charAt(1)) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '\\' -> '\\';
                    case '\'' -> '\'';
                    default -> body.charAt(1);
                };
            }
            return body.codePointAt(0);
        }

        private String unescapeString(String text) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\\' && i + 1 < text.length()) {
                    char next = text.charAt(++i);
                    sb.append(switch (next) {
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case '\\' -> '\\';
                        case '"' -> '"';
                        case '\'' -> '\'';
                        default -> next;
                    });
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }

    private static final class AssemblyEmitter {
        private final TacProgram program;

        private AssemblyEmitter(TacProgram program) {
            this.program = program;
        }

        private String emit() {
            return program.render();
        }
    }

    private static final class LabelAllocator {
        private int counter;

        private String next(String prefix) {
            return prefix + "_" + counter++;
        }
    }
}

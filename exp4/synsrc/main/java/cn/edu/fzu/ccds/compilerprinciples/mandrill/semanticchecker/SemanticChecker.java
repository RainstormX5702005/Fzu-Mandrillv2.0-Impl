package cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillBaseVisitor;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

public class SemanticChecker extends MandrillBaseVisitor<MandrillType> {

    private final SymbolTable table;
    private SymbolTable.FunctionSymbol currentFunction;

    public SemanticChecker(SymbolTable table) {
        this.table = table;
    }

    // 统一的报错入口，取出 ctx 的行列号后抛出 SemanticException
    private void error(ParserRuleContext ctx, String message) {
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();
        throw new SemanticException(line, col, message);
    }

    @Override
    public MandrillType visitProgram(MandrillParser.ProgramContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof MandrillParser.FunctionDefContext functionDef) {
                visitFunctionDef(functionDef);
            } else if (ctx.getChild(i) instanceof MandrillParser.StatementContext statement) {
                visitStatement(statement);
            }
        }
        return null;
    }

    // 进入函数体：切换作用域，声明形参，遍历函数体语句
    @Override
    public MandrillType visitFunctionDef(MandrillParser.FunctionDefContext ctx) {
        currentFunction = table.getFunction(ctx.Identifier().getText());
        if (currentFunction == null) {
            error(ctx, "Undefined function: " + ctx.Identifier().getText());
        }

        table.enterScope();
        // 将形参注入局部作用域，供函数体引用
        List<String> names = currentFunction.getParameterNames();
        List<MandrillType> types = currentFunction.getParameterTypes();
        for (int i = 0; i < names.size(); i++) {
            table.declare(ctx.parameterList().parameter(i), names.get(i), types.get(i), false);
        }
        visitStmtBlock(ctx.stmtBlock());
        table.exitScope();
        currentFunction = null;
        return null;
    }

    // 语句块创建一个新的局部作用域
    public MandrillType visitStmtBlock(MandrillParser.StmtBlockContext ctx) {
        table.enterScope();
        for (MandrillParser.StatementContext stmt : ctx.statement()) {
            visitStatement(stmt);
        }
        table.exitScope();
        return null;
    }

    // 语句分发器：根据子节点类型路由到具体访问方法
    public MandrillType visitStatement(MandrillParser.StatementContext ctx) {
        if (ctx.assignStatement() != null)    return visitAssignStatement(ctx.assignStatement());
        if (ctx.loopStatement() != null)      return visitLoopStatement(ctx.loopStatement());
        if (ctx.conditionStatement() != null) return visitConditionStatement(ctx.conditionStatement());
        if (ctx.jumpStmt() != null)           return visitJumpStmt(ctx.jumpStmt());
        if (ctx.declarationStmt() != null)    return visitDeclarationStmt(ctx.declarationStmt());
        if (ctx.stmtBlock() != null)          return visitStmtBlock(ctx.stmtBlock());
        return null;
    }

    // 变量/数组声明语句
    public MandrillType visitDeclarationStmt(MandrillParser.DeclarationStmtContext ctx) {
        String name = ctx.Identifier().getText();
        MandrillType type = ctx.arraySuffix() != null ? MandrillType.ARRAY : MandrillType.INT;
        table.declare(ctx, name, type, ctx.scope.getText().equals("global"));
        return null;
    }

    // return 语句：返回值类型必须与函数签名一致
    public MandrillType visitJumpStmt(MandrillParser.JumpStmtContext ctx) {
        if (ctx.Return() != null) {
            MandrillType type = visit(ctx.expression());
            if (currentFunction != null && type != currentFunction.getReturnType()) {
                error(ctx, "Return type mismatch");
            }
        }
        return null;
    }

    /**
     * 赋值 / print 语句 —— 本项目最复杂的语句类型。
     * 拆成四种情况按序判断：
     *  (1) print 系列            get/put
     *  (2) 数组字面量赋值        a[] = "Hello World"
     *  (3) 下标赋值              a[i] = expr
     *  (4) 普通变量赋值          a = expr
     */
    @Override
    public MandrillType visitAssignStatement(MandrillParser.AssignStatementContext ctx) {
        // (1) print 语句
        if (ctx.lvalue() instanceof MandrillParser.PrintIntegerContext) {
            visit(ctx.rvalue());
            return null;
        }
        if (ctx.lvalue() instanceof MandrillParser.PrintCharContext) {
            MandrillType type = visit(ctx.rvalue());
            if (type != MandrillType.INT) error(ctx, "put requires integer character value");
            return null;
        }

        // (2) 数组字面量赋值：a[] = "Hello World"
        if (ctx.lvalue() == null) {
            String name = ctx.Identifier().getText();
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            MandrillType valueType = resolveRvalueType(ctx.rvalue(), ctx, name, true);
            if (valueType != MandrillType.ARRAY) error(ctx, "Array variable must be assigned an array value");
            return null;
        }

        // (3)(4) 普通 / 下标赋值
        MandrillParser.TargetVariableContext target = (MandrillParser.TargetVariableContext) ctx.lvalue();
        String name = target.Identifier().getText();
        boolean indexed = target.expression() != null;

        // 左值类型解析
        MandrillType targetType = resolveTargetType(ctx, name, indexed, target.expression());

        // 右值类型解析（含 input/字符串字面量的特殊处理）
        MandrillType valueType = resolveRvalueType(ctx.rvalue(), ctx, name, indexed);

        // 类型兼容性检查
        checkAssignCompat(ctx, name, targetType, valueType);
        return null;
    }

    // 循环：循环条件必须是 INT
    public MandrillType visitLoopStatement(MandrillParser.LoopStatementContext ctx) {
        checkCondition(ctx.expression(), "Loop condition");
        visit(ctx.stmtBlock());
        return null;
    }

    // if/if-else：条件必须是 INT
    public MandrillType visitConditionStatement(MandrillParser.ConditionStatementContext ctx) {
        checkCondition(ctx.expression(), "Condition");
        visit(ctx.thenStatement);
        if (ctx.elseStatement != null) visit(ctx.elseStatement);
        return null;
    }

    /**
     * 二元整数运算校验模板 —— 工厂模式的"骨架"。
     * 左右操作数都必须是 INT，任何一个不是就报错。
     * MulDivMod / AddSub / Comparing 三类表达式在调用方先 visit 出左右类型，再传入此方法集中校验。
     */
    private MandrillType checkBinaryIntOp(MandrillType left, MandrillType right,
                                          ParserRuleContext ctx, String opName) {
        if (left != MandrillType.INT || right != MandrillType.INT)
            error(ctx, opName + " operators require integers");
        return MandrillType.INT;
    }

    // 条件检查模板：表达式必须求值为 INT
    private void checkCondition(MandrillParser.ExpressionContext expr, String role) {
        if (visit(expr) != MandrillType.INT) error(expr, role + " must be integer");
    }

    // 收集实参列表中每个表达式的类型
    private List<MandrillType> collectArgTypes(MandrillParser.ArgumentListContext argList) {
        List<MandrillType> types = new ArrayList<>();
        if (argList != null) {
            for (MandrillParser.ExpressionContext e : argList.expression())
                types.add(visit(e));
        }
        return types;
    }

    /**
     * 变量访问统一处理模板。
     * 下标访问与普通变量访问共享同一个控制流：
     *  - 下标访问：下标必须是 INT，数组变量自动创建，元素类型固定为 INT
     *  - 普通访问：根据 create 标志决定是 resolve（右值，必须已声明）还是 resolveOrCreate（左值，允许首次出现）
     *
     * @param create 是否允许自动创建：TargetVariable 用 true，SourceVariable 用 false
     */
    private MandrillType resolveVariable(ParserRuleContext ctx, String name,
                                         MandrillParser.ExpressionContext idx, boolean create) {
        if (idx != null) {
            // 数组下标访问：下标必须是 INT，数组本身允许自动创建
            if (visit(idx) != MandrillType.INT) error(idx, "Array index must be integer");
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.INT; // 数组元素类型固定为 INT
        }
        // 普通变量访问
        if (create) return table.resolveOrCreate(ctx, name, MandrillType.INT).getType();
        return table.resolve(ctx, name).getType();
    }

    // 解析左值类型：区分下标赋值 (ARRAY) 和普通赋值 (INT/ARRAY)
    private MandrillType resolveTargetType(MandrillParser.AssignStatementContext ctx,
                                           String name, boolean indexed,
                                           MandrillParser.ExpressionContext idxExpr) {
        if (indexed) {
            // 下标必须是 INT，数组变量自动创建
            if (visit(idxExpr) != MandrillType.INT) error(idxExpr, "Array index must be integer");
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.ARRAY;
        }
        // 普通变量：允许首次出现，默认按 INT 创建
        return table.resolveOrCreate(ctx, name, MandrillType.INT).getType();
    }

    /**
     * 解析右值类型，处理 input() / 字符串字面量等不通过正常求值就能确定类型的特殊情况。
     * 副作用：当普通变量首次被赋值为字符串字面量时，将其符号表类型更新为 ARRAY。
     */
    private MandrillType resolveRvalueType(MandrillParser.RvalueContext rvalue,
                                           MandrillParser.AssignStatementContext ctx,
                                           String name, boolean indexed) {
        MandrillParser.ExpressionContext expr = rvalue.expression();
        // input 函数族始终返回 INT
        if (expr instanceof MandrillParser.InputIntContext || expr instanceof MandrillParser.InputChatContext)
            return MandrillType.INT;
        // 字符串字面量是 ARRAY，若是首次用于赋值则修正左侧变量类型
        if (expr instanceof MandrillParser.StringLiteralContext) {
            if (!indexed) table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.ARRAY;
        }
        // 一般表达式：递归求值
        return visit(rvalue);
    }

    // 赋值兼容性检查：类型已确定则严格匹配，否则用右值类型推断
    private void checkAssignCompat(MandrillParser.AssignStatementContext ctx, String name,
                                   MandrillType targetType, MandrillType valueType) {
        if (targetType == null) {
            table.resolveOrCreate(ctx, name, valueType);
        } else if (valueType != targetType) {
            error(ctx, "Type mismatch in assignment");
        }
    }

    public MandrillType visitRvalue(MandrillParser.RvalueContext ctx) {
        return visit(ctx.expression());
    }

    // 左值变量访问（赋值目标、下标目标）—— 允许自动创建
    public MandrillType visitTargetVariable(MandrillParser.TargetVariableContext ctx) {
        return resolveVariable(ctx, ctx.Identifier().getText(), ctx.expression(), true);
    }

    // 右值变量访问（表达式中的变量引用）—— 必须已声明
    public MandrillType visitSourceVariable(MandrillParser.SourceVariableContext ctx) {
        return resolveVariable(ctx, ctx.Identifier().getText(), ctx.expression(), false);
    }

    // 字面量
    public MandrillType visitIntLiteral(MandrillParser.IntLiteralContext ctx)      { return MandrillType.INT; }
    public MandrillType visitCharLiteral(MandrillParser.CharLiteralContext ctx)    { return MandrillType.INT; }
    public MandrillType visitStringLiteral(MandrillParser.StringLiteralContext ctx){ return MandrillType.ARRAY; }

    // 输入
    public MandrillType visitInputInt(MandrillParser.InputIntContext ctx)   { return MandrillType.INT; }
    public MandrillType visitInputChat(MandrillParser.InputChatContext ctx) { return MandrillType.INT; }

    // 输出
    public MandrillType visitPrintInteger(MandrillParser.PrintIntegerContext ctx) { return MandrillType.INT; }
    public MandrillType visitPrintChar(MandrillParser.PrintCharContext ctx)       { return MandrillType.INT; }

    // 括号表达式
    public MandrillType visitSubgroupExpression(MandrillParser.SubgroupExpressionContext ctx) {
        return visit(ctx.expression());
    }

    // 算术运算（* / %）：两边必须是 INT
    public MandrillType visitMulDivModExpression(MandrillParser.MulDivModExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Arithmetic");
    }

    // 加减运算（+ -）：两边必须是 INT
    public MandrillType visitAddSubExpression(MandrillParser.AddSubExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Arithmetic");
    }

    // 比较运算（< > <= >=）：两边必须是 INT
    public MandrillType visitComparingExpression(MandrillParser.ComparingExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Comparison");
    }

    // == / != 不要求两边都是 INT，只要求类型相同（支持数组比较等）
    public MandrillType visitEqualityExpression(MandrillParser.EqualityExpressionContext ctx) {
        MandrillType left = visit(ctx.expression(0));
        MandrillType right = visit(ctx.expression(1));
        if (left != right) error(ctx, "Equality operands must have the same type");
        return MandrillType.INT;
    }

    public MandrillType visitFunctionCall(MandrillParser.FunctionCallContext ctx) {
        SymbolTable.FunctionSymbol symbol = table.getFunction(ctx.Identifier().getText());
        if (symbol == null) error(ctx, "Undefined function: " + ctx.Identifier().getText());

        // 收集实参类型
        List<MandrillType> args = collectArgTypes(ctx.argumentList());

        // 参数个数检查
        if (args.size() != symbol.getParameterTypes().size())
            error(ctx, "Argument count mismatch");

        // 逐参数类型检查
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) != symbol.getParameterTypes().get(i))
                error(ctx.argumentList().expression(i), "Argument type mismatch");
        }

        return symbol.getReturnType();
    }
}

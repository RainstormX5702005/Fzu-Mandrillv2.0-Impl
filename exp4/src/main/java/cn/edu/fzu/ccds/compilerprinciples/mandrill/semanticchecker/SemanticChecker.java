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

    @Override
    public MandrillType visitFunctionDef(MandrillParser.FunctionDefContext ctx) {
        currentFunction = table.getFunction(ctx.Identifier().getText());
        if (currentFunction == null) {
            error(ctx, "Undefined function: " + ctx.Identifier().getText());
        }

        table.enterScope();
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

    public MandrillType visitStmtBlock(MandrillParser.StmtBlockContext ctx) {
        table.enterScope();
        for (MandrillParser.StatementContext stmt : ctx.statement()) {
            visitStatement(stmt);
        }
        table.exitScope();
        return null;
    }

    public MandrillType visitStatement(MandrillParser.StatementContext ctx) {
        if (ctx.assignStatement() != null) return visitAssignStatement(ctx.assignStatement());
        if (ctx.loopStatement() != null) return visitLoopStatement(ctx.loopStatement());
        if (ctx.conditionStatement() != null) return visitConditionStatement(ctx.conditionStatement());
        if (ctx.jumpStmt() != null) return visitJumpStmt(ctx.jumpStmt());
        if (ctx.declarationStmt() != null) return visitDeclarationStmt(ctx.declarationStmt());
        if (ctx.stmtBlock() != null) return visitStmtBlock(ctx.stmtBlock());
        return null;
    }

    public MandrillType visitDeclarationStmt(MandrillParser.DeclarationStmtContext ctx) {
        String name = ctx.Identifier().getText();
        MandrillType type = ctx.arraySuffix() != null ? MandrillType.ARRAY : MandrillType.INT;
        table.declare(ctx, name, type, ctx.scope.getText().equals("global"));
        return null;
    }

    public MandrillType visitJumpStmt(MandrillParser.JumpStmtContext ctx) {
        if (ctx.Return() != null) {
            MandrillType type = visit(ctx.expression());
            if (currentFunction != null && type != currentFunction.getReturnType()) {
                error(ctx, "Return type mismatch");
            }
        }
        return null;
    }

    @Override
    public MandrillType visitAssignStatement(MandrillParser.AssignStatementContext ctx) {
        if (ctx.lvalue() instanceof MandrillParser.PrintIntegerContext) {
            visit(ctx.rvalue());
            return null;
        }
        if (ctx.lvalue() instanceof MandrillParser.PrintCharContext) {
            MandrillType type = visit(ctx.rvalue());
            if (type != MandrillType.INT) error(ctx, "put requires integer character value");
            return null;
        }

        if (ctx.lvalue() == null) {
            String name = ctx.Identifier().getText();
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            MandrillType valueType = resolveRvalueType(ctx.rvalue(), ctx, name, true);
            if (valueType != MandrillType.ARRAY) error(ctx, "Array variable must be assigned an array value");
            return null;
        }

        MandrillParser.TargetVariableContext target = (MandrillParser.TargetVariableContext) ctx.lvalue();
        String name = target.Identifier().getText();
        boolean indexed = target.expression() != null;
        MandrillType targetType = resolveTargetType(ctx, name, indexed, target.expression());
        MandrillType valueType = resolveRvalueType(ctx.rvalue(), ctx, name, indexed);
        checkAssignCompat(ctx, name, targetType, valueType);
        return null;
    }

    public MandrillType visitLoopStatement(MandrillParser.LoopStatementContext ctx) {
        checkCondition(ctx.expression(), "Loop condition");
        visit(ctx.stmtBlock());
        return null;
    }

    public MandrillType visitConditionStatement(MandrillParser.ConditionStatementContext ctx) {
        checkCondition(ctx.expression(), "Condition");
        visit(ctx.thenStatement);
        if (ctx.elseStatement != null) visit(ctx.elseStatement);
        return null;
    }

    private MandrillType checkBinaryIntOp(MandrillType left, MandrillType right,
                                          ParserRuleContext ctx, String opName) {
        if (left != MandrillType.INT || right != MandrillType.INT) {
            error(ctx, opName + " operators require integers");
        }
        return MandrillType.INT;
    }

    private void checkCondition(MandrillParser.ExpressionContext expr, String role) {
        if (visit(expr) != MandrillType.INT) error(expr, role + " must be integer");
    }

    private List<MandrillType> collectArgTypes(MandrillParser.ArgumentListContext argList) {
        List<MandrillType> types = new ArrayList<>();
        if (argList != null) {
            for (MandrillParser.ExpressionContext e : argList.expression()) {
                types.add(visit(e));
            }
        }
        return types;
    }

    private MandrillType resolveVariable(ParserRuleContext ctx, String name,
                                         MandrillParser.ExpressionContext idx, boolean create) {
        if (idx != null) {
            if (visit(idx) != MandrillType.INT) error(idx, "Array index must be integer");
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.INT;
        }
        if (create) return table.resolveOrCreate(ctx, name, MandrillType.INT).getType();
        return table.resolve(ctx, name).getType();
    }

    private MandrillType resolveTargetType(MandrillParser.AssignStatementContext ctx,
                                           String name, boolean indexed,
                                           MandrillParser.ExpressionContext idxExpr) {
        if (indexed) {
            if (visit(idxExpr) != MandrillType.INT) error(idxExpr, "Array index must be integer");
            table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.ARRAY;
        }
        return table.resolveOrCreate(ctx, name, MandrillType.INT).getType();
    }

    private MandrillType resolveRvalueType(MandrillParser.RvalueContext rvalue,
                                           MandrillParser.AssignStatementContext ctx,
                                           String name, boolean indexed) {
        MandrillParser.ExpressionContext expr = rvalue.expression();
        if (expr instanceof MandrillParser.InputIntContext || expr instanceof MandrillParser.InputChatContext) {
            return MandrillType.INT;
        }
        if (expr instanceof MandrillParser.StringLiteralContext) {
            if (!indexed) table.resolveOrCreate(ctx, name, MandrillType.ARRAY);
            return MandrillType.ARRAY;
        }
        return visit(rvalue);
    }

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

    public MandrillType visitTargetVariable(MandrillParser.TargetVariableContext ctx) {
        return resolveVariable(ctx, ctx.Identifier().getText(), ctx.expression(), true);
    }

    public MandrillType visitSourceVariable(MandrillParser.SourceVariableContext ctx) {
        return resolveVariable(ctx, ctx.Identifier().getText(), ctx.expression(), false);
    }

    public MandrillType visitIntLiteral(MandrillParser.IntLiteralContext ctx) { return MandrillType.INT; }
    public MandrillType visitCharLiteral(MandrillParser.CharLiteralContext ctx) { return MandrillType.INT; }
    public MandrillType visitStringLiteral(MandrillParser.StringLiteralContext ctx) { return MandrillType.ARRAY; }
    public MandrillType visitInputInt(MandrillParser.InputIntContext ctx) { return MandrillType.INT; }
    public MandrillType visitInputChat(MandrillParser.InputChatContext ctx) { return MandrillType.INT; }
    public MandrillType visitPrintInteger(MandrillParser.PrintIntegerContext ctx) { return MandrillType.INT; }
    public MandrillType visitPrintChar(MandrillParser.PrintCharContext ctx) { return MandrillType.INT; }

    public MandrillType visitSubgroupExpression(MandrillParser.SubgroupExpressionContext ctx) {
        return visit(ctx.expression());
    }

    public MandrillType visitMulDivModExpression(MandrillParser.MulDivModExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Arithmetic");
    }

    public MandrillType visitAddSubExpression(MandrillParser.AddSubExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Arithmetic");
    }

    public MandrillType visitComparingExpression(MandrillParser.ComparingExpressionContext ctx) {
        return checkBinaryIntOp(visit(ctx.expression(0)), visit(ctx.expression(1)), ctx, "Comparison");
    }

    public MandrillType visitEqualityExpression(MandrillParser.EqualityExpressionContext ctx) {
        MandrillType left = visit(ctx.expression(0));
        MandrillType right = visit(ctx.expression(1));
        if (left != right) error(ctx, "Equality operands must have the same type");
        return MandrillType.INT;
    }

    public MandrillType visitFunctionCall(MandrillParser.FunctionCallContext ctx) {
        SymbolTable.FunctionSymbol symbol = table.getFunction(ctx.Identifier().getText());
        if (symbol == null) error(ctx, "Undefined function: " + ctx.Identifier().getText());

        List<MandrillType> args = collectArgTypes(ctx.argumentList());
        if (args.size() != symbol.getParameterTypes().size()) error(ctx, "Argument count mismatch");
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) != symbol.getParameterTypes().get(i)) {
                error(ctx.argumentList().expression(i), "Argument type mismatch");
            }
        }
        return symbol.getReturnType();
    }
}

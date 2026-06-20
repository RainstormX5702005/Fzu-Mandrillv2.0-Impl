// Generated from Mandrill.g4 by ANTLR 4.13.2
package cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MandrillParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MandrillVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MandrillParser#arraySuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraySuffix(MandrillParser.ArraySuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MandrillParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#functionDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDef(MandrillParser.FunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(MandrillParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(MandrillParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#stmtBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmtBlock(MandrillParser.StmtBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MandrillParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#declarationStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationStmt(MandrillParser.DeclarationStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#jumpStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJumpStmt(MandrillParser.JumpStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#emptyStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStmt(MandrillParser.EmptyStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#assignStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStatement(MandrillParser.AssignStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code printInteger}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrintInteger(MandrillParser.PrintIntegerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code printChar}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrintChar(MandrillParser.PrintCharContext ctx);
	/**
	 * Visit a parse tree produced by the {@code targetVariable}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetVariable(MandrillParser.TargetVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#rvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRvalue(MandrillParser.RvalueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mulDivModExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivModExpression(MandrillParser.MulDivModExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inputChat}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputChat(MandrillParser.InputChatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(MandrillParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code addSubExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSubExpression(MandrillParser.AddSubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(MandrillParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLiteral(MandrillParser.IntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sourceVariable}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSourceVariable(MandrillParser.SourceVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code equalityExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(MandrillParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code charLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharLiteral(MandrillParser.CharLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inputInt}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputInt(MandrillParser.InputIntContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subgroupExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubgroupExpression(MandrillParser.SubgroupExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparingExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparingExpression(MandrillParser.ComparingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(MandrillParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStatement(MandrillParser.LoopStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MandrillParser#conditionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionStatement(MandrillParser.ConditionStatementContext ctx);
}
// Generated from Mandrill.g4 by ANTLR 4.13.2
package cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MandrillParser}.
 */
public interface MandrillListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MandrillParser#arraySuffix}.
	 * @param ctx the parse tree
	 */
	void enterArraySuffix(MandrillParser.ArraySuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#arraySuffix}.
	 * @param ctx the parse tree
	 */
	void exitArraySuffix(MandrillParser.ArraySuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MandrillParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MandrillParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#functionDef}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDef(MandrillParser.FunctionDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#functionDef}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDef(MandrillParser.FunctionDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(MandrillParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(MandrillParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(MandrillParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(MandrillParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#stmtBlock}.
	 * @param ctx the parse tree
	 */
	void enterStmtBlock(MandrillParser.StmtBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#stmtBlock}.
	 * @param ctx the parse tree
	 */
	void exitStmtBlock(MandrillParser.StmtBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MandrillParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MandrillParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#declarationStmt}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationStmt(MandrillParser.DeclarationStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#declarationStmt}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationStmt(MandrillParser.DeclarationStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void enterJumpStmt(MandrillParser.JumpStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void exitJumpStmt(MandrillParser.JumpStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#emptyStmt}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStmt(MandrillParser.EmptyStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#emptyStmt}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStmt(MandrillParser.EmptyStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignStatement(MandrillParser.AssignStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignStatement(MandrillParser.AssignStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code printInteger}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterPrintInteger(MandrillParser.PrintIntegerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code printInteger}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitPrintInteger(MandrillParser.PrintIntegerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code printChar}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterPrintChar(MandrillParser.PrintCharContext ctx);
	/**
	 * Exit a parse tree produced by the {@code printChar}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitPrintChar(MandrillParser.PrintCharContext ctx);
	/**
	 * Enter a parse tree produced by the {@code targetVariable}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterTargetVariable(MandrillParser.TargetVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code targetVariable}
	 * labeled alternative in {@link MandrillParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitTargetVariable(MandrillParser.TargetVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#rvalue}.
	 * @param ctx the parse tree
	 */
	void enterRvalue(MandrillParser.RvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#rvalue}.
	 * @param ctx the parse tree
	 */
	void exitRvalue(MandrillParser.RvalueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mulDivModExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMulDivModExpression(MandrillParser.MulDivModExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mulDivModExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMulDivModExpression(MandrillParser.MulDivModExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inputChat}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInputChat(MandrillParser.InputChatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inputChat}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInputChat(MandrillParser.InputChatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(MandrillParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(MandrillParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addSubExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAddSubExpression(MandrillParser.AddSubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addSubExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAddSubExpression(MandrillParser.AddSubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(MandrillParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(MandrillParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(MandrillParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(MandrillParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sourceVariable}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSourceVariable(MandrillParser.SourceVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sourceVariable}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSourceVariable(MandrillParser.SourceVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalityExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(MandrillParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalityExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(MandrillParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code charLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCharLiteral(MandrillParser.CharLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code charLiteral}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCharLiteral(MandrillParser.CharLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inputInt}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInputInt(MandrillParser.InputIntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inputInt}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInputInt(MandrillParser.InputIntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subgroupExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubgroupExpression(MandrillParser.SubgroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subgroupExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubgroupExpression(MandrillParser.SubgroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparingExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparingExpression(MandrillParser.ComparingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparingExpression}
	 * labeled alternative in {@link MandrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparingExpression(MandrillParser.ComparingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(MandrillParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(MandrillParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoopStatement(MandrillParser.LoopStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoopStatement(MandrillParser.LoopStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MandrillParser#conditionStatement}.
	 * @param ctx the parse tree
	 */
	void enterConditionStatement(MandrillParser.ConditionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MandrillParser#conditionStatement}.
	 * @param ctx the parse tree
	 */
	void exitConditionStatement(MandrillParser.ConditionStatementContext ctx);
}
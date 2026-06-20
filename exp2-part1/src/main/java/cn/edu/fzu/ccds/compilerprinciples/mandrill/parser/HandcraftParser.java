package cn.edu.fzu.ccds.compilerprinciples.mandrill.parser;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens.Token;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens.TokenType;
import java.util.List;

public class HandcraftParser implements Parser {
    private final List<Token> tokens;
    private boolean hasError = false;
    private int curr = 0;    

    // 基于 LL(1) 的递归下降分析程序实现

    private Token getPreviousToken() {
        return tokens.get(curr - 1);
    }

    private Token getCurrToken() {
        return tokens.get(curr);
    }

    private boolean isEnd() {
        return getCurrToken().type == TokenType.EOF; 
    }

    public Token advanceToken() {
        if(!isEnd()) curr++;
        return getPreviousToken();
    }

    // 辅助方法：检查类型匹配的函数
    // highlight：与 isTokenMatched 的区别在于这个辅助方法可以识别一个对应的类型，而 isTokenMatched 检查类型匹配后会向前移动
    private boolean check(TokenType type) {
        return !isEnd() && getCurrToken().type == type;
    }

    // 检查输入的 Token 是否是预期得到的类型
    private boolean isTokenMatched(TokenType... types) {
        for (TokenType type : types) {
            if(check(type)) {
                advanceToken();
                return true;
            }
        }
        return false;
    }

    // highlight: 目前这个函数只是作为设置错误位置的工具，暂时没用
    private void handleError(String message) {
        hasError = true;
        if (message != null && message.isEmpty()) {
            hasError = true;
        }
    }

    private void parseFirstStmt() {
        if (check(TokenType.INT_CONST)
                || check(TokenType.CHAR_CONST)
                || check(TokenType.STRING_CONST)
                || check(TokenType.IDENTIFIER)
                || check(TokenType.READ)
                || check(TokenType.GET)) {
            advanceToken();
            return;
        }

        // 专门处理 (expr) 的情况
        if (check(TokenType.LPAREN)) {
            advanceToken();
            parseExprs();
            if (!isTokenMatched(TokenType.RPAREN)) {
                handleError("ERROR");
            }
            return;
        }

        handleError("ERROR");
    }

    // 专门解析参数列表
    private void parseArrFuncAugs() {
        parseFirstStmt();
        while (!hasError) {
            if (check(TokenType.LBRACKET)) {
                advanceToken();
                if (check(TokenType.RBRACKET)) {
                    handleError("ERROR");
                    return;
                }
                parseExprs();
                if (!isTokenMatched(TokenType.RBRACKET)) {
                    handleError("ERROR");
                    return;
                }
                continue;
            }

            if (check(TokenType.LPAREN)) {
                advanceToken();
                if (!check(TokenType.RPAREN)) {
                    parseExprs();
                    while (check(TokenType.COMMA)) {
                        advanceToken();
                        parseExprs();
                    }
                }
                if (!isTokenMatched(TokenType.RPAREN)) {
                    handleError("ERROR");
                    return;
                }
                continue;
            }

            break;
        }
    }

    private void parseSecOrderCal() {
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            advanceToken();
        }
        parseArrFuncAugs();
        
        // 检测 * / % 运算符
        while (check(TokenType.STAR) || check(TokenType.SLASH) || check(TokenType.MOD)) {
            advanceToken();
            while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
                advanceToken();
            }
            parseExprs();
        }
    }

    private void parseAdditive() {
        parseSecOrderCal();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            advanceToken();
            parseSecOrderCal();
        }
    }

    private void parseExprs() {
        parseCmps();
        while (check(TokenType.EQ) || check(TokenType.NEQ)) {
            advanceToken();
            parseCmps();
        }
    }

    private void parseCmps() {
        parseAdditive();
        while (check(TokenType.LT)
                || check(TokenType.LTE)
                || check(TokenType.GT)
                || check(TokenType.GTE)) {
            advanceToken();
            parseAdditive();
        }
    }

    // 专门分析块 {}，只有封闭才算数
    private void parseBlock() {
        if (!isTokenMatched(TokenType.LBRACE)) {
            handleError("ERROR");
            return;
        }
        while (!isEnd() && getCurrToken().type != TokenType.RBRACE) {
            handleStmts();
            if (hasError) {
                return;
            }
        }
        if (!isTokenMatched(TokenType.RBRACE)) {
            handleError("ERROR");
        }
    }

    // 以下是实际的语法分析函数实现
    private void handleStmts() {

        // if
        if (check(TokenType.IF)) {
            advanceToken();

            if (!isTokenMatched(TokenType.LPAREN)) {
                handleError("ERROR");
                return;
            }
            parseExprs();
            if (hasError) {
                return;
            }
            if (!isTokenMatched(TokenType.RPAREN)) {
                handleError("ERROR");
                return;
            }
            parseBlock();
            if (hasError) {
                return;
            }

            if (check(TokenType.ELSE)) {
                advanceToken();
                parseBlock();
                if (hasError) {
                    return;
                }
            }
            return;
        }

        if (check(TokenType.WHILE)) {
            advanceToken();

            if (!isTokenMatched(TokenType.LPAREN)) {
                handleError("ERROR");
                return;
            }
            // 展开 while(cond) {expr} 中的 cond 分析
            parseExprs();
            if (hasError) {
                return;
            }
            if (!isTokenMatched(TokenType.RPAREN)) {
                handleError("ERROR");
                return;
            }
            // 展开 while(cond) {expr} 中的 {expr} 分析
            parseBlock();
            if (hasError) {
                return;
            }

            return;
        }

        if (check(TokenType.FUNC)) {
            advanceToken();

            if (!isTokenMatched(TokenType.IDENTIFIER)) {
                handleError("ERROR");
                return;
            }
            if (!isTokenMatched(TokenType.LPAREN)) {
                handleError("ERROR");
                return;
            }
            if (!check(TokenType.RPAREN)) {
                if (!isTokenMatched(TokenType.IDENTIFIER)) {
                    handleError("ERROR");
                    return;
                }
                while (check(TokenType.COMMA)) {
                    advanceToken();
                    if (!isTokenMatched(TokenType.IDENTIFIER)) {
                        handleError("ERROR");
                        return;
                    }
                }
            }
            if (!isTokenMatched(TokenType.RPAREN)) {
                handleError("ERROR");
                return;
            }
            parseBlock();
            if (hasError) {
                return;
            }

            return;
        }

        if (check(TokenType.RETURN)) {
            advanceToken();

            if (!check(TokenType.SEMI)) {
                parseExprs();
                if (hasError) {
                    return;
                }
            }
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
                return;
            }

            return;
        }

        if (check(TokenType.BREAK)) {
            advanceToken();
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
            }
            return;
        }

        if (check(TokenType.CONTINUE)) {
            advanceToken();
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
            }
            return;
        }

        if (check(TokenType.WRITE) || check(TokenType.PUT)) {
            advanceToken();

            if (!isTokenMatched(TokenType.ASSIGN)) {
                handleError("ERROR");
                return;
            }
            parseExprs();
            if (hasError) {
                return;
            }
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
                return;
            }
            return;
        }

        if (check(TokenType.GLOBAL) || check(TokenType.LOCAL)) {
            advanceToken();
            if (!isTokenMatched(TokenType.IDENTIFIER)) {
                handleError("ERROR");
                return;
            }
            while (check(TokenType.COMMA)) {
                advanceToken();
                if (!isTokenMatched(TokenType.IDENTIFIER)) {
                    handleError("ERROR");
                    return;
                }
            }
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
            }
            return;
        }

        if (check(TokenType.IDENTIFIER)) {
            advanceToken();

            if (check(TokenType.LPAREN)) {
                advanceToken();
                if (!check(TokenType.RPAREN)) {
                    parseExprs();
                    while (check(TokenType.COMMA)) {
                        advanceToken();
                        parseExprs();
                    }
                }
                if (!isTokenMatched(TokenType.RPAREN)) {
                    handleError("ERROR");
                    return;
                }
                if (!isTokenMatched(TokenType.SEMI)) {
                    handleError("ERROR");
                    return;
                }

                return;
            }

            // ( ) 的检查
            boolean emptyIndex = false;
            if (check(TokenType.LBRACKET)) {
                advanceToken();
                if (check(TokenType.RBRACKET)) {
                    emptyIndex = true;
                    advanceToken();
                } else {
                    parseExprs();
                    if (!isTokenMatched(TokenType.RBRACKET)) {
                        handleError("ERROR");
                        return;
                    }
                }
            }

            // =
            if (!isTokenMatched(TokenType.ASSIGN)) {
                handleError("ERROR");
                return;
            }

            // 不到分号之前都应该被认为是表达式的一部分。
            if (emptyIndex) {
                // warning: 需要专门处理字符串类型，否则可能出错
                if (!isTokenMatched(TokenType.STRING_CONST)) {
                    handleError("ERROR");
                    return;
                }
            } else {
                parseExprs();
                if (hasError) {
                    return;
                }
            }
            if (!isTokenMatched(TokenType.SEMI)) {
                handleError("ERROR");
            }
            return;
        }

        handleError("ERROR");
    }

    public HandcraftParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean parse() {
        while(!isEnd() && !hasError) {
            handleStmts();
        }
        return !hasError && isEnd();
    }
}

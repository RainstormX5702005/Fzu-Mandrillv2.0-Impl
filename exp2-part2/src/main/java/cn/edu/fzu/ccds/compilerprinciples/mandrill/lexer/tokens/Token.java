package cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("Ln %-3d, Col %-3d | %-15s '%s'", line, column, type, lexeme);
    }
}

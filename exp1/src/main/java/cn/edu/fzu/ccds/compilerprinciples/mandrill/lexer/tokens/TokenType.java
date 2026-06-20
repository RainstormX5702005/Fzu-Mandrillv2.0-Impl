package cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens;

public enum TokenType {
    // 关键字
    IF, ELSE, WHILE, READ, PUT, WRITE, GET,
    FUNC, GLOBAL, LOCAL, RETURN, BREAK, CONTINUE,

    // 字面量与标识符
    IDENTIFIER, INT_CONST, CHAR_CONST, STRING_CONST,

    // 运算符
    PLUS, MINUS, STAR, SLASH, MOD,       // + - * / %
    ASSIGN, EQ, NEQ,                     // = == !=
    LT, LTE, GT, GTE,                    // < <= > >=

    // 界符
    LPAREN, RPAREN,                      // ( )
    LBRACKET, RBRACKET,                  // [ ]
    LBRACE, RBRACE,                      // { }
    COMMA, SEMI,                         // , ;

    EOF, ERROR
}

package cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens.Token;
import cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.tokens.TokenType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandcraftLexer {
    // 核心 IO 组件
    private final PushbackReader reader;

    // 状态追踪
    private final List<Token> tokens = new ArrayList<>();

    // 关键字表：先把保留字统一映射成对应的 TokenType，后面识别标识符时直接查表即可
    private final Map<String, TokenType> keywords = new HashMap<>();

    private void initKeywords() {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("read", TokenType.READ);
        keywords.put("write", TokenType.WRITE);
        keywords.put("get", TokenType.GET);
        keywords.put("func", TokenType.FUNC);
        keywords.put("global", TokenType.GLOBAL);
        keywords.put("local", TokenType.LOCAL);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("put", TokenType.PUT);
    }
    
    public List<Token> scanTokens() {
        // 每次扫描前先清掉旧结果，避免复用同一个 lexer 实例时混入上一次的 token
        tokens.clear();
        keywords.clear();
        initKeywords();

        int line = 1;
        int column = 0;

        try {
            int ch;
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;

                // 换行单独处理，列号回到 0
                if (c == '\n') {
                    line++;
                    column = 0;
                    continue;
                }

                column++;
                int tokenColumn = column;
                int nextCh;
                StringBuilder builder;

                switch (c) {
                    // 直接跳过空白字符
                    case ' ':
                    case '\t':
                    case '\r':
                        break;

                    // 字符常量：支持普通字符和转义字符两种情况
                    case '\'': {
                        int first = reader.read();
                        if (first == -1) {
                            tokens.add(new Token(TokenType.ERROR, "'", line, tokenColumn));
                            break;
                        }
                        column++;

                        String lexeme;
                        if (first == '\\') {
                            int escaped = reader.read();
                            if (escaped == -1) {
                                tokens.add(new Token(TokenType.ERROR, "\\", line, tokenColumn));
                                break;
                            }
                            column++;
                            lexeme = "\\" + (char) escaped;
                        } else {
                            lexeme = String.valueOf((char) first);
                        }

                        int closing = reader.read();
                        if (closing == -1) {
                            tokens.add(new Token(TokenType.ERROR, lexeme, line, tokenColumn));
                            break;
                        }
                        column++;

                        if (closing != '\'') {
                            reader.unread(closing);
                            column--;
                            tokens.add(new Token(TokenType.ERROR, lexeme, line, tokenColumn));
                            break;
                        }

                        tokens.add(new Token(TokenType.CHAR_CONST, lexeme, line, tokenColumn));
                        break;
                    }

                    case '(':
                        tokens.add(new Token(TokenType.LPAREN, "(", line, tokenColumn));
                        break;
                    case ')':
                        tokens.add(new Token(TokenType.RPAREN, ")", line, tokenColumn));
                        break;
                    case '[':
                        tokens.add(new Token(TokenType.LBRACKET, "[", line, tokenColumn));
                        break;
                    case ']':
                        tokens.add(new Token(TokenType.RBRACKET, "]", line, tokenColumn));
                        break;
                    case '{':
                        tokens.add(new Token(TokenType.LBRACE, "{", line, tokenColumn));
                        break;
                    case '}':
                        tokens.add(new Token(TokenType.RBRACE, "}", line, tokenColumn));
                        break;
                    case ',':
                        tokens.add(new Token(TokenType.COMMA, ",", line, tokenColumn));
                        break;
                    case ';':
                        tokens.add(new Token(TokenType.SEMI, ";", line, tokenColumn));
                        break;
                    case '+':
                        tokens.add(new Token(TokenType.PLUS, "+", line, tokenColumn));
                        break;
                    case '-':
                        tokens.add(new Token(TokenType.MINUS, "-", line, tokenColumn));
                        break;
                    case '*':
                        tokens.add(new Token(TokenType.STAR, "*", line, tokenColumn));
                        break;
                    case '/':
                        // 这里先按普通除号处理；如果后面需要支持注释，可以在这里扩展
                        tokens.add(new Token(TokenType.SLASH, "/", line, tokenColumn));
                        break;
                    case '%':
                        tokens.add(new Token(TokenType.MOD, "%", line, tokenColumn));
                        break;

                    // 处理 !=
                    case '!':
                        nextCh = reader.read();
                        if (nextCh == '=') {
                            column++;
                            tokens.add(new Token(TokenType.NEQ, "!=", line, tokenColumn));
                        } else {
                            if (nextCh != -1) {
                                reader.unread(nextCh);
                            }
                            tokens.add(new Token(TokenType.ERROR, "!", line, tokenColumn));
                        }
                        break;

                    // 处理 = / ==
                    case '=':
                        nextCh = reader.read();
                        if (nextCh == '=') {
                            column++;
                            tokens.add(new Token(TokenType.EQ, "==", line, tokenColumn));
                        } else {
                            if (nextCh != -1) {
                                reader.unread(nextCh);
                            }
                            tokens.add(new Token(TokenType.ASSIGN, "=", line, tokenColumn));
                        }
                        break;

                    // 处理 < / <=
                    case '<':
                        nextCh = reader.read();
                        if (nextCh == '=') {
                            column++;
                            tokens.add(new Token(TokenType.LTE, "<=", line, tokenColumn));
                        } else {
                            if (nextCh != -1) {
                                reader.unread(nextCh);
                            }
                            tokens.add(new Token(TokenType.LT, "<", line, tokenColumn));
                        }
                        break;

                    // 处理 > / >=
                    case '>':
                        nextCh = reader.read();
                        if (nextCh == '=') {
                            column++;
                            tokens.add(new Token(TokenType.GTE, ">=", line, tokenColumn));
                        } else {
                            if (nextCh != -1) {
                                reader.unread(nextCh);
                            }
                            tokens.add(new Token(TokenType.GT, ">", line, tokenColumn));
                        }
                        break;

                    // 字符串常量：读到下一个未转义的双引号为止
                    case '"':
                        builder = new StringBuilder();
                        while (true) {
                            nextCh = reader.read();
                            if (nextCh == -1) {
                                tokens.add(new Token(TokenType.ERROR, builder.toString(), line, tokenColumn));
                                break;
                            }

                            char nextChar = (char) nextCh;
                            if (nextChar == '\n') {
                                line++;
                                column = 0;
                                tokens.add(new Token(TokenType.ERROR, builder.toString(), line, tokenColumn));
                                break;
                            }

                            column++;
                            if (nextChar == '"') {
                                tokens.add(new Token(TokenType.STRING_CONST, builder.toString(), line, tokenColumn));
                                break;
                            }

                            builder.append(nextChar);
                        }
                        break;

                    default: {
                        // 标识符和关键字：以字母或下划线开头，后续可跟字母、数字、下划线
                        if (Character.isLetter(c) || c == '_') {
                            builder = new StringBuilder();
                            builder.append(c);

                            while (true) {
                                nextCh = reader.read();
                                if (nextCh == -1) {
                                    break;
                                }

                                char nextChar = (char) nextCh;
                                if (Character.isLetterOrDigit(nextChar) || nextChar == '_') {
                                    builder.append(nextChar);
                                    column++;
                                } else {
                                    reader.unread(nextCh);
                                    break;
                                }
                            }

                            String text = builder.toString();
                            TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
                            tokens.add(new Token(type, text, line, tokenColumn));
                            break;
                        }

                        // 整数字面量：这里只做最基础的十进制整数扫描
                        if (Character.isDigit(c)) {
                            builder = new StringBuilder();
                            builder.append(c);

                            while (true) {
                                nextCh = reader.read();
                                if (nextCh == -1) {
                                    break;
                                }

                                char nextChar = (char) nextCh;
                                if (Character.isDigit(nextChar)) {
                                    builder.append(nextChar);
                                    column++;
                                } else {
                                    reader.unread(nextCh);
                                    break;
                                }
                            }

                            tokens.add(new Token(TokenType.INT_CONST, builder.toString(), line, tokenColumn));
                            break;
                        }

                        // 其余字符一律视为非法字符
                        tokens.add(new Token(TokenType.ERROR, String.valueOf(c), line, tokenColumn));
                        break;
                    }
                }
            }

            // 追加 EOF，方便后续 parser 判断结束
            tokens.add(new Token(TokenType.EOF, "<EOF>", line, column + 1));
        } catch (IOException e) {
            return tokens;
        }

        return tokens;
    }

    public HandcraftLexer(InputStream is) {
        this.reader = new PushbackReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws IOException {
        InputStream inputStream = args.length > 0 && !args[0].equals("-") ? new FileInputStream(args[0]) : System.in;
        PrintStream printStream = args.length > 1 && !args[1].equals("-") ? new PrintStream(args[1]) : System.out;
        try (inputStream; printStream) {
            HandcraftLexer lexer = new HandcraftLexer(inputStream);
            List<Token> tokens = lexer.scanTokens();

            for (Token token : tokens) {
                printStream.println(token);
            }
        }
    }
}

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
   
    private final PushbackReader reader;
    private final List<Token> tokens = new ArrayList<>();
    private final Map<String, TokenType> keywords = new HashMap<>();

    // 使用哈希表构造关键字表，便于后续识别
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
        // 需要实现的核心函数

        // 清空之前的调用结果，以免对后续造成使用障碍
        tokens.clear();
        keywords.clear();
        initKeywords();

        int line = 1;
        int col = 0;

        try {
            int ch;
            while((ch = reader.read()) != -1) {
                char c = (char) ch;
                if(c == '\n') {
                    line++;
                    col = 0;
                    continue;
                }

                col++;
                int token = col;
                int nextCh;
                StringBuilder sb;
                switch(c) {
                    // 过滤掉空白字符
                    case ' ':
                    case '\t':
                    case '\r':
                        break;

                    case '\'' : {
                        // warning: 针对转义字符的处理，需要考虑 '\' 后携带的字符
                        int ch1 = reader.read();
                        if(ch1 == -1) {
                            tokens.add(new Token(TokenType.ERROR, "'", line, token));
                            break;
                        }
                        col++;

                        String lexeme;
                        if(ch1 == '\\') {
                            int esc = reader.read();
                            if(esc == -1) {
                                tokens.add(new Token(TokenType.ERROR, "\\", line, token));
                                break;
                            }
                            col++;
                            lexeme = "\\" + (char) esc;
                        } else {
                            lexeme = String.valueOf((char) ch1);
                        }

                        int closing = reader.read();
                        if(closing == -1) {
                            tokens.add(new Token(TokenType.ERROR, lexeme, line, token));
                            break;
                        }
                        col++;

                        if(closing != '\'') {
                            reader.unread(closing);
                            col--;
                            tokens.add(new Token(TokenType.ERROR, lexeme, line, token));
                            break;
                        }

                        tokens.add(new Token(TokenType.CHAR_CONST, lexeme, line, token));
                        break;
                    }

                    case '(' : 
                        tokens.add(new Token(TokenType.LPAREN, "(", line, token));
                        break;
                    case ')' : 
                        tokens.add(new Token(TokenType.RPAREN, ")", line, token));
                        break;
                    case '[' : 
                        tokens.add(new Token(TokenType.LBRACKET, "[", line, token));
                        break;
                    case ']' : 
                        tokens.add(new Token(TokenType.RBRACKET, "]", line, token));
                        break;
                    case '{' : 
                        tokens.add(new Token(TokenType.LBRACE, "{", line, token));
                        break;
                    case '}' : 
                        tokens.add(new Token(TokenType.RBRACE, "}", line, token));
                        break;
                    case ',' : 
                        tokens.add(new Token(TokenType.COMMA, ",", line, token));
                        break;
                    case ';' : 
                        tokens.add(new Token(TokenType.SEMI, ";", line, token));
                        break;
                    case '+' : 
                        tokens.add(new Token(TokenType.PLUS, "+", line, token));
                        break;
                    case '-' : 
                        tokens.add(new Token(TokenType.MINUS, "-", line, token));
                        break;
                    case '*' : 
                        tokens.add(new Token(TokenType.STAR, "*", line, token));
                        break;
                    case '/' : 
                        tokens.add(new Token(TokenType.SLASH, "/", line, token));
                        break;
                    case '%' : 
                        tokens.add(new Token(TokenType.MOD, "%", line, token));
                        break;
                    case '!':
                        nextCh = reader.read();
                        if(nextCh == '=') {
                            col++;
                            tokens.add(new Token(TokenType.NEQ, "!=", line, token));
                        } else {
                            if(nextCh != -1) reader.unread(nextCh);
                            tokens.add(new Token(TokenType.ERROR, "!", line, token));
                        }
                        break;
                    case '=' :
                        // warning: 需要处理 '=' 和 '=='
                        nextCh = reader.read();
                        if(nextCh == '=') {
                            col++;
                            tokens.add(new Token(TokenType.EQ, "==", line, token));
                        } else {
                            if (nextCh != -1) reader.unread(nextCh);
                            tokens.add(new Token(TokenType.ASSIGN, "=", line, token));
                        }
                        break;
                    case '<' : 
                        nextCh = reader.read();
                        if(nextCh == '=') {
                            col++;
                            tokens.add(new Token(TokenType.LTE, "<=", line, token));
                        } else {
                            if (nextCh != -1) reader.unread(nextCh);
                            tokens.add(new Token(TokenType.LT, "<", line, token));
                        }
                        break;
                    case '>' :
                        nextCh = reader.read();
                        if(nextCh == '=') {
                            col++;
                            tokens.add(new Token(TokenType.GTE, ">=", line, token));
                        } else {
                            if (nextCh != -1) reader.unread(nextCh);
                            tokens.add(new Token(TokenType.GT, ">", line, token));
                        }
                        break;
                    case '"': 
                        // highlight：此处需要处理字符串，注意字符串的开闭状态和内部需要用的转义字符
                        sb = new StringBuilder();
                        
                        while(true) {
                            nextCh = reader.read();
                            if(nextCh == -1) {
                                tokens.add(new Token(TokenType.ERROR, sb.toString(), line, token));
                                break;
                            }
                            char nextc = (char) nextCh;

                            if(nextc == '\n') {
                                line++;
                                col = 0;
                                tokens.add(new Token(TokenType.ERROR, sb.toString(), line, token));
                                break;
                            }
                            col++;
                            if(nextc == '"') {
                                tokens.add(new Token(TokenType.STRING_CONST, sb.toString(), line, token));
                                break;
                            }

                            sb.append(nextc);
                        }
                        break;
                    default : {
                        // TODO: 这边需要处理关键字、标识符的内容
                        if (Character.isLetter(c) || c == '_') {
                            sb = new StringBuilder();
                            sb.append(c);

                            while (true) {
                                nextCh = reader.read();
                                if (nextCh == -1) {
                                    break;
                                }
                                char nextc = (char) nextCh;
                                if (Character.isLetterOrDigit(nextc) || nextc == '_') {
                                    sb.append(nextc);
                                    ++col;
                                } else {
                                    reader.unread(nextCh);
                                    break;
                                }
                            }

                            String text = sb.toString();
                            TokenType type = keywords.get(text);
                            if (type == null) {
                                type = TokenType.IDENTIFIER;
                            }
                            tokens.add(new Token(type, text, line, token));
                            break;
                        }

                        if (Character.isDigit(c)) {
                            sb = new StringBuilder();
                            sb.append(c);
                            while (true) {
                                nextCh = reader.read();
                                if (nextCh == -1) {
                                    break;
                                }
                                char nextc = (char) nextCh;
                                if (Character.isDigit(nextc)) {
                                    sb.append(nextc);
                                    ++col;
                                } else {
                                    reader.unread(nextCh);
                                    break;
                                }
                            }
                            tokens.add(new Token(TokenType.INT_CONST, sb.toString(), line, token));
                            break;
                        }

                        tokens.add(new Token(TokenType.ERROR, String.valueOf(c), line, token));
                        break;
                    }
                }
            }

            tokens.add(new Token(TokenType.EOF, "<EOF>", line, col + 1));
        } catch (IOException e) {
            return tokens;
        }

        return tokens;
    }

    
    // 这两个函数不要进行改动
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

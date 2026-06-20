package cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker;

public class SemanticException extends RuntimeException {
    public final int line;
    public final int column;

    public SemanticException(int line, int column, String message) {
        super("Line " + line + ", Col " + column + ": " + message);
        this.line = line;
        this.column = column;
    }
}

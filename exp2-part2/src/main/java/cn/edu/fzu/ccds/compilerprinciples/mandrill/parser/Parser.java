package cn.edu.fzu.ccds.compilerprinciples.mandrill.parser;

/**
 * Mandrill 语法分析器接口。
 * 学生需要实现该接口以完成语法分析功能。
 */
public interface Parser {
    /**
     * 对 Token 序列进行语法分析。
     *
     * @return true 表示文法正确；false 表示存在语法错误
     */
    boolean parse();
}

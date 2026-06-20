package cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler;

import java.io.IOException;
import java.io.InputStream;

/**
 * 编译器入口实现。
 *
 * 后端逻辑集中在 `TacCompiler`。
 */
public class CompilerImpl implements Compiler {

    @Override
    public String compile(InputStream inputStream) throws IOException {
        CompileContext context = Compiler.frontend(inputStream);
        return new TacCompiler().compile(context);
    }
}

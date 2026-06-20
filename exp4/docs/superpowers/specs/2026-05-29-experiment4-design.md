# 实验四 设计说明

**目标：** 完成 Mandrill 实验四编译器，使其能把源程序翻译为虚拟机可执行的汇编文本，并通过 `bash main.sh log.txt` 的样例测试。

**架构：** 复用现有 ANTLR 前端，补齐符号表/语义检查实现，再增加 TAC 中间层与汇编生成器。编译流程采用 `Parse Tree -> TAC -> 汇编文本`，输出直接写到 stdout 或文件。符号表、作用域、函数签名与语义约束遵循现有 `synsrc` 实现的思路，并修复迁移时出现的问题。

**技术栈：** Java 17, ANTLR 4.13.2, 现有 Mandrill 虚拟机指令集。

---

### 任务 1：迁移语义检查实现

**文件：**
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SemanticException.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/MandrillType.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SymbolTable.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SymbolCollector.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SemanticChecker.java`
- 修改：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/Compiler.java`

- [ ] **步骤 1：把 `synsrc` 中的语义检查类原样迁移到 `src` 下对应包名。**
- [ ] **步骤 2：修正迁移后 `Compiler.java` 的导入与前端调用。**
- [ ] **步骤 3：确保函数、变量、作用域、类型检查可被编译器前端复用。**
- [ ] **步骤 4：编译确认语义层能参与整体构建。**

### 任务 2：补 TAC 与汇编输出层

**文件：**
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/CompilerImpl.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/Tac.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacBuilder.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacEmitter.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacProgram.java`

- [ ] **步骤 1：定义 TAC 数据结构，覆盖表达式、赋值、跳转、函数调用、返回。**
- [ ] **步骤 2：实现从解析树到 TAC 的生成。**
- [ ] **步骤 3：实现 TAC 到文本汇编的线性输出。**
- [ ] **步骤 4：让 `CompilerMain` 调用新编译器实现。**

### 任务 3：实现目标代码映射

**文件：**
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/codegen/AssemblyEmitter.java`
- 创建：`src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/codegen/LabelAllocator.java`

- [ ] **步骤 1：实现表达式、条件跳转、函数调用、数组访问、I/O 的指令映射。**
- [ ] **步骤 2：补齐地址回填与标签解析。**
- [ ] **步骤 3：保证输出的汇编文本格式与模拟器解析器兼容。**

### 任务 4：验证与回归

**文件：**
- 修改：`src/test/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/simulator/*`（仅在必要时补充测试）

- [ ] **步骤 1：运行 `make` 确保工程编译通过。**
- [ ] **步骤 2：运行 `bash main.sh log.txt` 验证样例输出。**
- [ ] **步骤 3：修复回归问题并重新验证。**

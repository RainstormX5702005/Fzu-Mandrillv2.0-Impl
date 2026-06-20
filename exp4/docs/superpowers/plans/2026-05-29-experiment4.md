# 实验四 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 Mandrill 实验四编译器，使其输出虚拟机可执行的汇编文本并通过仓库测试样例。

**Architecture:** 先迁移符号表与语义检查，再用 TAC 作为中间层组织翻译逻辑，最后由汇编生成器把 TAC 转成一行一条指令的文本。这样前端、IR、后端边界清楚，便于阅读和维护。

**Tech Stack:** Java 17, ANTLR 4.13.2, 现有 Mandrill VM 指令集。

---

### Task 1: Migrate semantic checker support

**Files:**
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SemanticException.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/MandrillType.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SymbolTable.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SymbolCollector.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/semanticchecker/SemanticChecker.java`
- Modify: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/Compiler.java`

- [ ] **Step 1: Copy the semantic checker classes from `synsrc` into `src/main/java/.../semanticchecker/` with the same package names.**
- [ ] **Step 2: Update `Compiler.java` imports so `frontend()` compiles against the migrated classes.**
- [ ] **Step 3: Build once with `make` to confirm the semantic layer is wired in.**

### Task 2: Add TAC model and compiler implementation

**Files:**
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/CompilerImpl.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacProgram.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/Tac.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacBuilder.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/tac/TacEmitter.java`
- Modify: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/CompilerMain.java`

- [ ] **Step 1: Define TAC instructions for literals, loads/stores, arithmetic, branches, labels, function calls, and returns.**
- [ ] **Step 2: Implement AST/parse-tree to TAC generation using the existing ANTLR visitor tree.**
- [ ] **Step 3: Implement TAC serialization to plain text for the next backend stage.**
- [ ] **Step 4: Point `CompilerMain` at the new `CompilerImpl`.**

### Task 3: Implement assembly emission

**Files:**
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/codegen/AssemblyEmitter.java`
- Create: `src/main/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/compiler/codegen/LabelAllocator.java`

- [ ] **Step 1: Map TAC arithmetic, comparisons, jumps, function calls, loads/stores, input/output, malloc, and string handling to the VM instruction text.**
- [ ] **Step 2: Add label/address backpatching so branch targets become concrete instruction addresses.**
- [ ] **Step 3: Ensure the output format matches `AssemblyParser` expectations line-by-line.**

### Task 4: Validate against repository tests

**Files:**
- Modify only if needed: `src/test/java/cn/edu/fzu/ccds/compilerprinciples/mandrill/simulator/*`

- [ ] **Step 1: Run `make` and fix any compile errors.**
- [ ] **Step 2: Run `bash main.sh log.txt` and inspect failures.**
- [ ] **Step 3: Iterate until the provided sample set passes or the remaining gap is clearly isolated.**

# Mandrill v2.0 编译器

> 本项目是编译原理课程四次实验的完整实现，从零构建了 Mandrill v2.0 语言编译器，分四个阶段：词法分析、语法分析、语义检查、目标代码生成。

## 项目简介

[Mandrill v2.0](docs/mandrill-v2-intro.pdf) 是一个类 C 语法的教学命令式语言，支持 `if`/`while` 控制流、自定义函数（含递归）、全局/局部作用域、数组与字符串、整数/字符 I/O 等特性。

编译器采用 **前端手写 + 后端 ANTLR** 的混合架构：
- **阶段一、二**（词法分析 & 语法分析）：纯手写实现
- **阶段三、四**（语义检查 & 代码生成）：基于 ANTLR 4.13.2 生成的分析器之上构建

目标平台是一台 **栈式虚拟机**，指令集包含 18 条 8 字节定长指令，通过操作数栈完成所有计算。

## 项目结构

```
├── 实验一答题/          # 实验1：手写词法分析器
├── exp2-part1/          # 实验2-1：手写递归下降语法分析器（基础）
├── exp2-part2/          # 实验2-2：手写递归下降语法分析器（进阶）
├── exp3/                # 实验3：ANTLR 语义检查器
├── exp4/                # 实验4：完整编译器（含 VM 模拟器）
│   ├── Mandrill.g4              # ANTLR 文法描述
│   ├── src/main/java/.../
│   │   ├── compiler/            # 编译器后端（TacCompiler 等）
│   │   ├── semanticchecker/     # 语义分析（类型检查 + 符号表）
│   │   └── simulator/           # VM 模拟器（指令集实现）
│   ├── mandrill-src/            # 测试用 mandrill 源文件
│   ├── mandrill-in/             # 测试用例输入
│   ├── mandrill-ans/            # 预期输出
│   └── docs/experiment4.md      # 实验4详细说明（VM模型+指令集参考）
└── docs/
    ├── mandrill-v2-intro.pdf                           # Mandrill 语言参考手册
    └── 中文Compilers Principles Techniques and Tools (2nd Edition) .pdf  # 龙书中文版
```

## 实现介绍

### 实验一：词法分析 — 手写词法分析器

**技术路线**：纯手写有限状态自动机，不做 DFA/NFA 的形式化，而是将全部 Token 识别逻辑凝聚在一个 `switch-case` 结构内。

- **入口方法** `scanTokens()`：逐字符读取源文件后按首字符类型分派——数字进入数字字面量分支、字母/下划线进入标识符/关键字分支、运算符进入多字符前瞻分支等
- **前瞻机制**：`PushbackReader` 提供单字符 `read()`/`unread()` 能力，用于处理二义性（如 `=` vs `==`、`!` vs `!=`）
- **关键字识别**：标识符分支识别完毕后查 HashMap 判断是否为保留字
- **输出**：`Token` 流（包含类型 `TokenType`、词素、行列号）

**核心源文件**：
| 文件 | 职责 |
|---|---|
| `HandcraftLexer.java` (~297行) | 主词法分析器，`scanTokens()` 为核心入口 |
| `TokenType.java` | Token 类型枚举（含 ERROR 类型，词法错误时返回） |
| `Token.java` | Token 数据类 |

**对应龙书章节**：§3.3-3.4 (词法单元的识别与词法分析器生成)、§3.7 (从正则到状态转换图的构造)

---

### 实验二：语法分析 — 手写递归下降分析器

**技术路线**：LL(1) 递归下降分析，用 `while` 循环迭代消除左递归，构建完整的 Mandrill 语句和表达式文法。

**表达式优先级链**（6 级方法调用链，每级由低到高）：

```
handleStmts()
  └─ parseExprs()          ← 赋值 (LValue = Expr) 或纯表达式
       └─ parseCmps()       ← 比较运算 (==, !=, <, >, <=, >=)
            └─ parseAdditive()  ← 加减法 (+ -)
                 └─ parseSecOrderCal()  ← 乘除取模 (* / %)
                      └─ parseArrFuncAugs()  ← 数组索引 [ ] + 函数调用 ( )
                           └─ parseFirstStmt()  ← 基本项 (字面量、标识符、read、get、括号)
```

**语句级分派**：`handleStmts()` 是整个程序的入口——按当前 Token 类型，路由到 `if 语句`、`while 循环`、`func 定义`、`return/break/continue`、`print/write/put 输出`、`赋值` 等不同处理分支。

**左递归消除**：每一级使用 `while` 循环：
```java
// parseAdditive() 中的模式（所有层级结构相同）
parseSecOrderCal();
while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
    advanceToken();
    parseSecOrderCal();  // 继续吃右侧操作数
}
```
这对应龙书 §4.3.4 中 "左递归消除后的文法 A → β R, R → α β R | ε" 的编码实现。

**错误处理**：`handleError()` 设置 `hasError` 标志位，最终 `parse()` 返回 boolean 表示程序是否语法正确。

**核心源文件**：
| 文件 | 职责 |
|---|---|
| `HandcraftParser.java` (~422行) | 手写递归下降分析器核心 |
| `ParserMain.java` | Main入口，调用 Lexer→Parser 管线 |

**对应龙书章节**：§2.2 (上下文无关文法)、§4.4 (递归下降分析)、§4.3 (自上而下分析)、§4.3.4 (左递归消除)

---

### 实验三：语义检查 — ANTLR 驱动的类型检查与符号表

**技术路线**：从实验三开始引入 ANTLR 4.13.2 工具，文法定义在 `Mandrill.g4` 中。

**ANTLR 使用流程**：

1. **定义文法** (`Mandrill.g4`, ~277行)：词法规则（全大写命名：`If`, `While`, `Identifier` ...）和语法规则（全小写命名），每个产生式备选分支用 `# label` 标注
2. **生成代码**：`java -jar antlr-4.13.2-complete.jar -visitor Mandrill.g4` 生成 `MandrillLexer.java`、`MandrillParser.java`、`MandrillBaseVisitor.java` 等
3. **写 Visitor**：`SemanticChecker extends MandrillBaseVisitor<MandrillType>`，重写每个 `visitXxx()` 方法进行类型检查

**类型系统**：`MandrillType` 枚举仅包含两种类型——`INT`（整数标量）和 `ARRAY_PTR`（数组/字符串指针）。类型检查通过 Visitor 的返回值向上传播：每个 `visit` 方法返回当前表达式的静态类型。

**符号表设计**：`ScopeFrame` 链表式作用域模型

```
ScopeFrame (全局)
  └─ parent: null
  └─ locals: {x: INT, a: ARRAY_PTR}
  └─ globalAliases: {}

ScopeFrame (函数体)
  └─ parent: → 全局帧
  └─ locals: {n: INT}
  └─ globalAliases: {x → 全局 x}
```

- `resolve(ctx, name)`：从当前帧沿 `parent` 链表向上递归查找变量，实现最近嵌套规则
- `resolveOrCreate(ctx, name, type)`：工厂方法——未声明则自动视为全局变量并补注册（适配 Mandrill 隐式声明语义）
- `enterScope() / exitScope()`：进入/退出作用域时压栈/弹栈 `currentScope`

**语义检查覆盖**：
- 变量先使用后声明（隐式全局推断）的合法性
- 二元运算操作数类型匹配（算术要求 INT，索引要求 ARRAY_PTR[INT]）
- 函数调用的形参/实参数目与类型匹配
- 数组赋值左侧必须为 ARRAY_PTR
- 禁止对 INT 变量取下标

**核心源文件**：
| 文件 | 职责 |
|---|---|
| `Mandrill.g4` | ANTLR 文法定义 |
| `SemanticChecker.java` (~326行) | Visitor 型语义检查器 |
| `SymbolTable.java` (~194行) | 符号表（ScopeFrame 链表 + resolve/resolveOrCreate） |
| `SymbolCollector.java` | 第一遍：收集函数签名 |
| `MandrillType.java` | 类型枚举 (INT, ARRAY_PTR) |

**对应龙书章节**：§2.8 (符号表)、§6.5 (类型检查)、§6.3 (类型系统与类型表达式)

---

### 实验四：代码生成 — 栈式虚拟机汇编编译器

**技术路线**：三遍扫描管线，将 ANTLR AST 翻译为虚拟机汇编指令。

**编译器管线**：

```
Mandrill 源文件 (.mds)
    │
    ▼
ANTLR Lexer → Token Stream → ANTLR Parser → Parse Tree
    │
    ▼
TacCompiler.compile()
    │
    ├─ [第一遍] LayoutCollector: 静态布局收集
    │     ├─ 为全局变量分配全局编号 (globals 从0开始递增)
    │     ├─ 为每个函数建立 FunctionLayout(参数→局部槽位映射)
    │     └─ 收集 main 语句列表
    │
    ├─ [第二遍] TacBuilder: 生成线性 IR 指令序列
    │     ├─ emitStatement() / emitExpression() 深度遍历 AST
    │     ├─ 生成 TacItem 列表 (Type=LINE | LABEL)
    │     └─ 标签引用用字符串记录 (labelRef)，暂不解析为地址
    │
    └─ [第三遍] AssemblyEmitter → render() → 输出汇编文本
          ├─ 第一遍扫描: 统计各标签的 PC 地址（每条指令 8 字节）
          ├─ 第二遍扫描: 将 TacItem.labelRef 替换为数字地址（回填）
          └─ 格式化输出 (opcode + operand)
```

**TAC IR 设计**：

`TacItem` 记录包含 4 个字段：
- `Type`：`LINE` (普通指令) 或 `LABEL` (符号标签，不生成实际指令)
- `name`：指令助记符字符串（如 `"dload"`, `"eval"`, `"jump"`）
- `value`：长整型立即数
- `labelRef`：字符串标签引用（回填时解析为数字)

> 称为 "TAC" 是历史命名——实际上它不是教科书定义的三地址码（`x = y op z`），而是**面向栈式虚拟机的线性指令序列**。由于目标机器基于操作数栈，中间值天然存在于栈上，不需要引入临时变量名。这更接近龙书 §6.4 描述的 "线性中间表示"。

**条件跳转机制**：`eval CONDITION` (操作码 `0x0001000C`)

区别于教科书 `if x goto L` 风格，本项目采用一条指令完成分支：

```
dstore <falseTarget>   # 压入"假地址"
dstore <trueTarget>    # 压入"真地址"
<cond 求值指令序列>     # 结果压在栈顶
eval 0x0001000C        # 弹出 [false, true, cond]，cond≠0 → PC=trueTarget
```

三值共压一栈，由单条 `eval CONDITION` 统一处理。这体现了面向特定 VM 架构做代码生成时的灵活变通。

**函数调用约定**：

```
[调用者]
  实参逆序压栈:   for i = args.size()-1 down to 0: <推 arg[i]>  # 让第一个参数在栈顶
  jal <funcAddr>            # 分配局部帧，跳转

[被调用者]
  dlwrite <p_i>             # 从栈顶弹出参数值写入局部槽位 (逐参数依次弹出)
  ...函数体...
  返回值压入操作数栈
  ret                       # 恢复调用者帧
```

**VM 指令集**（18 条指令，8 字节定长编码）：

| 类别 | 助记符 | 功能 |
|------|--------|------|
| 数据加载 | `dload x` | 全局变量 #x → 压栈 |
| | `dlload x` | 局部变量 #x → 压栈 |
| | `daload x` | 栈顶地址指向的数组元素 → 压栈 |
| | `dstore x` | 立即数 x → 压栈 |
| 数据写入 | `dwrite x` | 栈顶值 → 全局 #x |
| | `dlwrite x` | 栈顶值 → 局部 #x |
| | `dawrite x` | 地址+值 → 数组元素 |
| 求值 | `eval x` | 算术/比较/条件跳转（12种操作码） |
| 控制流 | `jump x` | PC = x |
| | `jal x` | 调用：保存帧，PC = x |
| | `ret x` | 返回：恢复帧 |
| | `nop 0` | 空操作 |
| 内存 | `malloc x` | 分配内存，地址压栈 |
| I/O | `geti/getc/gets` | 整数/字符/字符串输入 |
| | `puti/putc/puts` | 整数/字符/字符串输出 |

运行终止：PC = `0xFFFFFFFF` 时 VM 退出。程序末尾生成 `jump 0xFFFFFFFF` 作为结束指令。

**核心源文件**：
| 文件 | 职责 |
|---|---|
| `TacCompiler.java` (~673行) | 编译器核心：LayoutCollector + TacBuilder + AssemblyEmitter |
| `SimulatorMain.java` | VM 模拟器入口：加载汇编 → 执行 |
| `AssemblyParser.java` | 汇编文本解析 → Instruction 列表 |
| `Eval.java` | `eval` 指令实现（含 CONDITION 跳转逻辑） |
| `Constants.java` | EVAL 操作码常量定义 |

**对应龙书章节**：§6.2.3 (三地址码与语法树)、§6.4 (线性中间表示)、§6.7 (回填技术)、§7.2 (运行时刻环境与栈帧)、§8.1-8.2 (目标代码生成)

---

## 运行结果

### 实验一（词法分析）：6/6 PASSED

```
mandrill-src/01-a+1.mds       : PASSED
mandrill-src/06-sum.mds       : PASSED
mandrill-src/11-wordcount.mds : PASSED
mandrill-src/21-if2.mds       : PASSED
mandrill-src/26-string.mds    : PASSED
mandrill-src/28-scope.mds     : PASSED
结果: 6 项全部通过
```

### 实验二（语法分析）：10/10 PASSED

```
【合法程序 5/5】
02-a+b.mds       : PASSED    (加法表达式)
04-gcd.mds       : PASSED    (欧几里得算法)
12-exprlexer.mds : PASSED    (复杂表达式)
26-string.mds    : PASSED    (字符串处理)
30-fibonacci.mds : PASSED    (斐波那契递归)

【非法程序 5/5 — 正确报告 Error】
05-accumulator.mds  : PASSED
08-quickpow.mds     : PASSED
19-func-paren.mds   : PASSED
22-empty-index.mds  : PASSED
a1-if.mds           : PASSED
结果: 10 项全部通过
```

### 实验三（语义检查）：9/9 PASSED

```
【合法程序 5/5】：02-a+b, 04-gcd, 12-exprlexer, 26-string, 30-fibonacci
【语义错误 4/4 — 正确捕获并报错】：
    01-array-assigned-int  (数组变量被赋整数值)
    02-array-get-put       (对非数组变量取下标)
    03-int-to-array-param  (整数值传给数组形参)
    04-array-to-int-param  (数组值传给整数形参)
结果: 9 项全部通过（经多次运行验证稳定）
```

### 实验四（代码生成）：最终 5/5 PASSED

经过迭代调试（早期存在条件跳转栈顺序错误、斐波那契递归返回值处理等问题），最终全部通过。

| 测试用例 | 功能说明 | 结果 |
|----------|---------|------|
| `01-a+1.mds` | 整数读取 + 加法 + 输出 | PASSED |
| `02-a+b.mds` | 双整数读取 + 加法 + 输出 | PASSED |
| `03-expr.mds` | 复杂算术表达式 | PASSED |
| `29-recursive-gcd.mds` | 递归欧几里得算法 | PASSED |
| `30-fibonacci.mds` | 递归斐波那契数列 | PASSED |

---

## 构建与运行

### 环境要求

- JDK 17+
- ANTLR 4.13.2 `antlr-4.13.2-complete.jar`（实验三、四需要，已内置于目录中）

### 各实验编译与运行

**实验一**（词法分析）：
```bash
cd 实验一答题
make                          # 编译
java -cp build cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.HandcraftLexer <input.mds>
./main.sh log.txt             # 运行全部测试
```

**实验二**（语法分析）：
```bash
cd exp2-part1   # 或 exp2-part2
make
java -cp build cn.edu.fzu.ccds.compilerprinciples.mandrill.parser.ParserMain <input.mds>
./main.sh res.log             # 运行全部测试（含合法与非法用例）
```

**实验三**（语义检查）：
```bash
cd exp3
make                          # 自动生成 ANTLR Lexer/Parser，编译全部 Java
make run                      # 运行 SemanticCheckerMain
./main.sh log.txt             # 运行全部测试
```

**实验四**（完整编译器）：
```bash
cd exp4
make                          # 生成 ANTLR + 编译
make run                      # 运行编译器（从 stdin 读取源码，输出汇编到 stdout）

# 完整管线：编译 .mds → 汇编 → VM 模拟执行
java -cp build:antlr-4.13.2-complete.jar \
  cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler.CompilerMain \
  <source.mds> output.asm

java -cp build:antlr-4.13.2-complete.jar \
  cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMain \
  output.asm <input.txt>

./main.sh log.txt             # 运行全部测试（编译+模拟+结果对比）
```

### 示例：编译器完整管线

输入 `02-a+b.mds`：
```
write = read + read;
put = '\n';
```

运行（输入为 `1 2`）：
```bash
$ java -cp build:antlr-4.13.2-complete.jar CompilerMain 02-a+b.mds data.asm
$ echo "1 2" | java -cp build:antlr-4.13.2-complete.jar SimulatorMain data.asm
3
```

编译器生成的 `data.asm` 片段：
```asm
; Mandrill 02-a+b.mds
geti 0
dwrite 0
geti 0
dwrite 1
dload 0
dload 1
eval 65537       ; 0x00010001 = EVAL_ADD
dstore 0
puti 0
putc '\n'
jump 4294967295
```

---

## 龙书（编译原理 第二版 中文版）章节对应

| 实验阶段 | 涉及知识点 | 龙书章节 |
|----------|-----------|---------|
| 实验一 词法分析 | 正则表达式→状态转换图→手写词法分析器、Token流生成、PushbackReader 单字符前瞻、关键字 vs 标识符区分 | §3.3-3.4 词法单元的识别与词法分析器构造、§3.7 从正则到有限自动机 |
| 实验二 语法分析 | LL(1)递归下降分析、上下文无关文法到分析程序的直译式映射、左递归消除（while循环迭代模式）、按Token类型分派的语句级路由 | §2.2 上下文无关文法、§4.3-4.4 递归下降分析、§4.3.4 左递归消除 |
| 实验三 语义检查 | 符号表作用域模型（ScopeFrame链表+parent指针）、最近嵌套规则、隐式声明推断（resolveOrCreate工厂方法）、类型检查（INT/ARRAY_PTR二元类型系统）、ANTLR Visitor模式与AST遍历 | §2.8 符号表、§6.5 类型检查、§6.3 类型系统与类型表达式 |
| 实验四 代码生成 | 栈式虚拟机汇编生成、线性IR与标签回填（两遍render()）、`eval CONDITION`三值一栈式条件跳转、函数调用约定（实参逆序+jl+dlwrite+ret）、活动记录与栈帧、`jump 0xFFFFFFFF`终止约定 | §6.2 中间代码形式、§6.4 线性中间表示、§6.7 回填、§7.2 运行时刻环境（栈分配与调用序列）、§8.1-8.3 目标代码生成 |

---

## 参考资源

- [Mandrill v2.0 语言参考手册](docs/mandrill-v2-intro.pdf) — 语言定义、语法规则、VM模型、指令集参考
- [编译原理（龙书）中文第二版](/docs/中文Compilers%20Principles%20Techniques%20and%20Tools.pdf) — 课程核心教材，涵盖编译全流程理论与算法
- [ANTLR 4 官方文档](https://github.com/antlr/antlr4) — ANTLR 4 的使用指南与 API 参考

# 变量定义
ANTLR_JAR = antlr-4.13.2-complete.jar
JC = javac
JV = java
BUILD_DIR = build
SRC_DIR = src/main/java
G4_FILE = Mandrill.g4
PACKAGE_NAME = cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr
GENERATED_DIR = $(SRC_DIR)/cn/edu/fzu/ccds/compilerprinciples/mandrill/antlr
MAIN_CLASS = cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker.SemanticCheckerMain

# ANTLR 生成的 Java 文件（显式列出，确保 clean 后仍能被编译）
ANTLR_GENERATED = \
	$(GENERATED_DIR)/MandrillLexer.java \
	$(GENERATED_DIR)/MandrillParser.java \
	$(GENERATED_DIR)/MandrillBaseListener.java \
	$(GENERATED_DIR)/MandrillListener.java \
	$(GENERATED_DIR)/MandrillBaseVisitor.java \
	$(GENERATED_DIR)/MandrillVisitor.java

# 手写 Java 文件
HANDWRITTEN_FILES := $(shell find $(SRC_DIR) -name "*.java" ! -path "$(GENERATED_DIR)/*")

# 所有需要编译的 Java 文件
JAVA_FILES = $(HANDWRITTEN_FILES) $(ANTLR_GENERATED)

# 默认目标
.PHONY: all clean run

all: $(BUILD_DIR)/.compiled

parser: $(ANTLR_GENERATED)

# 生成 ANTLR Lexer / Parser
$(ANTLR_GENERATED): $(G4_FILE)
	@echo "[ANTLR] Generating parser/lexer from $(G4_FILE)..."
	@java -jar $(ANTLR_JAR) $(G4_FILE) -o $(GENERATED_DIR) -package $(PACKAGE_NAME) -visitor -listener

# 编译所有 Java 文件到 build 目录（保持包结构）
# 依赖所有源文件（含生成的）以及 build 目录的存在
$(BUILD_DIR)/.compiled: $(JAVA_FILES) | $(BUILD_DIR)
	@echo "[JAVAC] Compiling $(words $(JAVA_FILES)) files..."
	@$(JC) -cp $(ANTLR_JAR) -d $(BUILD_DIR) $(JAVA_FILES)
	@touch $(BUILD_DIR)/.compiled

# 创建 build 目录
$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)

# 运行主类
run: $(BUILD_DIR)/.compiled
	$(JV) -cp $(BUILD_DIR):$(ANTLR_JAR) $(MAIN_CLASS)

# 清理
clean:
	@echo "[CLEAN] Removing build directory and generated files..."
	@rm -rf $(BUILD_DIR)
	@rm -f $(GENERATED_DIR)/MandrillLexer.java \
	       $(GENERATED_DIR)/MandrillParser.java \
	       $(GENERATED_DIR)/MandrillBaseListener.java \
	       $(GENERATED_DIR)/MandrillListener.java \
	       $(GENERATED_DIR)/MandrillBaseVisitor.java \
	       $(GENERATED_DIR)/MandrillVisitor.java \
	       $(GENERATED_DIR)/Mandrill*.tokens \
	       $(GENERATED_DIR)/Mandrill*.interp
	@rm -rf $(GENERATED_DIR)
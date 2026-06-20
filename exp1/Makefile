# 变量定义区（Variables）
# 定义了编译器别名和路径规则，方便后续统一修改，可以在这里添加额外的编译选项或路径。如：javac -cp xxx.jar
JV = java
JC = javac
BUILD_DIR = ./build
SRC_DIR = ./src/main/java

# 在src目录下查找所有的Java源文件，并将它们存储在JAVA_FILES变量中。
JAVA_FILES := $(shell find $(SRC_DIR) -name "*.java")
# 路径映射 (patsubst)：告诉 Make 如何在保持包结构（Package Structure）的同时，自动生成目标文件路径。
CLASS_FILES :=  $(patsubst $(SRC_DIR)/%.java,$(BUILD_DIR)/%.class,$(JAVA_FILES))


# Make the build directory
DIRS = $(BUILD_DIR)

# Remove files
CLEAN = $(SRCS) $(BUILD_DIR)

# After the Application is first built, use this
# to update single files. NOTE: Might not work
# if multiple files are updated.
# When A new file class is created, you
# will have to clean and build the project again
update:  $(BUILD_DIR) $(CLASS_FILES)
.PHONY =  update Main clean

# This is the main executable you want
TARGET = cn.edu.fzu.ccds.compilerprinciples.mandrill.lexer.HandcraftLexer

# Alias for that executable, you can use -> make Main
# to build it.
Main: $(TARGET)

# You can have more executables
# TARGET2 = someotherpack.OtherMain

# Rule to build .class files
$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java | $(BUILD_DIR)
	@echo "[$JC]: $@  << $<"
	@$(JC) -cp $(BUILD_DIR) -d $(BUILD_DIR) $<

# Rule to build main executable
$(TARGET): $(BUILD_DIR)
	@echo "[JV]: " $@
	$(JV) -cp build $(TARGET)

# Make the build directory
$(BUILD_DIR):
	@echo $(JAVA_FILES)
	@ if ! [ -e $(BUILD_DIR) ]; then mkdir $(BUILD_DIR); fi
	@echo "[JC]: " $@
	@$(JC) $(JAVA_FILES) -d $(BUILD_DIR)

# Remove files
clean:
	@$(foreach file, $(CLEAN), if [ -e "$(file)" ]; then echo "[RM] $(file)"; rm -r "$(file)";fi;)

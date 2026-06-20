package cn.edu.fzu.ccds.compilerprinciples.mandrill.semanticchecker;

import cn.edu.fzu.ccds.compilerprinciples.mandrill.antlr.MandrillParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    public static final class VariableSymbol {
        private final String name;
        private MandrillType type;
        private final boolean global;

        private VariableSymbol(String name, MandrillType type, boolean global) {
            this.name = name;
            this.type = type;
            this.global = global;
        }

        public String getName() {
            return name;
        }

        public MandrillType getType() {
            return type;
        }

        public void setType(MandrillType type) {
            this.type = type;
        }

        public boolean isGlobal() {
            return global;
        }
    }

    public static final class FunctionSymbol {
        private final String name;
        private final MandrillType retType;
        private final List<MandrillType> paramTypes;
        private final List<String> paramNames;
        private final MandrillParser.FunctionDefContext context;

        private FunctionSymbol(String name, MandrillType retType, List<MandrillType> paramTypes,
                               List<String> paramNames, MandrillParser.FunctionDefContext context) {
            this.name = name;
            this.retType = retType;
            this.paramTypes = paramTypes;
            this.paramNames = paramNames;
            this.context = context;
        }

        public String getName() {
            return name;
        }

        public MandrillType getReturnType() {
            return retType;
        }

        public List<MandrillType> getParameterTypes() {
            return paramTypes;
        }

        public List<String> getParameterNames() {
            return paramNames;
        }

        public MandrillParser.FunctionDefContext getContext() {
            return context;
        }
    }

    private static final class ScopeFrame {
        private final ScopeFrame parent;
        private final Map<String, VariableSymbol> locals = new LinkedHashMap<>();
        private final Map<String, VariableSymbol> globalAliases = new LinkedHashMap<>();

        private ScopeFrame(ScopeFrame parent) {
            this.parent = parent;
        }
    }

    private final Map<String, FunctionSymbol> functions = new LinkedHashMap<>();
    private final ScopeFrame globalScope = new ScopeFrame(null);
    private ScopeFrame currentScope = globalScope;

    public void enterScope() {
        currentScope = new ScopeFrame(currentScope);
    }

    public void exitScope() {
        if (currentScope.parent == null) {
            throw new IllegalStateException("Cannot exit global scope");
        }
        currentScope = currentScope.parent;
    }

    public FunctionSymbol defineFunction(MandrillParser.FunctionDefContext ctx, String name, MandrillType retType,
                                         List<MandrillType> paramTypes, List<String> paramNames) {
        if (functions.containsKey(name)) {
            throw semanticError(ctx, "Duplicate function: " + name);
        }
        FunctionSymbol symbol = new FunctionSymbol(name, retType, List.copyOf(paramTypes), List.copyOf(paramNames), ctx);
        functions.put(name, symbol);
        return symbol;
    }

    public FunctionSymbol getFunction(String name) {
        return functions.get(name);
    }

    public VariableSymbol declare(ParserRuleContext ctx, String name, MandrillType type, boolean isGlobal) {
        if (currentScope.locals.containsKey(name) || currentScope.globalAliases.containsKey(name)) {
            throw semanticError(ctx, "Duplicate variable: " + name);
        }
        if (isGlobal) {
            VariableSymbol global = globalScope.locals.get(name);
            if (global == null) {
                global = new VariableSymbol(name, type, true);
                globalScope.locals.put(name, global);
            } else if (global.getType() != null && type != null && global.getType() != type) {
                throw semanticError(ctx, "Type mismatch for global variable: " + name);
            } else if (global.getType() == null) {
                global.setType(type);
            }
            currentScope.globalAliases.put(name, global);
            return global;
        }
        VariableSymbol symbol = new VariableSymbol(name, type, currentScope == globalScope);
        currentScope.locals.put(name, symbol);
        return symbol;
    }

    public VariableSymbol resolve(ParserRuleContext ctx, String name) {
        VariableSymbol symbol = lookup(name);
        if (symbol == null) {
            throw semanticError(ctx, "Undefined variable: " + name);
        }
        return symbol;
    }

    public VariableSymbol resolveOrCreate(ParserRuleContext ctx, String name, MandrillType type) {
        VariableSymbol symbol = lookup(name);
        if (symbol == null) {
            symbol = new VariableSymbol(name, type, true);
            globalScope.locals.put(name, symbol);
            return symbol;
        }
        if (symbol.getType() == null) {
            symbol.setType(type);
        } else if (type != null && symbol.getType() != type) {
            throw semanticError(ctx, "Type mismatch for variable: " + name);
        }
        return symbol;
    }

    private VariableSymbol lookup(String name) {
        for (ScopeFrame frame = currentScope; frame != null; frame = frame.parent) {
            VariableSymbol alias = frame.globalAliases.get(name);
            if (alias != null) {
                return alias;
            }
            VariableSymbol local = frame.locals.get(name);
            if (local != null) {
                return local;
            }
        }
        return null;
    }

    private SemanticException semanticError(ParserRuleContext ctx, String message) {
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();
        return new SemanticException(line, col, message);
    }
}

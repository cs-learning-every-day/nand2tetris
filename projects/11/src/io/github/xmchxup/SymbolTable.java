package io.github.xmchxup;


import java.util.HashMap;
import java.util.Map;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class SymbolTable {
    private Map<String, Symbol> classSymbols;
    private Map<String, Symbol> subroutineSymbols;
    private Map<Kind, Integer> indices;


    public SymbolTable() {
        classSymbols = new HashMap<>();
        subroutineSymbols = new HashMap<>();
        indices = new HashMap<>();

        for (Kind value : Kind.values()) {
            indices.put(value, 0);
        }
    }

    void startSubroutine() {
        subroutineSymbols.clear();
        indices.put(Kind.VAR, 0);
        indices.put(Kind.ARG, 0);
    }

    void define(String name, String type, Kind kind) {
        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            int index = indices.get(kind);
            indices.put(kind, index + 1);
            classSymbols.put(name, new Symbol(type, kind, index));
        } else if (kind == Kind.ARG || kind == Kind.VAR) {
            int index = indices.get(kind);
            indices.put(kind, index + 1);
            subroutineSymbols.put(name, new Symbol(type, kind, index));
        }
    }

    int varCount(Kind kind) {
        return indices.get(kind);
    }

    Kind kindOf(String name) {
        Symbol symbol = lookUp(name);
        if (symbol != null) return symbol.getKind();

        return Kind.NONE;
    }

    String typeOf(String name) {
        Symbol symbol = lookUp(name);
        if (symbol != null) return symbol.getType();

        return "";
    }

    int indexOf(String name) {
        Symbol symbol = lookUp(name);
        if (symbol != null) return symbol.getIndex();
        return -1;
    }

    private Symbol lookUp(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name);
        }
        return classSymbols.getOrDefault(name, null);
    }

}

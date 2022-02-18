package io.github.xmchxup;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Symbol {
    private String type;
    private Kind kind;
    private int index;

    public Symbol(String type, Kind kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "type='" + type + '\'' +
                ", kind=" + kind +
                ", index=" + index +
                '}';
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }
}

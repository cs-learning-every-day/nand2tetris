package io.github.xmchxup;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(JackTokenizer.KeywordType.valueOf("CLASS"));
        for (JackTokenizer.KeywordType value : JackTokenizer.KeywordType.values()) {
            System.out.println(value.toString().toLowerCase());
        }
        var sb = new StringBuilder();
        sb.append("[")
                .append("\\+")
                .append("\\-")
                .append("\\*")
                .append("\\/")
                .append("\\(")
                .append("\\)")
                .append("\\[")
                .append("\\]")
                .append("\\{")
                .append("\\}")
                .append("\\.")
                .append("\\,")
                .append("\\&")
                .append("\\;")
                .append("\\~")
                .append("\\|")
                .append("\\>")
                .append("\\<")
                .append("\\=")
                .append("]");
        System.out.println(sb.toString());
    }
}

package io.github.xmchxup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class JackTokenizer {
    private Pattern tokenPatterns;
    private String keywordReg;
    private String symbolReg;
    private String intReg;
    private String strReg;
    private String idReg;

    private File inputFile;
    private String currentToken;
    private TokenType currentTokenType;
    private List<String> tokens;
    private int pToken; // 未处理的token的索引

    JackTokenizer(File inputFile) {
        initRegs();

        this.inputFile = inputFile;
        currentToken = "";
        pToken = 0;

        String lines = getInputFileLines();

        Matcher m = tokenPatterns.matcher(lines);
        tokens = new ArrayList<>();
        while (m.find()) {
            tokens.add(m.group());
        }
    }

    private void initRegs() {
        StringBuilder sb = new StringBuilder();

        for (KeywordType v : KeywordType.values()) {
            sb.append(v.toString().toLowerCase())
                    .append("|");
        }

        keywordReg = sb.toString();
        sb = new StringBuilder();
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

        symbolReg = sb.toString();
        intReg = "[0-9]+";
        strReg = "\"[^\"]*\"";
        idReg = "[\\w]+";

        tokenPatterns = Pattern.compile(keywordReg +
                symbolReg + "|" +
                intReg + "|" +
                strReg + "|" +
                idReg);
    }

    // 获取文件行 并且移除多余的空格和注释
    private String getInputFileLines() {
        String res = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                line = removeComment(line).trim();
                if (line.length() > 0) {
                    sb.append(line)
                            .append("\n");
                }
            }
            res = removeBlockComments(sb.toString()).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private String removeBlockComments(String lines) {
        // /***/
        int startIndex = lines.indexOf("/**");
        if (startIndex != -1) {
            int endIndex = lines.indexOf("*/");
            while (startIndex != -1 && endIndex != -1) { // 假设是成对出现的
                lines = lines.substring(0, startIndex) + lines.substring(endIndex + 2);
                startIndex = lines.indexOf("/**");
                endIndex = lines.indexOf("*/");
            }
        }
        return lines;
    }

    private String removeComment(String line) {
        // //
        int p = line.indexOf("//");
        if (p != -1) {
            line = line.substring(0, p);
        }
        return line;
    }

    boolean hasMoreTokens() {
        return pToken < tokens.size();
    }

    void retreat() {
        if (pToken > 0) {
            pToken--;
        }
    }

    void advance() {
        if (hasMoreTokens()) {
            currentToken = tokens.get(pToken);
            pToken++;
        } else {
            throw new IllegalStateException("No more tokens");
        }

        if (currentToken.matches(keywordReg)) {
            currentTokenType = TokenType.KEYWORD;
        } else if (currentToken.matches(symbolReg)) {
            currentTokenType = TokenType.SYMBOL;
        } else if (currentToken.matches(intReg)) {
            currentTokenType = TokenType.INT_CONST;
        } else if (currentToken.matches(strReg)) {
            currentTokenType = TokenType.STRING_CONST;
        } else if (currentToken.matches(idReg)) {
            currentTokenType = TokenType.IDENTIFIER;
        } else {
            throw new IllegalArgumentException("Unknown token: " + currentToken);
        }
    }

    String getCurrentToken() {
        return currentToken;
    }

    TokenType tokenType() {
        return currentTokenType;
    }

    KeywordType keyword() {
        if (tokenType() == TokenType.KEYWORD) {
            return KeywordType.valueOf(currentToken.toUpperCase());
        } else {
            throw new IllegalStateException("token type is not a KEYWORD!");
        }
    }

    char symbol() {
        if (tokenType() == TokenType.SYMBOL) {
            return currentToken.charAt(0);
        } else {
            throw new IllegalStateException("token type is not a SYMBOL!");
        }
    }

    String identifier() {
        if (tokenType() == TokenType.IDENTIFIER) {
            return currentToken;
        } else {
            throw new IllegalStateException("token type is not a IDENTIFIER!");
        }
    }

    int intVal() {
        if (tokenType() == TokenType.INT_CONST) {
            return Integer.parseInt(currentToken);
        } else {
            throw new IllegalStateException("token type is not a INT_CONST!");
        }
    }

    String stringVal() {
        if (tokenType() == TokenType.STRING_CONST) {
            return currentToken.substring(1, currentToken.length() - 1);
        } else {
            throw new IllegalStateException("token type is not a STRING_CONST!");
        }
    }
}

package io.github.xmchxup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class JackTokenizer {
    private enum KeywordType {
        CLASS,
        METHOD,
        INT,
        FUNCTION,
        BOOLEAN,
        CONSTRUCTOR,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS;
    }

    private enum TokenType {
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST;
    }

    private File inputFile;

    JackTokenizer(File inputFile) {
        this.inputFile = inputFile;
    }

    File Generate(String outputPath) {

        List<String> fileLines = getInputFileLines();

        File outputFile = new File(outputPath);
        return outputFile;
    }

    // 获取文件行 并且移除多余的空格和注释
    private List<String> getInputFileLines() {
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
            String processedLines = removeBlockComments(sb.toString()).trim();
            System.out.println(processedLines);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        return false;
    }

    void advance() {

    }

    TokenType tokenType() {
        return TokenType.IDENTIFIER;
    }

    KeywordType keyword() {
        if (tokenType() == TokenType.KEYWORD) {
            return null;
        } else {
            throw new IllegalStateException("token type is not a KEYWORD!");
        }
    }

    char symbol() {
        if (tokenType() == TokenType.SYMBOL) {
            return ' ';
        } else {
            throw new IllegalStateException("token type is not a SYMBOL!");
        }
    }

    String identifier() {
        if (tokenType() == TokenType.IDENTIFIER) {
            return null;
        } else {
            throw new IllegalStateException("token type is not a IDENTIFIER!");
        }
    }

    int intVal() {
        if (tokenType() == TokenType.INT_CONST) {
            return 0;
        } else {
            throw new IllegalStateException("token type is not a INT_CONST!");
        }
    }

    String stringVal() {
        if (tokenType() == TokenType.STRING_CONST) {
            return null;
        } else {
            throw new IllegalStateException("token type is not a STRING_CONST!");
        }
    }
}

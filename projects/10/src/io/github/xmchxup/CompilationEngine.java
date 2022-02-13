package io.github.xmchxup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class CompilationEngine {
    private PrintWriter printWriter;
    private PrintWriter tokenPrintWriter;
    private JackTokenizer tokenizer;

    CompilationEngine(File inFile, File outputFile, File outputTokenFile) {
        try {
            tokenizer = new JackTokenizer(inFile);
            printWriter = new PrintWriter(outputFile);
            tokenPrintWriter = new PrintWriter(outputTokenFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编译整个类
     * 形式: class className {
     * classVarDec*
     * subroutineDec*
     * }
     */
    void compileClass() {
        // class
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD ||
                tokenizer.keyword() != KeywordType.CLASS) {
            error("class");
        }

        tokenPrintWriter.println("<tokens>");

        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        // classname
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("classname");
        }
        tokenPrintWriter.println("<identifier> " + tokenizer.getCurrentToken() + " </identifier>");

        // {
        requireSymbol('{');

        compileClassVarDec();
        compileSubroutine();

        // }
        requireSymbol('}');

        if (tokenizer.hasMoreTokens()) {
            throw new IllegalStateException("Unexpected tokens");
        }

        tokenPrintWriter.println("</tokens>");
        // save file
        tokenPrintWriter.close();
    }

    /**
     * 编译静态声明颧字段声明
     * 形式：(static | filed) type varName (,varName)* ;
     */
    private void compileClassVarDec() {
        if (!isInClassScope()) {
            return;
        }

        if (tokenizer.tokenType() != TokenType.KEYWORD) {
            error("Keywords");
        }

        // is subroutine
        if (tokenizer.keyword() == KeywordType.CONSTRUCTOR ||
                tokenizer.keyword() == KeywordType.FUNCTION ||
                tokenizer.keyword() == KeywordType.METHOD) {
            tokenizer.retreat();
            return;
        }

        if (tokenizer.keyword() != KeywordType.STATIC &&
                tokenizer.keyword() != KeywordType.FIELD) {
            error("static or field");
        }

        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        compileType();


        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            // , or ;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL &&
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ',')) {
                error("',' or ';'");
            }

            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        } while (tokenizer.symbol() != ';');
        compileClassVarDec();
    }

    /**
     * int、bool、char、className
     */
    private void compileType() {
        tokenizer.advance();
        boolean isType = false;

        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                (tokenizer.keyword() == KeywordType.BOOLEAN ||
                        tokenizer.keyword() == KeywordType.INT ||
                        tokenizer.keyword() == KeywordType.CHAR)) {
            isType = true;
            tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
        }

        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            isType = true;
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        }

        if (!isType) {
            error("int | char | boolean | className");
        }
    }

    /**
     * 编译整个方法、函数或构造函数
     * 形式：(constructor | function | method) (void, type) functionName(paramList) {}
     */
    private void compileSubroutine() {
        if (!isInClassScope()) {
            return;
        }
    }

    // 编译参数列表（可能为空）, 不包含括号"( )"
    private void compileParameterList() {

    }

    private void compileVarDec() {

    }

    // 编译一系列语句，不包含大括号"{}"
    private void compileStatements() {

    }

    private void compileDo() {

    }

    private void compileLet() {

    }


    private void compileWhile() {

    }


    private void compileReturn() {

    }


    private void compileIf() {

    }


    private void compileExpression() {

    }


    private void compileTerm() {

    }


    private void compileExpressionList() {

    }

    private void error(String expectedToken) {
        throw new IllegalStateException("Expected token missing: " + expectedToken +
                " Current Token: " + tokenizer.getCurrentToken());
    }

    private void requireSymbol(char symbol) {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL ||
                tokenizer.symbol() != symbol) {
            error("'" + symbol + "'");
        }
        tokenPrintWriter.println("<symbol> " + symbol + " </symbol>");
    }

    private boolean isInClassScope() {
        tokenizer.advance();

        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '}') {
            tokenizer.retreat();
            return false;
        }
        return true;
    }
}

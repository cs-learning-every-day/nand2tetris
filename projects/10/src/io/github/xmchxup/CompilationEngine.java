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
    private JackTokenizer jackTokenizer;

    CompilationEngine(File inFile, File outputFile, File outputTokenFile) {
        try {
            jackTokenizer = new JackTokenizer(inFile);
            printWriter = new PrintWriter(outputFile);
            tokenPrintWriter = new PrintWriter(outputTokenFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 编译整个类
    void compileClass() {
        // class
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != TokenType.KEYWORD ||
                jackTokenizer.keyword() != KeywordType.CLASS) {
            error("class");
        }

        tokenPrintWriter.println("<tokens>");

        tokenPrintWriter.println("<keyword> " + jackTokenizer.getCurrentToken() + " </keyword>");

        // classname
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("classname");
        }
        tokenPrintWriter.println("<identifier> " + jackTokenizer.getCurrentToken() + " </identifier>");

        // {
        requireSymbol('{');

        compileClassVarDec();
        compileSubroutine();

        // }
        requireSymbol('}');

        if (jackTokenizer.hasMoreTokens()) {
            throw new IllegalStateException("Unexpected tokens");
        }

        tokenPrintWriter.println("</tokens>");
        // save file
        tokenPrintWriter.close();
    }

    // 编译静态声明颧字段声明
    void compileClassVarDec() {

    }

    // 编译整个方法、函数或构造函数
    void compileSubroutine() {

    }

    // 编译参数列表（可能为空）, 不包含括号"( )"
    void compileParameterList() {

    }

    void compileVarDec() {

    }

    // 编译一系列语句，不包含大括号"{}"
    void compileStatements() {

    }

    void compileDo() {

    }

    void compileLet() {

    }


    void compileWhile() {

    }


    void compileReturn() {

    }


    void compileIf() {

    }


    void compileExpression() {

    }


    void compileTerm() {

    }


    void compileExpressionList() {

    }

    private void error(String expectedToken) {
        throw new IllegalStateException("Expected token missing: " + expectedToken +
                " Current Token: " + jackTokenizer.getCurrentToken());
    }

    private void requireSymbol(char symbol) {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol() != symbol) {
            error("'" + symbol + "'");
        }
        tokenPrintWriter.println("<symbol> " + symbol + " </symbol>");
    }
}

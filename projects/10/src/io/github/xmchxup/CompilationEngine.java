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
    private StringBuilder sb;

    CompilationEngine(File inFile, File outputFile, File outputTokenFile) {
        try {
            sb = new StringBuilder();
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

        sb.append("<class>");
        tokenPrintWriter.println("<tokens>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        // classname
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("classname");
        }
        sb.append("<identifier> ")
                .append(tokenizer.getCurrentToken())
                .append(" </identifier>");
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

        sb.append("</class>");
        tokenPrintWriter.println("</tokens>");

        printWriter.println(XmlFormatter.prettyFormat(sb.toString(), "2"));
        // save file
        tokenPrintWriter.close();
        printWriter.close();
    }

    /**
     * 编译静态声明字段声明
     * 形式：(static | filed) type varName (,varName)* ;
     */
    private void compileClassVarDec() {
        tokenizer.advance();
        // }
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '}') {
            tokenizer.retreat();
            return;
        }

        if (tokenizer.tokenType() != TokenType.KEYWORD) {
            error("keyword");
        }

        // is subroutine
        if (tokenizer.keyword() == KeywordType.CONSTRUCTOR ||
                tokenizer.keyword() == KeywordType.FUNCTION ||
                tokenizer.keyword() == KeywordType.METHOD) {
            tokenizer.retreat();
            return;
        }

        sb.append("<classVarDec>");

        if (tokenizer.keyword() != KeywordType.STATIC &&
                tokenizer.keyword() != KeywordType.FIELD) {
            error("static | field");
        }

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            sb.append("<identifier> ")
                    .append(tokenizer.identifier())
                    .append(" </identifier>");
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            // , or ;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ';')) {
                error(", or ;");
            }

            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        } while (tokenizer.symbol() != ';');

        sb.append("</classVarDec>");
        compileClassVarDec();
    }

    /**
     * int、boolean、char、className
     */
    private void compileType() {
        tokenizer.advance();
        boolean isType = false;

        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                (tokenizer.keyword() == KeywordType.BOOLEAN ||
                        tokenizer.keyword() == KeywordType.INT ||
                        tokenizer.keyword() == KeywordType.CHAR)) {
            isType = true;
            sb.append("<keyword> ")
                    .append(tokenizer.getCurrentToken())
                    .append(" </keyword>");
            tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
        }

        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            isType = true;
            sb.append("<identifier> ")
                    .append(tokenizer.identifier())
                    .append(" </identifier>");
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
        tokenizer.advance();
        // }
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '}') {
            tokenizer.retreat();
            return;
        }

        if (tokenizer.tokenType() != TokenType.KEYWORD ||
                (tokenizer.keyword() != KeywordType.CONSTRUCTOR &&
                        tokenizer.keyword() != KeywordType.FUNCTION &&
                        tokenizer.keyword() != KeywordType.METHOD)) {
            error("construct | function | method");
        }

        sb.append("<subroutineDec>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        // return type
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                tokenizer.keyword() == KeywordType.VOID) { // Void
            sb.append("<keyword> ")
                    .append(tokenizer.getCurrentToken())
                    .append(" </keyword>");
            tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
        } else {
            tokenizer.retreat();
            compileType();
        }

        // name
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("subroutineName");
        }
        sb.append("<identifier> ")
                .append(tokenizer.identifier())
                .append(" </identifier>");
        tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        // (
        requireSymbol('(');
        // parameter list
        sb.append("<parameterList>");
        compileParameterList();
        sb.append("</parameterList>");
        // )
        requireSymbol(')');

        sb.append("<subroutineBody>");
        // {
        requireSymbol('{');
        // function body
        compileVarDec();
        sb.append("<statements>");
        compileStatements();
        sb.append("</statements>");
        // }
        requireSymbol('}');
        sb.append("</subroutineBody>");

        sb.append("</subroutineDec>");
        // next
        compileSubroutine();
    }

    // 编译参数列表（可能为空）, 不包含括号"( )"
    private void compileParameterList() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ')') {
            tokenizer.retreat();
            return;
        }

        tokenizer.retreat();

        do {
            // type
            compileType();
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            sb.append("<identifier> ")
                    .append(tokenizer.identifier())
                    .append(" </identifier>");
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            // , or )
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ')')) {
                error(", or )");
            }

            if (tokenizer.symbol() == ',') {
                sb.append("<symbol> ")
                        .append(tokenizer.symbol())
                        .append(" </symbol>");
                tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            } else {
                tokenizer.retreat();
                break;
            }
        } while (true);
    }

    /**
     * 编译一系列语句，不包含大括号"{}"
     * keyword: let、if、while、do、return
     */
    private void compileStatements() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '}') {
            tokenizer.retreat();
            return;
        }

        if (tokenizer.tokenType() != TokenType.KEYWORD) {
            error("keyword");
        }
        switch (tokenizer.keyword()) {
            case LET:
                compileLet();
                break;
            case IF:
                compileIf();
                break;
            case WHILE:
                compileWhile();
                break;
            case DO:
                compileDo();
                break;
            case RETURN:
                compileReturn();
                break;
            default:
                error("let | if | while | do | return");
        }
        compileStatements();
    }

    private void compileVarDec() {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD ||
                tokenizer.keyword() != KeywordType.VAR) {
            tokenizer.retreat();
            return;
        }
        sb.append("<varDec>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        // type
        compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            sb.append("<identifier> ")
                    .append(tokenizer.identifier())
                    .append(" </identifier>");
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            // , or ;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ';')) {
                error("',' or ';'");
            }

            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        } while (tokenizer.symbol() != ';');

        sb.append("</varDec>");
        // next
        compileVarDec();
    }

    private void compileDo() {
        sb.append("<doStatement>");
        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
        compileCall();
        // ;
        requireSymbol(';');
        sb.append("</doStatement>");
    }

    private void compileCall() {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("identifier");
        }
        sb.append("<identifier> ")
                .append(tokenizer.identifier())
                .append(" </identifier>");
        tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '.') {
            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            sb.append("<identifier> ")
                    .append(tokenizer.identifier())
                    .append(" </identifier>");
            tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            requireSymbol('(');
            sb.append("<expressionList>");
            compileExpressionList();
            sb.append("</expressionList>");
            requireSymbol(')');
        } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '(') {
            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            sb.append("<expressionList>");
            compileExpressionList();
            sb.append("</expressionList>");
            requireSymbol(')');
        } else {
            error(". | (");
        }
    }

    private void compileLet() {
        sb.append("<letStatement>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        // varName
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        sb.append("<identifier> ")
                .append(tokenizer.identifier())
                .append(" </identifier>");
        tokenPrintWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        // [ or =
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL ||
                (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')) {
            error("[ or =");
        }

        // []
        if (tokenizer.symbol() == '[') {
            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> [ </symbol>");
            compileExpression();
            requireSymbol(']');
            tokenizer.advance();
        }

        // =
        sb.append("<symbol> ")
                .append(tokenizer.symbol())
                .append(" </symbol>");
        tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        compileExpression();
        requireSymbol(';');
        sb.append("</letStatement>");
    }

    private void compileWhile() {
        sb.append("<whileStatement>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        sb.append("<statements>");
        compileStatements();
        sb.append("</statements>");
        requireSymbol('}');

        sb.append("</whileStatement>");
    }

    private void compileReturn() {
        sb.append("<returnStatement>");

        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ';') { // return;
            sb.append("<symbol> ")
                    .append(tokenizer.symbol())
                    .append(" </symbol>");
            tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        } else {
            tokenizer.retreat();
            compileExpression();
            requireSymbol(';');
        }

        sb.append("</returnStatement>");
    }

    private void compileIf() {
        sb.append("<ifStatement>");
        sb.append("<keyword> ")
                .append(tokenizer.getCurrentToken())
                .append(" </keyword>");
        tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        sb.append("<statements>");
        compileStatements();
        sb.append("</statements>");
        requireSymbol('}');

        // else
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                tokenizer.keyword() == KeywordType.ELSE) {
            sb.append("<keyword> ")
                    .append(tokenizer.getCurrentToken())
                    .append(" </keyword>");
            tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
            requireSymbol('{');
            sb.append("<statements>");
            compileStatements();
            sb.append("</statements>");
            requireSymbol('}');
        } else {
            tokenizer.retreat();
        }
        sb.append("</ifStatement>");
    }

    /***
     * 由,分隔的表达式列表
     * do Screen.drawRectangle(x, y, x + size, y + size);
     */
    private void compileExpressionList() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ')') {
            tokenizer.retreat();
        } else {
            tokenizer.retreat();
            compileExpression();
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == TokenType.SYMBOL &&
                        tokenizer.symbol() == ',') {
                    sb.append("<symbol> , </symbol>");
                    tokenPrintWriter.println("<symbol> , </symbol>");
                    compileExpression();
                } else {
                    tokenizer.retreat();
                    break;
                }
            } while (true);
        }
    }

    private void compileExpression() {
        sb.append("<expression>");
        // example: x + 2
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    tokenizer.isOperator()) {
                if (tokenizer.symbol() == '>') {
                    sb.append("<symbol> &gt; </symbol>");
                    tokenPrintWriter.println("<symbol> &gt; </symbol>");
                } else if (tokenizer.symbol() == '<') {
                    sb.append("<symbol> &lt; </symbol>");
                    tokenPrintWriter.println("<symbol> &lt; </symbol>");
                } else if (tokenizer.symbol() == '&') {
                    sb.append("<symbol> &amp; </symbol>");
                    tokenPrintWriter.println("<symbol> &amp; </symbol>");
                } else {
                    sb.append("<symbol> ")
                            .append(tokenizer.symbol())
                            .append(" </symbol>");
                    tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                }
                //term
                compileTerm();
            } else {
                tokenizer.retreat();
                break;
            }
        } while (true);
        sb.append("</expression>");
    }

    private void compileTerm() {
        sb.append("<term>");

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            String tmpId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    tokenizer.symbol() == '[') { // array
                sb.append("<identifier> ")
                        .append(tmpId)
                        .append(" </identifier>");
                tokenPrintWriter.println("<identifier> " + tmpId + " </identifier>");
                sb.append("<symbol> [ </symbol>");
                tokenPrintWriter.println("<symbol> [ </symbol>");
                compileExpression();
                requireSymbol(']');
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    ((tokenizer.symbol() == '(' || tokenizer.symbol() == '.'))) { // function call
                tokenizer.retreat();
                tokenizer.retreat();
                compileCall();
            } else { // var name
                sb.append("<identifier> ")
                        .append(tmpId)
                        .append(" </identifier>");
                tokenPrintWriter.println("<identifier> " + tmpId + " </identifier>");
                tokenizer.retreat();
            }
        } else {
            if (tokenizer.tokenType() == TokenType.INT_CONST) {
                sb.append("<integerConstant> ")
                        .append(tokenizer.intVal())
                        .append(" </integerConstant>");
                tokenPrintWriter.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
            } else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
                sb.append("<stringConstant> ")
                        .append(tokenizer.stringVal())
                        .append(" </stringConstant>");
                tokenPrintWriter.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
            } else if (tokenizer.tokenType() == TokenType.KEYWORD &&
                    (tokenizer.keyword() == KeywordType.TRUE ||
                            tokenizer.keyword() == KeywordType.FALSE ||
                            tokenizer.keyword() == KeywordType.NULL ||
                            tokenizer.keyword() == KeywordType.THIS)) {
                sb.append("<keyword> ")
                        .append(tokenizer.getCurrentToken())
                        .append(" </keyword>");
                tokenPrintWriter.println("<keyword> " + tokenizer.getCurrentToken() + " </keyword>");
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
                sb.append("<symbol> ( </symbol>");
                tokenPrintWriter.println("<symbol> ( </symbol>");
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                sb.append("<symbol> ")
                        .append(tokenizer.symbol())
                        .append(" </symbol>");
                tokenPrintWriter.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                //term
                compileTerm();
            } else {
                error("integerConstant | stringConstant | keywordConstant | '(' expression ')'| unaryOp term");
            }
        }

        sb.append("</term>");
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
        sb.append("<symbol> ")
                .append(symbol)
                .append(" </symbol>");
        tokenPrintWriter.println("<symbol> " + symbol + " </symbol>");
    }
}

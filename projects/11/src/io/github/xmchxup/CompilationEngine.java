package io.github.xmchxup;

import java.io.File;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class CompilationEngine {
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private JackTokenizer tokenizer;

    CompilationEngine(File inFile, File outputFile) {
        tokenizer = new JackTokenizer(inFile);
        vmWriter = new VMWriter(outputFile);
        symbolTable = new SymbolTable();
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


        // classname
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("classname");
        }

        // {
        requireSymbol('{');

        compileClassVarDec();
        compileSubroutine();

        // }
        requireSymbol('}');

        if (tokenizer.hasMoreTokens()) {
            throw new IllegalStateException("Unexpected tokens");
        }

        // save file
        vmWriter.close();
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

        if (tokenizer.keyword() != KeywordType.STATIC &&
                tokenizer.keyword() != KeywordType.FIELD) {
            error("static | field");
        }

        compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }

            // , or ;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ';')) {
                error(", or ;");
            }

        } while (tokenizer.symbol() != ';');

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
        }

        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            isType = true;
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

        // return type
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                tokenizer.keyword() == KeywordType.VOID) { // Void
        } else {
            tokenizer.retreat();
            compileType();
        }

        // name
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("subroutineName");
        }

        // (
        requireSymbol('(');
        // parameter list
        compileParameterList();
        // )
        requireSymbol(')');

        // {
        requireSymbol('{');
        // function body
        compileVarDec();
        compileStatements();
        // }
        requireSymbol('}');
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

            // , or )
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ')')) {
                error(", or )");
            }

            if (tokenizer.symbol() == ',') {
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

        // type
        compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }

            // , or ;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ';')) {
                error("',' or ';'");
            }

        } while (tokenizer.symbol() != ';');

        // next
        compileVarDec();
    }

    private void compileDo() {
        compileCall();
        // ;
        requireSymbol(';');
    }

    private void compileCall() {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("identifier");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '.') {

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }

            requireSymbol('(');
            compileExpressionList();
            requireSymbol(')');
        } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '(') {
            compileExpressionList();
            requireSymbol(')');
        } else {
            error(". | (");
        }
    }

    private void compileLet() {
        // varName
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        // [ or =
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL ||
                (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')) {
            error("[ or =");
        }

        // []
        if (tokenizer.symbol() == '[') {
            compileExpression();
            requireSymbol(']');
            tokenizer.advance();
        }

        // =
        compileExpression();
        requireSymbol(';');
    }

    private void compileWhile() {
        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');
    }

    private void compileReturn() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ';') { // return;
        } else {
            tokenizer.retreat();
            compileExpression();
            requireSymbol(';');
        }
    }

    private void compileIf() {
        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');

        // else
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                tokenizer.keyword() == KeywordType.ELSE) {
            requireSymbol('{');
            compileStatements();
            requireSymbol('}');
        } else {
            tokenizer.retreat();
        }
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
                    compileExpression();
                } else {
                    tokenizer.retreat();
                    break;
                }
            } while (true);
        }
    }

    private void compileExpression() {
        // example: x + 2
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    tokenizer.isOperator()) {
                if (tokenizer.symbol() == '>') {
                } else if (tokenizer.symbol() == '<') {
                } else if (tokenizer.symbol() == '&') {
                } else {
                }
                //term
                compileTerm();
            } else {
                tokenizer.retreat();
                break;
            }
        } while (true);
    }

    private void compileTerm() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            String tmpId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    tokenizer.symbol() == '[') { // array
                compileExpression();
                requireSymbol(']');
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    ((tokenizer.symbol() == '(' || tokenizer.symbol() == '.'))) { // function call
                tokenizer.retreat();
                tokenizer.retreat();
                compileCall();
            } else { // var name
                tokenizer.retreat();
            }
        } else {
            if (tokenizer.tokenType() == TokenType.INT_CONST) {
            } else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
            } else if (tokenizer.tokenType() == TokenType.KEYWORD &&
                    (tokenizer.keyword() == KeywordType.TRUE ||
                            tokenizer.keyword() == KeywordType.FALSE ||
                            tokenizer.keyword() == KeywordType.NULL ||
                            tokenizer.keyword() == KeywordType.THIS)) {
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                //term
                compileTerm();
            } else {
                error("integerConstant | stringConstant | keywordConstant | '(' expression ')'| unaryOp term");
            }
        }
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
    }
}

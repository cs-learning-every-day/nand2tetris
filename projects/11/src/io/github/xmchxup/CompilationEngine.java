package io.github.xmchxup;

import java.io.File;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class CompilationEngine {
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private JackTokenizer tokenizer;

    private String currentClass;
    private String currentSubroutine;
    private int labelIndex;

    CompilationEngine(File inFile, File outputFile) {
        tokenizer = new JackTokenizer(inFile);
        vmWriter = new VMWriter(outputFile);
        symbolTable = new SymbolTable();
        labelIndex = 0;
        currentSubroutine = "";
        currentClass = "";
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

        currentClass = tokenizer.identifier();

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

        Kind kind = null;
        String name = "";
        String type = "";

        switch (tokenizer.keyword()) {
            case STATIC:
                kind = Kind.STATIC;
                break;
            case FIELD:
                kind = Kind.FIELD;
                break;
        }

        type = compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            name = tokenizer.identifier();
            symbolTable.define(name, type, kind);

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
    private String compileType() {
        tokenizer.advance();

        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                (tokenizer.keyword() == KeywordType.BOOLEAN ||
                        tokenizer.keyword() == KeywordType.INT ||
                        tokenizer.keyword() == KeywordType.CHAR)) {
            return tokenizer.getCurrentToken();
        }

        if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            return tokenizer.identifier();
        }

        error("int | char | boolean | className");
        return "";
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

        KeywordType keyword = tokenizer.keyword();
        symbolTable.startSubroutine();

        if (keyword == KeywordType.METHOD) {
            symbolTable.define("this", currentClass, Kind.ARG);
        }

        String type = "";

        // return type
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD &&
                tokenizer.keyword() == KeywordType.VOID) { // Void
            type = "void";
        } else {
            tokenizer.retreat();
            type = compileType();
        }

        // name
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("subroutineName");
        }
        currentSubroutine = tokenizer.identifier();

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
        writeFunctionDec(keyword);
        compileStatements();
        // }
        requireSymbol('}');
        // next
        compileSubroutine();
    }

    private void writeFunctionDec(KeywordType k) {
        vmWriter.writeFunction(currentFunction(), symbolTable.varCount(Kind.VAR));
        if (k == KeywordType.METHOD) {
            vmWriter.writePush(Segment.ARGUMENT, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if (k == KeywordType.CONSTRUCTOR) {
            vmWriter.writePush(Segment.CONSTANT, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        }
    }

    private String currentFunction() {
        if (currentClass.length() != 0 && currentSubroutine.length() != 0) {
            return currentClass + "." + currentSubroutine;
        }
        return "";
    }

    // 编译参数列表（可能为空）, 不包含括号"( )"
    private void compileParameterList() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ')') {
            tokenizer.retreat();
            return;
        }

        String type = "";
        tokenizer.retreat();

        do {
            // type
            type = compileType();
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }
            symbolTable.define(tokenizer.identifier(), type, Kind.ARG);

            // , or )
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL ||
                    (tokenizer.symbol() != ',' &&
                            tokenizer.symbol() != ')')) {
                error(", or )");
            }

            if (tokenizer.symbol() == ')') {
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
        String type = compileType();

        do {
            // varName
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            symbolTable.define(tokenizer.identifier(), type, Kind.VAR);
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
        requireSymbol(';');
        // pop return value
        vmWriter.writePop(Segment.TEMP, 0);
    }

    private void compileCall() {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("identifier");
        }

        String name = tokenizer.identifier();
        int nArgs = 0;

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '.') {
            String objName = name;
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("identifier");
            }

            name = tokenizer.identifier();
            //c heck for if it is built-in type
            String type = symbolTable.typeOf(objName);

            if (type.equals("int") ||
                    type.equals("boolean") ||
                    type.equals("char") ||
                    type.equals("void")) {
                error("no built-in type");
            } else if (type.equals("")) {
                name = objName + "." + name;
            } else {
                nArgs = 1;
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(objName)), symbolTable.indexOf(objName));
                name = symbolTable.typeOf(objName) + "." + name;
            }


            requireSymbol('(');
            nArgs += compileExpressionList();
            requireSymbol(')');
            vmWriter.writeCall(name, nArgs);
        } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == '(') {
            // push this pointer
            vmWriter.writePush(Segment.POINTER, 0);
            // expressionList
            nArgs = compileExpressionList() + 1;
            requireSymbol(')');
            // call subroutine
            vmWriter.writeCall(currentClass + '.' + name, nArgs);
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
        String name = tokenizer.identifier();

        // [ or =
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL ||
                (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')) {
            error("[ or =");
        }

        boolean expExist = false;
        // []
        if (tokenizer.symbol() == '[') {
            expExist = true;
            // push array variable,base address into stack
            vmWriter.writePush(getSeg(symbolTable.kindOf(name)), symbolTable.indexOf(name));
            compileExpression();
            requireSymbol(']');
            // base+offset
            vmWriter.writeArithmetic(Command.ADD);

            tokenizer.advance();
        }

        // =
        compileExpression();
        requireSymbol(';');

        if (expExist) {
            // *(base+offset) = expression
            // pop expression value to temp
            vmWriter.writePop(Segment.TEMP, 0);
            // pop base+index into 'that'
            vmWriter.writePop(Segment.POINTER, 1);
            // pop expression value into *(base+index)
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.THAT, 0);
        } else {
            // pop expression value directly
            vmWriter.writePop(getSeg(symbolTable.kindOf(name)), symbolTable.indexOf(name));
        }
    }

    private Segment getSeg(Kind kind) {
        switch (kind) {
            case FIELD:
                return Segment.THIS;
            case STATIC:
                return Segment.STATIC;
            case VAR:
                return Segment.LOCAL;
            case ARG:
                return Segment.ARGUMENT;
            default:
                return Segment.NONE;
        }

    }

    private void compileWhile() {
        String continueLabel = newLabel();
        String topLabel = newLabel();

        // top label for while loop
        vmWriter.writeLabel(topLabel);

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        // if ~(condition) go to continue label
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(continueLabel);
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');
        //if (condition) go to top label
        vmWriter.writeGoto(topLabel);
        //or continue
        vmWriter.writeLabel(continueLabel);
    }

    private void compileReturn() {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ';') { // return;
            // no expression push 0 to stack
            vmWriter.writePush(Segment.CONSTANT, 0);
        } else {
            tokenizer.retreat();
            compileExpression();
            requireSymbol(';');
        }
        vmWriter.writeReturn();
    }

    private void compileIf() {
        String elseLabel = newLabel();
        String endLabel = newLabel();

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        // if ~(condition) go to else label
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(elseLabel);
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');
        // if condition after statement finishing, go to end label
        vmWriter.writeGoto(endLabel);

        vmWriter.writeLabel(elseLabel);
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
        vmWriter.writeLabel(endLabel);
    }

    /***
     * 由,分隔的表达式列表
     * do Screen.drawRectangle(x, y, x + size, y + size);
     */
    private int compileExpressionList() {
        int nArgs = 0;
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL &&
                tokenizer.symbol() == ')') {
            tokenizer.retreat();
        } else {
            nArgs = 1;
            tokenizer.retreat();
            compileExpression();
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == TokenType.SYMBOL &&
                        tokenizer.symbol() == ',') {
                    compileExpression();
                    nArgs++;
                } else {
                    tokenizer.retreat();
                    break;
                }
            } while (true);
        }
        return nArgs;
    }

    private void compileExpression() {
        // example: x + 2
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    tokenizer.isOperator()) {
                String opCmd = "";
                switch (tokenizer.symbol()) {
                    case '+':
                        opCmd = "add";
                        break;
                    case '-':
                        opCmd = "sub";
                        break;
                    case '*':
                        opCmd = "call Math.multiply 2";
                        break;
                    case '/':
                        opCmd = "call Math.divide 2";
                        break;
                    case '<':
                        opCmd = "lt";
                        break;
                    case '>':
                        opCmd = "gt";
                        break;
                    case '=':
                        opCmd = "eq";
                        break;
                    case '&':
                        opCmd = "and";
                        break;
                    case '|':
                        opCmd = "or";
                        break;
                    default:
                        error("Unknown op!");
                }
                //term
                compileTerm();
                vmWriter.writeCommand(opCmd, "", "");
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
                // push array variable,base address into stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tmpId)), symbolTable.indexOf(tmpId));

                compileExpression();
                requireSymbol(']');

                // base+offset
                vmWriter.writeArithmetic(Command.ADD);

                // pop into 'that' pointer
                vmWriter.writePop(Segment.POINTER, 1);
                // push *(base+index) onto stack
                vmWriter.writePush(Segment.THAT, 0);
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    ((tokenizer.symbol() == '(' || tokenizer.symbol() == '.'))) { // subroutine call
                tokenizer.retreat();
                tokenizer.retreat();
                compileCall();
            } else { // var name
                tokenizer.retreat();
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tmpId)), symbolTable.indexOf(tmpId));
            }
        } else {
            if (tokenizer.tokenType() == TokenType.INT_CONST) {
                //integerConstant just push its value onto stack
                vmWriter.writePush(Segment.CONSTANT, tokenizer.intVal());
            } else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
                //stringConstant new a string and append every char to the new stack
                String str = tokenizer.stringVal();

                vmWriter.writePush(Segment.CONSTANT, str.length());
                vmWriter.writeCall("String.new", 1);

                for (int i = 0; i < str.length(); i++) {
                    vmWriter.writePush(Segment.CONSTANT, (int) str.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
            } else if (tokenizer.tokenType() == TokenType.KEYWORD &&
                    tokenizer.keyword() == KeywordType.TRUE) {
                // ~0 is true
                vmWriter.writePush(Segment.CONSTANT, 0);
                vmWriter.writeArithmetic(Command.NOT);
            } else if (tokenizer.tokenType() == TokenType.KEYWORD &&
                    tokenizer.keyword() == KeywordType.THIS) {
                // push this pointer onto stack
                vmWriter.writePush(Segment.POINTER, 0);
            } else if (tokenizer.tokenType() == TokenType.KEYWORD &&
                    (tokenizer.keyword() == KeywordType.FALSE || tokenizer.keyword() == KeywordType.NULL)) {
                // 0 for false and null
                vmWriter.writePush(Segment.CONSTANT, 0);
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            } else if (tokenizer.tokenType() == TokenType.SYMBOL &&
                    (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                char s = tokenizer.symbol();
                //term
                compileTerm();

                if (s == '-') {
                    vmWriter.writeArithmetic(Command.NEG);
                } else {
                    vmWriter.writeArithmetic(Command.NOT);
                }
            } else {
                error("integerConstant | stringConstant | keywordConstant | '(' expression ')'| unaryOp term");
            }
        }
    }

    private String newLabel() {
        return "LABEL_" + (labelIndex++);
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

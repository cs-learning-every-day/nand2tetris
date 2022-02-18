package io.github.xmchxup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class VMWriter {
    private PrintWriter writer;

    public VMWriter(File outputFile) {
        try {
            writer = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void writePush(Segment segment, int index) {
        writeCommand("push", segment.name().toLowerCase(), String.valueOf(index));
    }

    void writePop(Segment segment, int index) {
        writeCommand("pop", segment.name().toLowerCase(), String.valueOf(index));
    }

    void writeArithmetic(Command command) {
        writeCommand(command.name().toLowerCase(), "", "");
    }

    void writeLabel(String label) {
        writeCommand("label", label, "");
    }

    void writeGoto(String label) {
        writeCommand("goto", label, "");
    }

    void writeIf(String label) {
        writeCommand("if-goto", label, "");
    }

    void writeCall(String name, int nArgs) {
        writeCommand("call", name, String.valueOf(nArgs));
    }

    void writeFunction(String name, int nArgs) {
        writeCommand("function", name, String.valueOf(nArgs));
    }

    void writeReturn() {
        writeCommand("return", "", "");
    }

    void writeCommand(String cmd, String arg1, String arg2) {
        writer.println(cmd + " " + arg1 + " " + arg2);
    }

    void close() {
        writer.close();
    }
}

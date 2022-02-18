package io.github.xmchxup;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class Test {
    public static void main(String[] args) {
        Kind[] values = Kind.values();
        for (Kind value : values) {
            System.out.println(value.name());
        }
    }
}

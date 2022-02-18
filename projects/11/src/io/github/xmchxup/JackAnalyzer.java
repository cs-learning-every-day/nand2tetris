package io.github.xmchxup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("read README.md");
        } else {
            String path = args[0];
            File file = new File(path);
            List<File> jackFiles = new ArrayList<>();

            if (file.isDirectory()) {
                jackFiles = getJackFiles(file);
            } else if (file.isFile()) {
                if (!path.endsWith(".jack")) {
                    return;
                }
                jackFiles.add(file);
            }

            String fileOutputPath = "";
            String tokenFileOutputPath = "";

            for (File f : jackFiles) {
                String p = f.getAbsolutePath();
                String pathWithoutSuffix = p.substring(0, p.lastIndexOf("."));
                // 这里写文件 出现了一个bug: XXXt.xml 和 XXXT.xml写的是同一个文件
                fileOutputPath = pathWithoutSuffix + "Tmp.xml";
                tokenFileOutputPath = pathWithoutSuffix + "TmpT.xml";

                var compilationEngine = new CompilationEngine(f, new File(fileOutputPath),
                        new File(tokenFileOutputPath));
                compilationEngine.compileClass();

                System.out.println("File created : " + fileOutputPath);
                System.out.println("File created : " + tokenFileOutputPath);
            }
        }
    }

    private static List<File> getJackFiles(File directory) {
        File[] files = directory.listFiles();
        Objects.requireNonNull(files);

        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().endsWith(".jack"))
                .collect(Collectors.toList());
    }
}

package com.flowapp.GasLine.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static void printOut(String text) {
        try (var fw = new FileWriter("out.text", StandardCharsets.UTF_8, true)) {
            fw.append(text).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clear() {
        final File file = new File("out.text");
        if (file.exists()) {
            boolean delete = file.delete();
        }
    }
}

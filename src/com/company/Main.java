package com.company;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.Vector;

public class Main {
    public static String[] args;
    public static void main(String[] args) {
        Main.args = args;
        Fingerprint.initializeFingerprints();
        if (args.length > 0) {
            String filename = args[0];
            String str;
            try {
                str = new String(Files.readAllBytes(Paths.get(filename)), Charset.forName("ISO-8859-1"));
                str = str.replaceAll("\\r\\n?", "\n");
            } catch (Exception e) {
                System.err.println("File couldn't be opened for reading.");
                return;
            }
            int dim = 2;
            if (Arrays.asList(args).contains("-u") || Arrays.asList(args).contains("--unefunge")) {
                dim = 1;
            } else if (Arrays.asList(args).contains("-t") || Arrays.asList(args).contains("--trefunge")) {
                dim = 3;
            } else if (Arrays.asList(args).contains("-d") || Arrays.asList(args).contains("--dimensions")) {
                int i = Arrays.asList(args).indexOf("-d");
                if (i == -1) i = Arrays.asList(args).indexOf("--dimensions");
                dim = Integer.parseInt(args[i+1]);
            }
            long now = System.nanoTime();
            FungeSpace space = new FungeSpace(str, 2);
            space.runProgram();
            System.err.println("\nProgram took " + (System.nanoTime() - now)/1000000 + "ms to construct and run.");
        } else {
            System.err.println("You must specify a file to run.");
        }
    }
}

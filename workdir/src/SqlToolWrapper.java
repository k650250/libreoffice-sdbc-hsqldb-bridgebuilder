// -*- coding: UTF-8; -*-

package com.k650250.odb;

import com.k650250.odb.ODBFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

class SqlToolWrapper {
    public static void main(String[] args) {
        try (final ODBFile odbFile = ODBFile.open(args[0])) {
            final ProcessBuilder pb = new ProcessBuilder(buildCommandLine(args, odbFile.toUrl()));
            pb.inheritIO();
            pb.start().waitFor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> buildCommandLine(String[] args, String url) {
        String loginParams = "user=sa";
        int startIndex = 2;
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("java");
        commandLine.add("-cp");
        commandLine.add(System.getProperty("java.class.path"));
        commandLine.add("org.hsqldb.util.SqlTool");
        commandLine.add("--inlineRc");
        if (args.length >= startIndex) {
            if (!args[1].equals("--")) {
                loginParams = args[1];
                startIndex++;
            }   
        }
        commandLine.add(String.format("url=%s,%s", url, loginParams));
        for (int i = startIndex; i < args.length; i++) {
            commandLine.add(args[i]);
        }

        return commandLine;
    }
}

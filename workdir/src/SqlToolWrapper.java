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
        int status = 1;

        try (final ODBFile odbFile = ODBFile.open(args[0])) {
            final ProcessBuilder pb = new ProcessBuilder(buildCommandLine(args, odbFile.toUrl()));
            status = pb.inheritIO().start().waitFor();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(status);
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

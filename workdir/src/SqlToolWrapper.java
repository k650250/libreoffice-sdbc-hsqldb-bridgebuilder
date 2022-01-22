// -*- coding: utf-8; -*-

package com.k650250.odb;

import com.k650250.odb.ODBFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

class SqlToolWrapper {
    /**
     * 静的メソッド
     * SqlToolWrapper を Main-Class とした場合
     * エントリーポイントとされるメソッドであり
     * objectMain メソッドのラッパーメソッド
     */
    public static void main(String[] args) {
        System.exit(SqlToolWrapper.objectMain(args));
    }

    /**
     * 静的メソッド
     * org.hsqldb.util.SqlTool
     * を子プロセスとして実行し
     * 終了ステータスを返却
     */
    public static int objectMain(String[] args) {
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
        
        return status;
    }

    // org.hsqldb.util.SqlTool に渡す引数を含むコマンドラインを作成
    private static List<String> buildCommandLine(String[] args, String url) {
        String loginParams = "user=sa";
        int startIndex = 2;
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("java");
        commandLine.add("-cp");
        commandLine.add(System.getProperty("java.class.path"));
        commandLine.add("org.hsqldb.util.SqlTool");
        commandLine.add("--inlineRc");
        if (args.length >= startIndex && !args[1].equals("--")) {
            loginParams = args[1];
            startIndex++;
        }
        commandLine.add(String.format("url=%s,%s", url, loginParams));
        for (int i = startIndex; i < args.length; i++) {
            commandLine.add(args[i]);
        }

        return commandLine;
    }
}

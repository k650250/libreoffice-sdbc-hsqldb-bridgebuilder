// -*- coding: utf-8; -*-

package com.k650250.odb.testing;

import com.k650250.odb.ODBFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

class TestDriver {
    public static void main(String[] args) {
        // ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
        //System.setProperty("java.util.logging.config.file", "logging.properties");
        
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("org.hsqldb.jdbcDriver が見つかりません。");
        }

        try (final ODBFile odbFile = ODBFile.open("sample.odb");
            final Connection conn = DriverManager.getConnection(odbFile.toUrl(), "sa", "");
            final Statement st = conn.createStatement()) {

            String sql;

            System.out.println("[更新前のデータ一覧]");
            //sql = "SELECT * FROM \"t_sample\"";  // テーブル
            //sql = "SELECT * FROM \"v_sample\"";  // ビュー
            //sql = odbFile.getQuery("q_sample").toString();         // クエリー (問合せ文中にクエリー名が含まれない場合)
            sql = odbFile.getQuery("q_SAMPLE").expand().toString();  // クエリー (問合せ文中にクエリー名が含まれる場合)
            try (final ResultSet records = st.executeQuery(sql)) {
                while (records.next()) {
                    System.out.println(records.getString("key") + "\t" + records.getString("value"));
                }
            }

            // データ挿入
            sql = "INSERT INTO \"t_sample\"(\"value\") VALUES(?)";
            try (final PreparedStatement prep = conn.prepareStatement(sql);
                final Scanner scan = new Scanner(System.in)) {
                
                System.out.print("追加データを入力してください: ");
                prep.setString(1, scan.nextLine());
                prep.executeUpdate();
            }

            System.out.println("[更新後のデータ一覧]");
            sql = "SELECT * FROM \"t_sample\"";
            try (final ResultSet records = st.executeQuery(sql)) {
                while (records.next()) {
                    System.out.println(records.getString("key") + "\t" + records.getString("value"));
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("考えられる原因:");
            System.err.println("- hsqldb.jar が正常に読み込まれていない");
            System.err.println("- このデータベースを別のプロセスが使用中");
            System.err.println("- 問合せ文中にクエリー名が含まれる");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

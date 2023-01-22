// -*- coding: utf-8; -*-

package com.k650250.odb.testing

import com.k650250.odb.ODBFile
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Scanner

fun main() {
    // ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
    //System.setProperty("java.util.logging.config.file", "logging.properties")

    try {
        Class.forName("org.hsqldb.jdbcDriver")

        ODBFile.open("sample.odb").use { odbFile ->
            val con = DriverManager.getConnection(odbFile.toUrl(), "sa", "")
            val st = con.createStatement();
            var sql: String
            var records: ResultSet

            println("[更新前のデータ一覧]")
            //sql = """SELECT * FROM "t_sample" """  // テーブル
            //sql = """SELECT * FROM "v_sample" """  // ビュー
            //sql = odbFile.getQuery("q_sample").toString()         // クエリー (問合せ文中にクエリー名が含まれない場合)
            sql = odbFile.getQuery("q_SAMPLE").expand().toString()  // クエリー (問合せ文中にクエリー名が含まれる場合)
            records = st.executeQuery(sql)
            while (records.next()) {
                println("${records.getString("key")}\t${records.getString("value")}")
            }
            records.close()

            // データ挿入
            sql = """INSERT INTO "t_sample"("value") VALUES(?)"""
            val prep = con.prepareStatement(sql)
            val sc = Scanner(System.`in`, System.getProperty("native.encoding"))
            print("追加データを入力してください: ")
            prep.setString(1, sc.nextLine())
            prep.executeUpdate()
            sc.close()
            prep.close()

            println("[更新後のデータ一覧]")
            sql = """SELECT * FROM "t_sample" """
            records = st.executeQuery(sql)
            while (records.next()) {
                println("${records.getString("key")}\t${records.getString("value")}")
            }
            records.close()

            st.close()
            con.close()
        }
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        System.err.println("org.hsqldb.jdbcDriver が見つかりません。")
    } catch (e: SQLException) {
        e.printStackTrace()
        System.err.println("考えられる原因:")
        System.err.println("- hsqldb.jar が正常に読み込まれていない")
        System.err.println("- このデータベースを別のプロセスが使用中")
        System.err.println("- 問合せ文中にクエリー名が含まれる")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// -*- coding: utf-8; -*-

@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.40")

import com.k650250.odb.ODBFile
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Scanner

// ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
//System.setProperty("java.util.logging.config.file", "logging.properties")

try {
    Class.forName("org.hsqldb.jdbcDriver")

    ODBFile.open("sample.odb").use { odbFile ->
        DriverManager.getConnection(odbFile.toUrl(), "sa", "").use { con ->
            con.createStatement().use { st ->
                var sql: String

                println("[更新前のデータ一覧]")
                //sql = """SELECT * FROM "t_sample" """  // テーブル
                //sql = """SELECT * FROM "v_sample" """  // ビュー
                //sql = odbFile.getQuery("q_sample").toString()         // クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQuery("q_SAMPLE").expand().toString()  // クエリー (問合せ文中にクエリー名が含まれる場合)
                st.executeQuery(sql).use { records ->
                    while (records.next()) {
                        println("${records.getString("key")}\t${records.getString("value")}")
                    }
                }

                // データ挿入
                sql = """INSERT INTO "t_sample"("value") VALUES(?)"""
                con.prepareStatement(sql).use { prep ->
                    Scanner(System.`in`, System.getProperty("native.encoding")).use { sc ->
                        println("追加データを入力してください。")
                        prep.setString(1, sc.nextLine())
                        prep.executeUpdate()
                    }
                }

                println("[更新後のデータ一覧]")
                sql = """SELECT * FROM "t_sample" """
                st.executeQuery(sql).use { records ->
                    while (records.next()) {
                        println("${records.getString("key")}\t${records.getString("value")}")
                    }
                }
            }
        }
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

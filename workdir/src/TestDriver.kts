// -*- coding: UTF-8; -*-
// kotlin-stdlib-jdk7

import com.k650250.odb.ODBFile
import java.sql.DriverManager
import java.sql.SQLException

// ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
//System.setProperty("java.util.logging.config.file", "logging.properties")

try {
    Class.forName("org.hsqldb.jdbcDriver")

    ODBFile.open("sample.odb").use { odbFile ->
        DriverManager.getConnection(odbFile.toUrl(), "sa", "").use { conn ->
            conn.createStatement().use { st ->
                var sql: String

                println("[更新前のデータ一覧]")
                //sql = """SELECT * FROM "t_sample" """    // テーブル
                //sql = """SELECT * FROM "v_sample" """    // ビュー
                sql = odbFile.getQueryCommand("q_sample")  // クエリー
                st.executeQuery(sql).use { records ->
                    while (records.next()) {
                        println("${records.getString("key")}\t${records.getString("value")}")
                    }
                }

                // データ挿入
                sql = """INSERT INTO "t_sample"("value") VALUES(?)"""
                conn.prepareStatement(sql).use { prep ->
                    print("追加データを入力してください: ")
                    prep.setString(1, readLine())
                    prep.executeUpdate()
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
} catch (e: Exception) {
    e.printStackTrace()
}
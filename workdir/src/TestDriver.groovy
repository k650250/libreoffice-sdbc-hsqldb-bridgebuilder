// -*- coding: utf-8; -*-

@GrabConfig(systemClassLoader = true)

import com.k650250.odb.ODBFile
import java.sql.DriverManager
import java.sql.SQLException

// ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
//System.setProperty("java.util.logging.config.file", "logging.properties")

try {
    Class.forName("org.hsqldb.jdbcDriver")

    ODBFile.open("sample.odb").withCloseable { odbFile ->
        DriverManager.getConnection(odbFile.toUrl(), "sa", "").withCloseable { con ->
            con.createStatement().withCloseable { st ->
                String sql

                println "[更新前のデータ一覧]"
                //sql = """SELECT * FROM "t_sample" """  // テーブル
                //sql = """SELECT * FROM "v_sample" """  // ビュー
                //sql = odbFile.getQuery("q_sample").toString()           // クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQuery("q_SAMPLE").expand().toString()  // クエリー (問合せ文中にクエリー名が含まれる場合)
                st.executeQuery(sql).withCloseable { records ->
                    while (records.next()) {
                        println "${records.getString("key")}\t${records.getString("value")}"
                    }
                }

                // データ挿入
                sql = """INSERT INTO "t_sample"("value") VALUES(?)"""
                con.prepareStatement(sql).withCloseable { prep ->
                    (new Scanner(System.in, System.getProperty("native.encoding"))).withCloseable { sc ->
                        println "追加データを入力してください。"
                        prep.setString(1, sc.nextLine())
                        prep.executeUpdate()
                    }
                }

                println "[更新後のデータ一覧]"
                sql = """SELECT * FROM "t_sample" """
                st.executeQuery(sql).withCloseable { records ->
                    while (records.next()) {
                        println "${records.getString("key")}\t${records.getString("value")}"
                    }
                }
            }
        }
    }
} catch (ClassNotFoundException e) {
    e.printStackTrace
    System.err.println "考えられる原因:"
    System.err.println "- hsqldb.jar がクラス・パス指定されていない"
    System.err.println "- odb.jar がクラス・パス指定されていない"
} catch (SQLException e) {
    e.printStackTrace
    System.err.println "考えられる原因:"
    System.err.println "- hsqldb.jar が正常に読み込まれていない"
    System.err.println "- このデータベースを別のプロセスが使用中"
    System.err.println "- 問合せ文中にクエリー名が含まれる"
}

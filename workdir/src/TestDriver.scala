// -*- coding: UTF-8; -*-

package com.k650250.odb.testing.scala

import com.k650250.odb.ODBFile
import java.sql.{ DriverManager, SQLException }
import scala.util.Using

object TestDriver:
    @main
    def test(): Unit =
        try
            Class.forName("org.hsqldb.jdbcDriver")

            Using.Manager { use =>
                val odbFile = use(ODBFile.open("sample.odb"))
                val conn = use(DriverManager.getConnection(odbFile.toUrl(), "sa", ""))
                val st = use(conn.createStatement())

                var sql: String = null

                println("[更新前のデータ一覧]")
                //sql = """SELECT * FROM "t_sample" """  // テーブル
                //sql = """SELECT * FROM "v_sample" """  // ビュー
                //sql = odbFile.getQuery("q_sample").toString()         // クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQuery("q_SAMPLE").expand().toString()  // クエリー (問合せ文中にクエリー名が含まれる場合)
                Using(st.executeQuery(sql)) { records =>
                    while (records.next())
                        println(s"${records.getString("key")}\t${records.getString("value")}")
                    end while
                }

                // データ挿入
                sql = """INSERT INTO "t_sample"("value") VALUES(?)"""
                Using(conn.prepareStatement(sql)) { prep =>
                    print("追加データを入力してください: ")
                    prep.setString(1, io.StdIn.readLine())
                    prep.executeUpdate()
                }

                println("[更新後のデータ一覧]")
                sql = """SELECT * FROM "t_sample" """
                Using(st.executeQuery(sql)) { records =>
                    while (records.next())
                        println(s"${records.getString("key")}\t${records.getString("value")}")
                    end while
                }
            }
        catch
            case e: ClassNotFoundException => {
                e.printStackTrace()
                System.err.println("org.hsqldb.jdbcDriver が見つかりません。")
            }
            case e: SQLException => {
                e.printStackTrace()
                System.err.println("考えられる原因:")
                System.err.println("- hsqldb.jar が正常に読み込まれていない")
                System.err.println("- このデータベースを別のプロセスが使用中")
                System.err.println("- 問合せ文中にクエリー名が含まれる")
            }
            case e: Exception => {
                e.printStackTrace()
            }
        end try
    end test
end TestDriver

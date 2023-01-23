# -*- coding: utf-8; -*-

import sys
import traceback
from contextlib import closing
from com.k650250.odb import ODBFile
from java.lang import Class, ClassNotFoundException, System
from java.sql import DriverManager, SQLException
from java.util import Scanner

# ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
#System.setProperty("java.util.logging.config.file", "logging.properties")

try:
    Class.forName("org.hsqldb.jdbcDriver")

    with closing(ODBFile.open("sample.odb")) as odbFile:
        with closing(DriverManager.getConnection(odbFile.toUrl(), "sa", "")) as con:
            with closing(con.createStatement()) as st:
                System.out.println(u"[更新前のデータ一覧]")
                #sql = 'SELECT * FROM "t_sample"'  # テーブル
                #sql = 'SELECT * FROM "v_sample"'  # ビュー
                #sql = odbFile.getQuery("q_sample").toString()          # クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQuery("q_SAMPLE").expand().toString()  # クエリー (問合せ文中にクエリー名が含まれる場合)
                with closing(st.executeQuery(sql)) as records:
                    while records.next():
                        System.out.println(u"{}\t{}".format(records.getString("key"), records.getString("value")))

                # データ挿入
                sql = 'INSERT INTO "t_sample"("value") VALUES(?)'
                with closing(con.prepareStatement(sql)) as prep:
                    with closing(Scanner(eval("System.in"), System.getProperty("native.encoding"))) as sc:
                        System.out.print(u"追加データを入力してください: ")
                        prep.setString(1, sc.nextLine())
                        prep.executeUpdate()

                System.out.println(u"[更新後のデータ一覧]")
                sql = 'SELECT * FROM "t_sample"'
                with closing(st.executeQuery(sql)) as records:
                    while records.next():
                        System.out.println(u"{}\t{}".format(records.getString("key"), records.getString("value")))

except ClassNotFoundException as e:
    e.printStackTrace()
    System.err.println(u"org.hsqldb.jdbcDriver が見つかりません。")
except SQLException as e:
    e.printStackTrace()
    System.err.println(u"考えられる原因:")
    System.err.println(u"- hsqldb.jar が正常に読み込まれていない")
    System.err.println(u"- このデータベースを別のプロセスが使用中")
    System.err.println(u"- 問合せ文中にクエリー名が含まれる")
except Exception as e:
    traceback.print_exc(file=sys.stderr)

# -*- coding: UTF-8; -*-

from contextlib import contextmanager
@contextmanager
def use(closeableObject):
    try:
        yield closeableObject
    finally:
        closeableObject.close()
import sys
import traceback
from com.k650250.odb import ODBFile
from java.lang import Class, ClassNotFoundException, System
from java.sql import DriverManager, SQLException
from java.util import Scanner

# ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
#System.setProperty("java.util.logging.config.file", "logging.properties")

try:
    Class.forName("org.hsqldb.jdbcDriver")

    with use(ODBFile.open("sample.odb")) as odbFile:
        with use(DriverManager.getConnection(odbFile.toUrl(), "sa", "")) as conn:
            with use(conn.createStatement()) as st:
                print(u"[更新前のデータ一覧]")
                #sql = 'SELECT * FROM "t_sample"'  # テーブル
                #sql = 'SELECT * FROM "v_sample"'  # ビュー
                #sql = odbFile.getQueryCommand("q_sample").toString()          # クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQueryCommand("q_SAMPLE").expand().toString()  # クエリー (問合せ文中にクエリー名が含まれる場合)
                with use(st.executeQuery(sql)) as records:
                    while records.next():
                        print(u"{}\t{}".format(records.getString("key"), records.getString("value")))

                # データ挿入
                sql = 'INSERT INTO "t_sample"("value") VALUES(?)'
                with use(conn.prepareStatement(sql)) as prep:
                    System.out.print(u"追加データを入力してください: ")
                    prep.setString(1, Scanner(System.in).nextLine())
                    prep.executeUpdate()

                print(u"[更新後のデータ一覧]")
                sql = 'SELECT * FROM "t_sample"'
                with use(st.executeQuery(sql)) as records:
                    while records.next():
                        print(u"{}\t{}".format(records.getString("key"), records.getString("value")))

except ClassNotFoundException as e:
    e.printStackTrace()
    print >> sys.stderr, u"org.hsqldb.jdbcDriver が見つかりません。"
except SQLException as e:
    e.printStackTrace()
    print >> sys.stderr, u"考えられる原因:"
    print >> sys.stderr, u"- hsqldb.jar が正常に読み込まれていない"
    print >> sys.stderr, u"- このデータベースを別のプロセスが使用中"
    print >> sys.stderr, u"- 問合せ文中にクエリー名が含まれる"
except Exception as e:
    traceback.print_exc(file=sys.stderr)

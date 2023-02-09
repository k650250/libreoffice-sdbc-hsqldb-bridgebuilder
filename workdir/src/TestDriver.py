#! jython
# -*- coding: utf-8; -*-

from contextlib import closing
from java.io import BufferedReader, File, InputStreamReader
from java.lang import Class, ClassNotFoundException, System
from java.net import URL, URLClassLoader
from java.sql import SQLException
from java.util import Properties
import jarray
import os.path
import sys
import traceback

# ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
#System.setProperty("java.util.logging.config.file", "logging.properties")

def add_classpath():
    """`-J-cp`オプションが存在しない処理系への対処"""
    libdir = os.path.join(os.path.dirname(__file__), "..", "lib")
    urls = jarray.array([
        File(os.path.join(libdir, "odb.jar")).toURI().toURL(),
        File(os.path.join(libdir, "hsqldb.jar")).toURI().toURL()
    ], URL)
    cl = URLClassLoader(urls)
    return cl

info = Properties()
info.setProperty("user", "sa")
info.setProperty("password", "")

try:
    with closing(add_classpath()) as cl, \
        closing(Class.forName("com.k650250.odb.ODBFile", True, cl)
            .open("sample.odb")) as odbFile, \
        closing(Class.forName("org.hsqldb.jdbcDriver", True, cl)
            .newInstance().connect(odbFile.toUrl(), info)) as con, \
        closing(con.createStatement()) as st:

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
            System.out.println(u"追加データを入力してください。")
            prep.setString(1, BufferedReader(InputStreamReader(
                eval("System.in"),
                System.getProperty("native.encoding"))).readLine())
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

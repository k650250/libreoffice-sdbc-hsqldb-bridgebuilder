# -*- coding: utf-8; -*-

require 'java'
java_import 'com.k650250.odb.ODBFile'
java_import java.lang.ClassNotFoundException
java_import java.lang.System
java_import java.sql.DriverManager
java_import java.sql.SQLException
java_import java.util.Scanner

# ロギングの環境設定ファイル (※ HSQLDB-1.8.0.10 では使用しない)
#System.setProperty("java.util.logging.config.file", "logging.properties")

def closing(thing)
    if block_given?
        yield thing
    end
ensure
    thing.close
end

begin
    loader = java.lang.Thread.current_thread.getContextClassLoader
    java.lang.Class.forName("org.hsqldb.jdbcDriver", true, loader)

    closing(ODBFile.open("sample.odb")) do |odbFile|
        closing(DriverManager.getConnection(odbFile.toUrl, "sa", "")) do |con|
            closing(con.createStatement()) do |st|
                puts "[更新前のデータ一覧]"
                #sql = 'SELECT * FROM "t_sample" '  # テーブル
                #sql = 'SELECT * FROM "v_sample" '  # ビュー
                #sql = odbFile.getQuery("q_sample").toString         # クエリー (問合せ文中にクエリー名が含まれない場合)
                sql = odbFile.getQuery("q_SAMPLE").expand.toString  # クエリー (問合せ文中にクエリー名が含まれる場合)
                closing(st.executeQuery(sql)) do |records|
                    while records.next do
                        puts "#{records.getString("key")}\t#{records.getString("value")}"
                    end
                end

                # データ挿入
                sql = 'INSERT INTO "t_sample"("value") VALUES(?)'
                closing(con.prepareStatement(sql)) do |prep|
                    closing(Scanner.new(System.in, System.getProperty("native.encoding"))) do |sc|
                        puts "追加データを入力してください。"
                        prep.setString(1, sc.nextLine)
                        prep.executeUpdate
                    end
                end

                puts "[更新後のデータ一覧]"
                sql = 'SELECT * FROM "t_sample" '
                closing(st.executeQuery(sql)) do |records|
                    while records.next do
                        puts "#{records.getString("key")}\t#{records.getString("value")}"
                    end
                end
            end
        end
    end
rescue ClassNotFoundException => e
    e.printStackTrace
    STDERR.puts "考えられる原因:"
    STDERR.puts "- hsqldb.jar がクラス・パス指定されていない"
    STDERR.puts "- odb.jar がクラス・パス指定されていない"
rescue SQLException => e
    e.printStackTrace
    STDERR.puts "考えられる原因:"
    STDERR.puts "- hsqldb.jar が正常に読み込まれていない"
    STDERR.puts "- このデータベースを別のプロセスが使用中"
    STDERR.puts "- 問合せ文中にクエリー名が含まれる"
end

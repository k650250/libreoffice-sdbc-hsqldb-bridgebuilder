;; -*- coding: utf-8; -*-

(import '[java.io BufferedReader InputStreamReader]
        '[java.net URL URLClassLoader]
        '[java.sql SQLException]
        '[java.util Properties])
(require '[clojure.java.io :as io])
(use 'clojure.stacktrace)

;; 「-cp」オプションが存在しない処理系への対処
(defn- -add-classpath []
    (let [libdir (io/file (.getParent (io/file *file*)) ".." "lib")
                urls [(io/as-url (io/file libdir "odb.jar"))
                      (io/as-url (io/file libdir "hsqldb.jar"))]
                loader (URLClassLoader. (into-array URL urls))]
        loader))

(defn -main []
    (let [info (doto (Properties.) (.setProperty "user" "sa")
                                   (.setProperty "password" ""))]
        (try
            (with-open [loader (-add-classpath)]
                (with-open [odbFile (-> (-> (Class/forName "com.k650250.odb.ODBFile" true loader) (.getMethod "open" (into-array [String]))) (.invoke [] (into-array ["sample.odb"])))]
                    (with-open [con (-> (-> (Class/forName "org.hsqldb.jdbcDriver" true loader) (.newInstance)) (.connect (.toUrl odbFile) info))]
                        (with-open [st (.createStatement con)]
                            (.println System/out "[更新前のデータ一覧]")
                            (let [sql (str (-> (-> odbFile (.getQuery "q_SAMPLE")) (.expand)))]
                                (with-open [records (.executeQuery st sql)]
                                    (while (.next records)
                                        (.println System/out (str (.getString records "key") "\t" (.getString records "value"))))))

                            ;; データ挿入
                            (.println System/out "追加データを入力してください。")
                            (let [value (.readLine (BufferedReader. (InputStreamReader. System/in (System/getProperty "native.encoding"))))]
                                (with-open [prep (.prepareStatement con "INSERT INTO \"t_sample\"(\"value\") VALUES(?)")]
                                    (.setString prep 1 value)
                                    (.executeUpdate prep)))

                            (.println System/out "[更新後のデータ一覧]")
                            (let [sql "SELECT * FROM \"t_sample\""]
                                (with-open [records (.executeQuery st sql)]
                                    (while (.next records)
                                        (.println System/out (str (.getString records "key") "\t" (.getString records "value"))))))))))

            (catch ClassNotFoundException e
                (.println System/err "[ERROR] ClassNotFoundException")
                (.println System/err "考えられる原因:")
                (.println System/err "- hsqldb.jar がクラス・パス指定されていない")
                (.println System/err "- odb.jar がクラス・ パス指定されていない")
                (.println System/err (str "Stack Trace: " (.getMessage e)) (print-stack-trace e)))
            (catch SQLException e
                (.println System/err "[ERROR] SQLException")
                (.println System/err "考えられる原因:")
                (.println System/err "- hsqldb.jar が正常に読み込まれていない")
                (.println System/err "- このデータベースを別のプロセスが使用中")
                (.println System/err "- 問合せ文中にクエリー名が含まれる")
                (.println System/err (str "Stack Trace: " (.getMessage e)) (print-stack-trace e))))))

(-main)

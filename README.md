# libreoffice-sdbc-hsqldb-bridgebuilder
OpenOffice / LibreOffice Base のデータベースファイル (*.odb) の埋め込み HSQLDB (HyperSQL Database Engine) に接続する為のツール。

例えば、`./sample.odb`の埋め込み HSQLDB にスタンドアロンモードで接続する際、

[Java]
```Java
try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "")) {
    // 諸々の処理
}
```
[Kotlinscript]
```Kotlin
      DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "").use { conn ->
          // 諸々の処理
      }
```

では接続不可能ですが、当ツールを導入し、次のように修正することによって接続可能となります。

[Java]
```Diff
- try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "")) {
+ try (ODBFile odbFile = ODBFile.open("sample.odb"); Connection conn = DriverManager.getConnection(odbFile.toUrl(), "sa", "")) {
    // 処理
}
```
[Kotlinscript]
```Diff
+ ODBFile.open("sample.odb").use { odbFile ->
-     DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "").use { conn ->
+     DriverManager.getConnection(odbFile.toUrl(), "sa", "").use { conn ->
          // 処理
      }
+ }
```

上記コードは、Javaは`workdir/src/TestDriver.java`、Kotlinは`workdir/src/TestDriver.kts`を参考。

## このツールの機能が及ぶ範囲
||読|書|
|---:|:---:|:---:|
|テーブル（とビュー）|&#10004;|&#10004;|
|クエリー|&#10004;|&#10006;|
|フォーム|&#10006;|&#10006;|
|レポート|&#10006;|&#10006;|

## コンパイラ／インタプリタ
- 必須
  - javac 19.0.1
- 任意：テストドライバプログラム実行用
  - kotlinc-jvm 1.7.21
  - Jython 2.7.1
  - Scala 3.2.1

## ダウンロードとビルド
<details>
<summary>macOS / Linux</summary>

1. コンソール（ターミナル）を開く
2. ホームディレクトリ（`~`）下の任意の空のディレクトリを、作業ディレクトリとする
3. コンソールに、次の内容を貼り付け、エンターキーを押す

```bash
mkdir -p './tmp' ; cd './tmp' ; curl -sL 'https://github.com/k650250/libreoffice-sdbc-hsqldb-bridgebuilder/archive/refs/heads/main.zip' | jar -x 'libreoffice-sdbc-hsqldb-bridgebuilder-main/workdir/' ; curl -sL 'https://ja.osdn.net/frs/g_redir.php?m=jaist&f=hsqldb%2Fhsqldb%2Fhsqldb_1_8_0%2Fhsqldb_1_8_0_10.zip' | jar -x 'hsqldb/lib/hsqldb.jar' ; cd '../' ; mv -f ./tmp/libreoffice-sdbc-hsqldb-bridgebuilder-main/workdir/* '.' ; mv -f './tmp/hsqldb/lib/hsqldb.jar' './lib/.' ; rm -rf 'tmp' ; javac -encoding 'UTF-8' ./src/*.java -d '.' ; jar -cfm './lib/odb.jar' './mf.txt' 'com' ; jar -cfm './lib/odbsql.jar' './mfsql.txt' ; ls
```

</details>
<details>
<summary>Windows</summary>

1. エクスプローラーを開く
2. 「ドキュメント」フォルダ下の任意の空のフォルダを開く
3. アドレスバーに`powershell`と入力し、エンターキーを押す
4. Windows PowerShell ウィンドウに、次の内容を貼り付け、エンターキーを押す

```powershell
New-Item -Name "tmp" -ItemType "directory" -Force > $null ; Invoke-WebRequest -Uri "https://github.com/k650250/libreoffice-sdbc-hsqldb-bridgebuilder/archive/refs/heads/main.zip" -OutFile ".\tmp/main.zip" ; Invoke-WebRequest -Uri "https://ja.osdn.net/frs/g_redir.php?m=jaist&f=hsqldb%2Fhsqldb%2Fhsqldb_1_8_0%2Fhsqldb_1_8_0_10.zip" -OutFile ".\tmp\hsqldb_1_8_0_10.zip" ; Expand-Archive -Path ".\tmp\main.zip" -DestinationPath ".\tmp" -Force ; Expand-Archive -Path ".\tmp\hsqldb_1_8_0_10.zip" -DestinationPath ".\tmp" -Force ; Get-ChildItem ".\tmp\libreoffice-sdbc-hsqldb-bridgebuilder-main\workdir" -Include "*" -Recurse | Move-Item -Force ; Move-Item -Path ".\tmp\hsqldb\lib\hsqldb.jar" -Destination ".\lib" -Force ; Remove-item -Path ".\tmp" -Recurse ; javac -encoding "UTF-8" "./src/*.java" -d "." ; jar -cfm "./lib/odb.jar" "./mf.txt" "com" ; jar -cfm "./lib/odbsql.jar" "./mfsql.txt" ; Get-ChildItem
```
</details>

最終的に次のようなファイル構成となる。
<pre><code>
&nbsp;./<br />
&#9507; com/<br />
&#65049;
&#9507; lib/<br />
&#9475;&#9507; dummy<br />
&#9475;&#9507; hsqldb.jar<br />
&#9475;&#9507; odb.jar<br />
&#9475;&#9495; odbsql.jar<br />
&#9507; src/<br />
&#9475;&#9507; ODBFile.java<br />
&#9475;&#9507; Query.java<br />
&#9475;&#9507; SqlToolWrapper.java<br />
&#9475;&#9507; TestDriver.java<br />
&#9475;&#9507; TestDriver.main.kts<br />
&#9475;&#9507; TestDriver.py<br />
&#9475;&#9495; TestDriver.scala<br />
&#9507; init.sql<br />
&#9507; logging.properties<br />
&#9507; mf.txt<br />
&#9507; mfsql.txt<br />
&#9495; sample.odb<br />
</code></pre>

## 動作確認

</details>
<details>
<summary>macOS / Linux</summary>

### JavaプログラムやJVM言語のスクリプトを用いた`com.k650250.odb.ODBFile`の動作確認

#### `./lib/odb.jar`に埋め込まれたテストドライバプログラムを実行する

```bash
java -jar "./lib/odb.jar"
```

#### Kotlinscript (`*.kts`) で記述されたテストドライバプログラムを実行する

```bash
kotlinc-jvm -cp "./lib/hsqldb.jar:./lib/odb.jar" -script "./src/TestDriver.main.kts"
```

#### Scala (`*.scala`) で記述されたテストドライバプログラムを実行する

```bash
scala -cp "./lib/hsqldb.jar:./lib/odb.jar" ./src/TestDriver.scala
```

### SQL コマンドラインシェルの動作確認

#### SQL スクリプトファイル`./init.sql`を実行する

```bash
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=,charset=utf-8" -- "init.sql"
```

※ 上記コマンドラインの`--`以降の引数が、`./lib/hsqldb.jar`の`org.hsqldb.util.SqlTool`に渡される。

#### 現在のテーブル`t_sample`の中身を全件表示する

```bash
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=" -- --sql 'SELECT * FROM "t_sample";'
```

#### REPL を起動する

```bash
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=,charset=utf-8"
```

</details>
<details>
<summary>Windows</summary>

### JavaプログラムやJVM言語のスクリプトを用いた`com.k650250.odb.ODBFile`の動作確認

#### `./lib/odb.jar`に埋め込まれたテストドライバプログラムを実行する

```powershell
java -jar "./lib/odb.jar"
```

#### Kotlinscript (`*.kts`) で記述されたテストドライバプログラムを実行する

```powershell
kotlinc-jvm -cp '"./lib/hsqldb.jar;./lib/odb.jar"' -script "./src/TestDriver.main.kts"
```

#### Jython/JPython (`*.py`) で記述されたテストドライバプログラムを実行する

```powershell
jython "-Dfile.encoding=MS932" -J-cp "./lib/hsqldb.jar;./lib/odb.jar" "./src/TestDriver.py"
```

#### Scala (`*.scala`) で記述されたテストドライバプログラムを実行する

```powershell
cs launch scala3 -- -cp "./lib/hsqldb.jar;./lib/odb.jar" ./src/TestDriver.scala
```

### SQL コマンドラインシェルの動作確認

#### SQL スクリプトファイル`./init.sql`を実行する

```powershell
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=,charset=utf-8" -- "init.sql"
```

※ 上記コマンドラインの`--`以降の引数が、`./lib/hsqldb.jar`の`org.hsqldb.util.SqlTool`に渡される。

#### 現在のテーブル`t_sample`の中身を全件表示する

```powershell
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=" -- --sql 'SELECT * FROM "t_sample";'
```

#### REPL を起動する

```powershell
java -jar "./lib/odbsql.jar" "sample.odb" "user=sa,password=,charset=cp932"
```

</details>

#### REPL 上での操作

##### 全件表示する

```
SELECT * FROM "t_sample";
```

##### データを追加する

```
INSERT INTO "t_sample"("value") VALUES('あいうえお');
```

##### 確定する

```
COMMIT;
```

##### REPL を終了する

```
\q
```

#### パスワード
`SET PASSWORD "hogehoge";`で`hogehoge`をパスワードに設定でき、`SET PASSWORD "";`で解除できる。但し、パスワードを設定すると LibreOffice で操作できなくなる。パスワードを設定する際は、誤って LibreOffice で開かないように、対象ファイル名の拡張子を変更（例:`sample.odbx`）しておくことを推奨する。

## 使用した外部ツール／ライブラリ
- [HyperSQL Database Engine (HSQLDB) -  Browse Files at SourceForge.net](https://sourceforge.net/projects/hsqldb/files/)
  - **[HSQLDB-1.8.0.10](https://sourceforge.net/projects/hsqldb/files/hsqldb/hsqldb_1_8_0/)**
    - `hsqldb.jar`は、ダウンロードしたzipファイルの中の`lib`ディレクトリの中に存在する
    - `hsqldb.jar`を<b>`workdir/lib`ディレクトリの中に配置する</b>
- [download | LibreOffice(リブレオフィス) - 無料で自由に使えるオフィスソフト - OpenOffice.orgの進化系 - Microsoft Officeと高い相互運用性](https://ja.libreoffice.org/download/download/)

HSQLDB のバージョンは`1.8.0.10`、LibreOffice のバージョンは`7.4.3.2`であることを前提としています。


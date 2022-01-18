# libreoffice-sdbc-hsqldb-bridgebuilder
OpenOffice / LibreOffice Base のデータベースファイル (*.odb) の埋め込み HSQLDB (HyperSQL Database Engine) に接続する為のツール。

例えば、`./sample.odb`の埋め込み HSQLDB にスタンドアロンモードで接続する際、

```
      DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "").use { conn ->
          // 諸々の処理
      }
```

では接続不可能ですが、次のように加筆・修正することによって接続可能となるツールです。

```diff
+ ODBFile.open("sample.odb").use { odbFile ->
-     DriverManager.getConnection("jdbc:hsqldb:file:./sample.odb;shutdown=true", "sa", "").use { conn ->
+     DriverManager.getConnection(odbFile.toUrl(), "sa", "").use { conn ->
          // 諸々の処理
      }
+ }
```

上記コードは、`workdir/src/TestDriver.kts`を参考。

## このツールの機能が及ぶ範囲
||読|書|
|---:|:---:|:---:|
|テーブル（とビュー）|&#10004;|&#10004;|
|クエリー|&#10004;|&#10006;|
|フォーム|&#10006;|&#10006;|
|レポート|&#10006;|&#10006;|

## 準備
1. `workdir`ディレクトリをダウンロード
2. `workdir/lib`ディレクトリの中に`hsqldb.jar`を配置（詳細は後述）
3. コンソールを起動し、`workdir`ディレクトリを作業ディレクトリとする

<pre><code>
&nbsp;./<br />
&#9507; lib/<br />
&#9475;&#9507; dummy<br />
&#9475;&#9495; <b>hsqldb.jar</b><br />
&#9507; src/<br />
&#9475;&#9507; ODBFile.java<br />
&#9475;&#9507; Query.java<br />
&#9475;&#9507; SqlToolWrapper.java<br />
&#9475;&#9507; TestDriver.java<br />
&#9475;&#9507; TestDriver.kt<br />
&#9475;&#9507; TestDriver.kts<br />
&#9475;&#9507; TestDriver.py<br />
&#9475;&#9495; TestDriver.scala<br />
&#9507; init.sql<br />
&#9507; logging.properties<br />
&#9507; mf.txt<br />
&#9507; mfkt.txt<br />
&#9507; mfsql.txt<br />
&#9495; sample.odb<br />
</code></pre>

## `hsqldb.jar`と LibreOffice の入手
- [HyperSQL Database Engine (HSQLDB) -  Browse Files at SourceForge.net](https://sourceforge.net/projects/hsqldb/files/)
  - **[HSQLDB-1.8.0.10](https://sourceforge.net/projects/hsqldb/files/hsqldb/hsqldb_1_8_0/)**
    - `hsqldb.jar`は、ダウンロードしたzipファイルの中の`lib`ディレクトリの中に存在する
    - `hsqldb.jar`を<b>`workdir/lib`ディレクトリの中に配置する</b>
- [download | LibreOffice(リブレオフィス) - 無料で自由に使えるオフィスソフト - OpenOffice.orgの進化系 - Microsoft Officeと高い相互運用性](https://ja.libreoffice.org/download/download/)

以後、HSQLDB のバージョンは`1.8.0.10`、LibreOffice のバージョンは`7.2.4.1`であることを前提とする。

## `./lib/odb.jar`の作成手順

### `workdir`ディレクトリを作業ディレクトリとする。

### Java コンパイラのバージョン情報を確認する。
```
$ javac -version
javac 17.0.1
```

### `./src`ディレクトリの中の`*.java`ソースファイルをコンパイルする。
```
$ javac -encoding UTF-8 ./src/*.java -d .
```
### テストドライバプログラムを実行する
`lib`ディレクトリの中に`hsqldb.jar`が置かれていることを確認してから

*macOS / Linux:*
```
$ java -cp ".:./lib/hsqldb.jar" com.k650250.odb.testing.TestDriver
```

*Windows:*
```
> java -cp ".;./lib/hsqldb.jar" com.k650250.odb.testing.TestDriver
```

### `./lib/odb.jar`の作成

```
$ jar cfm ./lib/odb.jar ./mf.txt com
```

### `./lib/odb.jar`の中に埋め込まれたテストドライバプログラムを実行する。

```
$ java -jar ./lib/odb.jar
```

## 他のJVM言語（例: Kotlin、Jython、Scala）で`./lib/odb.jar`を参照する

### この時点でのディレクトリ構成

#### `workdir`ディレクトリを作業ディレクトリとする。

<pre><code>
&nbsp;./<br />
&#9507; com/<br />
&#65049;
&#9507; lib/<br />
&#9475;&#9507; dummy<br />
&#9475;&#9507; hsqldb.jar<br />
&#9475;&#9495; odb.jar<br />
&#9507; src/<br />
&#9475;&#9507; ODBFile.java<br />
&#9475;&#9507; Query.java<br />
&#9475;&#9507; SqlToolWrapper.java<br />
&#9475;&#9507; TestDriver.java<br />
&#9475;&#9507; TestDriver.kt<br />
&#9475;&#9507; TestDriver.kts<br />
&#9475;&#9507; TestDriver.py<br />
&#9475;&#9495; TestDriver.scala<br />
&#9507; init.sql<br />
&#9507; logging.properties<br />
&#9507; mf.txt<br />
&#9507; mfkt.txt<br />
&#9507; mfsql.txt<br />
&#9495; sample.odb<br />
</code></pre>

### Kotlin (`*.kt`) の場合

#### Kotlin コンパイラのバージョン情報を確認する。

```
$ kotlinc-jvm -version
info: kotlinc-jvm 1.6.10 (JRE 17.0.1+12-LTS-39)
```

#### テストドライバプログラムの Kotlin ソースファイル（`./src/TestDriver.kt`）をコンパイルする。
`./lib/odb.jar`が置かれていることを確認してから

*macOS / Linux:*
```
$ kotlinc-jvm -cp ".:./lib/odb.jar" ./src/TestDriver.kt -jvm-target 17 -include-runtime -d ./lib/odbkt.jar
```

*Windows:*
```
> kotlinc-jvm -cp ".;./lib/odb.jar" ./src/TestDriver.kt -jvm-target 17 -include-runtime -d ./lib/odbkt.jar
```

#### `./lib/odb.jar`等を参照させる為、jarファイルのマニフェストを更新する。

```
$ jar uvfm ./lib/odbkt.jar ./mfkt.txt
マニフェストが更新されました
```

#### `./lib/odbkt.jar`の中に埋め込まれたテストドライバプログラムを実行する。

```
$ java -jar ./lib/odbkt.jar
```

### Kotlin スクリプト (`*.kts`) の場合

#### Kotlin のバージョン情報を確認する。

```
$ kotlin -version
Kotlin version 1.6.10-release-923 (JRE 17.0.1+12-LTS-39)
```

#### テストドライバプログラムを実行する。

*macOS / Linux:*
```
$ kotlin -howtorun script -cp "./lib/hsqldb.jar:./lib/odb.jar:$(dirname "`which kotlin`")/../lib/kotlin-stdlib-jdk7.jar" ./src/TestDriver.kts
```

*Windows:*
```
> kotlin -howtorun script -cp "./lib/hsqldb.jar;./lib/odb.jar;!_KOTLIN_HOME!/lib/kotlin-stdlib-jdk7.jar" ./src/TestDriver.kts
```

### Jython/JPython (`*.py`) の場合

#### Jython のバージョン情報を確認する。

```
$ jython --version
Jython 2.7.2
```

#### テストドライバプログラムを実行する。

*macOS / Linux:*
```
$ jython -J-cp "./lib/hsql.jar:./lib/odb.jar" ./src/TestDriver.py
```

*Windows:*
```
> jython -J-cp "./lib/hsql.jar;./lib/odb.jar" ./src/TestDriver.py
```

### Scala (`*.scala`) の場合

#### Scala のバージョン情報を確認する。

```
$ cs launch scala3 -- -version
Scala code runner version 3.1.2-RC1-bin-20220106-0b4c6e7-NIGHTLY-git-0b4c6e7 --
Copyright 2002-2022, LAMP/EPFL
```

#### テストドライバプログラムを実行する。

*macOS / Linux:*
```
$ cs launch scala3 -- -cp "./lib/hsqldb.jar:./lib/odb.jar" ./src/TestDriver.scala
```

*Windows:*
```
> cs launch scala3 -- -cp "./lib/hsqldb.jar;./lib/odb.jar" ./src/TestDriver.scala
```

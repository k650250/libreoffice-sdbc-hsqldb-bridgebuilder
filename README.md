# libreoffice-sdbc-hsqldb-bridgebuilder
OpenOffice / LibreOffice Base のデータベースファイル (*.odb) の埋め込み HSQLDB (HyperSQL Database Engine) に接続する為のツール

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

&nbsp;.<br />
&#9507; lib<br />
&#9475;&#9507; dummy<br />
&#9475;&#9495; **hsqldb.jar**<br />
&#9507; src<br />
&#9475;&#9507; ODBFile.java<br />
&#9475;&#9507; TestDriver.java<br />
&#9475;&#9495; TestDriver.kt<br />
&#9507; logging.properties<br />
&#9507; mf.txt<br />
&#9507; mfkt.txt<br />
&#9495; sample.odb<br />

## `hsqldb.jar`と LibreOffice の入手
- [HyperSQL Database Engine (HSQLDB) -  Browse Files at SourceForge.net](https://sourceforge.net/projects/hsqldb/files/)
  - **[HSQLDB-1.8.0.10](https://sourceforge.net/projects/hsqldb/files/hsqldb/hsqldb_1_8_0/)**
    - `hsqldb.jar`は、ダウンロードしたzipファイルの中の`lib`ディレクトリの中に存在する
    - `hsqldb.jar`を<b>`workdir/lib`ディレクトリの中に配置する</b>
- [download | LibreOffice(リブレオフィス) - 無料で自由に使えるオフィスソフト - OpenOffice.orgの進化系 - Microsoft Officeと高い相互運用性](https://ja.libreoffice.org/download/download/)

以後、HSQLDB のバージョンは`1.8.0.10`、LibreOffice のバージョンは`7.2.4.1`であることを前提とする。

## `workdir/lib/odb.jar`の作成手順

### `workdir`ディレクトリを作業ディレクトリとする。

### Javaコンパイラのバージョン情報を確認する。
```
$ javac -version
javac 17.0.1
```

### `workdir/src`ディレクトリの中の`*.java`ソースファイルをコンパイルする。
```
$ javac -encoding UTF-8 src/*.java -d .
```
### テストドライバプログラムを実行する
`lib`ディレクトリの中に`hsqldb.jar`が置かれていることを確認してから

*MacOS / Linux:*
```
$ java -cp ".:./lib/hsqldb.jar" com.k650250.odb.testing.TestDriver
```

*Windows:*
```
> java -cp ".;./lib/hsqldb.jar" com.k650250.odb.testing.TestDriver
```

### `workdir/lib/odb.jar`の作成

```
$ jar cfm lib/odb.jar mf.txt com
```

### `workdir/lib/odb.jar`の中に埋め込まれたテストドライバプログラムを実行する

```
$ java -jar lib/odb.jar
```

## 他のJVM言語（例: Kotlin）で`workdir/lib/odb.jar`を参照する

### `workdir`ディレクトリを作業ディレクトリとする。

### Kotlinコンパイラのバージョン情報を確認する。

```
$ kotlinc-jvm -version
info: kotlinc-jvm 1.6.10 (JRE 17.0.1+12-LTS-39)
```

### テストドライバプログラムの Kotlin ソースファイル（`workdir/src/TestDriver.kt`）をコンパイルする。
`lib`ディレクトリの中に`odb.jar`が置かれていることを確認してから

*MacOS / Linux:*
```
$ kotlinc-jvm -cp ".:./lib/odb.jar" src/TestDriver.kt -include-runtime -d lib/odbkt.jar
```

*Windows:*
```
> kotlinc-jvm -cp ".;./lib/odb.jar" src/TestDriver.kt -include-runtime -d lib/odbkt.jar
```

### `workdir/lib/odb.jar`等を参照させる為、jarファイルのマニフェストを更新する。

```
$ jar uvfm lib/odbkt.jar mfkt.txt
マニフェストが更新されました
```

### `workdir/lib/odbkt.jar`の中に埋め込まれたテストドライバプログラムを実行する

```
$ java -jar lib/odbkt.jar
```



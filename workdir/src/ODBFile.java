// -*- coding: UTF-8; -*-

package com.k650250.odb;

import com.k650250.odb.Query;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ODBFile extends File implements AutoCloseable, Closeable {
    protected final String DB_CLUSTER_DIR_NAME = "database";
    protected Document content = null;
    protected File dbRootDir = null;
    protected Properties info;

    /**
     * コンストラクタ
     */
    public ODBFile(File parent, String child, Properties info) {
        super(parent, child);
        this.initConnProps(info);
    }
    public ODBFile(File parent, String child) {
        this(parent, child, new Properties());
    }
    public ODBFile(String pathname, Properties info) {
        super(pathname);
        this.initConnProps(info);
    }
    public ODBFile(String pathname) {
        this(pathname, new Properties());
    }
    public ODBFile(String parent, String child, Properties info) {
        super(parent, child);
        this.initConnProps(info);
    }
    public ODBFile(String parent, String child) {
        this(parent, child, new Properties());
    }
    public ODBFile(URI uri, Properties info) {
        super(uri);
        this.initConnProps(info);
    }
    public ODBFile(URI uri) {
        this(uri, new Properties());
    }

    /**
     * 静的メソッド
     * インスタンス化し、直ちにファイルを開く
     */
    public static ODBFile open(File parent, String child, Properties info) throws FileNotFoundException, IOException, Exception {
        final ODBFile odb = new ODBFile(parent, child, info);
        odb.open();

        return odb;
    }
    public static ODBFile open(File parent, String child) throws FileNotFoundException, IOException, Exception {
        return ODBFile.open(parent, child, new Properties());
    }
    public static ODBFile open(String pathname, Properties info) throws FileNotFoundException, IOException, Exception {
        final ODBFile odb = new ODBFile(pathname, info);
        odb.open();

        return odb;
    }
    public static ODBFile open(String pathname) throws FileNotFoundException, IOException, Exception {
        return ODBFile.open(pathname, new Properties());
    }
    public static ODBFile open(String parent, String child, Properties info) throws FileNotFoundException, IOException, Exception {
        final ODBFile odb = new ODBFile(parent, child, info);
        odb.open();

        return odb;
    }
    public static ODBFile open(String parent, String child) throws FileNotFoundException, IOException, Exception {
        return ODBFile.open(parent, child, new Properties());
    }
    public static ODBFile open(URI uri, Properties info) throws FileNotFoundException, IOException, Exception {
        final ODBFile odb = new ODBFile(uri, info);
        odb.open();

        return odb;
    }
    public static ODBFile open(URI uri) throws FileNotFoundException, IOException, Exception {
        return ODBFile.open(uri, new Properties());
    }

    /**
     * インスタンス・メソッド
     * ファイルを開く
     *
     * 本ファイル内のデータをデータベース・ルート・ディレクトリ内に展開し
     * データベース管理システムを起動する。
     * ファイルを開いた後は、close メソッドを明示的に呼び出すか
     * 或いは try-with-resources 文による自動クローズによって
     * 本ファイルを閉じなければならない。
     */
    public boolean open() throws FileNotFoundException, IOException, Exception {
        if (!this.isClosed()) {
            return false;
        }

        // Zipファイルを読み込む入力ストリームByteArrayOutputStream
        try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(this))) {
            ZipEntry zipEntry = null;
            final String PREFIX = this.getName() + ".";
            final byte[] buf = new byte[1024];

            // データベース・ルート・ディレクトリを作成
            this.dbRootDir = new File(this.info.getProperty("_db_root_dir", ""));
            if (this.dbRootDir.getName().isEmpty()) {
                this.dbRootDir = Files.createTempDirectory(PREFIX).toFile();
            } else {
                this.dbRootDir.mkdirs();
            }
            // Zipファイル内のファイル・ディレクトリ分ループする
            while ((zipEntry = zis.getNextEntry()) != null) {
                // 展開先のパスを指定してファイルオブジェクトを生成
                File target = new File(this.dbRootDir, zipEntry.getName());
                // 対象がディレクトリか否か判定
                if (!zipEntry.isDirectory()) {
                    // 対象がファイルの場合
                    // ファイル出力先のディレクトリ取得
                    File dir = new File(target.getParent());
                    OutputStream os;
                    final boolean IS_DB_CLUSTER_DIR = dir.getName().equals(this.DB_CLUSTER_DIR_NAME);
                    final boolean IS_CONTENT = target.getName().equals("content.xml");
                    if (!IS_CONTENT) {
                        if (!IS_DB_CLUSTER_DIR) {
                            // "content.xml"でない
                            // かつデータベースクラスターディレクトリでない場合は次ループへ
                            continue;
                        }
                        // ディレクトリの存在チェック
                        if (!dir.exists()) {
                            // ディレクトリが存在しない場合
                            // ファイル配置ディレクトリを作成
                            dir.mkdirs();
                        }
                        // ファイル名に接頭辞を追加
                        target = new File(dir, PREFIX + target.getName());
                        os = new FileOutputStream(target);
                        this.zis2os(buf, zis, os);
                    } else {
                        os = new ByteArrayOutputStream();
                        this.zis2os(buf, zis, os);
                        this.setContent(((ByteArrayOutputStream)os).toByteArray());
                    }
                }
                zis.closeEntry();
            }
        }

        return true;
    }

    /**
     * インスタンス・メソッド
     * ファイルを閉じる
     *
     * データベース管理システムを終了し、データを本ファイルに反映した上で
     * データベース・ルート・ディレクトリを含む一切の一時ファイルを削除する。
     * インスタンス化オブジェクトを try-with-resources 文のリソースとした場合
     * 当該ブロックの終端に達すると自動で呼び出される。
     */
    @Override
    public void close() throws FileNotFoundException, IOException {
        if (this.isClosed()) {
            return;
        }

        final String PREFIX = this.getName() + ".";
        final byte[] buf = new byte[1024]; 
        final int DB_CLUSTER_DIR_NAME_LEN = this.DB_CLUSTER_DIR_NAME.length() + 1;
        final File dbClusterDir = new File(this.dbRootDir, this.DB_CLUSTER_DIR_NAME);
        File tmp = null;
        try {
            tmp = File.createTempFile(this.getName(), null);
            Files.copy(this.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (final ZipFile zip = new ZipFile(tmp); final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(this))) {
                for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); zos.closeEntry()) {
                    ZipEntry entry = entries.nextElement();
                    InputStream is = null;
                    if (entry.isDirectory()) {
                        zos.putNextEntry(entry);
                        continue;
                    }
                    if (entry.toString().startsWith(this.DB_CLUSTER_DIR_NAME)) {
                        // 更新

                        final File f = new File(dbClusterDir, PREFIX + entry.toString().substring(DB_CLUSTER_DIR_NAME_LEN));
                        try {
                            is = new FileInputStream(f);
                        } catch (FileNotFoundException e) {
                            continue;
                        }
                    } else {
                        // 従前

                        is = zip.getInputStream(entry);
                    }
                    zos.putNextEntry(entry);
                    this.is2zos(buf, is, zos);
                }
            }
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
            if (this.dbRootDir != null) {
                this.deleteRecursively(this.dbRootDir);
            }
            this.dbRootDir = null;
            this.content = null;
        }
    }

    /**
     * インスタンス・メソッド
     * データベースURLの文字列に変換
     */
    public String toUrl() throws MalformedURLException {
        if (this.isClosed()) {
            return null;
        }

        final URL fileURL = this.dbRootDir.toURI().toURL();
        final StringBuilder props = new StringBuilder();
        final String DELIMITER = ";";
        final String KEY_VALUE_SEPARATOR = "=";
        final char KEY_PREFIX = '_';
        String key;

        for (final Enumeration<?> e = this.info.propertyNames(); e.hasMoreElements();) {
            key = e.nextElement().toString();
            if (key.charAt(0) == KEY_PREFIX) {
                continue;
            }
            props.append(DELIMITER);
            props.append(key);
            props.append(KEY_VALUE_SEPARATOR);
            props.append(this.info.getProperty(key));
        }

        return String.format(
            "jdbc:hsqldb:%s%s/%s%s",
            fileURL.toString(),
            this.DB_CLUSTER_DIR_NAME,
            this.getName(),
            props.toString()
        );
    }

    /**
     * インスタンス・メソッド
     * クエリーを取得
     */
    public Query getQuery(String name) {
        if (this.isClosed()) {
            return null;
        }

        final String QUERY_TAG = "db:query";
        final String NAME_ATTR = "db:name";
        final String COMMAND_ATTR = "db:command";
        final Element root = this.content.getDocumentElement();
        final NodeList queries = root.getElementsByTagName(QUERY_TAG);

        for (int i = 0; i < queries.getLength(); i++) {
            Element query = (Element)queries.item(i);

            if (name.equals(query.getAttribute(NAME_ATTR))) {
                return new Query(query.getAttribute(COMMAND_ATTR), this);
            }
        }

        return null;
    }

    /**
     * インスタンス・メソッド
     * 本ファイルが閉じられた状態かを判定
     */
    public boolean isClosed() {
        return this.dbRootDir == null || this.content == null;
    }

    /**
     * インスタンス・メソッド
     * 本ファイルの情報を、構文解析済み XML 文書オブジェクトの複製ノードとして取得
     */
    public Node getContent() {
        return this.content.cloneNode(true);
    }

    // XML 形式の本ファイルの情報を構文解析した上で登録
    private void setContent(byte[] byteArray) throws IOException, Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        this.content = builder.parse(new ByteArrayInputStream(byteArray));
    }

    // 解凍・展開
    private void zis2os(byte[] buf, ZipInputStream zis, OutputStream os) throws IOException {
        try (final BufferedOutputStream bos = new BufferedOutputStream(os)) {
            int len;
            while ((len = zis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
        }
    }

    // 圧縮
    private void is2zos(byte[] buf, InputStream is, ZipOutputStream zos) throws IOException {
        try (final BufferedInputStream bis = new BufferedInputStream(is)) {
            int len;
            while ((len = bis.read(buf, 0, buf.length)) != -1) {
                zos.write(buf, 0, len);
            }
        }
    }

    // ディレクトリ（空でないのを含む。）を削除
    private void deleteRecursively(File dir) {
        final String[] fileNames = dir.list();
        if (fileNames == null) {
            return;
        }
        for (String fileName : fileNames) {
            final File file = new File(dir, fileName);
            if (file.isDirectory()) {
                this.deleteRecursively(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

    // データベース管理システムに渡すプロパティを登録
    private void initConnProps(Properties info) {
        info.setProperty("shutdown", "true");
        this.info = info;
    }
}

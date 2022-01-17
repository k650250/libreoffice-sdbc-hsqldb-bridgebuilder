// -*- coding: UTF-8; -*-

package com.k650250.odb;

import com.k650250.odb.ODBFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Query {
    protected String command;
    protected ODBFile odbFile;

    /* コンストラクタ
     */
    Query(String command, ODBFile odbFile) {
        this.command = command;
        this.odbFile = odbFile;
    }

    /* インスタンス・メソッド
     * 文字列表現に
     */
    @Override
    public String toString() {
        return this.command;
    }

    /* インスタンス・メソッド
     * 問合せ文中の一切のクエリー名をそれが指し示す副問合せに置換
     */
    public Query expand() {
        if (this.odbFile.isClosed()) {
            return this;
        }

        final String BEGIN_NAME = "\"";
        final String END_NAME = "\"";
        final String BEGIN_COMMAND = "( ";
        final String END_COMMAND = " )";
        final String QUERY_TAG = "db:query";
        final String NAME_ATTR = "db:name";
        final String COMMAND_ATTR = "db:command";
        final Element root = this.odbFile.content.getDocumentElement();
        final NodeList queries = root.getElementsByTagName(QUERY_TAG);
        
        String oldCommand;
        String newCommand = this.command;

        do {
            oldCommand = String.copyValueOf(newCommand.toCharArray());

            for (int i = 0; i < queries.getLength(); i++) {
                Element query = (Element)queries.item(i);

                newCommand = newCommand.replace(
                    BEGIN_NAME + query.getAttribute(NAME_ATTR) + END_NAME,
                    BEGIN_COMMAND + query.getAttribute(COMMAND_ATTR) + END_COMMAND
                );
            }
        } while (!newCommand.equals(oldCommand));

        return new Query(newCommand, this.odbFile);
    }
}

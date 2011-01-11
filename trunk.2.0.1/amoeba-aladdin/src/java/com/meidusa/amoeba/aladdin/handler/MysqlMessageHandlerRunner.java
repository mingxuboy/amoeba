package com.meidusa.amoeba.aladdin.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlResultSetPacket;
import com.meidusa.amoeba.mysql.net.packet.result.ResultPacket;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.net.packet.PacketBuffer;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.xmltable.Condition;
import com.meidusa.amoeba.xmltable.XmlColumn;
import com.meidusa.amoeba.xmltable.XmlRow;
import com.meidusa.amoeba.xmltable.XmlTable;
import com.meidusa.amoeba.xmltable.XmlTableLoader;

/**
 * 这个负责mysql 特有的一些 select query 的应答,该类只是简单的将一些特殊sql进行从xmltable 文件中进行定位。 然后反馈给客户端一些数据
 * 
 * @author struct
 */
public class MysqlMessageHandlerRunner implements MessageHandlerRunner, Initialisable {

    private ResultPacket                 packet        = null;
    private String                       query;
    private String                       xmlTable;
    private static Map<String, XmlTable> xmlTableMap;
    private static Map<String, byte[]>   resultContent = new HashMap<String, byte[]>();

    public String getXmlTable() {
        return xmlTable;
    }

    public void setXmlTable(String collationFile) {
        this.xmlTable = collationFile;
    }

    public MysqlMessageHandlerRunner(){

    }

    public void init(MessageHandler handler) {
        CommandMessageHandler chandler = (CommandMessageHandler) handler;
        this.packet = chandler.packet;
        this.query = chandler.query;
    }

    public void reset() {
        this.packet = null;
        this.query = null;
    }

    public void run() {
        query = query.toLowerCase();
        byte[] content = resultContent.get(query);
        if (content != null) {
            MysqlResultSetPacket resultPacket = (MysqlResultSetPacket) packet;
            resultPacket.setContent(content);
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(query, " 	%'");
        String[] tokens = new String[tokenizer.countTokens()];
        int index = 0;

        while (tokenizer.hasMoreTokens()) {
            tokens[index++] = tokenizer.nextToken();
        }
        if (tokens.length > 1) {
            boolean isSelect = tokens[0].equalsIgnoreCase("select") || tokens[0].equalsIgnoreCase("show");
            if (isSelect) {
                String tableName = tokens[1];
                Condition condition = null;
                for (int i = 2; i < tokens.length; i++) {
                    if (tokens[i].equals("where")) {
                        condition = new Condition();
                        condition.name = tokens[i + 1];
                        if (tokens[i + 2].equals("=")) {
                            condition.type = Condition.TYPE.match;
                        } else if (tokens[i + 2].equals("like")) {
                            condition.type = Condition.TYPE.match;
                        }
                        condition.value = tokens[i + 3];
                        break;
                    } else if (tokens[i].equals("like")) {
                        condition = new Condition();
                        condition.name = tokens[i + 1];
                        condition.type = Condition.TYPE.exist;
                    } else if (tokens[i].equals("from")) {
                        tableName = tokens[i + 1];
                    }
                }

                XmlTable xmlTable = xmlTableMap.get(tableName);
                if (xmlTable != null) {
                    if (tableName.equals("variables") && condition != null) {
                        if (condition.value == null) {
                            condition.value = condition.name;
                            condition.type = Condition.TYPE.match;
                            condition.name = "variable_name";
                        }
                    }
                    synchronized (resultContent) {
                        content = resultContent.get(query);
                        if (content == null) {
                            content = xmlTableToBytes(xmlTable.query(condition));
                            resultContent.put(query, content);
                        }
                    }
                    MysqlResultSetPacket resultPacket = (MysqlResultSetPacket) packet;
                    resultPacket.setContent(content);
                    return;
                }

                MysqlResultSetPacket resultPacket = (MysqlResultSetPacket) packet;
                ResultSetHeaderPacket resultHeader = new ResultSetHeaderPacket();
                resultHeader.packetId = 1;
                resultHeader.columns = 1;

                resultPacket.resulthead = resultHeader;
                FieldPacket field = new FieldPacket();
                field.name = "test";
                field.type = (byte) MysqlDefs.FIELD_TYPE_VAR_STRING;
                field.length = 8;
                resultPacket.fieldPackets = new FieldPacket[] { field };
            }
        }
    }

    private byte[] xmlTableToBytes(XmlTable table) {

        PacketBuffer buffer = new AbstractPacketBuffer(4086);
        byte paketId = 1;

        ResultSetHeaderPacket resultHeader = new ResultSetHeaderPacket();
        resultHeader.packetId = paketId++;
        resultHeader.columns = table.getColumns().size();
        buffer.writeBytes(resultHeader.toByteBuffer(null).array());

        for (int i = 0; i < resultHeader.columns; i++) {
            FieldPacket field = new FieldPacket();
            field.name = table.getColumns().get(i);
            field.length = 8;
            field.packetId = paketId++;
            field.type = (byte) MysqlDefs.FIELD_TYPE_VAR_STRING;
            buffer.writeBytes(field.toByteBuffer(null).array());
        }

        EOFPacket fieldEof = new EOFPacket();
        fieldEof.serverStatus = 2;
        fieldEof.warningCount = 0;
        fieldEof.packetId = paketId++;
        buffer.writeBytes(fieldEof.toByteBuffer(null).array());

        for (XmlRow xmlRow : table.getRows()) {
            RowDataPacket row = new RowDataPacket(false);
            row.columns = new ArrayList<Object>();
            for (int i = 0; i < resultHeader.columns; i++) {
                XmlColumn column = xmlRow.getColumMap().get(table.getColumns().get(i));
                if (column == null) {
                    row.columns.add(null);
                } else {
                    row.columns.add(column.getValue());
                }
            }
            row.packetId = paketId++;
            buffer.writeBytes(row.toByteBuffer(null).array());
        }

        EOFPacket rowEof = new EOFPacket();
        rowEof.serverStatus = 2;
        rowEof.warningCount = 0;
        rowEof.packetId = paketId++;
        buffer.writeBytes(rowEof.toByteBuffer(null).array());
        return buffer.toByteBuffer().array();
    }

    public void init() throws InitialisationException {
        if (xmlTableMap == null) {
            if (xmlTable != null) {
                synchronized (this) {
                    if (xmlTableMap != null) return;
                    XmlTableLoader loader = new XmlTableLoader();
                    loader.setDTD("/com/meidusa/amoeba/xml/table.dtd");
                    loader.setDTDSystemID("table.dtd");
                    xmlTableMap = loader.loadXmlTable(xmlTable);
                }
            } else {
                xmlTableMap = new HashMap<String, XmlTable>();
            }
        }
    }

}

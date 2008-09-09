package com.meidusa.amoeba.oracle.accessor;

public class SQLAccessor {

    private String     sqlStmt;
    private Accessor[] accessor;

    public String getSqlStmt() {
        return sqlStmt;
    }

    public void setSqlStmt(String sqlStmt) {
        this.sqlStmt = sqlStmt;
    }

    public Accessor[] getAccessor() {
        return accessor;
    }

    public void setAccessor(Accessor[] accessor) {
        this.accessor = accessor;
    }

    public Object getObject(int i, byte[] data) {
        if (accessor != null && accessor[i] != null) {
            return accessor[i].getObject(data);
        }
        return null;
    }

    public Object[] getObject(byte[][] data) {
        Object[] obj = new Object[accessor.length];
        for (int i = 0; i < accessor.length; i++) {
            obj[i] = getObject(i, data[i]);
        }
        return obj;
    }
}

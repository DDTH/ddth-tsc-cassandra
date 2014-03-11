package com.github.ddth.tsc.cassandra;

import java.text.MessageFormat;
import java.util.Calendar;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.ddth.tsc.AbstractCounter;
import com.github.ddth.tsc.DataPoint;

public class CassandraCounter extends AbstractCounter {

    private final static String CQL_TEMPLATE_ADD = "UPDATE {0} SET v=v+? WHERE c=? AND ym=? AND d=? AND t=?";
    private final static String CQL_TEMPLATE_GET = "SELECT c,ym,d,t,v FROM {0} WHERE c=? AND ym=? AND d=? AND t=?";
    private final static String CQL_TEMPLATE_GET_ROW = "SELECT c,ym,d,t,v FROM {0} WHERE c=? AND ym=? AND d=?";

    private Session session;
    private PreparedStatement pStmAdd, pStmGet, pStmGetRow;

    private String tableName;
    private String cqlAdd, cqlGet, cqlGetRow;

    public CassandraCounter() {
    }

    public CassandraCounter(String name, Session session, String tableName) {
        super(name);
        this.session = session;
        this.tableName = tableName;
    }

    public CassandraCounter setSession(Session session) {
        this.session = session;
        return this;
    }

    public CassandraCounter setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        super.init();
        cqlAdd = MessageFormat.format(CQL_TEMPLATE_ADD, tableName);
        pStmAdd = session.prepare(cqlAdd);

        cqlGet = MessageFormat.format(CQL_TEMPLATE_GET, tableName);
        pStmGet = session.prepare(cqlGet);

        cqlGetRow = MessageFormat.format(CQL_TEMPLATE_GET_ROW, tableName);
        pStmGetRow = session.prepare(cqlGetRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataPoint get(long timestampMs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampMs);

        int yyyy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);

        Long key = toTimeSeriesPoint(timestampMs);

        BoundStatement stm = pStmGet.bind(getName(), yyyy * 100 + mm, dd, key.longValue());
        ResultSet rs = session.execute(stm);
        Row row = rs.one();
        if (row == null) {
            return new DataPoint(key.longValue(), 0, RESOLUTION_MS);
        }
        DataPoint result = new DataPoint(key.longValue(), row.getLong("v"), RESOLUTION_MS);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long timestampMs, long value) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampMs);

        Long key = toTimeSeriesPoint(timestampMs);
        int yyyy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);

        BoundStatement stm = pStmAdd.bind(value, getName(), yyyy * 100 + mm, dd, key.longValue());
        session.execute(stm);
    }

}

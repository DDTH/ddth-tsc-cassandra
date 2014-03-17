package com.github.ddth.tsc.cassandra;

import java.text.MessageFormat;
import java.util.Calendar;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.ddth.tsc.AbstractCounter;
import com.github.ddth.tsc.DataPoint;
import com.github.ddth.tsc.DataPoint.Type;
import com.github.ddth.tsc.cassandra.internal.CassandraUtils;
import com.github.ddth.tsc.cassandra.internal.CounterMetadata;

/**
 * Cassandra-backed counter.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.2.0
 */
public class CassandraCounter extends AbstractCounter {

    private Session session;
    private PreparedStatement pStmAdd, pStmSet, pStmGet, pStmGetRow;
    private CounterMetadata metadata;

    public CassandraCounter() {
    }

    public CassandraCounter(String name, Session session, CounterMetadata metadata) {
        super(name);
        setSession(session).setMetadata(metadata);
    }

    public CassandraCounter setSession(Session session) {
        this.session = session;
        return this;
    }

    public CassandraCounter setMetadata(CounterMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        super.init();
        String tableName = metadata.table;

        if (metadata.isCounterColumn) {
            String cqlAdd = MessageFormat.format(CqlTemplate.CQL_TEMPLATE_ADD_COUNTER, tableName);
            pStmAdd = session.prepare(cqlAdd);
        } else {
            String cqlSet = MessageFormat.format(CqlTemplate.CQL_TEMPLATE_SET_COUNTER, tableName);
            pStmSet = session.prepare(cqlSet);
        }

        String cqlGet = MessageFormat.format(CqlTemplate.CQL_TEMPLATE_GET_COUNTER, tableName);
        pStmGet = session.prepare(cqlGet);

        String cqlGetRow = MessageFormat
                .format(CqlTemplate.CQL_TEMPLATE_GET_COUNTER_ROW, tableName);
        pStmGetRow = session.prepare(cqlGetRow);
    }

    /*----------------------------------------------------------------------*/

    private static int[] toYYYYMM_DD(long timestampMs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampMs);

        int yyyy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);

        return new int[] { yyyy * 100 + mm, dd };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long timestampMs, long value) {
        if (!metadata.isCounterColumn) {
            throw new IllegalStateException("Counter [" + getName()
                    + "] does not support ADD operator!");
        }
        Long key = toTimeSeriesPoint(timestampMs);
        int[] yyyymm_dd = toYYYYMM_DD(timestampMs);
        CassandraUtils.executeNonSelect(session, pStmAdd, value, getName(), yyyymm_dd[0],
                yyyymm_dd[1], key.longValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(long timestampMs, long value) {
        if (metadata.isCounterColumn) {
            throw new IllegalStateException("Counter [" + getName()
                    + "] does not support SET operator!");
        }
        Long key = toTimeSeriesPoint(timestampMs);
        int[] yyyymm_dd = toYYYYMM_DD(timestampMs);
        CassandraUtils.executeNonSelect(session, pStmSet, value, getName(), yyyymm_dd[0],
                yyyymm_dd[1], key.longValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataPoint get(long timestampMs) {
        Long key = toTimeSeriesPoint(timestampMs);
        int[] yyyymm_dd = toYYYYMM_DD(timestampMs);

        Row row = CassandraUtils.executeOne(session, pStmGet, getName(), yyyymm_dd[0],
                yyyymm_dd[1], key.longValue());
        if (row == null) {
            return new DataPoint(Type.NONE, key.longValue(), 0, RESOLUTION_MS);
        } else {
            return new DataPoint(Type.SUM, key.longValue(), row.getLong("v"), RESOLUTION_MS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataPoint get(long timestampMs, DataPoint.Type type, int steps) {
        int blockSize = steps * RESOLUTION_MS;
        Long key = toTimeSeriesPoint(timestampMs, steps);
        DataPoint result = new DataPoint().type(type).blockSize(blockSize)
                .timestamp(key.longValue());

        long _key = key.longValue();
        for (int i = 0; i < steps; i++) {
            DataPoint _temp = get(_key);
            result.add(_temp);
            _key += RESOLUTION_MS;
        }
        return result;
    }
}

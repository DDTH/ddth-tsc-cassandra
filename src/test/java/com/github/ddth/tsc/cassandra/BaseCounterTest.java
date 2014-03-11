package com.github.ddth.tsc.cassandra;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.github.ddth.tsc.cassandra.internal.EmbeddedCassandraServer;
import com.github.ddth.tsc.mem.InmemCounter;

/**
 * Test cases for {@link InmemCounter}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseCounterTest extends TestCase {

    protected CassandraCounter counter;
    protected CassandraCounterFactory counterFactory;
    protected EmbeddedCassandraServer embeddedCassandraServer;
    protected Cluster cluster;

    private final static String CASSANDRA_HOST = "127.0.0.1";
    private final static int CASSANDRA_PORT = 9042;
    private final static String KEYSPACE = "tsc";
    private final static String TABLE = "tsc_counters";

    public BaseCounterTest(String testName) {
        super(testName);
    }

    @Before
    public void setUp() throws Exception {
        embeddedCassandraServer = new EmbeddedCassandraServer();
        embeddedCassandraServer.start();

        cluster = Cluster.builder().addContactPoint(CASSANDRA_HOST).withPort(CASSANDRA_PORT)
                .build();
        Session session = cluster.connect("system");
        session.execute("CREATE KEYSPACE "
                + KEYSPACE
                + " WITH replication={'class':'SimpleStrategy','replication_factor':'1'} AND durable_writes=true");
        session.execute("CREATE TABLE "
                + KEYSPACE
                + "."
                + TABLE
                + " (c varchar, ym int, d int, t bigint, v counter, PRIMARY KEY ((c, ym, d), t) ) WITH COMPACT STORAGE");
        session.close();

        counterFactory = new CassandraCounterFactory();
        counterFactory.setCluster(cluster).setKeyspace(KEYSPACE).setTableTemplate(TABLE);
        counterFactory.init();

        counter = (CassandraCounter) counterFactory.getCounter("test_metric");
    }

    @After
    public void tearDown() throws Exception {

        if (counterFactory != null) {
            try {
                counterFactory.destroy();
            } catch (Exception e) {
            } finally {
                counterFactory = null;
            }
        }

        // if (counter != null) {
        // try {
        // counter.destroy();
        // } catch (Exception e) {
        // } finally {
        // counter = null;
        // }
        // }

        if (cluster != null) {
            try {
                cluster.close();
            } catch (Exception e) {
            } finally {
                cluster = null;
            }
        }

        if (embeddedCassandraServer != null) {
            try {
                embeddedCassandraServer.stop();
            } catch (Exception e) {
            } finally {
                embeddedCassandraServer = null;
            }
        }
    }
}

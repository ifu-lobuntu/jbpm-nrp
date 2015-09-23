package org.jbpm.vdml.services;

import bitronix.tm.resource.ResourceRegistrar;
import bitronix.tm.resource.common.XAResourceProducer;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractVdmlServiceTest {
    protected static EntityManagerFactory emf;
    private static Logger logger = LoggerFactory.getLogger(CollaborationImportTest.class);
    private static boolean usePostgres = true;
    private static PoolingDataSource ds;
    private Collection<EntityManager> entityManagers = new HashSet<EntityManager>();
    protected  static final String DEFAULT_DEPLOYMENT_ID="test-deployment";

    protected EntityManager getEntityManager() {
        clearEntityManagers();
        EntityManager em = emf.createEntityManager();
        entityManagers.add(em);
        return em;
    }

    @BeforeClass
    public static void setup() throws Exception {
        if (emf == null) {
            if (usePostgres) {
                DriverManager.registerDriver(new Driver());
                Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "itest", "itest");
                execute(c, "DROP DATABASE itest;");
                execute(c, "DROP DATABASE itest_template;");
                execute(c, "CREATE DATABASE itest WITH ENCODING='UTF8' OWNER=itest CONNECTION LIMIT=-1;");
                close(c);
                c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/itest", "itest", "itest");
                execute(c, "CREATE EXTENSION postgis;");
                close(c);
                ds = setupDatasource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432:itest", "itest", "itest");
                emf = Persistence.createEntityManagerFactory("org.jbpm.vdml.jpa");
                ds.reset();
                EntityManagerFactoryManager.get().addEntityManagerFactory("org.jbpm.vdml.jpa",emf);
                c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "itest", "itest");
                execute(c, "CREATE DATABASE itest_template TEMPLATE itest;");
                close(c);
            } else {
                setupDatasource("org.h2.Driver", "jdbc:h2:mem:jbpm-db;MVCC=true", "sa", "");
                emf = Persistence.createEntityManagerFactory("org.jbpm.vdml.jpa");
            }

        }
    }

    private static void execute(Connection c, String sql) {
        try {
            c.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static PoolingDataSource setupDatasource(String driverClass, String url, String username, String pwd) {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", username);
        pds.getDriverProperties().put("password", pwd);
        pds.getDriverProperties().put("url", url);
        pds.getDriverProperties().put("driverClassName", driverClass);
        try {
            pds.init();
        } catch (Exception e) {
            logger.warn("DBPOOL_MGR:Looks like there is an issue with creating db pool because of " + e.getMessage() + " cleaing up...");
            Set<String> resources = ResourceRegistrar.getResourcesUniqueNames();
            for (String resource : resources) {
                XAResourceProducer producer = ResourceRegistrar.get(resource);
                producer.close();
                ResourceRegistrar.unregister(producer);
                logger.debug("DBPOOL_MGR:Removed resource " + resource);
            }
            logger.debug("DBPOOL_MGR: attempting to create db pool again...");
            pds = new PoolingDataSource();
            pds.setUniqueName("jdbc/jbpm-ds");
            pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
            pds.setMaxPoolSize(5);
            pds.setAllowLocalTransactions(true);
            pds.getDriverProperties().put("user", username);
            pds.getDriverProperties().put("password", pwd);
            pds.getDriverProperties().put("url", url);
            pds.getDriverProperties().put("driverClassName", driverClass);
            pds.init();
            logger.debug("DBPOOL_MGR:Pool created after cleanup of leftover resources");
        }
        return pds;
    }

    @Before
    public void beginTx() throws Exception {
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        ut.begin();
    }

    @After
    public void afterTx() {
        try {
            clearEntityManagers();
            UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            Connection c = null;
            try {
                if (usePostgres) {
                    ut.commit();
                    c = truncateDbInPg();
                } else {
                    c = truncateInH2();
                    ut.commit();
                }
            } catch (Exception e) {
                ut.rollback();
            } finally {
                try {
                    ds.reset();
                } catch (Exception e) {
                }
                close(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void clearEntityManagers() {
        for (EntityManager entityManager : entityManagers) {
            close(entityManager);
        }
        entityManagers.clear();
    }

    private Connection truncateInH2() throws NamingException, SQLException {
        Connection c = ds.getConnection();
        c.createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE");
        ResultSet rst = c.createStatement().executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA()");
        while (rst.next()) {
            c.createStatement().execute("TRUNCATE TABLE " + rst.getString(1));
        }
        c.createStatement().execute("SET REFERENTIAL_INTEGRITY TRUE");
        return c;
    }

    private Connection truncateDbInPg() throws Exception {
        ds.reset();
        Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "itest", "itest");
        execute(c, "DROP DATABASE itest");
        execute(c, "CREATE database itest TEMPLATE itest_template;");
        return c;
    }

    private static void close(Object o) {
        try {
            o.getClass().getMethod("close").invoke(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

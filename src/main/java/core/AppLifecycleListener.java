package core;

import org.apache.ibatis.executor.ErrorContext;
import util.MyBatisUtil;
import util.RedisUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

@WebListener
public class AppLifecycleListener implements ServletContextListener {
    
    private JobDispatch jobDispatch;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        jobDispatch = new JobDispatch();
        jobDispatch.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (jobDispatch != null) {
            jobDispatch.stop();
        }

        try {
            MyBatisUtil.shutdown();
        } catch (Exception e) {
        }

        try {
            RedisUtil.shutdown();
        } catch (Exception e) {
        }

        clearMyBatisErrorContext();
        
        deregisterJdbcDrivers();
        
        stopMySQLCleanupThread();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void clearMyBatisErrorContext() {
        try {
            ErrorContext.instance().reset();
            
            Class<?> errorContextClass = Class.forName("org.apache.ibatis.executor.ErrorContext");
            Method instanceMethod = errorContextClass.getDeclaredMethod("instance");
            Object errorContext = instanceMethod.invoke(null);
            
            Method resetMethod = errorContextClass.getMethod("reset");
            resetMethod.invoke(errorContext);
        } catch (Exception e) {
        }
    }
    
    private void deregisterJdbcDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (Exception e) {
            }
        }
    }
    
    private void stopMySQLCleanupThread() {
        try {
            Class<?> cleanupThreadClass = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            Method shutdownMethod = cleanupThreadClass.getMethod("shutdown");
            shutdownMethod.invoke(null);
        } catch (Exception e) {
            try {
                Thread[] threads = new Thread[Thread.activeCount()];
                int count = Thread.enumerate(threads);
                
                for (int i = 0; i < count; i++) {
                    Thread thread = threads[i];
                    if (thread != null && thread.getName().contains("mysql-cj-abandoned-connection-cleanup")) {
                        thread.setDaemon(true);
                        thread.interrupt();
                        break;
                    }
                }
            } catch (Exception ex) {
            }
        }
    }
}

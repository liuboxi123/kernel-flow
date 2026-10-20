package core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

//应用启动监听器
@WebListener
public class AppStartupListener implements ServletContextListener {
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
    }
}

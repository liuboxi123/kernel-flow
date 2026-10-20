package util;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.mapping.Environment;
import com.baomidou.mybatisplus.core.MybatisConfiguration;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 通用工具类
 * 集成 HikariCP 连接池，提供 SqlSession 的获取和管理
 */
public class MyBatisUtil {
    private static SqlSessionFactory sqlSessionFactory;
    private static HikariDataSource dataSource;
    
    static {
        try {
            // 初始化 HikariCP 连接池
            initHikariCP();
            
            // 创建 MyBatis-Plus 的 SqlSessionFactory
            sqlSessionFactory = createSqlSessionFactory(dataSource);
            System.out.println("[MYBATIS-PLUS] SqlSessionFactory initialized with HikariCP");
        } catch (Exception e) {
            System.err.println("[MYBATIS-PLUS] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("MyBatis-Plus 初始化失败", e);
        }
    }
    
    /**
     * 创建 SqlSessionFactory
     */
    private static SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        // 创建事务工厂
        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();
        
        // 创建环境
        Environment environment = new Environment("development", transactionFactory, dataSource);
        
        // 创建 MyBatis-Plus 配置
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        
        // 启用驼峰命名转换
        configuration.setMapUnderscoreToCamelCase(true);
        
        // 添加 MyBatis-Plus 插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        configuration.addInterceptor(interceptor);
        
        // 注册 Mapper 接口
        configuration.addMapper(mapper.UserMapper.class);
        configuration.addMapper(mapper.PayMapper.class);
        
        // 创建 MyBatis-Plus 的 SqlSessionFactory
        MybatisSqlSessionFactoryBuilder builder = new MybatisSqlSessionFactoryBuilder();
        return builder.build(configuration);
    }
    
    /**
     * 初始化 HikariCP 连接池
     */
    private static void initHikariCP() {
        HikariConfig config = new HikariConfig();
        
        // 数据库配置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/kernel_flow?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
        config.setUsername("root");
        config.setPassword("123456");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置
        config.setMaximumPoolSize(20);           // 最大连接数
        config.setMinimumIdle(5);                // 最小空闲连接数
        config.setConnectionTimeout(30000);      // 连接超时时间（毫秒）
        config.setIdleTimeout(600000);           // 空闲连接超时时间（10分钟）
        config.setMaxLifetime(1800000);          // 连接最大生命周期（30分钟）
        config.setPoolName("KernelFlowHikariCP"); // 连接池名称
        
        // 性能优化配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        // 创建数据源
        dataSource = new HikariDataSource(config);
        System.out.println("[HIKARI] Connection pool initialized: " + config.getPoolName());
    }
    
    /**
     * 获取 SqlSessionFactory
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
    
    /**
     * 获取 HikariDataSource
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 获取 SqlSession（自动提交）
     * @return SqlSession
     */
    public static SqlSession openSession() {
        return sqlSessionFactory.openSession(true);
    }
    
    /**
     * 获取 SqlSession（手动提交）
     * @param autoCommit 是否自动提交
     * @return SqlSession
     */
    public static SqlSession openSession(boolean autoCommit) {
        return sqlSessionFactory.openSession(autoCommit);
    }
    
    /**
     * 获取 Mapper 代理对象
     * @param mapperClass Mapper 接口类
     * @param <T> Mapper 类型
     * @return Mapper 代理对象
     */
    public static <T> T getMapper(Class<T> mapperClass) {
        SqlSession session = openSession();
        return session.getMapper(mapperClass);
    }
    
    /**
     * 安全关闭 SqlSession
     * @param session SqlSession
     */
    public static void closeSession(SqlSession session) {
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * 执行数据库操作（自动管理事务）
     * @param action 数据库操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public static <T> T execute(DbAction<T> action) {
        SqlSession session = null;
        try {
            session = openSession(false); // 手动提交
            T result = action.execute(session);
            session.commit(); // 提交事务
            return result;
        } catch (Exception e) {
            if (session != null) {
                session.rollback(); // 回滚事务
            }
            System.err.println("[MYBATIS-PLUS] Transaction rollback: " + e.getMessage());
            throw new RuntimeException("数据库操作失败", e);
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * 关闭连接池（应用 shutdown 时调用）
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[HIKARI] Connection pool closed");
        }
    }
    
    /**
     * 数据库操作函数式接口
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface DbAction<T> {
        T execute(SqlSession session) throws Exception;
    }
}
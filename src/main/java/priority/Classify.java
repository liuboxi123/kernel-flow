package priority;
//作业优先级评价体系
public class Classify {
//    作业特权等级划分
//    系统保命与最高特权
    public static final int P1 = 1; // 系统熔断与自愈作业
    public static final int P2 = 2; // root账户触发操作
//    核心业务与VIP特权
    public static final int P3 = 3; // 金钱交易与核心链路
    public static final int P4 = 4; // 普通管理员的日常运作
    public static final int P5 = 5; // 高价值用户VIP的实时交互
//    普通用户交互
    public static final int P6 = 6; // 普通用户的实时操作（用户登录，点赞，收藏等）
    public static final int P7 = 7; // 普通用户的个人数居导出，非紧急的消息
    public static final int P8 = 8; // 未登录用户（游客）的搜索请求，静态资源加载
//    后台批处理与GC
    public static final int P9 = 9; // 常规数据统计与报表
    public static final int P10 = 10; // 系统清理与归档（GC级任务）（高峰期可直接丢弃）
}

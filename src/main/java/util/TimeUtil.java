package util;

import java.time.format.DateTimeFormatter;

//线程安全的时间日期格式工具类
public class TimeUtil {
//    时间日期格式缓存
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

//    获取当前时间
    public static long getCurrentMillis(){
        return System.currentTimeMillis();
    }
}

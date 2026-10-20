package priority;

import static priority.Classify.*;

//特权评估器(依赖后端的json node字段解析)
public class PriorityEvaluator {
    public int staticPriority(String s){
        
        switch (s) {
            case "ROOT":
                return P2;
            case "PAYMENT":
                return P3;
            case "ADMIN":
                return P4;
            case "VIP":
                return P5;
            case "USER":
                return P6;
            case "USER_WAIT":
                return P7;
            case "UNLOGIN":
                return P8;
            case "WAIT":
                return P9;
            case "GC":
                return P10;
            default:
                return P8;
        }
    }
}

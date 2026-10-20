package router;


import Job.Job;
import handle.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluatorRouter {
    private final SimpleTask simpleTask = new SimpleTask();
    private final PayTask payTask = new PayTask();
    private final AdminTask adminTask = new AdminTask();
    private final RootTask rootTask = new RootTask();
    private final ExecutorService executor = Executors.newFixedThreadPool(50);
    public static String staticPriority(String userRole,String type){
        if("ROOT".equals(userRole)){
            return "ROOT";
        }else if("ADMIN".equals(userRole)){
            return "ADMIN";
        }else if("VIP".equals(userRole)){
            return "VIP";
        }else if("PAYMENT".equals(type)){
            return "PAYMENT";
        }else if("USER".equals(userRole) && "".equals(type)){
            return "USER";
        }else if("USER".equals(userRole) && "WAIT".equals(type)){
            return "USER_WAIT";
        }else if("".equals(userRole) && "WAIT".equals(type)){
            return "WAIT";
        }else if("".equals(userRole) && "".equals(type)){
            return "GC";
        }else{
            return "UNLOGIN";
        }
    }

    public long deadLine(String jobType){
        switch (jobType) {
            case "ROOT":
                return 1000 * 60 *30;
            case "PAYMENT":
                return 1000 * 60 * 5;
            case "ADMIN":
                return 1000 * 60 * 3;
            case "VIP":
                return 1000 * 60 * 2;
            case "USER":
                return 1000 * 60;
            case "USER_WAIT":
                return 1000 * 60 * 7;
            case "UNLOGIN":
                return 1000 * 10;
            case "WAIT":
                return 1000 * 60 * 30;
            case "GC":
                return 100;
            default:
                return 1000 * 10;
        }
    }

    public int maxRetries(String jobType){
        switch (jobType) {
            case "ROOT":
                return 5;
            case "PAYMENT":
                return 3;
            case "ADMIN":
                return 2;
            case "VIP":
                return 1;
            case "USER":
                return 0;
            case "USER_WAIT":
                return 0;
            case "UNLOGIN":
                return 0;
            case "WAIT":
                return 0;
            case "GC":
                return 0;
            default:
                return 0;
        }
    }

    public void dispatch(Job job){
        String actionType = job.getActionType();
//        放入线程池执行
        Runnable taskRunnable = () -> {
            if(actionType == null || actionType.isEmpty()){
                simpleTask.simpleTask(job);
                return;
            }

            switch(actionType){
                case "ROOT":
                    rootTask.rootTask(job);
                    break;
                case "PAYMENT":
                    payTask.payTask(job);
                    break;
                case "ADMIN":
                    adminTask.adminTask(job);
                    break;
                case "VIP":
                case "USER":
                case "USER_WAIT":
                case "UNLOGIN":
                case "WAIT":
                case "GC":
                default:
                    simpleTask.simpleTask(job);
                    break;
            }
        };
        executor.execute(taskRunnable);
    }
}

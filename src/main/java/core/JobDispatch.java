package core;

import Job.Job;
import router.EvaluatorRouter;
import util.HardWareUtil;
import util.TimeUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobDispatch {
    private final PartitionedQueueManager queueManager = new PartitionedQueueManager();
    private final ExecutorService workerpool = Executors.newCachedThreadPool(r ->{  // 调度者线程
        Thread t = new Thread(r,"RealTimeJobWorker");
        t.setDaemon(true);
        return t;
    });
    private final int CORE_SIZE = Runtime.getRuntime().availableProcessors();   // CPU核心数
    private final ExecutorService businessPool = new ThreadPoolExecutor(    // 执行者线程池
            CORE_SIZE * 2,             // 核心线程数
            CORE_SIZE * 10,            // 最大线程数
            60L, TimeUnit.SECONDS,     // 空闲线程存活时间
            new LinkedBlockingQueue<>(1000), // 有界队列，防止 OOM
            r -> {
                Thread t = new Thread(r, "BusinessWorker");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：队列满了由调用者线程执行，起到限流作用
    );
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private EvaluatorRouter router = new EvaluatorRouter();

    public void start(){
        if(isRunning.get()) return;
        isRunning.set(true);

        workerpool.submit(() -> {
            while (isRunning.get()) {
                try{
                    Job job = queueManager.takeJobFromAny();
                    if(job != null && !HardWareUtil.getInstance().isSysBusy()){
//                        System.out.println("[JobDISPATCH]: job dispatch -- id" + job.getJobId() + "--time:" + TimeUtil.getCurrentMillis());
                        businessPool.submit(() -> router.dispatch(job));    // 提交给业务处理线程池
                    } else {
                        break;  // 如果当前系统负载过高，停止分发任务
                    }
                } catch (Exception e){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void stop(){
        isRunning.set(false);
        workerpool.shutdown();
        businessPool.shutdown();
    }
}

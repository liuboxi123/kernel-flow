package util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HardWareUtil {
    private static volatile HardWareUtil instance;
    
    private static final double MEMORY_THRESHOLD = 0.8;
    private static final int SAMPLE_INTERVAL_SECONDS = 10;
    
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<SystemStatus> currentStatus;
    
    private HardWareUtil() {
        this.currentStatus = new AtomicReference<>(new SystemStatus());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HardwareMonitor");
            t.setDaemon(true);
            return t;
        });
        
        startSampling();
    }
    
    public static HardWareUtil getInstance() {
        if (instance == null) {
            synchronized (HardWareUtil.class) {
                if (instance == null) {
                    instance = new HardWareUtil();
                }
            }
        }
        return instance;
    }
    
    private void startSampling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                SystemStatus status = sampleSystemStatus();
                currentStatus.set(status);
                
                if (status.isBusy) {
                    System.out.println("[HARDWARE] Sampled - Busy | Load: " + 
                        String.format("%.2f", status.loadAverage) + 
                        ", Memory: " + String.format("%.2f", status.memoryUsage * 100) + "%");
                }
            } catch (Exception e) {
                System.err.println("[HARDWARE] Sampling error: " + e.getMessage());
            }
        }, 0, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    private SystemStatus sampleSystemStatus() {
        SystemStatus status = new SystemStatus();
        
        Runtime runtime = Runtime.getRuntime();
        
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        status.memoryUsage = (double) usedMemory / maxMemory;
        
        status.availableProcessors = runtime.availableProcessors();
        status.loadAverage = getLoadAverage();
        
        double cpuThreshold = status.availableProcessors * 0.8;
        boolean isMemoryBusy = status.memoryUsage > MEMORY_THRESHOLD;
        boolean isCpuBusy = status.loadAverage > cpuThreshold;
        
        status.isBusy = isCpuBusy || isMemoryBusy;
        status.sampleTime = System.currentTimeMillis();
        
        return status;
    }
    
    private double getLoadAverage() {
        try {
            java.lang.management.OperatingSystemMXBean osBean = 
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            
            double loadAverage = osBean.getSystemLoadAverage();
            
            if (loadAverage < 0) {
                return 0.0;
            }
            
            return loadAverage;
            
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public boolean isSysBusy() {
        SystemStatus status = currentStatus.get();
        return status.isBusy;
    }
    
    public double getCpuLoadAverage() {
        return currentStatus.get().loadAverage;
    }
    
    public double getMemoryUsage() {
        return currentStatus.get().memoryUsage;
    }
    
    public int getAvailableProcessors() {
        return currentStatus.get().availableProcessors;
    }
    
    public long getLastSampleTime() {
        return currentStatus.get().sampleTime;
    }
    
    public SystemStatus getCurrentStatus() {
        return currentStatus.get();
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static class SystemStatus {
        public boolean isBusy;
        public double loadAverage;
        public double memoryUsage;
        public int availableProcessors;
        public long sampleTime;
        
        public SystemStatus() {
            this.isBusy = false;
            this.loadAverage = 0.0;
            this.memoryUsage = 0.0;
            this.availableProcessors = Runtime.getRuntime().availableProcessors();
            this.sampleTime = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return "SystemStatus{" +
                    "isBusy=" + isBusy +
                    ", loadAverage=" + String.format("%.2f", loadAverage) +
                    ", memoryUsage=" + String.format("%.2f", memoryUsage * 100) + "%" +
                    ", processors=" + availableProcessors +
                    ", sampleTime=" + sampleTime +
                    '}';
        }
    }
}

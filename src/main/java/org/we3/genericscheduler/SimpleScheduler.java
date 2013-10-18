package org.we3.genericscheduler;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Dumn scheduler that uses a fixed thread pool
 * User: Andrew
 * Date: 10/15/13
 * Time: 3:15 AM
 */
public class SimpleScheduler {
    private int size;
    ExecutorService service;

    public SimpleScheduler(int threadPoolSize) {
        this.size = threadPoolSize;
        service = Executors.newFixedThreadPool(this.size);
    }

    /**
     * Default to 15 threads
     */
    public SimpleScheduler() {
        this(15);
    }

    public void addJob(Runnable runnable){
        service.execute(runnable);
    }

    public void addJobs(Runnable[] runnable){
        for(Runnable r : runnable){
            service.execute(r);
        }
    }

    public void addJobs(Collection<Runnable> runnables){
        for(Runnable r : runnables){
            service.execute(r);
        }
    }

    public void shutdown() {
        service.shutdown();

        try {
            if(!service.awaitTermination(60, TimeUnit.SECONDS)){
                service.shutdownNow();

                if(!service.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("ExecuterService failed to shutdown");
                }
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            service.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public boolean isShutdown() {
        return service.isShutdown();
    }

    public boolean isTerminated() {
        return service.isTerminated();
    }
}

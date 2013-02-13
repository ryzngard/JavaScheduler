/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.we3.genericscheduler;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

/**
 * A class that allows to schedule runnable objects at a certain time. The class
 * is thread safe, although I believe adding to a thread pool is already thread
 * safe it uses blocking just in case. This may cause some performance issues,
 * so if performance becomes a problem then look into refactoring and taking out
 * the synchronized portions
 *
 * @author Andrew
 */
public class ThreadScheduler {

    /**
     * Time Unit to use, defaults to seconds.
     */
    TimeUnit unit = TimeUnit.SECONDS;
    /**
     * The thread pool for executing the threads
     */
    ScheduledThreadPoolExecutor executer;
    /**
     * Default thread pool size
     */
    private int DEFAULTPOOLSIZE = 10;

    /**
     * Default constructor, use the {@link DEFAULTPOOLSIZE} to make the thread
     * pool
     */
    public ThreadScheduler() {
        newThreadPool(DEFAULTPOOLSIZE);
    }

    /**
     *
     * @param threadPoolSize
     */
    public ThreadScheduler(int threadPoolSize) {
        newThreadPool(threadPoolSize);
    }

    /**
     * Start a new thread pool with the given size. Will close all other threads
     * that currently exist
     *
     * @param size
     */
    public synchronized void newThreadPool(int size) {
        if (executer != null) {
            executer.getQueue().removeAll(executer.getQueue());
        }
        executer = new ScheduledThreadPoolExecutor(size);
    }

    /**
     * Schedule the event for the specified time
     *
     * @param event what is to be run
     * @param time the time it is supposed to be run
     * @throws RejectedExecutionException
     */
    public synchronized void scheduleEvent(Runnable event, Date time) throws RejectedExecutionException {
        Date now = new Date();
        if (time.before(now)) {
            throw new IllegalArgumentException("The time for scheduling cannot be before the present! I can't change history...");
        }

        //get the time difference in milliseconds
        long difference = time.getTime() - now.getTime();

        checkIfFull();

        //Schedule the event once
        executer.schedule(event, difference, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule an event that continues to recur. The timebetween determines how
     * much time needs to pass between each event. This time can use the classes
     * value of TimeUnit by passing null for the TimeUnit argument.
     *
     * @param event
     * @param time
     * @param timeBetween
     * @param unit
     * @throws RejectedExecutionException
     */
    public synchronized void scheduleRecurringEvent(Runnable event, Date time, int timeBetween, TimeUnit unit) throws RejectedExecutionException {
        Date now = new Date();
        if (time.before(now)) {
            throw new IllegalArgumentException("The time for scheduling cannot be before the present! I can't change history...");
        }

        if (unit == null) {
            unit = this.unit;
        }

        //get the time difference in milliseconds
        long difference = time.getTime() - now.getTime();

        checkIfFull();

        System.out.println("Scheduling event for " + time);
        //Schedule the recurring event
        //Convert everything to milliseconds to reduce roundoff error.
        executer.scheduleAtFixedRate(event, difference, TimeUnit.MILLISECONDS.convert(timeBetween, unit), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @return
     */
    public TimeUnit getTimeUnit() {
        return unit;
    }

    /**
     *
     * @param unit
     */
    public void setTimeUnit(TimeUnit unit) {
        this.unit = unit;
    }

    /**
     * Used by internal methods to check if the active threads equal the maximum
     * pool size. If so, it throws a RejectedExecutionException
     */
    private void checkIfFull() throws RejectedExecutionException {
        if (isFull()) {
            throw new RejectedExecutionException();
        }
    }
    
    /**
     * True if the active threads fulfills the maximum pool size
     * @return 
     */
    public boolean isFull(){
        return executer.getActiveCount() >= executer.getMaximumPoolSize();
    }

    /**
     * Determines if the threads have been stopped. Call the stop() method to
     * try and stop, or the stopNow() to completely force all the threads to
     * stop.
     *
     * @return
     */
    public boolean isWorking() {
        return !executer.isShutdown();
    }

    /**
     * Sets the pool size, or the amount of threads that can be maintained
     *
     * @param size
     */
    public void reSize(int size) {
        executer.setCorePoolSize(size);
    }

    /**
     * Shutdown the internal threadpool executer. Thread existance is dependent
     * on the ContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public void stop() {
        executer.shutdown();
    }

    /**
     * Sets the policy on whether to continue executing existing periodic tasks
     * even when this executor has been shutdown. In this case, these tasks will
     * only terminate upon shutdownNow or after setting the policy to false when
     * already shutdown. This value is by default false.
     *
     * @param value true to continue, else don't
     */
    public void setContinueThreadOnStop(boolean value) {
        executer.setContinueExistingPeriodicTasksAfterShutdownPolicy(value);
    }
}

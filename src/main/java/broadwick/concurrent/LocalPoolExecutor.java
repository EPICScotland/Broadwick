/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.concurrent;

import broadwick.BroadwickException;
import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This Executor controls the execution of several threads using a fixed thread pool. Several instances of the callable
 * object are added to the execution pool which monitors their execution, if any fails, i.e. any of the jobs throws an
 * exception, then all non-running jobs are canceled and currently running jobs are terminated.
 */
@Slf4j
public class LocalPoolExecutor implements Executor {

    /**
     * Create the Callable executor that will run a supplied number of instances of a supplied callable, canceling the execution
     * if a thread throws an exception.
     * @param threadPoolSize    the number of concurrent threads to be run (the size of the thread pool)
     * @param numInstancesToRun the number of threads to be run.
     * @param callable          the actual Callable instance that is to be executed in the pool.
     */
    public LocalPoolExecutor(final int threadPoolSize, final int numInstancesToRun, final Callable<?> callable) {
        poolSize = threadPoolSize;
        numRuns = numInstancesToRun;
        job = callable;
        service = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Create the Callable executor that will run each callable in a collection, canceling the execution
     * if a thread throws an exception.
     * @param threadPoolSize    the number of concurrent threads to be run (the size of the thread pool)
     * @param jobs          a collection of [Callable] jobs to be run.
     */
    public LocalPoolExecutor(final int threadPoolSize, final List<Callable<?>> jobs) {
        poolSize = threadPoolSize;
        numRuns = jobs.size();
        job = null;
        service = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final void run() {

        final List<Future<?>> tasks = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(numRuns);

        status = Executor.Status.RUNNING;

        // Submit all the Callable objects to the executor, counting down the latch
        // when each submitted job is finished. If any of the jobs throws an exception,
        // it will be wrapped in an ExecutionException by the Future and we will check for it later.
        for (int i = 0; i < numRuns; i++) {
            final int taskId = i;
            log.trace("Adding task {} to the executor service.", i);
            tasks.add(service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        job.call();
                    } catch (Exception e) {
                        ok = false;
                        throw new BroadwickException(e);
                    } finally {
                        log.debug("Completed task {}", taskId);
                        latch.countDown();
                    }
                }
            }));
        }

        // We now have all the jobs submitted to the executor service. Now we iteratively check the status of each 
        // job (sleeping for 100ms between checks) to determine if each job has been completed. If any job
        // fails then we catch the resultant ExecutionException and cancel all remaining jobs.
        int completedTasks = 0;
        while (completedTasks < tasks.size()) {
            try {
                final Iterator<Future<?>> iterator = tasks.iterator();
                while (iterator.hasNext()) {
                    final Future<?> task = iterator.next();
                    if (task.isDone()) {
                        completedTasks++;
                        task.get();
                        iterator.remove();
                    } else if (task.isCancelled()) {
                        stopAllTasks(tasks);
                    }
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = Executor.Status.TERMINATED;
                ok = false;
                break;
            } catch (ExecutionException e) {
                // One of the submitted jobs threw an exception, we need to stop all the currently running threads.
                log.error("LocalPoolExecutor detected an error: {}", Throwables.getStackTraceAsString(e));
                stopAllTasks(tasks);
                service.shutdown();
                status = Executor.Status.TERMINATED;
                ok = false;
                break;
            }
        }

        try {
            if (status != Executor.Status.TERMINATED) {
                latch.await();
            }
        } catch (InterruptedException e) {
            log.error("Caught exception closing failed tasks");
            status = Executor.Status.TERMINATED;

        }

        shutdown();
    }

    /**
     * Send a message to all the futures to cancel their execution. On completion the status of the executor is set to
     * Executor.Status.TERMINATED.
     * @param tasks a collection of future object that are to be stopped.
     */
    private void stopAllTasks(final List<Future<?>> tasks) {
        for (final Future<?> task : tasks) {
            task.cancel(true);
        }
        status = Executor.Status.TERMINATED;
    }

    /**
     * Shutdown the ExecutionService, blocking until ALL the threads have finished before returning.
     */
    private void shutdown() {
        log.trace("Shutting down the LocalPoolExecutor");
        try {
            service.shutdown();
            service.awaitTermination(1000, TimeUnit.MILLISECONDS);

            log.trace("LocalPoolExecutor succesfully shutdown");
            status = Executor.Status.COMPLETED;

        } catch (InterruptedException e) {
            // Nothing to do - we are shuting done the service
            Thread.currentThread().interrupt();
            status = Executor.Status.TERMINATED;
        } catch (Exception e) {
            // Nothing to do - we are shuting done the service
            status = Executor.Status.TERMINATED;
        }
    }
    private final int poolSize;
    private final int numRuns;
    private final Callable<?> job;
    private final ExecutorService service;
    private Status status = Executor.Status.NOT_STARTED;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private boolean ok = true; /// true if the underlying task completed without error, false otherwise
}

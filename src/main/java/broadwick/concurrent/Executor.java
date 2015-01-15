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

/**
 * Interface for all Broadwick executors, these include a LocalPoolExecutor, which runs and monitors jobs on a local
 * machine, GridPoolExecutor which runs and monitors jobs on a grid environment and DistributedPoolExecutor which runs
 * and monitors jobs in a distributed pool.
 */
public interface Executor {

    /**
     * Status of the executor.
     */
    public static enum Status {

        /**
         * The jobs have not yet been started.
         */
        NOT_STARTED,
        /**
         * The jobs are still running.
         */
        RUNNING,
        /**
         * All jobs finished successfully.
         */
        COMPLETED,
        /**
         * Some job(s) threw an exception causing the other jobs to be terminated.
         */
        TERMINATED
    }

    /**
     * Get the current status of the jobs in the executor. The status is for the batch of jobs which can be wating to be
     * started, running. completed or terminated (completed with errors).
     * @return the status of the jobs in the executor.
     */
    Status getStatus();

    /**
     * Run the required number of jobs, checking for thrown exceptions once each task has completed. On detection of an
     * exception subsequent tasks will be cancelled.
     */
    void run();
}

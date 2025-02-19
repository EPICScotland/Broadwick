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
package broadwick.montecarlo;

import lombok.Getter;

/**
 * Implementing classes are used to take measurements during a Monte Carlo run.
 */
public abstract class McObserver implements Comparable<McObserver> {

    /**
     * Creates an observer dedicated to one Monte Carlo process.
     * @param mc the Monte Carlo process
     */
    public McObserver(final MonteCarlo mc) {
        this.monteCarlo = mc;
    }

    @Override
    public final int compareTo(final McObserver o) {
        // We really are only interested in determining if the Observers are equal so that a tree based table 
        // can distinguish between observers. Order does NOT matter so we return 1 if the objects are not equal.
        if (this == o) {
            return 0;
        }
        return 1;
    }

    /**
     * Gets called when the simulation has started after the initialization and before the termination condition is
     * checked the first time.
     */
    public abstract void started();

    /**
     * Gets called when the simulation has taken a step, whether or not the step is accepted or not or if the 
     * burn-in period has elapsed.
     */
    public abstract void step();


    /**
     * Gets called by the Monte Carlo process after the burnin period has elaspsed.
     */
    public abstract void takeMeasurements();

    /**
     * Gets called when a simulation has finished, directly after the termination check.
     */
    public abstract void finished();
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private MonteCarlo monteCarlo;
}

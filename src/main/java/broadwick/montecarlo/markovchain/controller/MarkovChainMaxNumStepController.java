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
package broadwick.montecarlo.markovchain.controller;

import broadwick.montecarlo.markovchain.MarkovChainMonteCarlo;

/**
 * Create a default controller that run 1000 steps.
 */
public class MarkovChainMaxNumStepController implements MarkovChainController {

    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path
     * length).
     */
    public MarkovChainMaxNumStepController() {
        this(1000);
    }

    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path
     * length).
     * @param maxSteps the maximum length of the monte carlo chain.
     */
    public MarkovChainMaxNumStepController(final int maxSteps) {
        this.maxSteps = maxSteps;
    }

    @Override
    public final boolean goOn(final MarkovChainMonteCarlo mc) {
        return mc.getNumStepsTaken() < maxSteps;
    }
    private int maxSteps;
}

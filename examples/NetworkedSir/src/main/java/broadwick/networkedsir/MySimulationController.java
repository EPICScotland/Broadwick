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
package broadwick.networkedsir;

import broadwick.stochastic.SimulationController;
import broadwick.stochastic.StochasticSimulator;

/**
 * A controller is used to tell the simulation to stop. We will create a simple controller
 * to stop the simulation when there are no more susceptibles left or when we have reached a maximum time limit.
 */
class MySimulationController implements SimulationController {
    
    /**
     * Create the controller that will stop at a set time.
     * @param maxTime the maximum time that the simulation will run for.
     */
    MySimulationController(double maxTime) {
        maxT = maxTime;
    }

    @Override
    public boolean goOn(final StochasticSimulator process) {
        return process.getCurrentTime() < maxT;
    }
    
    private final double maxT;
}

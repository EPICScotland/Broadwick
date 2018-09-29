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
package broadwick.stochastic;

import java.io.Serializable;

/**
 * A SimulationController (and its implementing classes) are responsible for controlling the simulation in the sense
 * that the StochasticSimulator checks with the controller object whether to go on or not.
 */
public interface SimulationController extends Serializable {

    /**
     * Returns whether or not to go on with the given simulation.
     * @param process the stochastic process this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(StochasticSimulator process);
}

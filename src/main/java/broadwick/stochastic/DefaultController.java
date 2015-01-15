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

import lombok.Getter;
import lombok.Setter;

/**
 * A simple implementation of a {@link SimulationController} causing the simulation to stop after a given time.
 */
public class DefaultController implements SimulationController {

    /**
     * Creates the controller for a given time where the simulation has to stop.
     * @param maxTime the maximum time the simulation should run. Once the simulation time reaches this value, goOn()
     *                will return false.
     */
    public DefaultController(final double maxTime) {
        this.maxTime = maxTime;
    }

    @Override
    public final boolean goOn(final StochasticSimulator process) {
        return process.getCurrentTime() < maxTime;
    }

    @Getter
    @Setter
    private double maxTime;
}

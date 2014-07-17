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
package broadwick.example.deterministicsir;

import broadwick.odesolver.OdeSolver;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple event that will add 50 new susceptibles to the system.
 */
@Slf4j
class MyThetaEvent {

    /**
     * Create the theta event that will add 50 to the susceptibles.
     * @param solver the solver this theta event will be attaached. 
     */
    public MyThetaEvent(final OdeSolver solver) {
        this.solver = solver;
    }
    
    /**
     * Perform the theta event. Here we will just add 50 individuals to the susceptibles.
     */
    public final void doThetaEvent() {
        log.info("Performing theta at {}", this.solver.getCurrentTime());
        solver.getDependentVariables().set(0, solver.getDependentVariables().get(0) + 50);
    }
    
    
    private final OdeSolver solver;
}

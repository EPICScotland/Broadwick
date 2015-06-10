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

import broadwick.odesolver.OdeController;
import broadwick.odesolver.OdeSolver;

/**
 * A simple controller object that will stop the solver at a specific time.
 */
public class MyOdeController implements OdeController {

    /**
     * Create the controller, specifying the time at which the solver should stop.
     * @param tMax the time at which the solver should stop.
     */
    MyOdeController(final double tMax) {
        this.tMax = tMax;
    }

    @Override
    public final boolean goOn(final OdeSolver solver) {
        return solver.getCurrentTime() < tMax;
    }

    private final double tMax;
}
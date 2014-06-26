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
package broadwick.odesolver;

/**
  * An OdeController (and its implementing classes) are responsible for controlling the ODE solver in the sense
 * that the solver checks with the controller object whether to go on or not.
 */
public interface OdeController {
    
    /**
     * Returns whether or not to continue with the stepwise solution of the ode.
     * @param solver the ODE solver this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(final OdeSolver solver);
}
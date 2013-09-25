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
package broadwick.abc;

/**
 * AbcController class for running abc. The controller (and its implementing classes) are responsible for determining
 * whether or not the calculation should end.
 */
public interface AbcController {

    /**
     * Returns whether or not to continue with the calculation.
     * @param process the stochastic process this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(ApproxBayesianComp process);
}

/**
 * Create a default controller that run 1000 samples from the prior.
 */
class AbcMaxNumStepController implements AbcController {

    @Override
    public boolean goOn(final ApproxBayesianComp abc) {
        return abc.getNumSamplesTaken() <= 1000;
    }
};
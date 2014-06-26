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

import lombok.extern.slf4j.Slf4j;

/**
 * A fourth order Runge Kutta solver for systems of ODEs.
 */
@Slf4j
public class RungeKutta4 extends OdeSolver {

    /**
     * Create the solver for a give ode object and initial consitions.
     * @param ode the ode object containing the specification of the ode.
     * @param tStart the start time (the independent variable is assumed to be time)
     * @param tEnd the end time (the independent variable is assumed to be time)
     * @param stepSize the size of the step to be used in the solver.
     */
    public RungeKutta4(final Ode ode, final double tStart, final double tEnd, final double stepSize) {
        super(ode, tStart, tEnd, stepSize);
    }

    @Override
    public final void run() {
        log.debug("Running Runge Kutta 4th Order solver");

        // tell our observers that the simulation has started
        for (Observer observer : getObservers()) {
            observer.started();
        }

        // create some internal working arrays
        final int n = this.getOde().getInitialValues().length;
        Double[] k1 = new Double[n];
        Double[] k2 = new Double[n];
        Double[] k3 = new Double[n];
        Double[] k4 = new Double[n];
        final double halfStep = 0.5 * stepSize;
        Double[] dydx = new Double[n];
        Double[] yd = new Double[n];

        do {
            currentTime = currentTime + stepSize;
            while (currentTime > getNextThetaEventTime()) {
                doThetaEvent();
            }
            dydx = this.getOde().computeDerivatives(currentTime, dependentVariables);
            for (int i = 0; i < n; i++) {
                k1[i] = stepSize * dydx[i];
            }

            for (int i = 0; i < n; i++) {
                yd[i] = dependentVariables[i] + k1[i] / 2;
            }
            dydx = this.getOde().computeDerivatives(currentTime + halfStep, yd);
            for (int i = 0; i < n; i++) {
                k2[i] = stepSize * dydx[i];
            }

            for (int i = 0; i < n; i++) {
                yd[i] = dependentVariables[i] + k2[i] / 2;
            }
            dydx = this.getOde().computeDerivatives(currentTime + halfStep, yd);
            for (int i = 0; i < n; i++) {
                k3[i] = stepSize * dydx[i];
            }

            for (int i = 0; i < n; i++) {
                yd[i] = dependentVariables[i] + k3[i];
            }
            dydx = this.getOde().computeDerivatives(currentTime + stepSize, yd);
            for (int i = 0; i < n; i++) {
                k4[i] = stepSize * dydx[i];
            }

            for (int i = 0; i < n; i++) {
                dependentVariables[i] += k1[i] / 6 + k2[i] / 3 + k3[i] / 3 + k4[i] / 6;
            }

            for (Observer observer : getObservers()) {
                observer.step();
            }

        } while (this.getController().goOn(this));

        for (Observer observer : getObservers()) {
            observer.finished();
        }
    }
}

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
package broadwick.example.mle;

import broadwick.odesolver.Ode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SeirModel implements Ode {

    /**
     * Create the model, initialising the models parameters.
     * @param N the population size
     * @param beta the S-E rate
     * @param sigma the E-I rate
     * @param gamma the recovery (I-R) rate
     */
    public SeirModel(final double N, final double beta, final double sigma, final double gamma) {
        this.N = N;
        this.beta = beta;
        this.gamma = gamma;
        this.sigma = sigma;
        this.initialValues.add(N-1.0);  // S
        this.initialValues.add(0.0);     // E
        this.initialValues.add(1.0);     // I
        this.initialValues.add(0.0);     // R
    }

    @Override
    public final List<Double> computeDerivatives(final double t, final List<Double> y) {
        final List<Double> yDot = new ArrayList<>(y.size());
        yDot.add(-beta * y.get(0) * y.get(2)/N);                     // dS/dt  = -\beta IS
        yDot.add(beta * y.get(0) * y.get(2)/N - sigma * y.get(1));   // dE/dt = \beta IS - \sigma E
        yDot.add(sigma * y.get(1) - gamma*y.get(2));                 // dI/dt = sigma E - \gamma I
        yDot.add(gamma * y.get(2));                                  // dR/dt = \gamma I
        return yDot;
    }

    @Override
    public List<Double> getInitialValues() {
        return initialValues;
    }

    private final double N;
    private final double beta, sigma, gamma;
    List<Double> initialValues = new ArrayList<>(3);

}

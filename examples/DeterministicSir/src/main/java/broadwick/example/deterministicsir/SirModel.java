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

import broadwick.odesolver.Ode;
import java.util.ArrayList;
import java.util.List;

/**
 * Create a simple set of ODE describing a density dependent SIR model.
 */
class SirModel implements Ode {

    /**
     * Create the model, initialising the models parameters.
     * @param beta the s-I rate
     * @param gamma the recovery (I-R) rate
     * @param initialS the initial number of susceptibles.
     * @param initialI the initial number of infectious.
     * @param initialR the initial number of recovered.
     */
    public SirModel(final double beta, final double gamma,
                    final double initialS, final double initialI, final double initialR) {
        this.beta = beta;
        this.gamma = gamma;
        this.initialValues.add(initialS);
        this.initialValues.add(initialI);
        this.initialValues.add(initialR);
    }

    @Override
    public final List<Double> computeDerivatives(final double t, final List<Double> y) {
        final List<Double> yDot = new ArrayList<>(y.size());
        yDot.add(-beta * y.get(0) * y.get(1));                  // dS/dt  = -\beta IS
        yDot.add(beta * y.get(0) * y.get(1) - gamma * y.get(1));    // dI/dt = \beta IS - \gamma I
        yDot.add(gamma * y.get(1));                         // dR/dt = \gamma I
        return yDot;
    }

    @Override
    public List<Double> getInitialValues() {
        return initialValues;
    }

    private final double beta, gamma;
    List<Double> initialValues = new ArrayList<>(3);
}

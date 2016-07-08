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
package broadwick.stochasticinf;

import broadwick.io.FileOutput;
import broadwick.model.Model;
import broadwick.odesolver.Observer;
import broadwick.odesolver.Ode;
import broadwick.odesolver.OdeController;
import broadwick.odesolver.OdeSolver;
import broadwick.odesolver.RungeKutta4;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple class to run a disease model to generate observations for the inference code.
 */
@Slf4j
public class ObsGenerator extends Model {

    @Override
    public final void init() {
        log.info("Initialising project");
        tStart = this.getParameterValueAsDouble("startTime");
        tEnd = this.getParameterValueAsDouble("endTime");
        stepSize = (tEnd - tStart) / this.getParameterValueAsDouble("numSteps");
    }

    @Override
    public final void run() {
        final Ode myOde = new SeeiModel(this.getParameterValueAsDouble("beta"),
                                       this.getParameterValueAsDouble("sigma"),
                                       this.getParameterValueAsDouble("gamma"),
                                       this.getParameterValueAsDouble("initialS"),
                                       this.getParameterValueAsDouble("initialE1"),
                                       this.getParameterValueAsDouble("initialE2"),
                                       this.getParameterValueAsDouble("initialI"));
        final OdeSolver solver = new RungeKutta4(myOde, tStart, tEnd, stepSize);

        // Create a simple controller object to tell the simulator when to stop.
        final OdeController controller = new OdeController() {
            @Override
            public boolean goOn(OdeSolver solver) {
                return solver.getCurrentTime() < tEnd;
            }
        };
        solver.setController(controller);

        // we will need an observer to 'observe' our simulation and record the simulation states. First we
        // remove any obervers attached to the simulator (we can have more than one observer!).
        solver.getObservers().clear();
        final MyObserver observer = new MyObserver(solver, this.getParameterValue("outputFile"));
        solver.addObserver(observer);

        solver.run();
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    private double stepSize;
    private double tStart, tEnd;
}

/**
 * Create a simple set of ODE describing a density dependent SEEI model.
 */
class SeeiModel implements Ode {

    /**
     * Create the model, initialising the models parameters.
     * @param beta     the s-I rate
     * @param gamma    the recovery (I-R) rate
     * @param initialS the initial number of susceptibles.
     * @param initialI the initial number of infectious.
     * @param initialR the initial number of recovered.
     */
    SeeiModel(final double beta, final double sigma, final double gamma,
                     final double initialS, final double initialE1,
                     final double initialE2, final double initialI) {
        this.beta = beta;
        this.sigma = sigma;
        this.gamma = gamma;
        this.initialValues.add(initialS);
        this.initialValues.add(initialE1);
        this.initialValues.add(initialE2);
        this.initialValues.add(initialI);
    }

    @Override
    public List<Double> computeDerivatives(double t, List<Double> y) {
        final List<Double> yDot = new ArrayList<>(y.size());
        yDot.add(-beta * y.get(0) * y.get(3));                  // dS/dt  = -\beta IS
        yDot.add(beta * y.get(0) * y.get(3) - sigma * y.get(1));  // dE1/dt = \beta IS - \sigma E1
        yDot.add(sigma * y.get(1) - gamma * y.get(2));            // dE2/dt = \sigma E1 - \gamma E2
        yDot.add(gamma * y.get(2));                             // dR/dt = \gamma E2
        return yDot;
    }

    @Override
    public List<Double> getInitialValues() {
        return initialValues;
    }

    private final double beta, sigma, gamma;
    List<Double> initialValues = new ArrayList<>(4);
}

/**
 * A simple observer class that will take measurements at each step and save them to file.
 */
@Slf4j
class MyObserver extends Observer {

    /**
     * Create an observer to take measurements at each time step.
     * @param solver     the solver object to observe.
     * @param outputFile the name of the file to which we will save the measurements.
     */
    MyObserver(final OdeSolver solver, final String outputFile) {
        super(solver);
        fo = new FileOutput(outputFile);
    }

    @Override
    public final void started() {
        fo.write(String.format("%f,%s%n", this.getSolver().getCurrentTime(), this.getSolver().getDependetVariablesAsCsv()));
    }

    @Override
    public final void step() {
        fo.write(String.format("%f,%s%n", this.getSolver().getCurrentTime(), this.getSolver().getDependetVariablesAsCsv()));
    }

    @Override
    public final void finished() {
        fo.close();
    }

    @Override
    public final void theta(final double thetaTime, final Collection<Object> events) {
        
    }

    private final FileOutput fo;
}

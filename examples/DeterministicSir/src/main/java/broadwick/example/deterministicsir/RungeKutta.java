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
import broadwick.odesolver.OdeController;
import broadwick.odesolver.OdeSolver;
import broadwick.odesolver.RungeKutta4;
import broadwick.model.Model;
import lombok.extern.slf4j.Slf4j;


/**
 * An exmaple of how to solve a SIR model using the Runge Kutta solver. The solver add 50 new susceptibles at t=20 to
 * demonstrate the use of theta events.
 */
@Slf4j
public class RungeKutta extends Model {

    @Override
    public final void init() {
        log.info("Initialising project");
        tStart = this.getParameterValueAsDouble("startTime");
        tEnd = this.getParameterValueAsDouble("endTime");
        stepSize = (tEnd - tStart) / this.getParameterValueAsDouble("numSteps");
    }

    @Override
    public final void run() {
        log.info("Running project");

        final Ode myOde = new SirModel(this.getParameterValueAsDouble("beta"),
                                 this.getParameterValueAsDouble("rho"),
                                 this.getParameterValueAsDouble("initialS"),
                                 this.getParameterValueAsDouble("initialI"),
                                 this.getParameterValueAsDouble("initialR"));
        final OdeSolver solver = new RungeKutta4(myOde, tStart, tEnd, stepSize);

        // we will need an observer to 'observe' our simulation and record the simulation states. First we
        // remove any obervers attached to the simulator (we can have more than one observer!).
        solver.getObservers().clear();
        final MyObserver observer = new MyObserver(solver, this.getParameterValue("outputFile"));
        solver.addObserver(observer);

        // Create a simple controller object to tell the simulator when to stop.
        final OdeController controller = new MyOdeController(tEnd);
        solver.setController(controller);

        // Register theta events, these are fixed events that will be triggered at set times.
        solver.registerNewTheta(observer, 20.0, new MyThetaEvent(solver));
        
        solver.run();
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    private double stepSize;
    private double tStart, tEnd;

}

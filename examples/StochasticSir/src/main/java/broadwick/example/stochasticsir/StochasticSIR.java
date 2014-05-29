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
package broadwick.example.stochasticsir;

import broadwick.model.Model;
import broadwick.stochastic.SimulationController;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import broadwick.stochastic.TransitionKernel;
import broadwick.stochastic.algorithms.TauLeaping;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple example on how to use a the stochastic solver of Broadwick to solve the SIR model.
 * 
 * We add some aded functionality that stops the simulation when the number of susceptibles reached zero or a maximum
 * set time is reached. If, after 40 time units, the simulation is still running we add a furthre 50 susceptibles to 
 * the simulation.
 */
@Slf4j
public class StochasticSIR extends Model {

    @Override
    public final void init() {
	log.info("Initialising project");
        
        outputFileName = getParameterValue("outputFile");
        log.info("Initialising project");

        outputFileName = getParameterValue("outputFile");
        betaVal = getParameterValueAsDouble("beta");
        rhoVal = getParameterValueAsDouble("rho");
        tMax = getParameterValueAsDouble("tMax");

        log.info("Saving data to {}", outputFileName);
        log.info("beta = {}, rho = {}", betaVal, rhoVal);
        log.info("Max t = {}", tMax);

        initialSusceptibles = getParameterValueAsInteger("initialSusceptibles");
        initialInfectious = getParameterValueAsInteger("initialInfectious");
        initialRemoved = getParameterValueAsInteger("initialRemoved");

        log.info("initialSusceptibles =  {}", initialSusceptibles);
        log.info("initialInfectious = {}",initialInfectious);
        log.info("initialRemoved = {}", initialRemoved);
    }

    @Override
    public final void run() {
        log.info("Running project");

        // we will need an amount manager to keep track of the number of S/I/R in our simulation.
        amountManager = new MyAmountManager(initialSusceptibles, initialInfectious, initialRemoved, this);

        // The transition kernel defines the transition rates between each state.
        kernel = new TransitionKernel();
        updateKernel();

        final StochasticSimulator simulator = new TauLeaping(amountManager, kernel);

        // we will need an observer to 'observe' our simulation and record the simulation states. First we
        // remove any obervers attached to the simulator (we can have more than one observer!).
        simulator.getObservers().clear();
        final MyObserver observer = new MyObserver(simulator, outputFileName);
        simulator.addObserver(observer);

        // Create a simple controller object to tell the simulator when to stop.
        final SimulationController controller = new MySimulationController(tMax);
        simulator.setController(controller);

        // Register theta events, these are fixed events that will be triggered at set times.
        simulator.registerNewTheta(observer, 20.0, new MyThetaEvent(amountManager)); // TODO

        simulator.run();
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }
    
    /**
     * Update the transition kernel. This method actually clears the transition kernel and re-creates it given the 
     * number of susceptibles, infectious and removed individuals.
     */
    protected final void updateKernel() {
        kernel.clear();
        
        kernel.addToKernel(new SimulationEvent(amountManager.getSusceptible(), amountManager.getInfectious()), 
                           amountManager.getNumberOfSusceptibles()*betaVal);
        kernel.addToKernel(new SimulationEvent(amountManager.getInfectious(), amountManager.getRemoved()), 
                           amountManager.getNumberOfInfectious()*rhoVal);
    }


    private String outputFileName;
    private double betaVal;
    private double rhoVal;
    private double tMax;
    private int initialSusceptibles;
    private int initialInfectious;
    private int initialRemoved;
    private TransitionKernel kernel;
    private MyAmountManager amountManager;
}

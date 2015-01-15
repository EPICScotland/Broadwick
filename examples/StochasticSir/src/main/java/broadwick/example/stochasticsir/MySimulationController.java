/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package broadwick.example.stochasticsir;

import broadwick.stochastic.SimulationController;
import broadwick.stochastic.StochasticSimulator;

/**
 * A controller is used to tell the simulation to stop. We will create a simple controller
 * to stop the simulation when there are no more susceptibles left or when we have reached a maximum time limit.
 */
class MySimulationController implements SimulationController {
    
    /**
     * Create the controller that will stop at a set time.
     * @param maxTime the maximum time that the simulation will run for.
     */
    public MySimulationController(final double maxTime) {
        maxT = maxTime;
    }

    @Override
    public boolean goOn(final StochasticSimulator process) {
        return ((MyAmountManager)process.getAmountManager()).getNumberOfSusceptibles() > 0 && 
               process.getCurrentTime() < maxT;
    }
    
    private final double maxT;
}

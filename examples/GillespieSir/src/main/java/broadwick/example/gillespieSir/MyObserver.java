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
package broadwick.example.gillespieSir;

import broadwick.io.FileOutput;
import broadwick.stochastic.Observer;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple observer class that records the number of S/I/R at each time step.
 */
@Slf4j
class MyObserver extends Observer {

    /**
     * Create an observer for the simulation that will just save the data at each step.
     * @param simulator      the simulator that will be observed.
     * @param outputFileName the name of the file to which the data will be saved.
     */
    MyObserver(final StochasticSimulator simulator, final String outputFileName) {
        super(simulator);
        output = new FileOutput(outputFileName);
    }

    @Override
    public void started() {
        log.info("Started Observing using MyObserver.");
        output.write("#t\tS\tI\tR\n");

        final MyAmountManager amountManager = (MyAmountManager) this.getProcess().getAmountManager();
        output.write("%.2f\t%d\t%d\t%d\n", this.getProcess().getCurrentTime(),
                     amountManager.getNumberOfSusceptibles(),
                     amountManager.getNumberOfInfectious(),
                     amountManager.getNumberOfRemoved());
    }

    @Override
    public void step() {
        final MyAmountManager amountManager = (MyAmountManager) this.getProcess().getAmountManager();
        output.write("%.2f\t%d\t%d\t%d\n", this.getProcess().getCurrentTime(),
                     amountManager.getNumberOfSusceptibles(),
                     amountManager.getNumberOfInfectious(),
                     amountManager.getNumberOfRemoved());
    }

    @Override
    public void finished() {
        log.info("Finished Observing using MyObserver.");
        output.write("#timeSeries <- read.table('broadwick.stochasticSIR.dat', sep=\"\\t\")\n");
        output.write("#pdf(file=\"broadwick.stochasticSIR.pdf\", onefile=FALSE, width=7.5, height=7.5)\n");
        output.write("#cols <- c(\"yellow\", \"red\", \"blue\")\n");
        output.write("#plot(timeSeries$V1, timeSeries$V2, type=\"l\", xlim=c(0,max(timeSeries$V1)), ylim=c(0,150), col=cols[1])\n");
        output.write("#lines (timeSeries$V1, timeSeries$V3, type=\"l\", col=cols[2])\n");
        output.write("#lines (timeSeries$V1, timeSeries$V4, type=\"l\", col=cols[3])\n");
        output.close();
    }

    @Override
    public void theta(final double thetaTime, final Collection<Object> events) {
        log.info("Performing a theta event at t={}", thetaTime);
        for (Object theta : events) {
            // I know that the only theta event I've added to the model is a MyThetaEvent
            ((MyThetaEvent) theta).doThetaEvent();
        }
    }

    @Override
    public void observeEvent(final SimulationEvent event, final double tau, final int times) {
        // we don't need to record each event but we can log it in case we want to see it later.
        // Note: this method will cause event.toString() to be called BEFORE checking if the log message should be run,
        // if speed is important to your simulation then perhaps wrap this call with  if (log.isTraceEnabled()) {...}
        log.trace("Observing event {}", event.toString());
    }

    private final FileOutput output;

}

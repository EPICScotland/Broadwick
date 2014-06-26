/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package broadwick.example.deterministicsir;

import broadwick.odesolver.Observer;
import broadwick.odesolver.OdeSolver;
import broadwick.io.FileOutput;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple observer class that will take measurements at each step and save them to file.
 */
@Slf4j
public class MyObserver extends Observer {

    /**
     * Create an observer to take measurements at each time step.
     * @param solver the solver object to observe.
     * @param outputFile the name of the file to which we will save the measurements.
     */
    MyObserver(final OdeSolver solver, final String outputFile) {
        super(solver);
        fo = new FileOutput(outputFile);
    }

    @Override
    public final void started() {
        fo.write(String.format("%f,%s\n", this.getSolver().getCurrentTime(), this.getSolver().getDependetVariablesAsCsv()));
    }

    @Override
    public final void step() {
        fo.write(String.format("%f,%s\n", this.getSolver().getCurrentTime(), this.getSolver().getDependetVariablesAsCsv()));
    }

    @Override
    public final void finished() {
        fo.close();
    }

    @Override
    public final void theta(final double thetaTime, final Collection<Object> events) {
        log.info("Performing a theta event at t={}", thetaTime);
        for (Object theta : events) {
            // I know that the only theta event I've added to the model is a MyThetaEvent
            ((MyThetaEvent) theta).doThetaEvent();
        }
    }

    private final FileOutput fo;
}

package broadwick.montecarlo;

import broadwick.io.FileOutput;
import broadwick.montecarlo.acceptor.Acceptor;
import broadwick.montecarlo.acceptor.MetropolisHastings;
import broadwick.montecarlo.path.PathGenerator;
import broadwick.montecarlo.path.Step;
import broadwick.rng.RNG;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Monte Carlo class that is responsible for constructing Monte Carlo chains and the simulations thereon.
 */
@Slf4j
public class MonteCarlo {

    /**
     * Create a Monte Carlo instance.
     * @param model     The Monte Carlo Model to be run.
     * @param generator the object that will generate the Monte Carlo chain/path.
     */
    public MonteCarlo(final McModel model, final PathGenerator generator) {
        this(model, new McMaxNumStepController(1000), generator);
    }

    /**
     * Create a Monte Carlo instance.
     * @param model      The Monte Carlo Model to be run.
     * @param controller the controller object for this class.
     * @param generator  the object that will generate the Monte Carlo chain/path.
     */
    public MonteCarlo(final McModel model, final McController controller,
                      final PathGenerator generator) {
        this.observers = new HashSet<>(1);
        this.model = model;
        this.mcController = controller;
        this.pathGenerator = generator;
        this.acceptor = new MetropolisHastings(GENERATOR.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Run the MonteCarlo Simulation.
     */
    public final void run() {

        writer.write("# Steps taken\n");
        writer.write("# Current step accepted?\n");
        writer.write("# Current step coordinates\n");
        writer.write("# Results for current step\n");

        for (McObserver observer : observers) {
            observer.started();
        }

        Step currentStep = pathGenerator.getInitialStep();
        log.info("Running Monte Carlo simulation with initial step {}", currentStep.toString());
        McResults prevResults = model.compute(currentStep);
        writer.write("%d,%d,%s,%s\n", numStepsTaken, 1, currentStep.toString(), prevResults.toCsv());
            
        while (mcController.goOn(this)) {
            numStepsTaken++;
            int stepsSinceLastMeasurement = 0;
            final Step proposedStep = pathGenerator.generateNextStep(currentStep);
            final McResults currentResults = model.compute(proposedStep);
            
            for (McObserver observer : observers) {
                observer.step();
            }

            final boolean accepted = acceptor.accept(prevResults, currentResults);

            writer.write("%d,%d,%s,%s\n", numStepsTaken, (accepted ? 1 : 0),
                         proposedStep.toString(), currentResults.toCsv());
            if (accepted) {
                log.info("Accepted Monte Carlo step {}", proposedStep.toString());
                numAcceptedSteps++;
                if (numStepsTaken > burnIn
                    && (thinningInterval == 0 || stepsSinceLastMeasurement % thinningInterval == 0)) {
                    stepsSinceLastMeasurement++;
                    for (McObserver observer : observers) {
                        observer.takeMeasurements();
                    }
                }
                currentStep = proposedStep;
                prevResults = currentResults;
            } else {
                log.info("Rejected Monte Carlo step {}", proposedStep.toString());
            }
        }

        for (McObserver observer : observers) {
            observer.finished();
        }
    }

    /**
     * Add an observer to the list of observers.
     * @param observer the observer that is used to monitor/take measurements.
     */
    public final void addObserver(final McObserver observer) {
        observers.add(observer);
    }
    @Getter
    private int numStepsTaken = 0;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int numAcceptedSteps = 0;
    @Setter
    private int burnIn = 0;
    @Setter
    private int thinningInterval = 0;
    @Setter
    private Acceptor acceptor;
    @Setter
    private McController mcController;
    @Getter
    private Set<McObserver> observers;
    @Setter
    private FileOutput writer = new FileOutput();
    private McModel model;
    private PathGenerator pathGenerator;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
    // TODO measure [auto]correlation function(s)
}

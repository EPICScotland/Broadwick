package broadwick.example.montecarlo;

import broadwick.model.Model;
import broadwick.montecarlo.MonteCarlo;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.rng.RNG;
import broadwick.statistics.Samples;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This overly simplistic Monte Carlo example simulates throwing darts at a square dartboard and estimates pi by
 * calculating the fraction of darts that lie within the unit circle encompasses by the square. The simulation in this
 * case simply calculates a random (x,y) coordinate for the throw. As an example of how to use a custom
 * MonteCarloResults object we create one that records a hit or a miss (see Simulation.run()). We could have used the
 * default MonteCarloResults object to only record the hits; in that case we should not use the last 2 lines of the App
 * constructor and update the results object in Simulation.run() via <code>mc.getResults().getSamples().add(1)</code>.
 * The <code>mc.getResults().getSamples().getSum()</code> value would be the number of hits so we can divide that by
 * <code>this.getParameterValueAsInteger("numSimulations")</code> in our estimation of pi.
 */
@Slf4j
public class App extends Model {

    @Override
    public final void init() {
        log.info("Initialising project");

        mc = new MonteCarlo(new Simulation(),
                            this.getParameterValueAsInteger("numSimulations"));

        mc.setResultsConsumer(new MyResultsConsumer());
    }

    @Override
    public final void run() {
        log.info("Running project");
        mc.run();

        // this should return a Samples object. The results class should contain a samples object for each measureable in 
        // the simulation......
        final MyResultsConsumer results = (MyResultsConsumer) mc.getResults();
        log.info("Hits : Misses = {}", results.toCsv());
        log.info("Estimation of Pi = {}", 4 * results.getExpectedValue());
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    MonteCarlo mc;
}

/**
 * The Monte Carlo simulation is encompassed in this class. It simulates a random dart throw on a square from (-1,1) in
 * both the x and y coordinates. A hit is defined as those throws that lie within the unit circle. This class performs
 * one throw and the MonteCarlo object is responsible for performing many throws and serving up the results.
 */
@Slf4j
class Simulation extends MonteCarloScenario {

    /**
     * Create the simulation.
     */
    Simulation() {
        super();
        this.rng = new RNG();
    }

    @Override
    public MonteCarloResults run(final int seed) {
        // Estimate pi by randomly throwing darts into a square of side 2 units centered on (0,0). Te ratio of the area
        // of the circle to the square is pi/4, so the ratio of the number of darts that lie inside the cirlce compared
        // to those that lie insode the square is pi/4.

        // In this simulation we wil throw one dart!
        final MyResultsConsumer results = new MyResultsConsumer();

        final double x = rng.getDouble(-1, 1);
        final double y = rng.getDouble(-1, 1);

        final double r = Math.sqrt(x * x + y * y);
        if (r < 1) {
//            log.info("Hit");
            results.addHit();
        } else {
//            log.info("Miss");
            results.addMiss();
        }

        return results;
    }
    private final RNG rng;
}

/**
 * MonteCarloResults object that contains the results of the simulation above. It records the number of hits and misses
 * and serves the fraction of throws that 'hit' as the explected value.
 */
class MyResultsConsumer implements MonteCarloResults {

    @Override
    public double getExpectedValue() {
        return hits.getSum() / (hits.getSum() + misses.getSum());
    }

    @Override
    public Samples getSamples() {
        return hits;
    }

    @Override
    public String toCsv() {
        return String.format("%d ;  %d", hits.getSize(), misses.getSize());
    }

    @Override
    public MonteCarloResults join(final MonteCarloResults results) {
        final MyResultsConsumer r = (MyResultsConsumer) results;
        this.hits.add(r.hits);
        this.misses.add(r.misses);

        return this;
    }

    /**
     * Increment the number of hits.
     */
    public void addHit() {
        hits.add(1);
    }

    /**
     * Increment the number of misses.
     */
    public void addMiss() {
        misses.add(1);
    }
    
    @Override
    public void reset() {
        // do nothing
    }
    
    @Getter
    private final Samples hits = new Samples();
    @Getter
    private final Samples misses = new Samples();

}

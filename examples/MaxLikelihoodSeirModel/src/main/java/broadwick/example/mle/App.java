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

import broadwick.config.generated.Prior;
import broadwick.config.generated.UniformPrior;
import broadwick.io.FileOutput;
import broadwick.math.Matrix;
import broadwick.math.Vector;
import broadwick.model.Model;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.markovchain.MarkovStepGenerator;
import broadwick.odesolver.Ode;
import broadwick.odesolver.OdeController;
import broadwick.odesolver.OdeSolver;
import broadwick.odesolver.RungeKutta4;
import broadwick.rng.RNG;
import broadwick.statistics.distributions.TruncatedMultivariateNormalDistribution;
import broadwick.statistics.distributions.TruncatedNormalDistribution;
import com.google.common.collect.Iterables;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class App extends Model {

    @Override
    public final void init() {
        generator = new RNG(RNG.Generator.Well19937c);
        N = 10000;

        // The following values were obtained in a simulation of the SEIR model for 
        // beta=0.287, sigma=0.67, gamma=0.16, giving S=4583.446639, E+I=1067.932697, R=4348.620665
        observedS = 4583;
        observedInfected = 1068;
        observedR = 4349;
    }

    @Override
    public final void run() {
        log.info("Running project");

        runMCMC(new AdaptiveMetropolisStepGenerator(this.getPriors(), generator), this.getParameterValue("Adaptive_outputFile"));

        runMCMC(new SRWMStepGenerator(this.getPriors(), generator), this.getParameterValue("SRWM_outputFile"));

        runMCMC(new NSRWMStepGenerator(this.getPriors(), generator), this.getParameterValue("NSRWM_outputFile"));
    }

    private void runMCMC(MarkovStepGenerator chain, final String outputFileName) {
        try (FileOutput fo = new FileOutput(outputFileName)) {
            fo.write("# df <- read.csv(file=\"");
            fo.write(outputFileName);
            fo.write("\", blank.lines.skip=T, header=F, comment.char = \"#\")\n");
            fo.write("# plot(df$V1, df$V2, xlab=\"x\", ylab=\"y\", main=\"Markov Chain\", type=\"b\")\n");

            MonteCarloStep step = new MonteCarloStep(chain.getInitialStep());
            double[] results = runModel(step);
            double likelihood = calculateLikelihood(results);
            fo.write(String.format("1,%f,%s,%f,%f,%f\n", likelihood, step.toString(), results[0], results[1], results[2]));

            for (int i = 0; i < this.getParameterValueAsInteger("maxNumSamples"); i++) {
                final MonteCarloStep nextStep = chain.generateNextStep(step);
                results = runModel(nextStep);
                final double trialL = calculateLikelihood(results);

                if (accept(trialL, likelihood)) {
                    step = nextStep;
                    likelihood = trialL;
                    k++;
                    fo.write("1,");
                } else {
                    fo.write("0,");
                }

                log.info(String.format("%f,%s,%f,%f,%f", trialL, nextStep.toString(), results[0], results[1], results[2]));
                fo.write(String.format("%f,%s,%f,%f,%f\n", trialL, nextStep.toString(), results[0], results[1], results[2]));
                log.trace("{}", nextStep.toString());
            }
        }
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    private boolean accept(final double trialL, final double oldL) {
        // for log-likelihood (divide by 50 to smooth the likelihood surface)
        return Math.log(generator.getDouble()) < (trialL - oldL);

        // for likelihood function.
        //return trialL / oldL > generator.getDouble();
    }

    /**
     * Run the SEIR model for the step, calculate the likelihood and return it.
     * @param step the step in the Markov Chain.
     * @return the likelihood.
     */
    private double[] runModel(MonteCarloStep step) {
        // Implement the SEIR model, calculate

        final double maxT = this.getParameterValueAsDouble("endT");
        final Ode myOde = new SeirModel(N, step.getCoordinates().get("beta"),
                                        step.getCoordinates().get("sigma"),
                                        step.getCoordinates().get("gamma"));
        final OdeSolver solver = new RungeKutta4(myOde, 0.0, maxT, this.getParameterValueAsDouble("h"));

        // Create a simple controller object to tell the simulator when to stop.
        final OdeController controller = new OdeController() {

            @Override
            public boolean goOn(final OdeSolver solver) {
                return solver.getCurrentTime() < maxT;
            }
        };
        solver.setController(controller);

        solver.run();

        List<Double> results = solver.getDependentVariables();
        return new double[]{results.get(0), results.get(1) + results.get(2), results.get(3)};
    }

    public final double calculateLikelihood(double[] x) {

        final double[] p = new double[]{observedS / N, observedInfected / N, observedR / N};

        //return Math.pow(p[0], x[0]) + Math.pow(p[1], x[1]) + Math.pow(p[2], x[2]);
        // We want the log-likelihood here so we copy most of the code from MultinomialDistribution but  
        // neglecting the final Math.exp() call.
        double sumXFact = 0.0;
        double sumX = 0;
        double sumPX = 0.0;

        for (int i = 0; i < p.length; i++) {
            sumX += x[i];
            sumXFact += broadwick.math.Factorial.lnFactorial((int) Math.round(x[i]));
            if (p[i] > 1E-15) {
                // just in case probabilities[i] == 0.0
                sumPX += (x[i] * Math.log(p[i]));
            }
        }

        if (Math.abs(sumX - N) > 0.0001) {
            throw new IllegalArgumentException(String.format("Multinomial distribution error: Sum_x [%f] != number of samples [%d].", sumX, N));
        } else {
            final double logLikelihood = broadwick.math.Factorial.lnFactorial((int) N) - sumXFact + sumPX;
            log.debug("logLikelihood : {}", logLikelihood);
            return logLikelihood;
        }
    }

    @Getter
    public static int k = 0;
    private RNG generator;
    private int N;
    private double observedS;
    private double observedInfected;
    private double observedR;
}

/**
 * Class that generates the next step in the Markov chain using the SRWM method
 */
@Slf4j
class SRWMStepGenerator implements MarkovStepGenerator {

    public SRWMStepGenerator(final List<Prior> priors, final RNG rng) {
        this.generator = rng;

        final int n = priors.size();

        // initialise the mean and covariance matrix that we will update during the simulation.
        // We want the vector elements to be consistent so we iterate over the priors.
        this.lower = new Vector(n);
        this.upper = new Vector(n);

        final Map<String, Double> initialVals = new LinkedHashMap<>();
        int i = 0;
        for (Prior prior : priors) {
            UniformPrior uniformPrior = (UniformPrior) prior;

            if (uniformPrior.getInitialVal() > uniformPrior.getMax() || uniformPrior.getInitialVal() < uniformPrior.getMin()) {
                throw new IllegalArgumentException(String.format("Invalid prior [%s]. Initial value [%f] not in range [%f,%f]",
                                                                 uniformPrior.getId(), uniformPrior.getInitialVal(),
                                                                 uniformPrior.getMin(), uniformPrior.getMax()));
            }

            initialVals.put(uniformPrior.getId(), uniformPrior.getInitialVal());
            this.lower.setEntry(i, uniformPrior.getMin());
            this.upper.setEntry(i, uniformPrior.getMax());

            i++;
        }
        this.initialStep = new MonteCarloStep(initialVals);

        log.info("lower = {}", lower.toString());
        log.info("upper = {}", upper.toString());
    }

    @Override
    public MonteCarloStep generateNextStep(MonteCarloStep step) {
        final int n = step.getCoordinates().size();

        // Now package this proposal into a MonteCarloStep
        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        int i = 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.000");
        for (Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {

            TruncatedNormalDistribution tmvn = new TruncatedNormalDistribution(entry.getValue(), 1.0,
                                                                               lower.element(i), upper.element(i));
            // update the proposed step, rounding to 3 places
            proposedStep.put(entry.getKey(), Double.valueOf(df.format(tmvn.sample())));

            i++;
        }

        stepsTaken++;
        return new MonteCarloStep(proposedStep);
    }

    @Getter
    private final MonteCarloStep initialStep;
    private int stepsTaken = 0;
    private final Vector lower;
    private final Vector upper;
    private final RNG generator;
}

/**
 * Class that generates the next step in the Markov chain using the NSRWM method
 */
@Slf4j
class NSRWMStepGenerator implements MarkovStepGenerator {

    public NSRWMStepGenerator(final List<Prior> priors, final RNG rng) {
        this.generator = rng;

        final int n = priors.size();

        // initialise the mean and covariance matrix that we will update during the simulation.
        // We want the vector elements to be consistent so we iterate over the priors.
        this.means = new Vector(n);
        this.lower = new Vector(n);
        this.upper = new Vector(n);
        this.covariances = new Matrix(n, n);

        final Map<String, Double> initialVals = new LinkedHashMap<>();
        int i = 0;
        for (Prior prior : priors) {
            UniformPrior uniformPrior = (UniformPrior) prior;

            if (uniformPrior.getInitialVal() > uniformPrior.getMax() || uniformPrior.getInitialVal() < uniformPrior.getMin()) {
                throw new IllegalArgumentException(String.format("Invalid prior [%s]. Initial value [%f] not in range [%f,%f]",
                                                                 uniformPrior.getId(), uniformPrior.getInitialVal(),
                                                                 uniformPrior.getMin(), uniformPrior.getMax()));
            }

            initialVals.put(uniformPrior.getId(), uniformPrior.getInitialVal());
            this.lower.setEntry(i, uniformPrior.getMin());
            this.upper.setEntry(i, uniformPrior.getMax());
            this.covariances.setEntry(i, i, 1.0);

            i++;
        }
        this.initialStep = new MonteCarloStep(initialVals);

        log.info("covariance = {}", covariances.toString());
        log.info("lower = {}", lower.toString());
        log.info("upper = {}", upper.toString());
    }

    @Override
    public MonteCarloStep generateNextStep(MonteCarloStep step) {
        final int n = step.getCoordinates().size();
        means.setEntry(0, step.getCoordinates().get("beta"));
        means.setEntry(1, step.getCoordinates().get("sigma"));
        means.setEntry(2, step.getCoordinates().get("gamma"));

        TruncatedMultivariateNormalDistribution tmvn = new TruncatedMultivariateNormalDistribution(means, covariances,
                                                                                                   lower, upper);
        Vector proposal = tmvn.sample();

        // Now package this proposal into a MonteCarloStep
        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        int i = 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.000");
        for (Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {
            // update the proposed step, rounding to 3 places
            proposedStep.put(entry.getKey(), Double.valueOf(df.format(proposal.element(i++))));
        }

        stepsTaken++;
        return new MonteCarloStep(proposedStep);
    }

    @Getter
    private final MonteCarloStep initialStep;
    private int stepsTaken = 0;
    private final Vector means;
    private final Matrix covariances;
    private final Vector lower;
    private final Vector upper;
    private final RNG generator;
}

/**
 * Class that generates the next step in the Markov chain. This is where the Adaptive Metropolis algorithm is
 * implemented.
 */
@Slf4j
class AdaptiveMetropolisStepGenerator implements MarkovStepGenerator {

    public AdaptiveMetropolisStepGenerator(final List<Prior> priors, final RNG rng) {
        this.generator = rng;

        final int n = priors.size();

        // initialise the mean and covariance matrix that we will update during the simulation.
        // We want the vector elements to be consistent so we iterate over the priors.
        this.means = new Vector(n);
        this.lower = new Vector(n);
        this.upper = new Vector(n);
        this.covariances = new Matrix(n, n);

        final Map<String, Double> initialVals = new LinkedHashMap<>();
        int i = 0;
        for (Prior prior : priors) {
            UniformPrior uniformPrior = (UniformPrior) prior;

            if (uniformPrior.getInitialVal() > uniformPrior.getMax() || uniformPrior.getInitialVal() < uniformPrior.getMin()) {
                throw new IllegalArgumentException(String.format("Invalid prior [%s]. Initial value [%f] not in range [%f,%f]",
                                                                 uniformPrior.getId(), uniformPrior.getInitialVal(), uniformPrior.getMin(), uniformPrior.getMax()));
            }

            initialVals.put(uniformPrior.getId(), uniformPrior.getInitialVal());
            this.lower.setEntry(i, uniformPrior.getMin());
            this.upper.setEntry(i, uniformPrior.getMax());

            i++;
        }
        this.initialStep = new MonteCarloStep(initialVals);

        log.trace("{}", covariances.toString());
    }

    @Override
    public MonteCarloStep generateNextStep(MonteCarloStep step) {

        // We sample from a mulitvariate normal distribution here, 
        // the means for this distribution are the current values of the step: step.getValues()
        // The covariances are the updated at each step according to the adaptive Metropolis algorithm from 
        // http://dept.stat.lsa.umich.edu/~yvesa/afmp.pdf, continuously updating the covraiances in this way
        // means we don't have to store the whole chain.
        final int n = step.getCoordinates().size();
        final double scale = 2.85 / Math.sqrt(n);
        final double[] x = new double[n];

        // Now update the means and covariances. 
        // Doing it after the proposed step means that the means are ALWAYS updated (even for rejected steps) doing 
        // it here means that only the accepted steps (i.e. the members of the chain) are used but the means are
        // skewed by using the same values repeatedly
        for (int i = 0; i < n; i++) {
            x[i] = Iterables.get(step.getCoordinates().values(), i);
            means.setEntry(i, means.element(i) + (x[i] - means.element(i)) / (App.k + 1));
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double cov = scale* (x[i] - means.element(i)) * (x[j] - means.element(j));
                covariances.setEntry(i, j, (covariances.element(i, j)
                                            + (cov - covariances.element(i, j)) / (App.k + 1)));
                if (i == j) {
                    // to avoid potentially obtaining a singular matrix
                    covariances.setEntry(i, j, covariances.element(i, j) + 0.001);
                    // the variances that are used when sampling from the multivariate distribution are along the 
                    // diagonal and we want these relatively large to have good mixing.
                    //covariances[i][j] = Math.pow(sDev*means[i]/100.0,2); //1.0;
                }
            }
        }

        TruncatedMultivariateNormalDistribution tmvn = new TruncatedMultivariateNormalDistribution(means, covariances,
                                                                                                   lower, upper);
        Vector proposal = tmvn.sample();

        // Now package this proposal into a MonteCarloStep
        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        int i = 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.000");
        for (Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {
            // update the proposed step, rounding to 3 places
            proposedStep.put(entry.getKey(), Double.valueOf(df.format(proposal.element(i++))));
        }
        stepsTaken++;
        return new MonteCarloStep(proposedStep);
    }

    @Getter
    private final MonteCarloStep initialStep;
    private int stepsTaken = 0;
    private final Vector means;
    private final Matrix covariances;
    private final Vector lower;
    private final Vector upper;
    private final RNG generator;
}

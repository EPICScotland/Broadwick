package abc;

import broadwick.abc.AbcController;
import broadwick.abc.AbcModel;
import broadwick.abc.AbcNamedQuantity;
import broadwick.abc.AbcPriorsSampler;
import broadwick.abc.ApproxBayesianComp;
import broadwick.io.FileOutput;
import broadwick.model.Model;
import broadwick.rng.RNG;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple Markov Chain generator demonstrating the creation of a chain.
 */
@Slf4j
public class AbcExample extends Model {

    @Override
    public final void init() {

        // In this example we do not have any configuration parameters!!!

        log.debug("Initialising the variables in AbcExample");
        fo = new FileOutput("AbcExample.csv");
        fo.write("# df <- read.csv(file=\"AbcExample.csv\", blank.lines.skip=T, header=F, comment.char = \"#\")\n");
        fo.write("# x <- seq(2.5,4,length=length(df$V1))\n");
        fo.write("# y <- lapply(dnorm(x,mean=df$V1,sd=0.1), sum)\n");
        fo.write("# plot (x, y, ylim=c(0,6))\n");
        fo.write("# library(fitdistrplus)\n");
        fo.write("# fit <- mgedist(df$V1,\"norm\",gof=\"CvM\")\n");
        fo.write("# curve (dnorm(x,mean=fit$estimate[1],sd=fit$estimate[2]), add=T, col=\"red\")\n");

        epsilon = 0.1;
    }

    @Override
    public final void run() {

        // I am going to use the ABC method to sample from a posterier that is normally distributed around PI. I will
        // assume that the standard deviation is known (and is unity).
        // Using a uniform prior (for the mean of the posterior distribution) in [3,4] I will sample from this using
        // a simple abs() function as my distance function. 
        // I should get a set of points [normally] distributed around PI.

        // first set up the observed data (the mean value of my unknown distribution).
        final Map<String, Double> observed = new LinkedHashMap<>();
        observed.put("value", 3.142);
        final AbcNamedQuantity observedData = new AbcNamedQuantity(observed);

        // Next create a simple controller (we will sample 20000) points from the prior (the default controller 
        // samples 1000!
        final AbcController controller = new AbcController() {
            @Override
            public boolean goOn(final ApproxBayesianComp abc) {
                return abc.getNumSamplesTaken() <= 20000;
            }
        };

        // Create a dummy model to run
        final AbcModel myModel = new AbcModel() {
            @Override
            public AbcNamedQuantity run(final AbcNamedQuantity parameters) {
                // this is trivially simple model. Instead of doing any calculations we just return the parameters.
                return parameters;
            }
        };

        // Create a method for sampling our priors.
        final AbcPriorsSampler priors = new AbcPriorsSampler() {
            @Override
            public AbcNamedQuantity sample() {
                // Another dummy method here, we uniformly sample 'value' in the range [3,4]
                final LinkedHashMap<String, Double> sample = new LinkedHashMap<>();
                sample.put("value", generator.getDouble(3.0, 4.0));
                return new AbcNamedQuantity(sample);
            }
            private final RNG generator = new RNG(RNG.Generator.Well19937c);
        };

        final ApproxBayesianComp abc = new ApproxBayesianComp(observedData, myModel, priors, 0.05);
        abc.setController(controller);
        abc.run();

        final Map<String, LinkedList<Double>> posteriors = abc.getPosteriors();
        final LinkedList<Double> posteriorValues = posteriors.get("value");
        for (Double value : posteriorValues) {
            fo.write(String.format("%f\n", value));
        }

    }

    @Override
    public final void finalise() {
        fo.close();
    }
    private FileOutput fo;
    @SuppressWarnings("PMD.UnusedPrivateField")
    private double epsilon;
}

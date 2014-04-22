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
package broadwick.example.markovchain;

import broadwick.io.FileOutput;
import broadwick.model.Model;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.markovchain.MarkovChain;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple Markov Chain generator demonstrating the creation of a chain.
 */
@Slf4j
public class MarkovChainExample extends Model {

    @Override
    public final void init() {

        log.debug("Initialising the variables in MarkovChainExample");

        chainLength = this.getParameterValueAsInteger("chainLength");
        final String[] step = this.getParameterValue("initialStep").split(",");
        final Map<String, Double> coordinates = new LinkedHashMap<>();
        {
            coordinates.put("x", Double.parseDouble(step[0]));
            coordinates.put("y", Double.parseDouble(step[1]));
        }
        initialStep = new MonteCarloStep(coordinates);

        fo = new FileOutput(this.getParameterValue("outputFile"));
        fo.write("# df <- read.csv(file=\"MarkovChain.csv\", blank.lines.skip=T, header=F, comment.char = \"#\")\n");
        fo.write("# plot(df$V1, df$V2, xlab=\"x\", ylab=\"y\", main=\"Markov Chain\", type=\"b\")\n");

    }

    @Override
    public final void run() {

        final MonteCarloStep step = new MonteCarloStep(initialStep);
        fo.write(step.toString() + "\n");
        log.trace("{}", step.toString());

        final MarkovChain mc = new MarkovChain(step);
        // if we want to add a new proposal function we can add one simply like
        // final MarkovChain mc = new MarkovChain(step, new broadwick.markovchain.proposals.NormalProposal());
        for (int i = 0; i < chainLength; i++) {
            final MonteCarloStep nextStep = mc.generateNextStep(mc.getCurrentStep());
            mc.setCurrentStep(nextStep);

            fo.write(nextStep.toString() + "\n");
            log.trace("{}", nextStep.toString());
        }
    }

    @Override
    public final void finalise() {
        fo.close();
    }
    private int chainLength;
    private MonteCarloStep initialStep;
    private FileOutput fo;
}

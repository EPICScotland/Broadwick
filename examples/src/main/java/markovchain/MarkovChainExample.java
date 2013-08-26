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
package markovchain;

import broadwick.BroadwickException;
import broadwick.io.FileOutput;
import broadwick.montecarlo.path.MarkovChain;
import broadwick.montecarlo.path.Step;
import broadwick.model.Model;
import broadwick.xml.XmlParser;
import com.google.common.base.Throwables;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple Markov Chain generator demonstrating the creation of a chain.
 */
@Slf4j
public class MarkovChainExample extends Model {

    @Override
    public final void init() {

        log.debug("Initialising the variables in MarkovChainExample");
        try {
            final markovchain.config.generated.Model model = (markovchain.config.generated.Model) XmlParser.unmarshallXmlString(
                    this.getModel(),
                    markovchain.config.generated.Model.class);

            chainLength = model.getChainLength();
            initialStep = model.getInitialStep();
            fo = new FileOutput(model.getOutput());
            fo.write("# df <- read.csv(file=\"MarkovChain.csv\", blank.lines.skip=T, header=F, comment.char = \"#\")\n");
            fo.write("# plot(df$V1, df$V2, xlab=\"x\", ylab=\"y\", main=\"Markov Chain\", type=\"b\")\n");
        } catch (JAXBException ex) {
            log.error("Error initialising BtbIbmClusterDynamics. {}", ex.getLocalizedMessage());
            log.error(Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex.getLocalizedMessage());
        }
    }

    @Override
    public final void run() {

        final Map<String, Double> coordinates = new LinkedHashMap<>(2);
        coordinates.put("x", initialStep.getX());
        coordinates.put("y", initialStep.getY());
        final Step step = new Step(coordinates);
        fo.write(step.toString() + "\n");
        log.trace("{}", step.toString());

        final MarkovChain mc = new MarkovChain(step);
        // if we want to add a new proposal function we can add one simply like
        // final MarkovChain mc = new MarkovChain(step, new broadwick.markovchain.proposals.NormalProposal());
        for (int i = 0; i < chainLength; i++) {
            final Step nextStep = mc.generateNextStep(mc.getCurrentStep());
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
    private markovchain.config.generated.Step initialStep;
    private FileOutput fo;
}

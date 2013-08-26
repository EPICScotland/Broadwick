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

package stochasticSIR;

import broadwick.model.Model;
import lombok.extern.slf4j.Slf4j;

/**
 * Example SIR model supplied with the Broadwick framework using a stochastic solver.
 */
@Slf4j
public class StochasticSIR extends Model {

    @Override
    public void init() {
    }

    @Override
    public void run() {
        // Simple example of how to use/obtain paremters and priors from the configuration file.
        // Member that are marked as priors and paramters are read from the config file, if the configuration
        // file does not specify values, default ones that are apecified in the annotation are used.
//        final Parameters.Parameter betaParameter = getParameters().get("beta");
//        if (betaParameter != null) {
//            log.info("{} - {}", betaParameter.getName(), betaParameter.getValue());
//        }
//
//        final Parameters.Parameter rhoParameter = getParameters().get("rho");
//        if (rhoParameter != null) {
//            log.info("{} - {}", rhoParameter.getName(), rhoParameter.getValue());
//        }
//
//        final Priors.Prior rhoPrior = getPriors().get("rho");
//        if (rhoPrior != null) {
//            final StringBuilder sb = new StringBuilder();
//            sb.append("[").append(rhoPrior.getMin()).append(",").append(rhoPrior.getMax()).append("]");
//            sb.append(" - ").append(rhoPrior.getDistribution());
//            log.info("Prior {} {}", rhoPrior.getName(), sb.toString());
//        }
    }

    @Override
    public void finalise() {
    }
    private double beta;
    private double rho;
}

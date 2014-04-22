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

package broadwick.example.stochasticsir;

import broadwick.model.Model;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple example on how to use a the stochastic solver of Broadwick to solve the SIR model.
 */
@Slf4j
public class StochasticSIR extends Model {

    @Override
    public final void init() {
	log.info("Initialising project");
        
        outputFileName = getParameterValue("outputFile");
        betaVal = getParameterValueAsDouble("beta");
        rhoVal = getParameterValueAsDouble("rho");
        tMax = getParameterValueAsDouble("tMax");
        
        log.info("Saving data to {}", outputFileName);
        log.info("beta = {}, rho = {}", betaVal, rhoVal);
        log.info("Max t = {}", tMax);
    }

    @Override
    public final void run() {
	log.info("Running project");
    }

    @Override
    public final void finalise() {
	log.info("Closing project");
    }
    
    private String outputFileName;
    private double betaVal;
    private double rhoVal;
    private double tMax;

}


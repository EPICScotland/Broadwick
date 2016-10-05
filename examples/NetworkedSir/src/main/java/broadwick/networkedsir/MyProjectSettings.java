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
package broadwick.networkedsir;

import lombok.Getter;

/**
 *
 * @author anthony
 */
public class MyProjectSettings {

    public MyProjectSettings() {

    }

    /**
     * Set the maximum time to run the simulation.
     * @param tMax the maximum time to run the simulation.
     * @return this object.
     */
    public final MyProjectSettings setTMax(final double tMax) {
        this.tMax = tMax;
        return this;
    }
    
    /**
     * Set the number of observed infections.
     * @param num the number of observations.
     * @return this object.
     */
    public final MyProjectSettings setObservedNumInfected(final int num) {
        this.observedNumInfected = num;
        return this;
    }
    
    /**
     * Set the number of observed infections.
     * @param num the number of observations.
     * @return this object.
     */
    public final MyProjectSettings setObservedNumRemoved(final int num) {
        this.observedNumRemoved = num;
        return this;
    }

    @Getter
    private double tMax;
    @Getter
    private int observedNumInfected;
    @Getter
    private int observedNumRemoved;
    @Getter
    private final int meanNumIndividualsAtLocation = 60;
    @Getter
    private final int stdDevNumIndividualsAtLocation = 20;
    @Getter
    private final int meanNumIndividualsMoved = 6;
    @Getter
    private final int stdDevNumIndividualsMoved = 4;
}

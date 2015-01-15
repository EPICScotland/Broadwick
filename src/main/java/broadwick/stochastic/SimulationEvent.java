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
package broadwick.stochastic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A class containing all the details of an event in the stochastic ODE model.
 */
@EqualsAndHashCode
public class SimulationEvent {

    /**
     * Construct a new event from the initial state in one bin to a final state in another bin.
     * @param initialState the name and index of the bin of the initial state.
     * @param finalState the name and index of the bin of the final state.
     */
    public SimulationEvent(final SimulationState initialState, final SimulationState finalState) {
        this.initialState = initialState;
        this.finalState = finalState;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(50);
        sb.append(initialState.toString());
        sb.append(" -->  ");
        sb.append(finalState.toString());

        return sb.toString();
    }    

    @Getter
    @Setter
    private SimulationState initialState;
    @Getter
    @Setter
    private SimulationState finalState;
}

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

import broadwick.graph.Vertex;
import broadwick.stochastic.SimulationState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A class to encapsulate the properties of an infected badger.
 */
@Slf4j
public class Agent extends Vertex implements SimulationState  {

    /**
     * Create an agent in a given infection state.
     * @param id          the id of the infected agent.
     * @param location    th location of the infected agent.
     * @param state       the infection state of the agent.
     */
    public Agent(final String id, final String location, final SimulationState state) {
        super(id);
        this.id = id;
        this.location = location;
        this.state = state;
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        return ((Agent) obj).getId().equals(id);
    }

    @Override
    public final String getStateName() {
        return this.toString();
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(10);
            sb.append(id).append("(").append(state.getStateName()).append(")-").append(location);
        return sb.toString();
    }
    
    /**
     * Obtain a copy of (i.e. a clone) of this object
     * @return a deep copy of this object.
     */
    public final Agent copyOf() {
        return new Agent(id, location, state);
    }

    @Getter
    private final String id;
    @Getter
    @Setter
    private String location;
    @Getter
    @Setter
    private SimulationState state;
}

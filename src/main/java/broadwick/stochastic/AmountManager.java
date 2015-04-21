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

/**
 * An amount manager is responsible for keeping track of the populations within the simulation. Each
 * <code>StochasticProcess</code> calls the
 * <code>performEvent</code> method when it fires an event. The amount manager then
 */
public interface AmountManager {

    /**
     * Reflects a (multiple) firing of a reaction by adjusting the populations of the states. If a population becomes
     * negative, a <code> RuntimeException</code> is thrown.
     * @param reaction    the index of the reaction fired
     * @param times    the number of firings
     */
    void performEvent(SimulationEvent reaction, int times);

    /**
     * Get a detailed description of the states and their sizes (potentially for debugging).
     * @return a detailed description of the states in the manager.
     */
    String toVerboseString();

    /**
     * Resets the amount of each species to the initial amount retrieved by the networks {@link AnnotationManager}. This
     * is called whenever a {@link Simulator} is started.
     */
    void resetAmount();

    /**
     * Makes a copy of the amount array.
     */
    void save();

    /**
     * Restore the amount array from the recently saved one.
     */
    void rollback();
}

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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that holds the transition kernel for a stochastic simulation. The kernel comprises of a map of SimulationEvents
 * to the probability of that event occurring. This is NOT thread safe.
 */
@ToString
@Slf4j
public class TransitionKernel implements Cloneable, Serializable {

    /**
     * Create a transition kernel object to hold the transition probabilities between vents.
     */
    public TransitionKernel() {
    }

    /**
     * Copy this kernel object.
     * @return a copy of this object.
     * @throws CloneNotSupportedException if cloneable is not implemented.
     * @see{@code Object.clone()}
     */
    @Override
    public final Object clone() throws CloneNotSupportedException {
        super.clone();
        final TransitionKernel newKernel = new TransitionKernel();
        for (final Map.Entry<SimulationEvent, Double> entry : kernel.entrySet()) {
            newKernel.addToKernel(entry.getKey(), entry.getValue());
        }
        return newKernel;
    }

    /**
     * Add the transition rate for a given event.
     * @param event the event to be added to the kernel
     * @param rate  the rate at which this event occurs.
     */
    public final void addToKernel(final SimulationEvent event, final Double rate) {
        if (rate > 0.0) {
            double eventRate = rate;
            if (kernel.containsKey(event)) {
                eventRate += kernel.get(event);
                if (log.isTraceEnabled()) {
                    log.trace("Kernel already contains event {}, updating rate by {}", event, rate);
                }
            } else if (log.isTraceEnabled()) {
                log.trace("Adding event to kernel {} {}", event, eventRate);
            }
            kernel.put(event, eventRate);
        }
    }

    /**
     * Clear all the events from the kernel.
     */
    public final void clear() {
        kernel.clear();
    }

    /**
     * Get the collection of events stored in the transition kernel. This actually returns the keyset from the
     * transition rates since they are always present and will always be in the kernel.
     * @return the events in the kernel.
     */
    public final Set<SimulationEvent> getTransitionEvents() {
        return kernel.keySet();
    }

    /**
     * Get the probability of an event occurring.
     * @param event the event
     * @return the probability of the event occurring.
     */
    public final Double getTransitionProbability(final SimulationEvent event) {
        return kernel.get(event);
    }

    /**
     * Set the transition probabilities between states.
     * @param probabilities the new set of probabilities.
     */
    public final void setTransitionProbabilities(final Map<SimulationEvent, Double> probabilities) {
        kernel.clear();
        kernel.putAll(probabilities);
    }

    /**
     * Get the cumulativeDistFn for this transition kernel.
     * @return the cumulativeDistFn;
     */
    public final Map<SimulationEvent, Double> getCDF() {

        double cumulativeProb = 0.0;
        final Map<SimulationEvent, Double> cumulativeDistFn = new LinkedHashMap<>(5);
        for (final SimulationEvent e : getTransitionEvents()) {
            cumulativeProb += getTransitionProbability(e);
            cumulativeDistFn.put(e, cumulativeProb);
        }

        return cumulativeDistFn;
    }

    // A collection of probabilities of progressing from one state to another.
    private Map<SimulationEvent, Double> kernel = new LinkedHashMap<>(5);
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -953479930339424502L;
}

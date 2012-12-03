package broadwick.stochastic;

import lombok.Getter;
import lombok.Setter;

/**
 * A class containing all the details of an event in the stochastic ODE model.
 */
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

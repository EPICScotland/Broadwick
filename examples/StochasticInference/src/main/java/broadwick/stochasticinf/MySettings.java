package broadwick.stochasticinf;

import lombok.Getter;

/**
 * This class contains all the (constant) project settings such as simulation start and end dates etc. These will never
 * change and are available to every scenario, thus calls is NOT thread safe as it should only be read once created.
 */
public class MySettings {

    /**
     * Create the settings. This class acts like a cheap singleton (without the checks etc.) for storing the projects settings.
     * @param initialSusceptibles the initial number of individuals in the susceptible class
     * @param initialExposed1 the initial number of individuals in the exposed 1 class
     * @param initialExposed2 the initial number of individuals in the exposed 2 class
     * @param initialInfectious the initial number of individuals in the infectious class
     * @param numobservedExposed the number of individuals observed in the exposed (2) class.
     * @param numObservedInfectious the number of individuals observed in the infectious class.
     * @param maxT 
     */
    public MySettings(final int initialSusceptibles, final int initialExposed1,
                      final int initialExposed2,
                      final int initialInfectious,
                      final int numobservedExposed,
                      final int numObservedInfectious,
                      final double maxT) {

        this.initialSusceptibles = initialSusceptibles;
        this.initialExposed1 = initialExposed1;
        this.initialExposed2 = initialExposed2;
        this.initialInfectious = initialInfectious;
        this.numObservedExposed = numobservedExposed;
        this.numObservedInfectious = numObservedInfectious;
        this.maxT = maxT;
        this.populationSize = initialSusceptibles + initialExposed1 + initialExposed2 + initialInfectious;
    }

    @Getter
    private final int initialSusceptibles;
    @Getter
    private final int initialExposed1;
    @Getter
    private final int initialExposed2;
    @Getter
    private final int initialInfectious;
    @Getter
    private final int numObservedExposed;
    @Getter
    private final int numObservedInfectious;
    @Getter
    private final int populationSize;
    @Getter
    private final double maxT;

}

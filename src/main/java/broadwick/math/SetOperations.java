package broadwick.math;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Some basic set operations on the Java Set class.
 */
public final class SetOperations {

    /**
     * Hidden utility constructor.
     */
    private SetOperations() {
    }

    /**
     * Get the union of two sets.
     * @param <T> The genetic type of the sets used.
     * @param setA one of two sets for the union
     * @param setB second of two sets for the union
     * @return setA union setB
     */
    public static <T> Set<T> union(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmp = new TreeSet<>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    /**
     * Get the intersection of two sets.
     * @param <T> The generic type of the sets used.
     * @param setA one of two sets for the intersection
     * @param setB second of two sets for the intersection
     * @return setA intersection setB
     */
    public static <T> Set<T> intersection(final Collection<T> setA, final Collection<T> setB) {
        final Set<T> tmp = new TreeSet<>();
        for (T x : setA) {
            if (setB.contains(x)) {
                tmp.add(x);
            }
        }
        return tmp;
    }

    /**
     * Get the difference of two sets.
     * @param <T> The genetic type of the sets used.
     * @param setA one of two sets for the difference
     * @param setB second of two sets for the difference
     * @return setA minus setB
     */
    public static <T> Set<T> difference(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmp = new TreeSet<>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    /**
     * Get the elements of sets A,B that are not in their intersection.
     * @param <T> The generic type of the sets used.
     * @param setA one of two sets for the intersection
     * @param setB second of two sets for the intersection
     * @return (setA union setB) minus (setA union setB)
     */
    public static <T> Set<T> symDifference(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmpA;
        final Set<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    /**
     * Is setA a subset of setB.
     * @param <T> The generic type of the sets used.
     * @param setA the (potential) subset.
     * @param setB the original (superset).
     * @return true if setA is a subset of setB.
     */
    public static <T> boolean isSubset(final Set<T> setA, final Set<T> setB) {
        return setB.containsAll(setA);
    }

    /**
     * Is setA a superset of setB.
     * @param <T> The generic type of the sets used.
     * @param setA the (potential) superset.
     * @param setB setB the original set.
     * @return true if setA is a superset of setB
     */
    public static <T> boolean isSuperset(final Set<T> setA, final Set<T> setB) {
        return setA.containsAll(setB);
    }
}

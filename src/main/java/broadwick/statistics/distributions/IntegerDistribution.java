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
package broadwick.statistics.distributions;

import broadwick.rng.RNG;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Synchronized;

/**
 * A simple class for defining custom distributions. This is, in effect, a histogram.
 */
public class IntegerDistribution implements Serializable {

    /**
     * Create an empty distribution.
     */
    public IntegerDistribution() {
        bins = new ConcurrentSkipListMap<>();
        this.init(0);
    }

    /**
     * Create the distribution with a specified number of bins.
     * @param nbins the number of bins
     */
    public IntegerDistribution(final int nbins) {
        bins = new ConcurrentSkipListMap<>();
        this.init(nbins);
    }

    /**
     * Add a number of entries (keys) to the histogram (with zero value).
     * @param nbins the number of bins to add.
     */
    @Synchronized
    private void init(final int nbins) {
        for (int i = 1; i <= nbins; i++) {
            bins.put(i, 0);
        }
    }

    /**
     * Clear all the data from the distribution, after this method the distribution has no denominator or frequency.
     */
    @Synchronized
    public final void clear() {
        bins.clear();
    }

    /**
     * Resets the value in each bin.
     * @param val the reset value.
     */
    @Synchronized
    public final void reset(final int val) {
        for (final int i : getBins()) {
            bins.put(i, val);
        }
    }

    /**
     * Resets the value in each bin.
     */
    @Synchronized
    public final void reset() {
        for (final int i : getBins()) {
            bins.put(i, 0);
        }
    }

    /**
     * Adds the content of the argument to the current object.
     * @param hist the histogram data to add.
     */
    @Synchronized
    public final void add(final IntegerDistribution hist) {
        for (final Integer i : hist.getBins()) {
            if (bins.containsKey(i)) {
                bins.put(i, this.getFrequency(i) + hist.getFrequency(i));
            } else {
                bins.put(i, hist.getFrequency(i));
            }
        }
    }

    /**
     * Get the number of bins.
     * @return the number of bins.
     */
    @Synchronized
    public final int getNumBins() {
        return bins.size();
    }

    /**
     * Increment the size of a bin.
     * @param bin the bin to increment.
     */
    @Synchronized
    public final void setFrequency(final Integer bin) {
        Integer value = 1;
        if (bins.get(bin) != null) {
            value = bins.get(bin) + 1;
        }
        bins.put(bin, value);
    }

    /**
     * Set the value of a bin.
     * @param bin  the bin.
     * @param data the data.
     */
    @Synchronized
    public final void setFrequency(final Integer bin, final Integer data) {
        bins.put(bin, data);
    }

    /**
     * Get the size of the histogram at the given bin.
     * @param bin the bin.
     * @return the size of the bin, or null if the bin is not in the histogram.
     */
    @Synchronized
    public final Integer getFrequency(final Integer bin) {
        return bins.get(bin);
    }

    /**
     * Increment the size of a bin.
     * @param bin the bin to increment.
     * @deprecated use getFrequency() instead
     */
    @Synchronized
    public final void setData(final Integer bin) {
        this.setFrequency(bin);
    }

    /**
     * Set the value of a bin.
     * @param bin  the bin.
     * @param data the data.
     * @deprecated use setFrequency() instead
     */
    @Synchronized
    public final void setData(final Integer bin, final Integer data) {
        this.setFrequency(bin, data);
    }

    /**
     * Get the size of the histogram at the given bin.
     * @param bin the bin.
     * @deprecated use getFrequency() instead
     * @return the size of the bin, or null if the bin is not in the histogram.
     */
    @Synchronized
    public final Integer getData(final Integer bin) {
        return getFrequency(bin);
    }

    /**
     * Select a random bin from the cumulative distribution of the histograms contents.
     * @return the index of the random bin, or zero if no bin could be selected.
     */
    @Synchronized
    public final Integer getRandomBin() {
        final int cumulativeSum = getSumCounts();
        if (cumulativeSum == 0) {
            throw new IllegalArgumentException("Cannot select random bin: there are no bins!");
        }
        final int randomBin = GENERATOR.getInteger(0, cumulativeSum);

        final Integer[] arr = bins.keySet().toArray(new Integer[bins.size()]);
        int index = 0;
        int sum = 0;
        for (final int value : bins.values()) {
            sum += value;
            if (sum >= randomBin) {
                return arr[index];
            }
            index++;
        }
        return arr[index - 1];
    }

    /**
     * Select a random bin from the cumulative distribution of the frequencies and return the frequency.
     * @return the contents of the randomly selected bin.
     */
    @Synchronized
    public final Integer getRandomBinFrequency() {
        final int cumulativeSum = getSumCounts();
        final int randomBin = GENERATOR.getInteger(0, cumulativeSum);

        int sum = 0;
        for (final int value : bins.values()) {
            sum += value;
            if (sum >= randomBin) {
                return value;
            }
        }

        // return the contents of the last bin.
        return bins.keySet().toArray(new Integer[bins.size()])[bins.size() - 1];
    }

    /**
     * Creates a copy of this histogram and returns it. The returned IntegerDistribution is a completely different
     * object.
     * @return a copy of this histogram.
     */
    @Synchronized
    public final IntegerDistribution copy() {
        final IntegerDistribution clone = new IntegerDistribution(bins.size());

        for (final Integer bin : this.getBins()) {
            clone.setFrequency(bin, bins.get(bin));
        }

        return clone;
    }

    /**
     * Get the sum of the counts in the histogram.
     * @return the sum of all the bins in the histogram.
     */
    @Synchronized
    public final Integer getSumCounts() {
        int sum = 0;
        for (final int value : bins.values()) {
            sum += value;
        }
        return sum;
    }

    /**
     * Get the sum of the counts in the histogram.
     * @return the sum of all the bins in the histogram.
     */
    @Synchronized
    public final Integer getTotal() {
        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : bins.entrySet()) {
            sum += entry.getKey() * entry.getValue();
        }
        return sum;
    }

    /**
     * Get the bins in the histogram. The bins are the keySet of the underlying map object.
     * @return the number of bins.
     */
    @Synchronized
    public final Collection<Integer> getBins() {
        return bins.keySet();
    }

    /**
     * Get a collection of the values held in the histogram. The bins are the keySet of the underlying map object, the
     * values are the contents if the bin.
     * @return a collection of the values held in the histogram.
     */
    @Synchronized
    public final Collection<Integer> getBinContents() {
        return bins.values();
    }

    /**
     * Get the size (the number of bins) in the histogram.
     * @return the size
     */
    @Synchronized
    public final int size() {
        return bins.size();
    }

    /**
     * Get an array of the bin values.
     * @return an array of int values.
     */
    @Synchronized
    public final synchronized int[] toArray() {
        int[] arr = new int[size()];

        int i = 0;
        final Iterator<Integer> it = bins.values().iterator();
        while (it.hasNext()) {
            arr[i] = it.next();
            i++;
        }

        return arr;
    }

    /**
     * Get an array of the bin values.
     * @return an array of long values.
     */
    @Synchronized
    public final synchronized long[] toLongArray() {
        long[] arr = new long[size()];

        int i = 0;
        final Iterator<Integer> it = bins.values().iterator();
        while (it.hasNext()) {
            arr[i] = (long) it.next();
            i++;
        }

        return arr;
    }

    /**
     * Scale the values in the histogram by a given factor. A copy of the histogram is returned, the original is not
     * modified.
     * @param factor the value by which every value in the hiistogram will be multiplied.
     * @return the scaled histogram.
     */
    public final IntegerDistribution scaleBins(final double factor) {
        final IntegerDistribution data = this.copy();

        for (final Integer bin : data.getBins()) {
            final double d = data.getFrequency(bin) * factor;
            data.setFrequency(bin, (int) d);
        }

        return data;
    }

    /**
     * Normalise the frequency distribution. The returned histogram is a scaled copy of the input histogram so that the
     * sum of the returned histogram is equal to the normalising factor.
     * @param constant the normalising constant
     * @return the scaled and normalised histogram.
     */
    public final IntegerDistribution normaliseBins(final int constant) {
        final IntegerDistribution data = this.copy();
        final IntegerDistribution normalisedBins = new IntegerDistribution(this.getNumBins());
        final Map<Integer, Double> fractions = new HashMap<>(this.getNumBins());
        final double factor = constant / Double.valueOf(this.getSumCounts());

        for (final Integer bin : data.getBins()) {
            final double d = data.getFrequency(bin) * factor;
            final int size = (int) Math.floor(d);
            fractions.put(bin, d - size);
            normalisedBins.setFrequency(bin, size);
        }

        // Now normalisedBins.getSumCounts() < data.getSumCounts()/runs so we need to increment the
        // bins with the (data.getSumCounts()/runs - normalisedBins.getSumCounts()) largest fractions.
        int diff = constant - normalisedBins.getSumCounts();
        while (diff > 0) {
            int binMax = -1;
            double max = 0.0;

            for (final Entry<Integer, Double> entry : fractions.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                    binMax = entry.getKey();
                }
            }
            if (binMax > 0) {
                normalisedBins.setFrequency(binMax);
                fractions.put(binMax, 0.0);
            }
            diff--;
        }

        return normalisedBins;
    }

    /**
     * Get a string representation of the histogram.
     * @return the string.
     */
    @Override
    @Synchronized
    public final String toString() {
        final StringBuilder str = new StringBuilder(10);
        for (final Map.Entry<Integer, Integer> entry : bins.entrySet()) {
            str.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        return str.toString();
    }

    /**
     * Reseed the random number generator used.
     * @param seed the new seed to use.
     */
    public final void reseed(final int seed) {
        GENERATOR.seed(seed);
    }

    /**
     * Get a string representation of the histogram in a csv format.
     * @return the string.
     */
    @Synchronized
    public final String toCsv() {
        final StringBuilder str = new StringBuilder(10);
        for (final Map.Entry<Integer, Integer> entry : bins.entrySet()) {
            str.append(entry.getValue()).append(",");
        }
        if (str.length() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }
    private ConcurrentMap<Integer, Integer> bins;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
    private static final long serialVersionUID = -8753849301390754581L;
}

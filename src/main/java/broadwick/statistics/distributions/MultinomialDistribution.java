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

import broadwick.math.Factorial;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines the Multinomial distribution with parameters <I>numSamples</I> and <I>probabilities</I><SUB>1</SUB>,
 * ...,<I>probabilities</I><SUB>d</SUB>. The probability mass function is
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>P</I>[<I>X</I> = (<I>x</I><SUB>1</SUB>,..., <I>x</I><SUB>d</SUB>)] =
 * <I>numSamples</I>!&prod;<SUB>i=1</SUB><SUP>d</SUP><I>probabilities</I><SUB>i</SUB><SUP>x<SUB>i</SUB></SUP>/(&prod;<SUB>i=1</SUB><SUP>d</SUP><I>x</I><SUB>i</SUB>!),
 * </DIV><P></P>
 * where
 * <SPAN CLASS="MATH">&sum;<SUB>i=1</SUB><SUP>d</SUP><I>x</I><SUB>i</SUB> = <I>numSamples</I></SPAN> and
 * <SPAN CLASS="MATH">&sum;<SUB>i=1</SUB><SUP>d</SUP><I>probabilities</I><SUB>i</SUB> = 1</SPAN>.
 * <p/>
 */
@Slf4j
public class MultinomialDistribution {

    /**
     * Creates a <TT>MultinomialDist</TT> object with parameters <SPAN CLASS="MATH"><I>numSamples</I></SPAN> and (<SPAN
     * CLASS="MATH"><I>probabilities</I><SUB>1</SUB></SPAN>,...,<SPAN
     * CLASS="MATH"><I>probabilities</I><SUB>d</SUB></SPAN>) such that
     * <SPAN CLASS="MATH">&sum;<SUB>i=1</SUB><SUP>d</SUP><I>probabilities</I><SUB>i</SUB> = 1</SPAN>. We have
     * <SPAN CLASS="MATH"><I>probabilities</I><SUB>i</SUB> =</SPAN> <TT>probabilities[i-1]</TT>.
     * @param n the number of samples.
     * @param p the array of probabilities
     */
    public MultinomialDistribution(final int n, final double p[]) {
        double sumP = 0.0;

        if (n <= 0) {
            throw new IllegalArgumentException("n <= 0");
        }
        if (p.length < 2) {
            throw new IllegalArgumentException("p.length < 2");
        }

        this.numSamples = n;
        this.dimension = p.length;
        this.probabilities = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            if ((p[i] < 0) || (p[i] > 1)) {
                throw new IllegalArgumentException("p is not a probability vector");
            }

            this.probabilities[i] = p[i];
            sumP += p[i];
        }

        if (Math.abs(sumP - 1.0) > 1E-15) {
            throw new IllegalArgumentException("p is not a probability vector");
        }
    }

    /**
     * Returns the parameter <I>N</I> of this object.
     * @return the total number of samples.
     */
    public final int getN() {
        return numSamples;
    }

    /**
     * Returns the parameters (<I>probabilities</I><SUB>1</SUB>,...,<I>probabilities</I><SUB>d</SUB>) of this object.
     * @return the array of probabilities.
     */
    public final double[] getP() {
        return probabilities.clone();
    }

    /**
     * Returns the probability mass function
     * <SPAN CLASS="MATH"><I>p</I>(<I>x</I><SUB>1</SUB>, <I>x</I><SUB>2</SUB>,&#8230;, <I>x</I><SUB>d</SUB>)</SPAN>,
     * which should be a real number in <SPAN CLASS="MATH">[0, 1]</SPAN>.
     * @param x value at which the mass function must be evaluated
     * @return the mass function evaluated at <TT>x</TT>
     */
    public final double prob(final int x[]) {
        double sumXFact = 0.0;
        int sumX = 0;
        double sumPX = 0.0;

        if (x.length != probabilities.length) {
            throw new IllegalArgumentException("x and p must have the same dimension");
        }

        for (int i = 0; i < probabilities.length; i++) {
            sumX += x[i];
            sumXFact += Factorial.lnFactorial(x[i]);
            if (probabilities[i] > 1E-15) {
                // just in case probabilities[i] == 0.0
                sumPX += (x[i] * Math.log(probabilities[i]));
            }
        }

        if (sumX != numSamples) {
            log.error("Multinomial distribution error: Sum_x [{}] != number of samples [{}].", sumX, numSamples);
            return 0.0;
        } else {
            return Math.exp(Factorial.lnFactorial(numSamples) - sumXFact + sumPX);
        }
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("p(x) = ");
        for (int i = 0; i < probabilities.length; i++) {
            sb.append(probabilities[i]).append(" ");
        }
        return sb.toString();
    }
    private int numSamples;
    private double[] probabilities;
    private int dimension;
}

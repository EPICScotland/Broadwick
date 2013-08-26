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
package broadwick.stat.regression;

import org.apache.commons.math.stat.regression.SimpleRegression;

/**
 * Estimates an ordinary least squares regression model with one independent variable. y = intercept + slope * x
 */
public class SimpleLinearRegression {

    /**
     * Create an empty SimpleLinearRegression instance.
     */
    public SimpleLinearRegression() {
        regression = new SimpleRegression();
    }

    /**
     * Adds the observation (x,y) to the regression data set. Uses updating formulas for means and sums of squares
     * defined in "Algorithms for Computing the Sample Variance: Analysis and Recommendations", Chan, T.F., Golub, G.H.,
     * and LeVeque, R.J. 1983, American Statistician, vol. 37, pp. 242-247, referenced in Weisberg, S. "Applied Linear
     * Regression". 2nd Ed. 1985. Note: this uses the apache commons math library.
     * @param x independent variable value
     * @param y dependent variable value
     */
    public final void addData(final double x, final double y) {
        regression.addData(x, y);
    }
    
    
    /**
     * Removes the observation (x,y) from the regression data set, mirroring the addData method. 
     * The method has no effect if there are no points of data (i.e. n=0)
     * @param x independent variable value
     * @param y dependent variable value
     */
    public final void removeData(final double x, final double y) {
        regression.removeData(x, y);
    }

    /**
     * Returns the slope of the estimated regression line. At least two observations (with at least two different x
     * values) must have been added before invoking this method. If this method is invoked before a model can be
     * estimated,
     * <code>Double,NaN</code> is returned.
     * @return the slope of the regression line
     */
    public final double getSlope() {
        return regression.getSlope();
    }

    /**
     * Returns the intercept of the estimated regression line. At least two observations (with at least two different x
     * values) must have been added before invoking this method. If this method is invoked before a model can be
     * estimated,
     * <code>Double,NaN</code> is returned.
     * @return the intercept of the regression line
     */
    public final double getIntercept() {
        return regression.getIntercept();
    }

    /**
     * Returns the number of observations that have been added to the model.
     * @return n number of observations that have been added.
     */
    public final long getN() {
        return regression.getN();
    }

    /**
     * Returns the "predicted"
     * <code>y</code> value associated with the supplied
     * <code>x</code> value, based on the data that has been added to the model when this method is activated.
     * <p>
     * <code> predict(x) = intercept + slope * x </code>
     * </p>
     * At least two observations (with at least two different x values) must have been added before invoking this
     * method. If this method is invoked before a model can be estimated,
     * <code>Double,NaN</code> is returned.
     * @param x input <code>x</code> value
     * @return predicted <code>y</code> value
     */
    public final double predict(final double x) {
        return regression.predict(x);
    }

    /**
     * Returns <a href="http://mathworld.wolfram.com/CorrelationCoefficient.html"> Pearson's product moment correlation
     * coefficient</a>, usually denoted r. At least two observations (with at least two different x values) must have
     * been added before invoking this method. If this method is invoked before a model can be estimated,
     * <code>Double,NaN</code> is returned.
     * <p/>
     * @return Pearson's r
     */
    public final double getR() {
        return regression.getR();
    }

    /**
     * Returns the <a href="http://www.xycoon.com/standarderrorb0.htm">
     * standard error of the intercept estimate</a>, usually denoted s(b0). If there are fewer that
     * <strong>three</strong> observations in the model, or if there is no variation in x, this returns
     * <code>Double.NaN</code>.
     * @return standard error associated with intercept estimate
     */
    public final double getInterceptStdErr() {
        return regression.getInterceptStdErr();
    }

    /**
     * Returns the <a href="http://www.xycoon.com/standerrorb(1).htm">standard error of the slope estimate</a>. If there
     * are fewer that <strong>three</strong> data pairs in the model, or if there is no variation in x, this returns
     * <code>Double.NaN</code>.
     * @return standard error associated with slope estimate
     */
    public final double getSlopeStdErr() {
        return regression.getSlopeStdErr();
    }
    
    @Override
    public final String toString() {
        return String.format("Slope [%f], Intercept [%f]. N=%d", getSlope(), getIntercept(), getN());
    }
    private SimpleRegression regression;
}

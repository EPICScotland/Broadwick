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
package broadwick.odesolver;

/**
 * Interface defining a First Order Ordinary Differential Equation.
 */
public interface Ode {

    /**
     * Get the current time derivative of the state vector.
     * @param t current value of the independent <I>time</I> variable.
     * @param y array containing the current value of the state vector.
     * @return the derivative of the dependent variables.
     */
    Double[] computeDerivatives(double t, Double[] y);

    /**
     * Get the initial values of each dependent variable.
     * @return double[] the (list of) initial values of each dependent variable
     */
    Double[] getInitialValues();

}

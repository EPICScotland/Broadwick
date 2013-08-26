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
package broadwick.math;

import java.math.BigDecimal;

/**
 *   A recursive program to calculate the factorial of a number.
 *   n! = n * (n-1) * (n-2) * ... * 1
 */
public final class Factorial {

    /**
     * Hiding constructor for utility class.
     */
    private Factorial() {
    }

    /**
     * Calculate the factorial of n.
     * @param n the number to calculate the factorial of.
     * @return the factorial of n.
     */
    public static BigDecimal factorial(final int n) {
        if (n <= 1) {
            return BigDecimal.ONE;
        } else {
            return factorial(n - 1).multiply(BigDecimal.valueOf(n));
        }
    }

       /**
    * Returns the value of the natural logarithm of
    *   factorial <SPAN CLASS="MATH"><I>n</I></SPAN>. Gives 16 decimals of precision
    *   (relative error
    * <SPAN CLASS="MATH">&lt; 0.5&#215;10<SUP>-15</SUP></SPAN>).
    *  @param n the integer for which the log-factorial has to be computed
    *  @return the natural logarithm of <SPAN CLASS="MATH"><I>n</I></SPAN> factorial
    */
   public static double lnFactorial (final int n) {
      final int nLIM = 14;

      if (n < 0) {
        throw new IllegalArgumentException ("lnFactorial:   n < 0");
        }

      if (n == 0 || n == 1) {
         return 0.0;
         }
      if (n <= nLIM) {
         long z = 1;
         long x = 1;
         for (int i = 2; i <= n; i++) {
            ++x;
            z *= x;
         }
         return Math.log (z);
      }
      else {
         final double x = (double)(n + 1);
         final double y = 1.0/(x*x);
         double z = ((-(5.95238095238E-4*y) + 7.936500793651E-4)*y -
            2.7777777777778E-3)*y + 8.3333333333333E-2;
         z = ((x - 0.5)*Math.log (x) - x) + 9.1893853320467E-1 + z/x;
         return z;
      }
   }
}

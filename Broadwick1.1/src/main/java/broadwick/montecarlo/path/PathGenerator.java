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
package broadwick.montecarlo.path;

/**
 * A Path consists of several steps generated at random.
 */
public interface PathGenerator {
    
    /**
     * Generate the next step in the path.
     * @param step the current step.
     * @return the proposed next step.
     */
     Step generateNextStep(final Step step);
     
     /**
      * Get the initial Step of the path.
      * @return the initial step.
      */
     Step getInitialStep();
}

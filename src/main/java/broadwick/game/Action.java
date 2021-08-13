/*
 * Copyright 2015 University of Glasgow.
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
package broadwick.game;

/**
 * The action class is simply a way of referencing the rows in the payoff matrix. For example, for the prisoners
 * dilemma the actions are coop and defect and allows us to refer to the payoff matrix P as P[coop,defect].
 *
 */
public abstract class Action {

    /**
     * This method translates the action (from the string description of its name) to an integer that is used as the 
     * row or column index in the payoff matrix. Note: this is just the position in the enumerated action type.
     * @param act th action
     * @throw InvalidArgumentException if this enum type has no constant with the specified name
     * @return the index of the action for the payoff matrix for one player.
     */
    public final int action(final String act) {
            return actions.valueOf(act).ordinal();
    }

    protected enum actions{};
}

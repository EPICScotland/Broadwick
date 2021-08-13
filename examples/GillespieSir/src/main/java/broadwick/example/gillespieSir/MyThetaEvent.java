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
package broadwick.example.gillespieSir;

/**
 * This theta event will simply add 50 new susceptibles to the amount manager.
 */
public class MyThetaEvent {

    /**
     * Create the theta event that will add 50 to the susceptibles.
     * @param amountManager 
     */
    public MyThetaEvent(final MyAmountManager amountManager) {
        this.amountManager = amountManager;
    }
    
    /**
     * Perform an event that we call a theta event. Here we will just add 50 individuals to the susceptibles.
     */
    public final void doThetaEvent() {
        
        amountManager.setNumberOfSusceptibles(amountManager.getNumberOfSusceptibles()+50);
    }
    
    
    private final MyAmountManager amountManager;
}

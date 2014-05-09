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
package broadwick.example.stochasticsir;

/**
 * This theta event will simply add 50 new susceptibles to the amount manager.
 */
public class MyThetaEvent {

    public MyThetaEvent(final MyAmountManager amountManager) {
        this.amountManager = amountManager;
    }
    
    public void doThetaEvent() {
        
        amountManager.setNumberOfSusceptibles(amountManager.getNumberOfSusceptibles()+50);
    }
    
    
    public final MyAmountManager amountManager;
}

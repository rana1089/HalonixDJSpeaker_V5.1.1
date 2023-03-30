/*
 * Copyright 2016 Junk Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wansnn.csc.wsbulb.BlueTooth;


public class MyGattAttributes extends GattAttributes {
    //GATT Characteristics

    public static final String BLESENDCHARACTERISTIC = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static final String BLESERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";


    static {
        // Characteristics name.
        attributes.put(BLESENDCHARACTERISTIC, "Config Control");
        attributes.put(BLESERVICE, "Config Control");
    }
}

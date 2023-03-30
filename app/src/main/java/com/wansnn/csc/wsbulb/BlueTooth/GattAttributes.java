/*
 * Copyright 2015 Junk Chen
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

import java.util.HashMap;
import java.util.Map;

public abstract class GattAttributes {
    public static final Map<String, String> attributes = new HashMap<>();
    //GATT Services
    public static final String SERVICE_GENERIC_ACCESS = "00001800-0000-1000-8000-00805F9B34FB";
    public static final String SERVICE_GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805F9B34FB";

    //GATT Characteristics
    public static final String CHARACTERISTIC_SERVICE_CHANGED = "00002A05-0000-1000-8000-00805F9B34FB";
    public static final String CHARACTERISTIC_DEVICE_NAME = "00002A00-0000-1000-8000-00805F9B34FB";


    public static final String DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION =
            "00002902-0000-1000-8000-00805F9B34FB";

    //Units
    public static final String UNIT_ = "00000000-0000-1000-8000-00805F9B34FB";
    public static final String UNIT_UNITLESS = "00002700-0000-1000-8000-00805F9B34FB";

    static {
        // Sample Services name.
        attributes.put(SERVICE_GENERIC_ACCESS, "Generic Access");
        attributes.put(SERVICE_GENERIC_ATTRIBUTE, "Generic Attribute");

        // Sample Characteristics name.
        attributes.put(CHARACTERISTIC_DEVICE_NAME, "Device Name");
        attributes.put(CHARACTERISTIC_SERVICE_CHANGED, "Service Changed");
        attributes.put(DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, "Client Characteristic Configuration");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid.toUpperCase());
        return name == null ? defaultName : name;
    }

}

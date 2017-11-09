/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.androidthings.bluetooth.audio;

import android.os.Build;

@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX6UL_PICO = "imx6ul_pico";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";

    /**
     * Return the GPIO pin with a button that will trigger the Pairing command.
     */
    public static String getGPIOForPairing() {
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "BCM21";
            case DEVICE_IMX6UL_PICO:
                return "GPIO2_IO03";
            case DEVICE_IMX7D_PICO:
                return "GPIO6_IO14";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    /**
     * Return the GPIO pin with a button that will trigger the Disconnect All command.
     */
    public static String getGPIOForDisconnectAllBTDevices() {
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "BCM20";
            case DEVICE_IMX6UL_PICO:
                return "GPIO4_IO22";
            case DEVICE_IMX7D_PICO:
                return "GPIO6_IO15";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}

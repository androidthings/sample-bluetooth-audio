/*
 * Copyright 2019, Rosário Pereira Fernandes
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
package io.github.rosariopfernandes.bluetoothingspeaker

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.util.Log
import java.lang.reflect.InvocationTargetException

fun Intent.getPreviousAdapterState(): Int {
    return getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)
}

fun Intent.getCurrentAdapterState(): Int {
    return getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
}

fun Intent.getPreviousProfileState(): Int {
    return getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)
}

fun Intent.getCurrentProfileState(): Int {
    return getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)
}

fun Intent.getDevice(): BluetoothDevice? {
    return getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
}

fun BluetoothDevice.disconnect(profile: BluetoothProfile): Boolean {
    val TAG = "Extensions.BluetoothDevice.disconnect"
    return try {
        val m = profile.javaClass.getMethod("disconnect", BluetoothDevice::class.java)
        m.invoke(profile, this)
        true
    } catch (e: NoSuchMethodException) {
        Log.w(TAG, "No disconnect method in the ${profile.javaClass.name}" +
                " class, ignoring request.")
        false
    } catch (e: IllegalAccessException) {
        Log.w(TAG, "Could not execute method 'disconnect' in profile " +
                 "${profile.javaClass.name}, ignoring request.", e)
        false
    } catch (e: InvocationTargetException) {
        Log.w(TAG, "Could not execute method 'disconnect' in profile " +
                "${profile.javaClass.name}, ignoring request.", e)
        false
    }

}
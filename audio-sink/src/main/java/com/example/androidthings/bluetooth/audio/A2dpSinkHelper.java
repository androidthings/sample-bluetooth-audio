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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper methods and constants related to the A2DP_SINK profile.
 *
 * Most constants defined here are just copied from classes in the
 * {@link android.bluetooth} package, since they are hidden from the public Android API and cannot
 * be directly used.
 */
public final class A2dpSinkHelper {

    private static final String TAG = "A2DPSinkHelper";

    /**
     * Profile number for A2DP_SINK profile.
     */
    public static final int A2DP_SINK_PROFILE = 11;

    /**
     * Intent used to broadcast the change in connection state of the A2DP Sink
     * profile.
     *
     * <p>This intent will have 3 extras:
     * <ul>
     *   <li> {@link BluetoothProfile#EXTRA_STATE} - The current state of the profile. </li>
     *   <li> {@link BluetoothProfile#EXTRA_PREVIOUS_STATE}- The previous state of the
     *         profile.</li>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     *
     * <p>{@link BluetoothProfile#EXTRA_STATE} or {@link BluetoothProfile#EXTRA_PREVIOUS_STATE}
     * can be any of {@link BluetoothProfile#STATE_DISCONNECTED},
     * {@link BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_CONNECTED},
     * {@link BluetoothProfile#STATE_DISCONNECTING}.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission to
     * receive.
     */
    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED";

    /**
     * Intent used to broadcast the change in the Playing state of the A2DP Sink
     * profile.
     *
     * <p>This intent will have 3 extras:
     * <ul>
     *   <li> {@link BluetoothProfile#EXTRA_STATE} - The current state of the profile. </li>
     *   <li> {@link BluetoothProfile#EXTRA_PREVIOUS_STATE}- The previous state of the
     *         profile. </li>
     *   <li> {@link BluetoothDevice#EXTRA_DEVICE} - The remote device. </li>
     * </ul>
     *
     * <p>{@link BluetoothProfile#EXTRA_STATE} or {@link BluetoothProfile#EXTRA_PREVIOUS_STATE}
     * can be any of {@link #STATE_PLAYING}, {@link #STATE_NOT_PLAYING},
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission to
     * receive.
     */
    public static final String ACTION_PLAYING_STATE_CHANGED =
            "android.bluetooth.a2dp-sink.profile.action.PLAYING_STATE_CHANGED";

    /**
     * A2DP sink device is streaming music. This state can be one of
     * {@link BluetoothProfile#EXTRA_STATE} or {@link BluetoothProfile#EXTRA_PREVIOUS_STATE} of
     * {@link #ACTION_PLAYING_STATE_CHANGED} intent.
     */
    public static final int STATE_PLAYING   =  10;

    /**
     * A2DP sink device is NOT streaming music. This state can be one of
     * {@link BluetoothProfile#EXTRA_STATE} or {@link BluetoothProfile#EXTRA_PREVIOUS_STATE} of
     * {@link #ACTION_PLAYING_STATE_CHANGED} intent.
     */
    public static final int STATE_NOT_PLAYING   =  11;

    public static int getPreviousAdapterState(Intent intent) {
        return intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
    }

    public static int getCurrentAdapterState(Intent intent) {
        return intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
    }

    public static int getPreviousProfileState(Intent intent) {
        return intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
    }

    public static int getCurrentProfileState(Intent intent) {
        return intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
    }

    public static BluetoothDevice getDevice(Intent intent) {
        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    }

    /**
     * Provides a way to call the disconnect method in the BluetoothA2dpSink class that is
     * currently hidden from the public API. Avoid relying on this for production level code, since
     * hidden code in the API is subject to change.
     *
     * @param profile
     * @param device
     * @return
     */
    public static boolean disconnect(BluetoothProfile profile, BluetoothDevice device) {
        try {
            Method m = profile.getClass().getMethod("disconnect", BluetoothDevice.class);
            m.invoke(profile, device);
            return true;
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "No disconnect method in the " + profile.getClass().getName() +
                    " class, ignoring request.");
            return false;
        } catch (InvocationTargetException | IllegalAccessException e) {
            Log.w(TAG, "Could not execute method 'disconnect' in profile " +
                    profile.getClass().getName() + ", ignoring request.", e);
            return false;
        }
    }

}

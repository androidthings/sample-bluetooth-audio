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

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import com.example.androidthings.bluetooth.audio.R
import com.google.android.things.bluetooth.BluetoothProfileManager
import java.util.Objects
import java.util.Locale

class A2dpSinkActivity : Activity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var a2DPSinkProxy: BluetoothProfile? = null

    private lateinit var btnPair: Button
    private lateinit var btnDisconnect: Button

    private var ttsEngine: TextToSpeech? = null

    /**
     * Handle an intent that is broadcast by the Bluetooth adapter whenever it changes its
     * state (after calling enable(), for example).
     * Action is [BluetoothAdapter.ACTION_STATE_CHANGED] and extras describe the old
     * and the new states. You can use this intent to indicate that the device is ready to go.
     */
    private val adapterStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val oldState = intent.getPreviousAdapterState()
            val newState = intent.getCurrentAdapterState()
            Log.d(TAG, "Bluetooth Adapter changing state from $oldState to $newState")
            if (newState == BluetoothAdapter.STATE_ON) {
                Log.i(TAG, "Bluetooth Adapter is ready")
                initA2DPSink()
            }
        }
    }

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * connects or disconnects to it.
     * Action is [ACTION_CONNECTION_STATE_CHANGED] and
     * extras describe the old and the new connection states. You can use it to indicate that
     * there's a device connected.
     */
    private val sinkProfileStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_CONNECTION_STATE_CHANGED) {
                val oldState = intent.getPreviousProfileState()
                val newState = intent.getCurrentProfileState()
                val device = intent.getDevice()
                Log.d(TAG, "Bluetooth A2DP sink changing connection state from $oldState" +
                        " to $newState device $device")
                device?.let {
                    val deviceName = Objects.toString(device.name, "a device")
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        speak("Connected to $deviceName")
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        speak("Disconnected from $deviceName")
                    }
                }
            }
        }
    }

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * starts or stops playing through the A2DP sink.
     * Action is [ACTION_PLAYING_STATE_CHANGED] and
     * extras describe the old and the new playback states. You can use it to indicate that
     * there's something playing. You don't need to handle the stream playback by yourself.
     */
    private val sinkProfilePlaybackChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_PLAYING_STATE_CHANGED) {
                val oldState = intent.getPreviousProfileState()
                val newState = intent.getCurrentProfileState()
                val device = intent.getDevice()
                Log.d(TAG, "Bluetooth A2DP sink changing playback state from $oldState" +
                        " to $newState device $device" )
                if (device != null) {
                    if (newState == STATE_PLAYING) {
                        Log.i(TAG, "Playing audio from device ${device.address}")
                    } else if (newState == STATE_NOT_PLAYING) {
                        Log.i(TAG, "Stopped playing audio from ${device.address}")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a2dpsink)

        btnPair = findViewById(R.id.btnPair)
        btnDisconnect = findViewById(R.id.btnDisconnect)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.w(TAG, "No default Bluetooth adapter. Device likely does not support bluetooth.")
            return
        }

        // We use Text-to-Speech to indicate status change to the user
        initTts()

        registerReceiver(adapterStateChangeReceiver, IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(sinkProfileStateChangeReceiver, IntentFilter(
                ACTION_CONNECTION_STATE_CHANGED))
        registerReceiver(sinkProfilePlaybackChangeReceiver, IntentFilter(
                ACTION_PLAYING_STATE_CHANGED))

        bluetoothAdapter?.let {
            if (it.isEnabled) {
                Log.d(TAG, "Bluetooth Adapter is already enabled.")
                initA2DPSink()
            } else {
                Log.d(TAG, "Bluetooth adapter not enabled. Enabling.")
                it.enable()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        unregisterReceiver(adapterStateChangeReceiver)
        unregisterReceiver(sinkProfileStateChangeReceiver)
        unregisterReceiver(sinkProfilePlaybackChangeReceiver)

        a2DPSinkProxy?.let {
            bluetoothAdapter!!.closeProfileProxy(A2DP_SINK_PROFILE, a2DPSinkProxy)
        }

        ttsEngine?.let {
            it.stop()
            it.shutdown()
        }

        // we intentionally leave the Bluetooth adapter enabled, so that other samples can use it
        // without having to initialize it.
    }

    private fun setupBTProfiles() {
        val bluetoothProfileManager = BluetoothProfileManager.getInstance()
        val enabledProfiles = bluetoothProfileManager.enabledProfiles
        if (!enabledProfiles.contains(A2DP_SINK_PROFILE)) {
            Log.d(TAG, "Enabling A2dp sink mode.")
            val toDisable = listOf(BluetoothProfile.A2DP)
            val toEnable = listOf(A2DP_SINK_PROFILE, AVRCP_CONTROLLER_PROFILE)
            bluetoothProfileManager.enableAndDisableProfiles(toEnable, toDisable)
        } else {
            Log.d(TAG, "A2dp sink profile is enabled.")
        }
    }

    /**
     * Initiate the A2DP sink.
     */
    private fun initA2DPSink() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth adapter not available or not enabled.")
            return
        }
        setupBTProfiles()
        Log.d(TAG, "Set up Bluetooth Adapter name and profile")
        bluetoothAdapter!!.name = ADAPTER_FRIENDLY_NAME
        bluetoothAdapter!!.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                a2DPSinkProxy = proxy
                enableDiscoverable()
            }

            override fun onServiceDisconnected(profile: Int) {

            }
        }, A2DP_SINK_PROFILE)

        configureButton()
    }

    /**
     * Enable the current [BluetoothAdapter] to be discovered (available for pairing) for
     * the next [.DISCOVERABLE_TIMEOUT_MS] ms.
     */
    private fun enableDiscoverable() {
        Log.d(TAG, "Registering for discovery.")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIMEOUT_MS)
        }
        startActivityForResult(discoverableIntent, REQUEST_CODE_ENABLE_DISCOVERABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_DISCOVERABLE) {
            Log.d(TAG, "Enable discoverable returned with result $resultCode")

            // ResultCode, as described in BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE, is either
            // RESULT_CANCELED or the number of milliseconds that the device will stay in
            // discoverable mode. In a regular Android device, the user will see a popup requesting
            // authorization, and if they cancel, RESULT_CANCELED is returned. In Android Things,
            // on the other hand, the authorization for pairing is always given without user
            // interference, so RESULT_CANCELED should never be returned.
            if (resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Enable discoverable has been cancelled by the user. " +
                        "This should never happen in an Android Things device.")
                return
            }
            Log.i(TAG, "Bluetooth adapter successfully set to discoverable mode. " +
                    "Any A2DP source can find it with the name $ADAPTER_FRIENDLY_NAME"  +
                    " and pair for the next $DISCOVERABLE_TIMEOUT_MS ms. " +
                    "Try looking for it on your phone, for example.")

            // There is nothing else required here, since Android framework automatically handles
            // A2DP Sink. Most relevant Bluetooth events, like connection/disconnection, will
            // generate corresponding broadcast intents or profile proxy events that you can
            // listen to and react appropriately.

            speak("Bluetooth audio sink is discoverable for $DISCOVERABLE_TIMEOUT_MS" +
                    " milliseconds. Look for a device named $ADAPTER_FRIENDLY_NAME")

        }
    }

    private fun disconnectConnectedDevices() {
        if (a2DPSinkProxy == null || bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            return
        }
        speak("Disconnecting devices")
        for (device in a2DPSinkProxy!!.connectedDevices) {
            Log.i(TAG, "Disconnecting device $device")
            device.disconnect(a2DPSinkProxy!!)
        }
    }

    private fun configureButton() {
        btnPair.setOnClickListener {
            // Enable Pairing mode (discoverable)
            enableDiscoverable()
        }
        btnDisconnect.setOnClickListener {
            // Disconnect any currently connected devices
            disconnectConnectedDevices()
        }
    }

    private fun initTts() {
        ttsEngine = TextToSpeech(this@A2dpSinkActivity,
                TextToSpeech.OnInitListener { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        ttsEngine!!.language = Locale.US
                    } else {
                        Log.w(TAG, "Could not open TTS Engine (onInit status=$status" +
                                "). Ignoring text to speech")
                        ttsEngine = null
                    }
                })
    }


    private fun speak(utterance: String) {
        Log.i(TAG, utterance)
        ttsEngine?.speak(utterance, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID)
    }

    companion object {
        private const val TAG = "A2dpSinkActivity"

        private const val ADAPTER_FRIENDLY_NAME = "Bluetooth Speaks Things"
        private const val DISCOVERABLE_TIMEOUT_MS = 300
        private const val REQUEST_CODE_ENABLE_DISCOVERABLE = 100
        private const val UTTERANCE_ID = "io.github.rosariopfernandes.btspeaksthings.UTTERANCE_ID"

        /**
         * Profile number for A2DP_SINK profile.
         */
        private const val A2DP_SINK_PROFILE = 11

        /**
         * Profile number for AVRCP_CONTROLLER profile.
         */
        private const val AVRCP_CONTROLLER_PROFILE = 12

        /**
         * Intent used to broadcast the change in connection state of the A2DP Sink
         * profile.
         *
         *
         * This intent will have 3 extras:
         *
         *  *  [BluetoothProfile.EXTRA_STATE] - The current state of the profile.
         *  *  [BluetoothProfile.EXTRA_PREVIOUS_STATE]- The previous state of the
         * profile.
         *  *  [BluetoothDevice.EXTRA_DEVICE] - The remote device.
         *
         *
         *
         * [BluetoothProfile.EXTRA_STATE] or [BluetoothProfile.EXTRA_PREVIOUS_STATE]
         * can be any of [BluetoothProfile.STATE_DISCONNECTED],
         * [BluetoothProfile.STATE_CONNECTING], [BluetoothProfile.STATE_CONNECTED],
         * [BluetoothProfile.STATE_DISCONNECTING].
         *
         *
         * Requires [android.Manifest.permission.BLUETOOTH] permission to
         * receive.
         */
        private const val ACTION_CONNECTION_STATE_CHANGED =
                "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED"

        /**
         * Intent used to broadcast the change in the Playing state of the A2DP Sink
         * profile.
         *
         *
         * This intent will have 3 extras:
         *
         *  *  [BluetoothProfile.EXTRA_STATE] - The current state of the profile.
         *  *  [BluetoothProfile.EXTRA_PREVIOUS_STATE]- The previous state of the
         * profile.
         *  *  [BluetoothDevice.EXTRA_DEVICE] - The remote device.
         *
         *
         *
         * [BluetoothProfile.EXTRA_STATE] or [BluetoothProfile.EXTRA_PREVIOUS_STATE]
         * can be any of [.STATE_PLAYING], [.STATE_NOT_PLAYING],
         *
         *
         * Requires [android.Manifest.permission.BLUETOOTH] permission to
         * receive.
         */
        private const val ACTION_PLAYING_STATE_CHANGED = "android.bluetooth.a2dp-sink.profile.action.PLAYING_STATE_CHANGED"

        /**
         * A2DP sink device is streaming music. This state can be one of
         * [BluetoothProfile.EXTRA_STATE] or [BluetoothProfile.EXTRA_PREVIOUS_STATE] of
         * [.ACTION_PLAYING_STATE_CHANGED] intent.
         */
        private const val STATE_PLAYING = 10

        /**
         * A2DP sink device is NOT streaming music. This state can be one of
         * [BluetoothProfile.EXTRA_STATE] or [BluetoothProfile.EXTRA_PREVIOUS_STATE] of
         * [.ACTION_PLAYING_STATE_CHANGED] intent.
         */
        private const val STATE_NOT_PLAYING = 11
    }
}
package io.github.rosariopfernandes.bluetoothingspeaker.remotecontrol

data class Device (
    var friendly_name: String = "Bluetooth Speaks Things",
    var settings: DeviceSettings = DeviceSettings()
)
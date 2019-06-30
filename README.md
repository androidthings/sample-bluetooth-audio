# bluetoothingspeaker

Use an Android Things device to transform any speaker in a bluetooth speaker.

## Introduction

This Android Things app makes use of Bluetooth Android APIs to allow devices like phones or
 computers to connect and play audio in your Android Things device.

## Pre-requisites

- Android Things compatible board
- Android Studio 3.4+
- A speaker or headsets, so that you can listen to the audio and notifications.
- A touch screen connected to the Android Things device so that you can control the
  sample at runtime. Without the buttons, you can use adb.

## Build and install

On Android Studio, click on the "Run" button.

If you prefer to run on the command line, type

```bash
./gradlew installDebug
adb shell am start io.github.rosariopfernandes.bluetoothingspeaker/.A2dpSinkActivity
```

_Note_: If you connect an audio source to an Android Things audio sink (eg this
sample) but you can't hear your media playing through the audio jack, check if
you have an HDMI display connected. If so, the audio will be routed to the HDMI
output.

## License

Copyright 2017 The Android Open Source Project, Inc.
Copyright 2019 Rosário Pereira Fernandes

Copyright 2019 Rosário Pereira Fernandes

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

[demo-yt]: https://www.youtube.com/watch?v=EDV_DaspP60&list=PLWz5rJ2EKKc-GjpNkFe9q3DhE2voJscDT&index=2
[demo-gif]: demo1.gif

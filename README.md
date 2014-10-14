WearSense
===========

Sensor data acquisition from Android Wear devices.


## Build

These instructions are for building and testing using the command
line.  If you prefer to use Studio, just follow the standard
instructions available online.  More details regarding command line
usage can also be found online, and you can always run `$ adb help`,
`$ android -h`, etc.

Run `./gradlew tasks` to get a list of all tasks; add `--all` to get
all tasks and more detail.

To build:

```
$ ./gradlew assembleDebug
```

You may see a message along the lines of

```
Relying on packaging to define the extension of the main artifact has been deprecated and is scheduled to be removed in Gradle 2.0
```

Ignore it; it's a harmless bug in the plugin.


## Debug install

Plug in phone to usb.  Enable debugging on phone and watch (google
it).  For moto 360 enable bluetooth debugging.

*Hint* Bluetooth debugging is very slow - it takes over a minute
 usually just to install.  Use a USB connected smartwatch for
 development and debugging, then only test on the moto 360 once
 everything is hunky-dory.

List devices `$ adb devices`.

For bluetooth (moto 360) debugging, do:

```
$ adb forward tcp:4444 localabstract:/adb-hub; adb connect localhost:4444
```

Install to the wearable:

```
$ adb -s localhost:4444 install -r wear/build/outputs/apk/wear-debug.apk
```

to the handset:

```
$ adb -s <device id> install -r mobile/build/outputs/apk/mobile-debug.apk
```

The -s switch points to the device listed by `adb devices`

Uninstall app from wearable:

```
adb -s localhost:4444 shell pm uninstall -k org.norc.sparky.wearsense
```

The -k switch is for the app's package name.

## logging

To see the options for logcat:  `$ adb logcat -h`

Dump the log to the screen:

```
$ adb -s localhost:4444 logcat
```

Same, but only show the log messages tagged with `WearSDCService`

```
$ adb -s localhost:4444 logcat -s WearSDCService
```

Same, but for the handset:

```
$ adb -s <device ID> logcat -s HappActivity
```

were <device ID> is the id displayed by `adb devices`.

"Happ" is for "Handset App"; "Wapp" is for "Wear App".

See the code to find out which tags are available.

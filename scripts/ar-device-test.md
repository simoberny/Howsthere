# Camera AR device test

The optional Gradle init script builds a separate app, **Howsthere AR Test**
(`it.howsthere.howsthere2.artest`). It leaves the installed production app and
its history untouched. The test seeds a synthetic skyline; it makes no horizon
API requests. Google Maps API restrictions may prevent maps loading under this
test package, which does not affect the camera test.

Build:
```
gradlew -I scripts/ar-device-test.init.gradle :app:assembleDebug :app:assembleDebugAndroidTest
```

With a connected, unlocked Android device:
```
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell pm grant it.howsthere.howsthere2.artest android.permission.CAMERA
adb shell am instrument -w -e class it.howsthere.howsthere2.ArCameraTest it.howsthere.howsthere2.artest.test/androidx.test.runner.AndroidJUnitRunner
```

The test checks preview streaming and its projection transform, opening from Sun
and Moon, alignment controls, recreation, landscape rotation and closing.
It runs only in the isolated package. It does not assess compass accuracy or
optical alignment against real mountains; those need a field check at the map
location used for the panorama.

A normal Gradle build without the init script keeps the original application ID.

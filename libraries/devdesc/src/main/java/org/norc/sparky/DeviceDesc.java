/* TODO:  make this a shared resource

   Task:  get device description from e.g.

   import android.os.Build;
   import android.os.Build.VERSION;
   import android.hardware.Sensor;
   import android.hardware.SensorManager;
   etc.
*/

package org.norc.sparky;

import android.os.Build;
/*
  android.os.Build string constants:
  BOARD	The name of the underlying board, like "goldfish".
  BOOTLOADER	The system bootloader version number.
  BRAND	The consumer-visible brand with which the product/hardware
  will be associated, if any.
  CPU_ABI	The name of the instruction set (CPU type + ABI convention)
  of native code.
  CPU_ABI2	The name of the second instruction set
  (CPU type + ABI convention) of native code.
  DEVICE	The name of the industrial design.
  DISPLAY	A build ID string meant for displaying to the user
  FINGERPRINT	A string that uniquely identifies this build.
  HARDWARE	The name of the hardware (from the kernel command line or /proc).
  HOST
  ID		Either a changelist number, or a label like "M4-rc20".
  MANUFACTURER The manufacturer of the product/hardware.
  MODEL	The end-user-visible name for the end product.
  PRODUCT	The name of the overall product.
  RADIO	This field was deprecated in API level 14.
  The radio firmware version is frequently not available
  when this class is initialized, leading to a blank or
  "unknown" value for this string. Use getRadioVersion() instead.
  SERIAL	A hardware serial number, if available.
  TAGS	Comma-separated tags describing the build, like "unsigned,debug".
  TIME
  TYPE	The type of build, like "user" or "eng".
  USER
*/

import android.os.Build.VERSION;
/*  android.os.Build.VERSION constants:
    CODENAME	The current development codename,
    or the string "REL" if this is a release build.
    INCREMENTAL	The internal value used by the underlying source
    control to represent this build.
    RELEASE	The user-visible version string.
    SDK		This field was deprecated in API level 4.
    Use SDK_INT to easily get this as an integer.
    SDK_INT	The user-visible SDK version of the framework;
    its possible values are defined in Build.VERSION_CODES.
*/


public class DeviceDesc {

    static void describe() {
	mTextView = (TextView) stub.findViewById(R.id.textMfg);
	mTextView.setText("Mfg: " + android.os.Build.MANUFACTURER);

	mTextView = (TextView) stub.findViewById(R.id.textBrand);
	mTextView.setText("Brand: " + android.os.Build.BRAND);

	mTextView = (TextView) stub.findViewById(R.id.textProduct);
	mTextView.setText("Product: " + android.os.Build.PRODUCT);

	mTextView = (TextView) stub.findViewById(R.id.textModel);
	mTextView.setText("Model: " + android.os.Build.MODEL);

	mTextView = (TextView) stub.findViewById(R.id.textSerial);
	mTextView.setText("Serial: " + android.os.Build.SERIAL);

	mTextView = (TextView) stub.findViewById(R.id.textHardware);
	mTextView.setText("Hardware: " + android.os.Build.HARDWARE);

	mTextView = (TextView) stub.findViewById(R.id.textCPU);
	mTextView.setText("CPU: " + getCPUInfo());

	mTextView = (TextView) stub.findViewById(R.id.textBoard);
	mTextView.setText("Board: " + android.os.Build.BOARD);

	mTextView = (TextView) stub.findViewById(R.id.textBootloader);
	mTextView.setText("Bootloader: " + android.os.Build.BOOTLOADER);

	mTextView = (TextView) stub.findViewById(R.id.textDevice);
	mTextView.setText("Device: " + android.os.Build.DEVICE);

	mTextView = (TextView) stub.findViewById(R.id.textDisplay);
	mTextView.setText("Display: " + android.os.Build.DISPLAY);

	mTextView = (TextView) stub.findViewById(R.id.textCPU_ABI);
	mTextView.setText("CPU ABI: " + android.os.Build.CPU_ABI);

	mTextView = (TextView) stub.findViewById(R.id.textCPU_ABI2);
	mTextView.setText("CPU ABI2: " + android.os.Build.CPU_ABI2);

    }
}

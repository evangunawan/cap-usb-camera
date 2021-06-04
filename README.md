# @periksa/cap-usb-camera

UVC Cameras support for capacitor apps connected via device's USB.
Based on [UVCCamera](https://github.com/saki4510t/UVCCamera) library,
using [Metuuu's](https://github.com/Metuuu/UVCCamera) forked repository to support Android SDK 30.

This plugin enable web apps to take picture or access your UVC external camera (e.g. webcams).
The plugin provides a native activity to access and opens stream from external camera,
which you can capture, fetch photo, and get the data from the plugin call
(it works similarly with [@capacitor/camera](https://capacitorjs.com/docs/apis/camera) plugin).
It supports the functionality to save the image to your media storage too.

Currently supports only Android platform, and not tested on many devices.

### Tested Devices

Tested and developed on an Android 11 Samsung Galaxy A51.

Please report or open an issue if your device is bugged or crashed using the plugin.

## Install

```bash
npm install @periksa/cap-usb-camera
npx cap sync
```

Modify your `build.gradle` (top-level project scope),
usually on `/android/build.gradle` directory if you are developing ionic-capacitor project.

Add new maven repository line to your `allprojects` -> `repositories` body.

```gradle
allprojects {
  repositories {
      google()
      jcenter()
      // Add This Line
      maven { url 'https://raw.github.com/saki4510t/libcommon/master/repository/' }
  }
}
```
Then sync your gradle project by pressing "Sync Now" on the top after you modify the file.

## Usage

Here is some example to use the plugin inside your web app.

Example code is written in Angular platform.

```typescript
import { UsbCamera } from '@periksa/cap-usb-camera';

// ...

private async fetchExternalCameraPhoto(): Promise<void> {
  const photoResult = await UsbCamera.getPhoto({ saveToStorage: false });
  if (photoResult.status_code === 0) {
    if (photoResult.exit_code === 'exit_no_devices') {
      // Handle show alert/notice when there is no device attached.
    }
    return;
  }
  if (photoResult.data) {
    const photoSrc = photoResult.data.dataURL; //Base64 enconded image.
    // Handle the base64 encoded image.
    // e.g. put it on <img src=""> tag.
  }
}

```

`status_code` is retrieved from Android's Activity `RESULT_CODE`,we use these 2 values:
- `-1`: OK, image fetched successfully. Will be accompanied by `success` exit_code.
- `0`: CANCELED, canceled by user, or plugin can't access camera.


Then you can have the exit reason inside `exit_code`. Available values are:
- `user_canceled` - User clicked *cancel* button on the camera activity. 
- `dialog_selection_canceled` - User clicked *cancel* button on camera selection dialog
(selection dialog appears if there is more than 1 camera device connected).
- `exit_no_devices` - Plugin won't start the activity when there is no device connected.
- `success` - Exit code if the plugin succeed to take the photo.


## API

<docgen-index>

* [`getPhoto(...)`](#getphoto)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getPhoto(...)

```typescript
getPhoto(config?: UsbCameraPhotoOptions) => any
```

Open native activity and get photo from usb camera device attached to the phone.
If there is no usb device connected, will return canceled exit code.

| Param        | Type                                                                    |
| ------------ | ----------------------------------------------------------------------- |
| **`config`** | <code><a href="#usbcameraphotooptions">UsbCameraPhotoOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### UsbCameraPhotoOptions

| Prop                | Type                 | Description                                        |
| ------------------- | -------------------- | -------------------------------------------------- |
| **`saveToStorage`** | <code>boolean</code> | Let app save captured photo to the device storage. |


#### UsbCameraResult

| Prop                | Type                                                 | Description                                                                                    |
| ------------------- | ---------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **`status_code`**   | <code>number</code>                                  | Status Code from Intent ResultCode.                                                            |
| **`status_code_s`** | <code>string</code>                                  | Description of the status code number.                                                         |
| **`exit_code`**     | <code>string</code>                                  | Description of exit or cancel reason.                                                          |
| **`data`**          | <code>{ dataURL?: string; fileURI?: string; }</code> | Result data payload, contains image in base64 DataURL, and Android filesystem URI to the file. |

</docgen-api>

## Contributing

Any contribution is very much appreciated, just clone the project and post a pull request!
Thank you very much!

### Unimplemented and future functionalities

- Activity not yet supports image manipulation, such as brightness, contrast and mirroring.
However the library supports this.
- Still have bugs with device connectivity.
- Activity user interface is somehow very simple, and not tested on more screen dimensions.

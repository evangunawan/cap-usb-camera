# @periksa/cap-usb-camera

UVC Cameras support for capacitor apps connected via device's USB.
Based on [UVCCamera](https://github.com/saki4510t/UVCCamera) library,
using [Metuuu's](https://github.com/Metuuu/UVCCamera) forked repository to support SDK 30.

This plugin enable web apps to take picture or access your UVC external camera.
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

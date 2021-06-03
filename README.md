# @periksa/cap-usb-camera

USB Camera Support

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

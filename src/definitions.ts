export interface UsbCameraPhotoOptions {
  /** Let app save captured photo to the device storage. */
  saveToStorage?: boolean;
}

export interface UsbCameraResult {
  /** Status Code from Intent ResultCode. */
  status_code: number;
  /** Description string of the status code number. */
  status_code_s: string;
  /** Description of exit or cancel reason. */
  exit_code: string;
  /**
   * Result data payload, contains image in base64 DataURL,
   * and Android filesystem URI to the file.
   * */
  data?: {
    dataURL?: string,
    fileURI?: string,
  };
}

export interface UsbCameraPlugin {
  /**
   * Open native activity and get photo from usb camera device attached to the phone.
   * If there is no usb device connected, will return canceled exit code.
   * @returns {Promise<UsbCameraResult>} Image and result status.
   * */
  getPhoto(config?: UsbCameraPhotoOptions): Promise<UsbCameraResult>;
}

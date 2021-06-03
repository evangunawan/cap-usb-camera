import { WebPlugin } from '@capacitor/core';

import type { UsbCameraPlugin, UsbCameraResult } from './definitions';

export class UsbCameraWeb extends WebPlugin implements UsbCameraPlugin {
  getPhoto(): Promise<UsbCameraResult> {
    throw new Error('Method not implemented.');
  }
}

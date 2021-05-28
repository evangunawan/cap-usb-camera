import { WebPlugin } from '@capacitor/core';

import type { UsbCameraPlugin } from './definitions';

export class UsbCameraWeb extends WebPlugin implements UsbCameraPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

import { registerPlugin } from '@capacitor/core';

import type { UsbCameraPlugin } from './definitions';

const UsbCamera = registerPlugin<UsbCameraPlugin>('UsbCamera', {
  web: () => import('./web').then(m => new m.UsbCameraWeb()),
});

export * from './definitions';
export { UsbCamera };

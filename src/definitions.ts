export interface UsbCameraPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}

import hilog from '@ohos.hilog';

class Logger {
  private domain: number;
  private prefix: string;
  private format: string = '%{public}s, %{public}s';
  private isDebug: boolean;

  constructor(prefix: string = 'MyApp', domain: number = 0xFF00, isDebug = false) {
    this.prefix = prefix;
    this.domain = domain;
    this.isDebug = isDebug;
  }

  debug(...args: string[]): void {
    if (this.isDebug) {
      hilog.debug(this.domain, this.prefix, this.format, args);
    }
  }

  info(...args: string[]): void {
    hilog.info(this.domain, this.prefix, this.format, args);
  }

  warn(...args: string[]): void {
    hilog.warn(this.domain, this.prefix, this.format, args);
  }

  error(...args: string[]): void {
    hilog.error(this.domain, this.prefix, this.format, args);
  }
}

export default new Logger('ImageCropPickerTurboModule', 0xFF00, true)
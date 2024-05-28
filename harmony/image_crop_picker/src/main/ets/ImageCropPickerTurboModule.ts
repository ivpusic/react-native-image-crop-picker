import type { TurboModuleContext } from '@rnoh/react-native-openharmony/ts';
import { TM } from "@rnoh/react-native-openharmony/generated/ts"
import { TurboModule } from '@rnoh/react-native-openharmony/ts'
import Logger from './Logger';
import type Want from '@ohos.app.ability.Want';
import image from '@ohos.multimedia.image';
import media from '@ohos.multimedia.media';
import util from '@ohos.util';
import picker from '@ohos.multimedia.cameraPicker';
import camera from '@ohos.multimedia.camera';
import { BusinessError } from '@ohos.base';
import fs, { Filter } from '@ohos.file.fs';
import { JSON } from '@kit.ArkTS';
import abilityAccessCtrl, { Permissions } from '@ohos.abilityAccessCtrl';


export type MediaType = 'photo' | 'video' | 'any';
export type OrderType = 'asc' | 'desc' | 'none';
export type ErrorCode = 'camera_unavailable' | 'permission' | 'others';
const MaxNumber = 5;
const MinNumber = 1;
const ImageQuality = -1;
const TAG : string = 'ImageCropPickerTurboModule';
const WANT_PARAM_URI_SELECT_SINGLE: string = 'singleselect';
const WANT_PARAM_URI_SELECT_MULTIPLE: string = 'multipleselect';
const ENTER_GALLERY_ACTION: string = "ohos.want.action.photoPicker";
const filePrefix = 'file://'
const atManager = abilityAccessCtrl.createAtManager();

let avMetadataExtractor: media.AVMetadataExtractor;

export type SmartAlbums = | 'Regular' | 'SyncedEvent' | 'SyncedFaces';
export type CompressVideoPresets = | 'LowQuality' | 'MediumQuality' | 'HighestQuality' | 'Passthrough';
export type CropperOptions = ImageOptions & {
  path: string;
}
export type Options = AnyOptions | VideoOptions | ImageOptions;
export type AnyOptions = Omit<ImageOptions, 'mediaType'> & Omit<VideoOptions, 'mediaType'> & {
  mediaType?: 'any';
};
export type VideoOptions = CommonOptions & {
  mediaType: 'video';
  compressVideoPreset?: CompressVideoPresets;
};
export type ImageOptions = CommonOptions & {
  mediaType: MediaType;
  width?: number;
  height?: number;
  includeBase64?: boolean;
  includeExif?: boolean;
  forceJpg?: boolean;
  cropping?: boolean;
  avoidEmptySpaceAroundImage?: boolean;
  cropperActiveWidgetColor?: string;
  cropperStatusBarColor?: string;
  cropperToolbarColor?: string;
  cropperToolbarWidgetColor?: string;
  cropperToolbarTitle?: string;
  freeStyleCropEnabled?: boolean;
  cropperTintColor?: string;
  cropperCircleOverlay?: boolean;
  cropperCancelText?: string;
  cropperCancelColor?: string;
  cropperChooseText?: string;
  cropperChooseColor?: string;
  cropperRotateButtonsHidden?: boolean
  showCropGuidelines?: boolean;
  showCropFrame?: boolean;
  enableRotationGesture?: boolean;
  disableCropperColorSetters?: boolean;
  compressImageMaxWidth?: number;
  compressImageMaxHeight?: number;
  compressImageQuality?: number;
}

export interface CommonOptions {
  multiple?: boolean;
  minFiles?: number;
  maxFiles?: number;
  waitAnimationEnd?: boolean;
  smartAlbums?: SmartAlbums[];
  useFrontCamera?: boolean;
  loadingLabelText?: string;
  showsSelectedCount?: boolean;
  sortOrder?: 'none' | 'asc' | 'desc';
  hideBottomControls?: boolean;
  compressImageQuality?: number;
  mediaType: MediaType;
  width?: number;
  height?: number;
  includeBase64?: boolean;
  includeExif?: boolean;
  forceJpg?: boolean;
  cropping?: boolean;
  avoidEmptySpaceAroundImage?: boolean;
  cropperActiveWidgetColor?: string;
  cropperStatusBarColor?: string;
  cropperToolbarColor?: string;
  cropperToolbarWidgetColor?: string;
  cropperToolbarTitle?: string;
  freeStyleCropEnabled?: boolean;
  cropperTintColor?: string;
  cropperCircleOverlay?: boolean;
  cropperCancelText?: string;
  cropperCancelColor?: string;
  cropperChooseText?: string;
  cropperChooseColor?: string;
  cropperRotateButtonsHidden?: boolean
  showCropGuidelines?: boolean;
  showCropFrame?: boolean;
  enableRotationGesture?: boolean;
  disableCropperColorSetters?: boolean;
  compressImageMaxWidth?: number;
  compressImageMaxHeight?: number;
  writeTempFile?: boolean;
}

export interface Image extends ImageVideoCommon {
  data?: string | null;
  cropRect?: CropRect | null;
}

export interface Video extends ImageVideoCommon {
  duration: number | null;
}

export interface CropRect {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface ImageOrVideo{
  data?: string | null;
  width?: number | null;
  height?: number | null;
  size?: number | null;
  cropRect?: CropRect | null;
  filename?: string | null;
  path?: string | null;
  exif?: Exif | null;
  mime?: string | null;
  sourceURL?: string | null;
  creationDate?: string | null;
  modificationDate?: string | null;
  localIdentifier?: string | null;
  duration?: string | null;
}

export interface ImageVideoCommon {
  path: string;
  size: number;
  width: number;
  height: number;
  mime: string;
  exif?: Exif;
  localIdentifier?: string;
  sourceURL?: string;
  filename?: string;
  creationDate?: string;
  modificationDate?: string;
}

export interface Exif {

}

export class FilePath{
  path?: string;
}

export interface VideoImageInfo {
  data?: string | null;
  width?: number | null;
  height?: number | null;
  size?: number | null;
  cropRect?: CropRect | null;
  filename?: string | null;
  path?: string | null;
  exif?: Exif | null;
  mime?: string | null;
  sourceURL?: string | null;
  creationDate?: number | null;
  modificationDate?: number | null;
  localIdentifier?: string | null;
  duration?: string | null;
}

export interface FilePathResult {
  result?: FilePath[];
}

export class AbilityResult {
  resultCode: number;
  want?: Want;
}

export class ImageCropPickerTurboModule extends TurboModule implements TM.ImageCropPicker.Spec{

  constructor(protected ctx: TurboModuleContext) {
    super(ctx);
  }

  isNullOrUndefined(value: any): boolean {
    return value === null || value === undefined || value === '';
  }

  async openPicker(options?: Options): Promise<Video[] | Video | ImageOrVideo[] | ImageOrVideo | Image [] | Image> {
    Logger.info(TAG, 'into openPicker request' + JSON.stringify(options));
    let minFiles = this.isNullOrUndefined(options?.minFiles) ? MinNumber : options.minFiles;
    let maxFiles = this.isNullOrUndefined(options?.maxFiles) ? MaxNumber : options.maxFiles;
    let isShowSerialNum = this.isNullOrUndefined(options?.showsSelectedCount) ? true : options?.showsSelectedCount;
    if (options.multiple && minFiles > maxFiles) {
      return new Promise(async(res, rej) => {
        rej('minFiles is error')
      })
    }
    let quality = options.compressImageQuality;
    if (!this.isNullOrUndefined(quality) && !(quality >= 0 && quality <= 1)) {
      return new Promise(async(res, rej) => {
        rej('quality is error')
      })
    }
    let multiple = this.isNullOrUndefined(options.multiple) ? false : options.multiple;
    let mediaType = options.mediaType == 'photo' ? 'FILTER_MEDIA_TYPE_IMAGE'
      : (options.mediaType == 'video' ? 'FILTER_MEDIA_TYPE_VIDEO' : 'FILTER_MEDIA_TYPE_ALL');

    let writeTempFile = this.isNullOrUndefined(options.writeTempFile) ? true :options.writeTempFile;
    let qualityNumber = this.isNullOrUndefined(options.compressImageQuality) ? ImageQuality : options.compressImageQuality;
    let forceJpg = this.isNullOrUndefined(options.forceJpg) ? false : options.forceJpg;

    let want : Want = {};
    if (multiple) {
      want = {
        "type": WANT_PARAM_URI_SELECT_MULTIPLE,
        "action": ENTER_GALLERY_ACTION,
        "parameters": {
          uri: WANT_PARAM_URI_SELECT_MULTIPLE,
          filterMediaType: mediaType,
          maxSelectCount: maxFiles,
          isShowSerialNum: isShowSerialNum,
        },
        "entities": []
      }
    } else {
      want = {
        "type": WANT_PARAM_URI_SELECT_SINGLE,
        "action": ENTER_GALLERY_ACTION,
        "parameters": {
          uri: WANT_PARAM_URI_SELECT_SINGLE,
          filterMediaType: mediaType,
        },
        "entities": []
      }
    }
    let result: AbilityResult = await this.ctx.uiAbilityContext.startAbilityForResult(want as Want);
    if (result.resultCode == -1) {
      return new Promise(async(res, rej) => {
        rej('result.resultCode is -1')
      })
    }
    let sourceFilePaths: Array<string> = result.want.parameters['select-item-list'] as Array<string>;
    let tempFilePaths = null;
    Logger.info(TAG, 'into openPicker tempFilePaths = '+ qualityNumber + ' ' + forceJpg);
    if (!(qualityNumber == -1 && !forceJpg)) {
      tempFilePaths = await this.compressPictures(qualityNumber * 100, forceJpg, sourceFilePaths);
    } else {
      tempFilePaths = writeTempFile ? this.getTempFilePaths(sourceFilePaths) : null;
    }
    return this.getPickerResult(options, sourceFilePaths, tempFilePaths);
  }

  getTempFilePaths(images : Array<string>): Array<string> {
    let resultImages : Array<string> = new Array<string>();
    Logger.info(TAG, 'getTempFilePaths images = '+ images);
    for (let srcPath of images) {
        Logger.info(TAG, 'getTempFilePaths img srcPath = '+ srcPath);
        let i = srcPath.lastIndexOf('.');
        let imageType = '';
        if (i != -1) {
          imageType = srcPath.substring(i + 1);
          Logger.info(TAG, 'getTempFilePaths img imageType = '+ imageType);
        }
        let file = fs.openSync(srcPath, fs.OpenMode.CREATE);
        let dstPath = this.ctx.uiAbilityContext.tempDir + '/rn_image_crop_picker_lib_temp_' + util.generateRandomUUID(true) + '.' + imageType;
        try {
          fs.copyFileSync(file.fd, dstPath, 0);
          resultImages.push(dstPath);
          Logger.info(TAG, 'getTempFilePaths suc dstPath = '+ dstPath);
        } catch (err) {
          Logger.info(TAG, 'getTempFilePaths fail err = '+ err.toString());
        }
        fs.closeSync(file);
    }
    return resultImages;
  }

  async getPickerResult(options: Options, sourceFilePaths : Array<string>, tempFilePaths : Array<string>): Promise<ImageOrVideo[] | ImageOrVideo> {
    Logger.info(TAG, 'into openPickerResult :');
    let resultsList: ImageOrVideo[] = [];
    let images = this.isNullOrUndefined(tempFilePaths) ? sourceFilePaths : tempFilePaths;
    Logger.info(TAG, 'into openPickerResult : images = '+ images);
    let includeBase64 = this.isNullOrUndefined(options.includeBase64) ? false : options.includeBase64;
    let results = {duration:null,data:null,cropRect:null,path:null,size:0,width:0,height:0,mime:'',exif:null,localIdentifier:'',sourceURL:'',filename:'',creationDate:null,modificationDate:null}
    for (let j = 0;j < images.length;j++) {

      let value = images[j];
      if (this.isNullOrUndefined(value)) {
        return;
      }
      let imageType;
      let i = value.lastIndexOf('/')
      let fileName = value.substring(i + 1)
      i = value.lastIndexOf('.')
      if (i != -1) {
        imageType = value.substring(i + 1)
      }
      results.sourceURL = sourceFilePaths[j];
      results.filename = fileName;

      let file = fs.openSync(value, fs.OpenMode.READ_ONLY)
      Logger.info(TAG, 'into openSync : file.fd = '+ file.fd + ' file.path = ' + file.path + ' file.name = ' + file.name);
      let stat = fs.statSync(file.fd);
      let length = stat.size;
      results.size = length;
      results.creationDate = stat.ctime + '';
      results.modificationDate = stat.mtime+ '';
      results.path = this.isNullOrUndefined(tempFilePaths) ? null : filePrefix + value;
      if (this.isImage(value)) {
        results.data = includeBase64 ? this.imageToBase64(value) : null;
        results.mime = 'image/' + imageType;
        Logger.info(TAG, 'into openPickerResult value :' + value);
        let imageIS = image.createImageSource(file.fd)
        let imagePM = await imageIS.createPixelMap()
        Logger.info(TAG, 'end createImageSource : imageIS = ' + imageIS + ' imagePM = '+ imagePM);
        let imgInfo = await imagePM.getImageInfo();
        results.height = imgInfo.size.height;
        results.width = imgInfo.size.width;
        imagePM.release().then(() => {
          imagePM = undefined;
        })
        imageIS.release().then(() => {
          imageIS = undefined;
        })
        results.duration = null;
      } else {
        Logger.info(TAG, 'into getPickerResult video start');
        results.data = null;
        results.mime = 'video/' + imageType;
        let url = 'fd://' + file.fd;
        Logger.info(TAG, 'start avPlayer url:' + url);
        avMetadataExtractor = await media.createAVMetadataExtractor();
        avMetadataExtractor.fdSrc = { fd: file.fd, offset: 0, length: length };
        try {
          const res = await avMetadataExtractor.fetchMetadata();
          results.duration = res.duration;
          results.width = Number(res.videoWidth);
          results.height = Number(res.videoHeight);
        } catch (error) {
          Logger.error(TAG, 'get video info suc error:' + error);
        }
      }
      resultsList.push(results);
      fs.closeSync(file);
    }
    return options.multiple ? resultsList : results;
  }

  async openCamera(options?: Options): Promise<Video[] | Video | ImageOrVideo[] | ImageOrVideo | Image [] | Image> {
    Logger.info(TAG, 'into openCamera request: ' + JSON.stringify(options));
    let isImg = false;
    let imgResult: Image = {data:null,cropRect:null,path:null,size:0,width:0,height:0,mime:'',exif:null,localIdentifier:'',sourceURL:'',filename:'',creationDate:null,modificationDate:null};
    let videoResult: Video = {duration:null,path:null,size:0,width:0,height:0,mime:'',exif:null,localIdentifier:'',sourceURL:'',filename:'',creationDate:null,modificationDate:null};
    let useFrontCamera = this.isNullOrUndefined(options.useFrontCamera) ? false : options.useFrontCamera;
    let writeTempFile = this.isNullOrUndefined(options?.writeTempFile) ? true :options?.writeTempFile;
    let mediaType = options.mediaType == 'photo' ? [picker.PickerMediaType.PHOTO] : (options.mediaType == 'video'
      ? [picker.PickerMediaType.VIDEO] : [picker.PickerMediaType.PHOTO,picker.PickerMediaType.VIDEO]);
    return new Promise(async(res, rej) => {
    try {
      let mContext = await this.ctx.uiAbilityContext;
      let pickerProfile: picker.PickerProfile = {
        cameraPosition: useFrontCamera? camera.CameraPosition.CAMERA_POSITION_FRONT : camera.CameraPosition.CAMERA_POSITION_BACK,
      };
      let pickerResult: picker.PickerResult = await picker.pick(mContext, mediaType, pickerProfile);
      Logger.info(TAG, 'into openCamera results: ' + JSON.stringify(pickerResult));
      let imgOrVideoPath = pickerResult.resultUri;
      isImg = this.isImage(imgOrVideoPath);

      let tempFilePaths = null;
      let sourceFilePaths : Array<string> = [imgOrVideoPath];
      tempFilePaths = writeTempFile ? this.getTempFilePaths(sourceFilePaths) : null;
      let tempFilePath = this.isNullOrUndefined(tempFilePaths) ? null : tempFilePaths[0];
      if (isImg) {
        this.getFileInfo(options?.includeBase64, imgOrVideoPath, tempFilePath).then((imageInfo)=>{
          imgResult.sourceURL = imgOrVideoPath;
          imgResult.path = this.isNullOrUndefined(tempFilePath) ? null : filePrefix + tempFilePath;
          imgResult.exif = imageInfo.exif;
          imgResult.data = imageInfo.data;
          imgResult.size = imageInfo.size;
          imgResult.width = imageInfo.width;
          imgResult.height = imageInfo.height;
          imgResult.filename = imageInfo.filename;
          imgResult.mime = imageInfo.mime;
          imgResult.localIdentifier = imageInfo.localIdentifier;
          imgResult.cropRect = imageInfo.cropRect;
          imgResult.creationDate = imageInfo.creationDate + '';
          imgResult.modificationDate = imageInfo.modificationDate + '';
          res(imgResult);
        })
      } else {
        this.getFileInfo(options?.includeBase64, imgOrVideoPath, tempFilePath).then((imageInfo)=>{
          videoResult.sourceURL = imgOrVideoPath;
          videoResult.path = this.isNullOrUndefined(tempFilePath) ? null : filePrefix + tempFilePath;
          videoResult.exif = imageInfo.exif;
          videoResult.size = imageInfo.size;
          videoResult.width = imageInfo.width;
          videoResult.height = imageInfo.height;
          videoResult.filename = imageInfo.filename;
          videoResult.mime = imageInfo.mime;
          videoResult.localIdentifier = imageInfo.localIdentifier;
          videoResult.creationDate = imageInfo.creationDate + '';
          videoResult.modificationDate = imageInfo.modificationDate + '';
          videoResult.duration = Number(imageInfo.duration);
          res(videoResult);
        })
      }
    } catch (error) {
      let err = error as BusinessError;
      rej(JSON.stringify(err));
    }
    })
  };

  imageToBase64(filePath: string): string {
    Logger.info(TAG, 'into imageToBase64 filePath: ' + filePath);
    let base64Data;
    try {
      let file = fs.openSync(filePath, fs.OpenMode.READ_ONLY);
      let stat = fs.lstatSync(filePath);
      Logger.info(TAG, 'into imageToBase64 stat.size = ' + stat.size);
      let buf = new ArrayBuffer(stat.size);
      fs.readSync(file.fd, buf);
      let unit8Array: Uint8Array = new Uint8Array(buf);
      let base64Helper = new util.Base64Helper();
      base64Data = base64Helper.encodeToStringSync(unit8Array, util.Type.BASIC);
      fs.closeSync(file);
      Logger.info(TAG, 'into imageToBase64 base64Data: ' + base64Data);
    } catch (err) {
      Logger.error(TAG, 'into imageToBase64 err: ' + JSON.stringify(err));
    }
    return base64Data;
  }

  isImage(filePath : string) : boolean{
    Logger.info(TAG, 'into isImage fileName = ' + filePath);
    const imageExtensionsRegex = /\.(jpg|jpeg|png|gif|bmp|webp)$/i;
    return imageExtensionsRegex.test(filePath);
  }

  async compressPictures(quality: number, forceJpg : boolean, sourceURL: Array<string>) : Promise<Array<string>> {
    Logger.info(TAG, 'into compressPictures sourceURL: ' + sourceURL + ' quality: ' + quality + ' forceJpg: ' + forceJpg);
    let imageType : string = 'jpeg';
    let resultImages : Array<string> = new Array<string>();
    for (let srcPath of sourceURL) {
      if (this.isImage(srcPath)) {
        Logger.info(TAG, 'into compressPictures img srcPath :' + srcPath);
        let i = srcPath.lastIndexOf('.');
        if (!forceJpg && i != -1) {
          imageType = srcPath.substring(i + 1);
        }
        Logger.info(TAG, 'into compressPictures imageType: ' + imageType);
        let files = fs.openSync(srcPath, fs.OpenMode.READ_ONLY)
        let imageISs = image.createImageSource(files.fd);
        let imagePMs = await imageISs.createPixelMap() ;
        let imagePackerApi = image.createImagePacker();
        let options: image.PackingOption = {
          format: 'image/jpeg',
          quality: quality,
        };
        try{
          let packerData = await imagePackerApi.packing(imagePMs, options);
          Logger.info(TAG, 'into compressPictures data: ' + JSON.stringify(packerData));
          let dstPath = this.ctx.uiAbilityContext.tempDir + '/rn_image_crop_picker_lib_temp_' + util.generateRandomUUID(true) + '.' + imageType;
          let newFile = fs.openSync(dstPath, fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE);
          Logger.info(TAG, 'into compressPictures newFile id: ' + newFile.fd);
          const number = fs.writeSync(newFile.fd, packerData);
          Logger.info(TAG, 'into compressPictures write data to file succeed size: ' + number);
          resultImages.push(dstPath);
          fs.closeSync(files);
        } catch (err) {
          Logger.error(TAG, 'into compressPictures write data to file failed err: ' + JSON.stringify(err));
        }
      } else {
        Logger.info(TAG, 'into compressPictures video srcPath :' + srcPath);
        resultImages.push(srcPath);
      }
    }
    return resultImages;
  }

  async getListFile(): Promise<FilePathResult> {
    let filePathResult : FilePathResult = { result: []};
    let filesDir = this.ctx.uiAbilityContext.tempDir;
    class ListFileOption {
      public recursion: boolean = false;
      public listNum: number = 0;
      public filter: Filter = {};
    }
    let option = new ListFileOption();
    option.filter.suffix = ['.jpg'];
    option.filter.displayName = ['*'];
    option.filter.fileSizeOver = 0;
    option.filter.lastModifiedAfter = new Date(0).getTime();
    Logger.info(TAG, "into getListFile filesDir = " + filesDir);
    let files = fs.listFileSync(filesDir, option);
    for (let i = 0; i < files.length; i++) {
      let listFilePath: FilePath = {}
      listFilePath.path = files[i];
      filePathResult.result.push(listFilePath);
    }
    Logger.info(TAG, "into getListFile arrayList = " + JSON.stringify(filePathResult));
    return filePathResult;
  }

  async cleanSingle(path: string): Promise<void> {
    Logger.info(TAG, "into cleanSingle path = "+path);
    let filePath = path.trim();
    fs.access(filePath, (err) => {
      if (err) {
        Logger.info(TAG, 'cleanSingle access error data =' + err.data);
      } else {
        fs.unlink(filePath).then(() => {
          Logger.info(TAG, "cleanSingle file succeed");
        }).catch((err: BusinessError) => {
          Logger.error(TAG, "cleanSingle err : " + JSON.stringify(err));
        });
      }
    });
  }

  async clean(): Promise<void> {
    let dirPath = this.ctx.uiAbilityContext.tempDir;
    fs.rmdir(dirPath).then(() => {
      Logger.info(TAG, "clean rmdir succeed");
    }).catch((err: BusinessError) => {
      Logger.error(TAG, "clean rmdir err : " + JSON.stringify(err));
    });
  }

  async openCropper(options?: CropperOptions): Promise<Image>{
    Logger.info(TAG, 'into openCropper : ' + JSON.stringify(options));
    let result: Image = {data:null,cropRect:null,path:null,size:0,width:0,height:0,mime:'',exif:null,localIdentifier:'',sourceURL:'',filename:'',creationDate:null,modificationDate:null};
    const permissions: Array<Permissions> = [
      'ohos.permission.READ_MEDIA',
      'ohos.permission.WRITE_MEDIA',
      'ohos.permission.MEDIA_LOCATION',
    ];
    return new Promise((res, rej) => {
      atManager.requestPermissionsFromUser(this.ctx.uiAbilityContext, permissions, (err, data) => {
        if (err) {
          Logger.error(TAG,'Failed in requestPermission')
          rej('Failed in requestPermission')
        } else {
          Logger.info(TAG,'Succeeded in requestPermission')
          let title : string = this.isNullOrUndefined(options?.cropperToolbarTitle) ? '编辑图片' : options?.cropperToolbarTitle;
          let chooseText : string = this.isNullOrUndefined(options?.cropperChooseText) ? 'Choose' : options?.cropperChooseText;
          let chooseTextColor : string = this.isNullOrUndefined(options?.cropperChooseColor) ? '#FFCC00' : options?.cropperChooseColor;
          let cancelText : string = this.isNullOrUndefined(options?.cropperCancelText) ? 'Cancel' : options?.cropperCancelText;
          let cancelTextColor : string = this.isNullOrUndefined(options?.cropperCancelColor) ? '#0000FF' : options?.cropperCancelColor;
          let showCropGuidelines : boolean = this.isNullOrUndefined(options?.showCropGuidelines) ? true : options?.showCropGuidelines;
          let showCropFrame : boolean = this.isNullOrUndefined(options?.showCropFrame) ? true : options?.showCropFrame;
          let cropperRotate : string = options?.cropperRotateButtonsHidden + '';
          AppStorage.setOrCreate('title', title);
          AppStorage.setOrCreate('chooseText', chooseText);
          AppStorage.setOrCreate('chooseTextColor', chooseTextColor);
          AppStorage.setOrCreate('cancelText', cancelText);
          AppStorage.setOrCreate('cancelTextColor', cancelTextColor);
          AppStorage.setOrCreate('cropperRotate', cropperRotate);
          AppStorage.setOrCreate('showCropGuidelines', showCropGuidelines);
          AppStorage.setOrCreate('showCropFrame', showCropFrame);
          let uri = options.path;
          AppStorage.setOrCreate('filePath', uri);
          let bundleName = this.ctx.uiAbilityContext.abilityInfo.bundleName;
          try {
            let want: Want = {
              "bundleName": bundleName,
              "abilityName": "ImageEditAbility",
            }
            this.ctx.uiAbilityContext.startAbilityForResult(want, (error, data) => {
              let imgPath = AppStorage.get('cropImagePath') as string;
              AppStorage.setOrCreate('cropImagePath', '')
              Logger.info(TAG, 'into openCropper startAbility suc : ' + imgPath);
              this.getFileInfo(options?.includeBase64, imgPath, null).then((imageInfo) =>{
                result.path = filePrefix + imgPath;
                result.exif = imageInfo.exif;
                result.sourceURL = uri;
                result.data = imageInfo.data;
                result.size = imageInfo.size;
                result.width = imageInfo.width;
                result.height = imageInfo.height;
                result.filename = imageInfo.filename;
                result.mime = imageInfo.mime;
                result.localIdentifier = imageInfo.localIdentifier;
                result.cropRect = imageInfo.cropRect;
                result.creationDate = imageInfo.creationDate + '';
                result.modificationDate = imageInfo.modificationDate + '';
                res(result);
              })
            } );
          } catch (err) {
            Logger.info(TAG, 'into openCropper startAbility err: ' + JSON.stringify(err));
            rej(result)
          }
        }
      });
    })
  }

  async getFileInfo(includeBase64: boolean, filePath: string, tempFilePath: string) : Promise<VideoImageInfo>{
    let videoImageInfo : VideoImageInfo = {duration: null};
    let imageType;
    let i = this.isNullOrUndefined(tempFilePath) ? filePath.lastIndexOf('/') : tempFilePath.lastIndexOf('/')
    let fileName = this.isNullOrUndefined(tempFilePath) ? filePath.substring(i + 1) : tempFilePath.substring(i + 1)
    i = filePath.lastIndexOf('.')
    if (i != -1) {
      imageType = filePath.substring(i + 1)
    }
    videoImageInfo.path = this.isNullOrUndefined(tempFilePath) ? filePrefix + filePath : tempFilePath + filePath;
    videoImageInfo.filename = fileName;
    videoImageInfo.mime = 'image/' + imageType;

    let file = fs.openSync(filePath, fs.OpenMode.READ_ONLY)
    let stat = fs.statSync(file.fd);
    let length = stat.size;
    videoImageInfo.size = length;
    videoImageInfo.creationDate = stat.ctime;
    videoImageInfo.modificationDate = stat.mtime;
    if (this.isImage(filePath)) {
      let imageIS = image.createImageSource(file.fd)
      let imagePM = await imageIS.createPixelMap()
      let imgInfo = await imagePM.getImageInfo();
      videoImageInfo.data = includeBase64 ? this.imageToBase64(filePath) : null;
      videoImageInfo.height = imgInfo.size.height;
      videoImageInfo.width = imgInfo.size.width;
      imagePM.release().then(() => {
        imagePM = undefined;
      })
      imageIS.release().then(() => {
        imageIS = undefined;
      })
    } else {
      videoImageInfo.mime = 'video/' + imageType;
      let url = 'fd://' + file.fd;
      Logger.info(TAG, 'start avPlayer url:' + url);
      avMetadataExtractor = await media.createAVMetadataExtractor();
      avMetadataExtractor.fdSrc = { fd: file.fd, offset: 0, length: length };
      try {
        const res = await avMetadataExtractor.fetchMetadata();
        videoImageInfo.duration = res.duration;
        videoImageInfo.width = Number(res.videoWidth);
        videoImageInfo.height = Number(res.videoHeight);
      } catch (error) {
        Logger.error(TAG, 'get video info suc error:' + error);
      }
    }
    fs.close(file.fd);
    return videoImageInfo;
  }
}
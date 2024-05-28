














import fs from '@ohos.file.fs';
import image from '@ohos.multimedia.image';
import Logger from '../Logger';
import { Constants } from '../utils/Constants'
import util from '@ohos.util';

const TAG: string = 'imageEdit_Encode';


export async function encode(component: Object, pixelMap: ESObject) : Promise<string> {
  let imgPath: string = '';
  const newPixelMap: ESObject = pixelMap;
  const imagePackerApi = image.createImagePacker();
  const packOptions: image.PackingOption = {
    format: Constants.ENCODE_FORMAT,
    quality: Constants.ENCODE_QUALITY
  }
  let packerData = await imagePackerApi.packing(newPixelMap, packOptions);
  Logger.info(TAG, 'into compressPictures data: ' + JSON.stringify(packerData));
  const context = getContext(component);
  imgPath = context.tempDir + '/rn_image_crop_picker_lib_temp_' + util.generateRandomUUID(true) + Constants.IMAGE_FORMAT;
  let newFile = fs.openSync(imgPath, fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE);
  Logger.info(TAG, 'into compressPictures newFile id: ' + newFile.fd);
  const number = fs.writeSync(newFile.fd, packerData);
  Logger.info(TAG, 'into compressPictures write data to file succeed size: ' + number);
  fs.closeSync(newFile.fd);
  imagePackerApi.release();
  return imgPath;
}
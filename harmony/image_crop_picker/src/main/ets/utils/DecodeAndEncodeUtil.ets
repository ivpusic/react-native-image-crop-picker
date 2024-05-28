import fs from '@ohos.file.fs';
import image from '@ohos.multimedia.image';
import { Constants } from '../utils/Constants';
import mediaLibrary from '@ohos.multimedia.mediaLibrary';


const TAG: string = 'imageCrop_Decode_Encode';

/**
 * Async get resource fd.
 *
 * @return file fd.
 */
async function getResourceFd(pageContent: Object) {
  const context = getContext(pageContent);
  const resourceMgr = context.resourceManager;
  let imageBuffer = await resourceMgr.getMediaContent($r('app.media.ic_low'));
  let filePath = context.cacheDir + '/' + 'low.jpg';
  let file = fs.openSync(filePath, fs.OpenMode.READ_WRITE | fs.OpenMode.CREATE);
  fs.writeSync(file.fd, imageBuffer.buffer);
  return file.fd;
}

/**
 * Async create pixel map.
 *
 * @return pixelMa.
 */
export async function getPixelMap(uri: string){

  const file = fs.openSync(uri, fs.OpenMode.READ_ONLY);
  console.log('imageEdit_Decode file:', JSON.stringify(file))
  const fd = file?.fd;
  console.log('imageEdit_Decode fd:', JSON.stringify(fd))
  // path为已获得的沙箱路径
  const imageSource = image.createImageSource(fd);
  console.log('imageEdit_Decode imageSource:', JSON.stringify(imageSource))
  const pixelMap = await imageSource.createPixelMap({
    editable: true
  });
  return pixelMap
}

export async function encode(component: Object, pixelMap: PixelMap) {
  const newPixelMap = pixelMap;
  // Packing image.
  const imagePackerApi = image.createImagePacker();
  const packOptions: image.PackingOption = {
    format: Constants.ENCODE_FORMAT,
    quality: Constants.ENCODE_QUALITY
  }
  const imageData = await imagePackerApi.packing(newPixelMap, packOptions);
  // Get album's path.
  const context = getContext(component);
  console.log(TAG,'context---',JSON.stringify(context))
  const media = mediaLibrary.getMediaLibrary(context);
  console.log(TAG,'media---',JSON.stringify(media))
  const publicPath = await media.getPublicDirectory(mediaLibrary.DirectoryType.DIR_IMAGE);
  const currentTime = new Date().getTime();
  // Create image asset.
  const imageAssetInfo = await media.createAsset(
    mediaLibrary.MediaType.IMAGE,
    `${Constants.IMAGE_PREFIX}_${currentTime}${Constants.IMAGE_FORMAT}`,
    publicPath
  );
  const imageFd = await imageAssetInfo.open(Constants.ENCODE_FILE_PERMISSION);
  await fs.write(imageFd, imageData);
  // Image resource release.
  await imageAssetInfo.close(imageFd);
  imagePackerApi.release();
  await media.release();
}

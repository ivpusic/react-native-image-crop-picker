import React, { useState } from 'react';
import { Alert, Image, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import ImageCropPicker, { ImageOrVideo, Image as CropImage, Video as CropVideo } from 'react-native-image-crop-picker';
import Video from 'react-native-video';

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    button: {
        backgroundColor: 'blue',
        marginBottom: 10,
    },
    text: {
        color: 'white',
        fontSize: 20,
        textAlign: 'center',
    },
});

const App: React.FC = () => {
    const [image, setImage] = useState<any | null>(null);
    const [images, setImages] = useState<any[] | null>(null);

    const pickSingleWithCamera = (cropping: boolean, mediaType: 'photo' | 'video' = 'photo') => {
        ImageCropPicker.openCamera({
            cropping,
            width: 500,
            height: 500,
            includeExif: true,
            mediaType,
        })
            .then((image) => {
                console.log('received image', image);
                setImage({
                    uri: image.path,
                    width: image.width,
                    height: image.height,
                    mime: image.mime,
                });
                setImages(null);
            })
            .catch((e) => console.log(e));
    };

    const pickSingleBase64 = (cropit: boolean) => {
        ImageCropPicker.openPicker({
            width: 300,
            height: 300,
            cropping: cropit,
            includeBase64: true,
            includeExif: true,
        })
            .then((result) => {
                console.log('received base64 image');

                if (isImage(result)) {
                    setImage({
                        uri: `data:${result.mime};base64,` + result.data,
                        width: result.width,
                        height: result.height,
                    });
                    setImages(null);
                }
            })
            .catch((e) => console.log(e));
    };

    function isImage(value: ImageOrVideo): value is CropImage {
        return (value as CropImage).cropRect !== undefined;
    }

    const cleanupImages = () => {
        ImageCropPicker.clean()
            .then(() => {
                console.log('removed tmp images from tmp directory');
            })
            .catch((e) => console.log(e));
    };

    const cleanupSingleImage = () => {
        const currentImage = image || (images && images.length ? images[0] : null);
        if (currentImage) {
            ImageCropPicker.cleanSingle(currentImage.uri)
                .then(() => {
                    console.log(`removed tmp image ${currentImage.uri} from tmp directory`);
                })
                .catch((e) => console.log(e));
        }
    };

    const cropLast = () => {
        if (!image) {
            return Alert.alert('No image', 'Before open cropping only, please select image');
        }

        ImageCropPicker.openCropper({
            path: image.uri,
            mediaType: 'photo',
            width: 200,
            height: 200,
        })
            .then((croppedImage) => {
                console.log('received cropped image', croppedImage);
                setImage({
                    uri: croppedImage.path,
                    width: croppedImage.width,
                    height: croppedImage.height,
                    mime: croppedImage.mime,
                });
                setImages(null);
            })
            .catch((e) => {
                console.log(e);
                Alert.alert(e.message ? e.message : e);
            });
    };

    const pickSingle = (cropit: boolean, circular: boolean = false, mediaType: 'photo' | 'video' = 'photo') => {
        ImageCropPicker.openPicker({
            width: 500,
            height: 500,
            cropping: cropit,
            cropperCircleOverlay: circular,
            sortOrder: 'none',
            compressImageMaxWidth: 1000,
            compressImageMaxHeight: 1000,
            compressImageQuality: 1,
            compressVideoPreset: 'MediumQuality',
            includeExif: true,
            mediaType,
        })
            .then((image) => {
                console.log('received image', image);
                setImage({
                    uri: image.path,
                    width: image.width,
                    height: image.height,
                    mime: image.mime,
                });
                setImages(null);
            })
            .catch((e) => {
                console.log(e);
                Alert.alert(e.message ? e.message : e);
            });
    };

    const pickMultiple = () => {
        ImageCropPicker.openPicker({
            multiple: true,
            waitAnimationEnd: false,
            sortOrder: 'desc',
            includeExif: true,
            forceJpg: true,
        })
            .then((images) => {
                setImages(
                    images.map((i) => {
                        console.log('received image', i);
                        return {
                            uri: i.path,
                            width: i.width,
                            height: i.height,
                            mime: i.mime,
                        };
                    }),
                );
                setImage(null);
            })
            .catch((e) => console.log(e));
    };

    const renderVideo = (video: CropVideo) => {
        console.log('rendering video');
        return (
            <View style={{ height: 300, width: 300 }}>
                <Video
                    source={{ uri: video.path, type: video.mime }}
                    style={{ position: 'absolute', top: 0, left: 0, bottom: 0, right: 0 }}
                    rate={1}
                    paused={false}
                    volume={1}
                    muted={false}
                    resizeMode={'cover'}
                    onError={(e) => console.log(e)}
                    onLoad={(load) => console.log(load)}
                    repeat={true}
                />
            </View>
        );
    };

    const renderImage = (image: CropImage) => {
        return <Image style={{ width: 300, height: 300, resizeMode: 'contain' }} source={image} />;
    };

    const renderAsset = (asset: any) => {
        if (asset.mime && asset.mime.toLowerCase().indexOf('video/') !== -1) {
            return renderVideo(asset);
        }
        return renderImage(asset);
    };

    return (
        <View style={styles.container}>
            <ScrollView>
                {image ? renderAsset(image) : null}
                {images ? images.map((i) => <View key={i.uri}>{renderAsset(i)}</View>) : null}
            </ScrollView>
            <TouchableOpacity onPress={() => pickSingleWithCamera(false)} style={styles.button}>
                <Text style={styles.text}>Select Single Image With Camera</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingleWithCamera(false, 'video')} style={styles.button}>
                <Text style={styles.text}>Select Single Video With Camera</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingleWithCamera(true)} style={styles.button}>
                <Text style={styles.text}>Select Single With Camera With Cropping</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingle(false)} style={styles.button}>
                <Text style={styles.text}>Select Single</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => cropLast()} style={styles.button}>
                <Text style={styles.text}>Crop Last Selected Image</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingleBase64(false)} style={styles.button}>
                <Text style={styles.text}>Select Single Returning Base64</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingle(true)} style={styles.button}>
                <Text style={styles.text}>Select Single With Cropping</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => pickSingle(true, true)} style={styles.button}>
                <Text style={styles.text}>Select Single With Circular Cropping</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={pickMultiple} style={styles.button}>
                <Text style={styles.text}>Select Multiple</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={cleanupImages} style={styles.button}>
                <Text style={styles.text}>Cleanup All Images</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={cleanupSingleImage} style={styles.button}>
                <Text style={styles.text}>Cleanup Single Image</Text>
            </TouchableOpacity>
        </View>
    );
};

export default App;

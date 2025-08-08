/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {Component} from 'react';
import {
  Alert,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import ImagePicker from 'react-native-image-crop-picker';
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

interface ImageState {
  uri: string;
  width: number;
  height: number;
  mime: string;
}

interface AppState {
  image: ImageState | null;
  images: ImageState[] | null;
}

export default class App extends Component<{}, AppState> {
  constructor(props: {}) {
    super(props);
    this.state = {
      image: null,
      images: null,
    };
  }

  pickSingleWithCamera(
    cropping: boolean,
    mediaType: 'photo' | 'video' = 'photo',
  ) {
    ImagePicker.openCamera({
      cropping: cropping,
      width: 500,
      height: 500,
      includeExif: true,
      mediaType,
    })
      .then(image => {
        this.setState({
          image: {
            uri: image.path,
            width: image.width,
            height: image.height,
            mime: image.mime,
          },
          images: null,
        });
      })
      .catch(e => Alert.alert('Error', e.message));
  }

  pickSingleBase64(cropit: boolean) {
    ImagePicker.openPicker({
      width: 300,
      height: 300,
      cropping: cropit,
      includeBase64: true,
      includeExif: true,
    })
      .then(image => {
        console.log('received base64 image');
        this.setState({
          image: {
            uri: `data:${image.mime};base64,` + (image as any).data,
            width: image.width,
            height: image.height,
            mime: image.mime,
          },
          images: null,
        });
      })
      .catch(e => Alert.alert('Error', e.message));
  }

  cleanupImages() {
    ImagePicker.clean()
      .then(() => {
        console.log('removed tmp images from tmp directory');
      })
      .catch(e => {
        Alert.alert('Error', e.message);
      });
  }

  cleanupSingleImage() {
    let image =
      this.state.image ||
      (this.state.images && this.state.images.length
        ? this.state.images[0]
        : null);
    console.log('will cleanup image', image);

    ImagePicker.cleanSingle(image?.uri || '')
      .then(() => {
        console.log(`removed tmp image ${image?.uri} from tmp directory`);
      })
      .catch(e => {
        Alert.alert('Error', e.message);
      });
  }

  cropLast() {
    if (!this.state.image) {
      return Alert.alert(
        'No image',
        'Before open cropping only, please select image',
      );
    }

    ImagePicker.openCropper({
      path: this.state.image.uri,
      width: 200,
      height: 200,
      mediaType: 'photo',
    })
      .then(image => {
        console.log('received cropped image', image);
        this.setState({
          image: {
            uri: image.path,
            width: image.width,
            height: image.height,
            mime: image.mime,
          },
          images: null,
        });
      })
      .catch(e => {
        console.log(e);
        Alert.alert('Error', e.message);
      });
  }

  pickSingle(cropit: boolean, circular: boolean = false) {
    ImagePicker.openPicker({
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
      cropperStatusBarLight: true,
      cropperNavigationBarLight: false,
      cropperToolbarColor: 'white',
      cropperActiveWidgetColor: 'white',
      cropperToolbarWidgetColor: '#3498DB',
    })
      .then(image => {
        console.log('received image', image);
        this.setState({
          image: {
            uri: image.path,
            width: image.width,
            height: image.height,
            mime: image.mime,
          },
          images: null,
        });
      })
      .catch(e => {
        console.log(e);
        Alert.alert('Error', e.message);
      });
  }

  pickMultiple() {
    ImagePicker.openPicker({
      multiple: true,
      waitAnimationEnd: false,
      sortOrder: 'desc',
      includeExif: true,
      forceJpg: true,
    })
      .then(images => {
        this.setState({
          image: null,
          images: images.map(i => {
            console.log('received image', i);
            return {
              uri: i.path,
              width: i.width,
              height: i.height,
              mime: i.mime,
            };
          }),
        });
      })
      .catch(e => Alert.alert('Error', e.message));
  }

  scaledHeight(oldW: number, oldH: number, newW: number) {
    return (oldH / oldW) * newW;
  }

  renderVideo(video: ImageState) {
    console.log('rendering video');
    return (
      <View style={{height: 300, width: 300}}>
        <Video
          source={{uri: video.uri, type: video.mime}}
          style={{position: 'absolute', top: 0, left: 0, bottom: 0, right: 0}}
          rate={1}
          paused={false}
          volume={1}
          muted={false}
          resizeMode={'cover'}
          onError={e => console.log(e)}
          onLoad={load => console.log(load)}
          repeat={true}
        />
      </View>
    );
  }

  renderImage(image: ImageState) {
    return (
      <Image
        style={{width: 300, height: 300, resizeMode: 'contain'}}
        source={image}
      />
    );
  }

  renderAsset(image: ImageState) {
    if (image.mime && image.mime.toLowerCase().indexOf('video/') !== -1) {
      return this.renderVideo(image);
    }

    return this.renderImage(image);
  }

  render() {
    return (
      <View style={styles.container}>
        <ScrollView>
          {this.state.image ? this.renderAsset(this.state.image) : null}
          {this.state.images
            ? this.state.images.map(i => (
                <View key={i.uri}>{this.renderAsset(i)}</View>
              ))
            : null}
        </ScrollView>

        <TouchableOpacity
          onPress={() => this.pickSingleWithCamera(false)}
          style={styles.button}>
          <Text style={styles.text}>Select Single Image With Camera</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingleWithCamera(false, 'video')}
          style={styles.button}>
          <Text style={styles.text}>Select Single Video With Camera</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingleWithCamera(true)}
          style={styles.button}>
          <Text style={styles.text}>
            Select Single With Camera With Cropping
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingle(false)}
          style={styles.button}>
          <Text style={styles.text}>Select Single</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => this.cropLast()} style={styles.button}>
          <Text style={styles.text}>Crop Last Selected Image</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingleBase64(false)}
          style={styles.button}>
          <Text style={styles.text}>Select Single Returning Base64</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingle(true)}
          style={styles.button}>
          <Text style={styles.text}>Select Single With Cropping</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => this.pickSingle(true, true)}
          style={styles.button}>
          <Text style={styles.text}>Select Single With Circular Cropping</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={this.pickMultiple.bind(this)}
          style={styles.button}>
          <Text style={styles.text}>Select Multiple</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={this.cleanupImages.bind(this)}
          style={styles.button}>
          <Text style={styles.text}>Cleanup All Images</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={this.cleanupSingleImage.bind(this)}
          style={styles.button}>
          <Text style={styles.text}>Cleanup Single Image</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

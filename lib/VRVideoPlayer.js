import React, { Component } from 'react';
import {
  View, Image,
  StyleSheet, NativeModules,
  requireNativeComponent, findNodeHandle,
} from 'react-native';
import PropTypes from 'prop-types';
import VRFilterType from './VRFilterType.js';

class VRVideoPlayer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showPoster: !!props.poster
    };
  }

  setNativeProps(nativeProps) {
    this._root.setNativeProps(nativeProps);
  }


  render() {
    const nativeProps = Object.assign({}, this.props)
    Object.assign(nativeProps, {
      style: [styles.container, nativeProps.style],
      onVideoLoadStart: this._onVideoLoadStart,
      onVideoLoad: this._onVideoLoad,
      onVideoError: this._onVideoError,
      onVideoEnd: this._onVideoEnd,
      onVideoSeek: this._onVideoSeek,
      onVideoProgress: this._onVideoProgress,
      onPlaybackStalled: this._onPlaybackStalled,
      onPlaybackRotation: this._onPlaybackRotation,
      onPlaybackResume: this._onPlaybackResume,
      onReadyForDisplay: this._onReadyForDisplay,
    })

    const posterStyle = {
      ...StyleSheet.absoluteFillObject,
      resizeMode: this.props.posterResizeMode || 'contain',
    };

    return (
      <View style={nativeProps.style}>
        <VRPlayer
          ref={this._assignRoot}
          {...nativeProps}
          style={StyleSheet.absoluteFill}
        />
        {
          this.state.showPoster && (
            <Image style={posterStyle} source={{ uri: this.props.poster }} />
          )
        }
      </View>
    );
  }
  _assignRoot = (component) => {
    this._root = component;
  };

  _hidePoster = () => {
    if (this.state.showPoster) {
      this.setState({ showPoster: false });
    }
  }

  save = async (options) => {
    return await NativeModules.VideoManager.save(options, findNodeHandle(this._root));
  }

  _onVideoLoadStart = event => {
    this.props.onLoadStart && this.props.onLoadStart(event.nativeEvent)
  }

  _onVideoLoad = (event) => {
    this.props.onLoad && this.props.onLoad(event.nativeEvent)
  }
  _onVideoError = (event) => {
    this.props.onError && this.props.onError(event.nativeEvent)
  }
  _onVideoEnd = (event) => {
    this.props.onEnd && this.props.onEnd(event.nativeEvent)
  }
  _onVideoSeek = (event) => {
    this.props.onSeek && this.props.onSeek(event.nativeEvent)
  }
  _onVideoProgress = (event) => {
    this.props.onProgress && this.props.onProgress(event.nativeEvent)
  }
  _onPlaybackStalled = (event) => {
    this.props.onPlaybackStalled && this.props.onPlaybackStalled(event.nativeEvent)
  }
  _onPlaybackRotation = (event) => {
    this.props.onPlaybackRotation && this.props.onPlaybackRotation(event.nativeEvent)
  }
  _onPlaybackResume = (event) => {
    this.props.onPlaybackResume && this.props.onPlaybackResume(event.nativeEvent)
  }
  _onReadyForDisplay = (event) => {
    this._hidePoster();
    this.props.onReadyForDisplay && this.props.onReadyForDisplay(event.nativeEvent)
  }
}

const styles = StyleSheet.create({
  // container: {
  //   flex: 1,
  // },
  container: {
    overflow: 'hidden',
  },
});
export default VRVideoPlayer;

VRVideoPlayer.propTypes = {
  switchInteractiveMode: PropTypes.oneOf([
    VRFilterType.INTERACTIVE_MODE_CARDBORAD_MOTION,
    VRFilterType.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH,
    VRFilterType.INTERACTIVE_MODE_TOUCH,
    VRFilterType.INTERACTIVE_MODE_MOTION,
    VRFilterType.INTERACTIVE_MODE_MOTION_WITH_TOUCH
  ]),
  switchDisplayMode: PropTypes.oneOf([
    VRFilterType.DISPLAY_MODE_GLASS,
    VRFilterType.DISPLAY_MODE_NORMAL,
  ]),
  switchProjectionMode: PropTypes.oneOf([
    VRFilterType.PROJECTION_MODE_SPHERE,
    VRFilterType.PROJECTION_MODE_CUBE
  ]),
  setAntiDistortionEnabled: PropTypes.bool,
  seek: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.object
  ]),
  url: PropTypes.string,
  paused: PropTypes.bool,
  progressUpdateInterval: PropTypes.number,

  poster: PropTypes.string,
  posterResizeMode: Image.propTypes.resizeMode,

  onVideoLoadStart: PropTypes.func,
  onVideoLoad: PropTypes.func,
  onVideoError: PropTypes.func,
  onVideoEnd: PropTypes.func,
  onVideoSeek: PropTypes.func,
  onVideoProgress: PropTypes.func,

  onLoadStart: PropTypes.func,
  onLoad: PropTypes.func,
  onError: PropTypes.func,
  onEnd: PropTypes.func,
  onSeek: PropTypes.func,
  onProgress: PropTypes.func,
  onPlaybackStalled: PropTypes.func,
  onPlaybackRotation: PropTypes.func,
  onPlaybackResume: PropTypes.func,
  onReadyForDisplay: PropTypes.func,
}

const VRPlayer = requireNativeComponent('RNVRVideoPlayer', VRVideoPlayer);


# react-native-vrplayer
## Table of Contents

* [Installation](#installation)
* [Platform](#platform)
* [Usage](#usage)
* [Updating](#updating)
* [Supports](#supports)

## Installation

Using npm:

```shell
npm install react-native-vrplayer -save
```

link:

### 1、Android：

Run `react-native link react-native-vrplayer` to link the react-native-vrplayer library.

Or if you have trouble, make the following additions to the given files manually:

**android/settings.gradle**

```gradle
include ':react-native-vrplayer'
project(':react-native-vrplayer').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-vrplayer/android')
```

**android/app/build.gradle**

```gradle
dependencies {
   ...
    implementation project(':react-native-vrplayer')
    implementation "com.android.support:appcompat-v7:${rootProject.ext.supportLibVersion}"

}
```

**MainApplication.java**

On top, where imports are:

```java
import com.zjiaxin.vrvideo.RNVRVideoPackage;
```

Add the `ReactVideoPackage` class to your list of exported packages.

```java
@Override
protected List<ReactPackage> getPackages() {
    return Arrays.asList(
            new MainReactPackage(),
            new RNVRVideoPackage()
    );
}
```
### 2、iOS:
 ...

## Platform
1. Android (support)
2. iOS (nonsupport now)

## Usage
```javascript
import { VRVideoPlayer } from 'react-native-vrplayer';
class VRVideoPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
        };
    }

    render() {
        return (
            <View style={{ flex: 1 }}>
                <VRVideoPlayer
                    url={'xxx.mp4'}
                    paused={false}
                    style={{ flex: 1 }}
                />
            </View>
        );
    }
}

export default VRVideoPage;

```
## Updating
### 1.0.0 
### 1.0.1    
## Supports
[MD360Player4Android](https://github.com/ashqal/MD360Player4Android),
[react-native-video](https://github.com/react-native-community/react-native-video)




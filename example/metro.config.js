const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');

/**
 * react-native-svg-transformer 설정을 따름
 * https://github.com/kristerkari/react-native-svg-transformer/blob/2ca947ec3fb43d827183d5d877b2ae11f1d7e127/README.md?plain=1#L74-L104
 *
 * -> 기존 프로젝트 세팅대로 `inlineRequires: true` 만 추가함.
 */
const defaultConfig = getDefaultConfig(__dirname);
const { assetExts, sourceExts } = defaultConfig.resolver;

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
    transformer: {
        inlineRequires: true,
        babelTransformerPath: require.resolve('react-native-svg-transformer'),
    },
    resolver: {
        assetExts: assetExts.filter((ext) => ext !== 'svg'),
        sourceExts: [...sourceExts, 'svg'],
    },
};

module.exports = mergeConfig(defaultConfig, config);

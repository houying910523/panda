var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var cssExtractor = new ExtractTextPlugin("css/styles.css");

module.exports = {
    entry: {
        app: './src/index.js',
        vendors: ['vue', 'vue-router', 'jquery', 'bootstrap']
    },
    output: {
        path: path.join(__dirname, '/dist/'),
        filename: '[name].[hash].js',
        publicPath: '/'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: 'babel-loader',
                include: path.join(__dirname, '/src/'),
                exclude: /node_modules/,
                query: {
                    presets: ["es2015"]
                }
            },
            {
                test: /\.css$/,
                //loader: cssExtractor.extract('style-loader', 'css-loader')
                loader: 'style-loader!css-loader'
            },
            {
                test: /\.(png|jpg|gif)$/,
                loader: 'url-loader?limit=25&name=img/[name].[ext]'   // limit 是转换base64的文件大小的阀值8兆
            },
            {
                test: /\.(|woff|woff2|ttf|eot|svg)$/,
                loader: 'url-loader?limit=25&name=font/[name].[ext]'
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                include: path.join(__dirname, '/src/')
            }
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: 'index.html',
            filename: 'index.html',
            inject: 'body'/*,
            chunks: ['app', 'vendors'],
            minify: {
                removeComments: true,
                collapseWhitespace: true
            }*/
        }),
        cssExtractor,
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendors',
            filename: '[name].[hash].js',
            chunks: ['app', 'vendors']  // extract commonChunk from index & common
        }),
        new webpack.ProvidePlugin({
            "$": "jquery",
            "jQuery": "jquery"
            //"window.jQuery": "jquery"
        })
    ],
    resolve: {
        alias: {
            'vue': 'vue/dist/vue.js'
        }
    }
};

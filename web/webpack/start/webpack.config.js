const path = require('path')
const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const uglifyJsPlugin = require('uglifyjs-webpack-plugin')

module.exports = {
    entry: './src/main.js',
    output: {
        path:  path.resolve(__dirname) + '/dist',
        filename: 'bundle.js'
    },
    module: {
        rules: [
            {
              test: /\.css$/,
              use: [ 'style-loader', 'css-loader' ]
            },
            {
                test: /\.less$/,
                use: [ 'style-loader', 'css-loader', 'less-loader' ]
            },
            {
                test: /\.(png|jpg|gif|jpeg)$/,
                use: [
                    {
                        loader: 'url-loader',
                        options: {
                            limit: 8192,
                            name: 'assets/[name].[hash:8].[ext]'
                        }
                    }
                ]
            },
        ]
    },
    resolve: {
        alias: {
            "vue$": 'vue/dist/vue.esm.js'
        }
    },
    plugins: [
        new webpack.BannerPlugin("all right by mzx"),
        new HtmlWebpackPlugin({
            template: 'index.html'
        }),
        new uglifyJsPlugin()
    ]
}
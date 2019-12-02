const path = require('path')

module.exports = {
    entry: './src/main.js',
    output: {
        path:  path.resolve(__dirname) + '/dist',
        filename: 'bundle.js',
        publicPath: 'dist/'
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
    }
}
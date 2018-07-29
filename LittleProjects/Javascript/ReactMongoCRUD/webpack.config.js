const path = require('path');

module.exports = {
    entry: path.resolve(__dirname, "public/js/client.js"),
    mode: 'development',
    output: {
        path: path.resolve(__dirname, "public/bin"),
        filename: "bundle.js"
    },
    module: {
        rules: [
            {
                test: /.jsx?$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ['env', 'react']
                }
            }
        ]
    }
};
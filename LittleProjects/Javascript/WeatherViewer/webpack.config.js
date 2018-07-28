const path = require('path');

module.exports = {
    entry: path.resolve(__dirname, "client.js"),
    mode: 'development',
    output: {
        path: path.resolve(__dirname, ""),
        filename: "bundle.js"
    },
    module: {
        rules: [
            {
                test: /.jsx?$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ['env', 'stage-0', 'react']
                }
            }
        ]
    }
};
(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(['exports', 'echarts'], factory);
    } else if (typeof exports === 'object' && typeof exports.nodeName !== 'string') {
        // CommonJS
        factory(exports, require('echarts'));
    } else {
        // Browser globals
        factory({}, root.echarts);
    }
}(this, function (exports, echarts) {
    var log = function (msg) {
        if (typeof console !== 'undefined') {
            console && console.error && console.error(msg);
        }
    };
    if (!echarts) {
        log('ECharts is not Loaded');
        return;
    }
    var colorPalette = ['#00706B','#FF6384','#FF9F40', '#FFCD56',  '#4BC0C0','#36A2EB','#9966FF', '#C9CBCF'];
    echarts.registerTheme('theme1', {
        color: colorPalette,
        backgroundColor: '#efefef',
        // backgroundColor: '#fef8ef',
        graph: {
            color: colorPalette
        }
    });
}));
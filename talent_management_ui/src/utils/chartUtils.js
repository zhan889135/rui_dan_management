// utils/chartUtils.js
import * as echarts from 'echarts';
import html2canvas from 'html2canvas';

//下载柱形图
export function downloadColumnChart(chartDom, title) {
  const chartInstance = echarts.getInstanceByDom(chartDom);
  const originalOption = chartInstance.getOption();

  // 动态添加标题并调整图表的布局
  const newOption = {
    ...originalOption,
    title: {
      text: title,
      left: 'left',
      top: 8, // 调整标题与图表的距离
      textStyle: {
        fontFamily: 'Microsoft YaHei',
        fontWeight: 'bold',
        fontSize: 16,
        color: '#2B8698',
        lineHeight: 0,
      },
    },
  };

  // 设置新的选项
  chartInstance.setOption(newOption);

  // 获取带有标题的图表图片数据
  const dataURL = chartInstance.getDataURL({
    pixelRatio: 2,
    backgroundColor: '#fff'
  });

  // 恢复原来的选项
  chartInstance.setOption(originalOption);

  // 生成下载链接并点击下载
  const link = document.createElement('a');
  link.href = dataURL;
  link.download = title + '.png';
  link.click();
}


//下载多个饼图
export function downloadMultiplePieCharts(chartDom, title) {
  const chartDoms = document.querySelectorAll(chartDom);
  const pieChartWidth = 250; // 饼图的宽度
  const pieChartHeight = 225; // 饼图的高度
  const barChartWidth = 700; // 柱状图的宽度
  const barChartHeight = 150; // 柱状图的高度

  const titleHeight = 40; // 标题的高度
  const canvasWidth = pieChartWidth * 3; // 三个饼图的总宽度
  const canvasHeight = pieChartHeight + barChartHeight + titleHeight; // 总高度包括标题

  const canvas = document.createElement('canvas');
  canvas.width = canvasWidth;
  canvas.height = canvasHeight;
  const context = canvas.getContext('2d');
  context.fillStyle = '#fff';
  context.fillRect(0, 0, canvasWidth, canvasHeight);
  // 绘制标题
  context.font = 'bold 16px Microsoft YaHei';
  context.fillStyle = '#2B8698';
  context.fillText(title, 10, 30); // 在 (10, 30) 位置绘制标题

  const promises = Array.from(chartDoms).map((chartDom, index) => {
    return new Promise((resolve) => {
      const chart = echarts.getInstanceByDom(chartDom);
      const dataURL = chart.getDataURL({
        pixelRatio: 2,
        backgroundColor: '#fff'
      });
      const img = new Image();
      img.src = dataURL;
      img.onload = () => {
        let x, y, width, height;
        if (index < 3) {
          x = index * pieChartWidth; // 饼图的x坐标
          y = titleHeight; // 饼图的y坐标
          width = pieChartWidth;
          height = pieChartHeight;
        } else {
          x = (canvasWidth - barChartWidth) / 2; // 居中的柱状图x坐标
          y = pieChartHeight + titleHeight; // 柱状图的y坐标
          width = barChartWidth;
          height = barChartHeight;
        }
        context.drawImage(img, x, y, width, height); // 绘制图表到画布上
        resolve();
      };
    });
  });

  Promise.all(promises).then(() => {
    const link = document.createElement('a');
    link.href = canvas.toDataURL('image/png');
    link.download = title + '.png';
    link.click();
  });
}

//页面截图下载
export function downloadScreenshot(element, title, excludeSelectors = []) {
  // 临时隐藏不需要的元素
  const excludedElements = excludeSelectors.map(selector => document.querySelector(selector));
  excludedElements.forEach(el => {
    if (el) el.style.visibility = 'hidden';
  });

  // 使用 html2canvas 截图页面
  html2canvas(element, {
    backgroundColor: '#fff', // 确保背景为白色
    scale: 2 // 提高分辨率
  }).then(canvas => {
    const context = canvas.getContext('2d');

    // 设置标题样式
    context.font = 'bold 16px Microsoft YaHei';
    context.fillStyle = '#2B8698';
    context.textAlign = 'left';

    // 在截图上方添加标题
    context.fillText(title, 10, 30);

    // 创建下载链接
    const link = document.createElement('a');
    link.href = canvas.toDataURL('image/png');
    link.download = null === title? '图片下载':title + '.png';
    link.click();

    // 恢复不需要的元素
    excludedElements.forEach(el => {
      if (el) el.style.visibility = '';
    });
  });
}

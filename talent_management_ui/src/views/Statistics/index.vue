<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" label-width="90px" style="margin-left: 5%">
      <el-row :gutter="10" v-if="deptLevel === 1 || deptLevel === 2">
        <el-col :span="5">
          <el-form-item label="面试点位" prop="locationId">
            <el-select v-model="queryParams.locationId" placeholder="请选择面试点位" @change="getList" clearable style="width: 100%;">
              <el-option v-for="item in locationList" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5" v-if="deptLevel === 1">
          <el-form-item label="归属供应商" prop="deptId">
            <el-select v-model="queryParams.deptId" placeholder="归属供应商" style="width: 100%" @change="getList" clearable>
              <el-option v-for="(item, index) in deptLevel2List" :key="index" :label="item.deptName" :value="item.deptId"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="面试日期">
            <el-date-picker
              v-model="queryParams.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              clearable
              style="width: 100%"
              @change="getList"
            />
          </el-form-item>
        </el-col>

        <el-col :span="6" align="right">
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="getList">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>


    <!-- 图表区域 -->
    <div class="chart-wrapper">
      <!-- 上半部分统计图 -->
      <div class="chart-card" v-loading="loading1" v-if="deptLevel === 1">
        <div class="chart-title">供应商统计</div>
        <div ref="chart1" class="chart"></div>
      </div>

      <!-- 下半部分统计图 -->
      <div class="chart-card" v-loading="loading2" v-if="deptLevel === 1 || deptLevel === 2">
        <div class="chart-title">招聘人统计</div>
        <div ref="chart2" class="chart"></div>
      </div>
    </div>

  </div>
</template>

<script>
import { allListNoDept } from "@/api/location";
import { listDept } from "@/api/system/dept";
import { deptBillingCount, personBillingCount } from "@/api/statistics";
import * as echarts from "echarts";

export default {
  name: "Statistics",
  data() {
    return {
      // 登录人部门等级
      deptLevel: this.$store?.state?.user?.deptLevel,
      deptId: this.$store?.state?.user?.deptId,
      locationList: [], // 查询点位名称
      deptLevel2List: [], // 三级部门列表
      // 查询参数
      queryParams: {
        locationId: undefined,
        deptId: undefined,
        dateRange: [], // ["2025-10-01", "2025-10-09"]
      },

      loading1: false,
      loading2: false,
      chart1: null,
      chart2: null,
      flowInterval: null,

      chartData1:{
        x: ['青岛', '北京', '济南', '吉林', '南京', '武汉', '青岛', '北京', '济南', '吉林', '南京', '武汉'],
        y: [5, 20, 36, 10, 10, 20, 5, 20, 36, 10, 10, 20],
      },

      chartData2:{
        x: ['张三', '李四', '五', '二', '一'],
        y: [33, 20, 15, 5, 3],
      },
    };
  },
  created() {
    this.getLocationList();
    this.getDeptLevel3List();
    this.getList();
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.flowInterval) clearInterval(this.flowInterval);
    if (this.chart1) this.chart1.dispose();
    if (this.chart2) this.chart2.dispose();
  },
  methods: {
    /** 查询当前供应商,所关联的点位 */
    getLocationList() {
      allListNoDept({ deptId : this.$store.state.user.deptId }).then(response => this.locationList = response.data)
    },

    /** 查询二级部门列表 */
    getDeptLevel3List() {
      listDept({ deptLevel : 2 }).then(response => this.deptLevel2List = response.data)
    },

    /** 查询列表 */
    getList() {
      // 总部看到 两个
      if(this.deptLevel === 1){
        // 统计供应商计费人数
        this.loading1 = true;
        deptBillingCount(this.queryParams).then(response => {
          this.chartData1.x = response.data?.x || [];
          this.chartData1.y = response.data?.y || [];
          this.initChart1();
          this.loading1 = false;
        });

        // 统计招聘员工计费人数
        if(this.queryParams.deptId){
          this.loading2 = true;
          personBillingCount(this.queryParams).then(response => {
            this.chartData2.x = response.data?.x || [];
            this.chartData2.y = response.data?.y || [];
            this.initChart2();
            this.loading2 = false;
          });
        }else{
          this.chartData2.x = [];
          this.chartData2.y = [];
          this.initChart2();
        }
      }

      // 供应商看到一个
      if(this.deptLevel === 2){
        // 统计招聘员工计费人数
        this.loading2 = true;
        this.queryParams.deptId = this.deptId;
        personBillingCount(this.queryParams).then(response => {
          this.chartData2.x = response.data?.x || [];
          this.chartData2.y = response.data?.y || [];
          this.initChart2();
          this.loading2 = false;
        });
      }
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.dateRange = [];
      this.getList();
    },

    /** 查询统计图 */
    handleResize() {
      if (this.chart1) this.chart1.resize();
      if (this.chart2) this.chart2.resize(); // 👈 新增
    },

    // 上方统计图
    initChart1() {
      if (this.chart1) {
        this.chart1.dispose();
      }
      this.chart1 = echarts.init(this.$refs.chart1);
      clearInterval(this.flowInterval)

      const realData = this.chartData1.y; // 可替换为动态数据
      const cities = this.chartData1.x;

      const option = {
        tooltip: {
          trigger: 'axis',
          borderColor: '#14ECA4', // 边框颜色
          borderWidth: 1,         // 边框宽度（可选，默认 1）
        },
        grid: {
          top: '12%',      // 上边距（给 xAxis 标签和 label 留空间）
          bottom: '10%',   // 下边距（给 x 轴文字留空间）
          left: '8%',      // 左边距（给 y 轴数值留空间）
          right: '4%',     // 右边距（通常可小一点）
          containLabel: true // 自动包含 axisLabel，防止文字被裁剪
        },
        xAxis: {
          data: cities,
          axisLabel: {
            fontSize: 12,
            color: '#444',
            interval: 0, // 0 表示显示所有，1 表示隔一个显示一个，以此类推
            // 不设 interval:0，让 ECharts 自动优化
            // 但配合 formatter 实现“省略+提示”
            formatter: function(value) {
              return value.length > 6 ? value.slice(0, 6) + '...' : value;
            }
          },
          axisTick: {
            show: false // 可选：隐藏刻度线更干净
          },
          axisLine: {
            lineStyle: {
              color: '#ccc'
            }
          }
        },
        color: ['#ccc', 'red'],
        yAxis: {
          axisLabel: {
            formatter: '{value}人',
            color: '#666'
          },
          splitLine: {
            lineStyle: {
              type: 'dashed',     // 虚线
              color: '#e0e0e0',   // 颜色浅灰
              opacity: 0.6        // 更虚一点
            }
          },
          axisLine: {
            show: false // 可选：隐藏 Y 轴线更简洁
          },
          axisTick: {
            show: false
          }
        },
        series: [
          // 流光层（上层）—— 不要 label！
          {
            name: '',
            type: 'bar',
            barWidth: 30,
            barGap: '-100%',
            data: [...realData],
            tooltip: {
              show: false
            },
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#b1cae640' },
                { offset: 1, color: '#2f88e778' }
              ]),
              borderRadius: [12, 12, 0, 0]
            }
          },
          // 真实数据层（底层）—— label 放这里 ✅
          {
            name: '计费人数',
            type: 'bar',
            barWidth: 30,
            z: -1,
            data: realData,
            itemStyle: {
              color: '#14eca4',
              borderRadius: [12, 12, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              fontSize: 14,
              fontWeight: 'bold',
              color: '#333',
              formatter: '{c}人'
            }
          }
        ]
      };

      this.chart1.setOption(option);

      // 动画逻辑
      const run1 = () => {
        this.chart1.setOption({
          series: [{
            data: new Array(realData.length).fill(0)
          }]
        });
      };

      const run2 = () => {
        this.chart1.setOption({
          series: [{
            data: [...realData]
          }]
        });
      };

      // 启动循环动画
      this.flowInterval = setInterval(() => {
        run1();
        setTimeout(run2, 300); // 短暂延迟后恢复，形成流光效果
      }, 3000);
    },

    // 下方统计图
    initChart2() {
      if (this.chart2) {
        this.chart2.dispose();
      }
      this.chart2 = echarts.init(this.$refs.chart2);

      // ========== 数据 ==========
      const xData = this.chartData2.x;
      const yData = this.chartData2.y;
      const title = '人数';

      // ========== 注册自定义图形 ==========
      const CubeFront = echarts.graphic.extendShape({
        shape: { x: 0, y: 0 },
        buildPath(ctx, shape) {
          const xAxisPoint = shape.xAxisPoint;
          const offset = [25, 25]; // 👈 从 [40,40] 改为 [25,25]
          const c0 = [shape.x - offset[0], shape.y];
          const c1 = [shape.x + offset[1], shape.y];
          const c2 = [xAxisPoint[0] + offset[1], xAxisPoint[1]];
          const c3 = [xAxisPoint[0] - offset[0], xAxisPoint[1]];
          ctx.moveTo(c0[0], c0[1])
            .lineTo(c1[0], c1[1])
            .lineTo(c2[0], c2[1])
            .lineTo(c3[0], c3[1])
            .closePath();
        }
      });

      const CubeRight = echarts.graphic.extendShape({
        shape: { x: 0, y: 0 },
        buildPath(ctx, shape) {
          const xAxisPoint = shape.xAxisPoint;
          // 👈 所有 +40 / +60 改为 +25 / +35
          const c0 = [shape.x + 25, shape.y];
          const c1 = [shape.x + 35, shape.y - 16];
          const c2 = [xAxisPoint[0] + 35, xAxisPoint[1] - 16];
          const c3 = [xAxisPoint[0] + 25, xAxisPoint[1]];
          ctx.moveTo(c0[0], c0[1])
            .lineTo(c1[0], c1[1])
            .lineTo(c2[0], c2[1])
            .lineTo(c3[0], c3[1])
            .closePath();
        }
      });

      const CubeTop = echarts.graphic.extendShape({
        shape: { x: 0, y: 0 },
        buildPath(ctx, shape) {
          // 👈 对应调整顶部宽度
          const c0 = [shape.x - 25, shape.y];
          const c1 = [shape.x + 25, shape.y];
          const c2 = [shape.x + 35, shape.y - 16];
          const c3 = [shape.x - 15, shape.y - 16];
          ctx.moveTo(c0[0], c0[1])
            .lineTo(c1[0], c1[1])
            .lineTo(c2[0], c2[1])
            .lineTo(c3[0], c3[1])
            .closePath();
        }
      });

      echarts.graphic.registerShape('cubeFront', CubeFront);
      echarts.graphic.registerShape('cubeRight', CubeRight);
      echarts.graphic.registerShape('cubeTop', CubeTop);

      const imageArr = [
        require('@/assets/images/statistics/1.png'),
        require('@/assets/images/statistics/2.png'),
        require('@/assets/images/statistics/3.png'),
        require('@/assets/images/statistics/4.png'),
        require('@/assets/images/statistics/5.png'),
      ];

      const CubeColors = {
        front: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(45,193,178,1)' },
          { offset: 1, color: 'rgba(191,237,232,1)' }
        ]),
        right: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(23,148,135,1)' },
          { offset: 1, color: 'rgba(138,219,211,1)' }
        ]),
        top: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(100,231,218,1)' },
          { offset: 1, color: 'rgba(177,244,237,1)' }
        ])
      };

      const renderItem = (params, api) => {
        const location = api.coord([api.value(0), api.value(1)]);
        const dataIndex = params.dataIndex;
        const imageURL = imageArr[dataIndex] || '';

        return {
          type: 'group',
          children: [
            {
              type: 'cubeFront',
              shape: {
                x: location[0],
                y: location[1],
                xAxisPoint: api.coord([api.value(0), 0])
              },
              style: { fill: CubeColors.front }
            },
            {
              type: 'cubeRight',
              shape: {
                x: location[0],
                y: location[1],
                xAxisPoint: api.coord([api.value(0), 0])
              },
              style: { fill: CubeColors.right }
            },
            {
              type: 'cubeTop',
              shape: {
                x: location[0],
                y: location[1]
              },
              style: { fill: CubeColors.top }
            },
            {
              type: 'image',
              x: location[0] - 20,
              y: location[1] - 95,
              style: { image: imageURL },
              z: 2
            }
          ]
        };
      };

      const maxData = Math.max(...yData);
      const yAxisMax = Math.ceil(maxData * 1.2); // 多留 20% 空间

      // ========== 配置 ==========
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          borderColor: '#2DC9C0',
          borderWidth: 2
        },
        grid: {
          top: '20%',      // 上边距（给 xAxis 标签和 label 留空间）
          bottom: '2%',   // 下边距（给 x 轴文字留空间）
          left: '8%',      // 左边距（给 y 轴数值留空间）
          right: '4%',     // 右边距（通常可小一点）
          containLabel: true // 自动包含 axisLabel，防止文字被裁剪
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#EDF2F7' } },
          axisLabel: {
            fontSize: 16,        // 字体放大
            fontWeight: 'bold',  // 加粗
            color: '#333',
            formatter: (value, index) => {
              return `${value}：${yData[index]}人`;
            }
          },
        },
        yAxis: {
          max: yAxisMax, // ✅ 动态最大值
          axisLabel: {
            formatter: function(value) {
              // 只有整数才显示，否则显示空（ECharts 会自动跳过空标签）
              if (Number.isInteger(value)) {
                return value + '人';
              }
              return ''; // 非整数不显示
            },
            color: '#666'
          },
          splitLine: {
            lineStyle: {
              type: 'dashed',     // 虚线
              color: '#e0e0e0',   // 颜色浅灰
              opacity: 0.6        // 更虚一点
            }
          },
          axisLine: {
            show: false // 可选：隐藏 Y 轴线更简洁
          },
          axisTick: {
            show: false
          }
        },
        series: [{
          name: title,
          type: 'custom',
          renderItem: renderItem,
          data: yData,
        }]
      };

      this.chart2.setOption(option);
    }
  }
};
</script>
<style lang="scss" scoped>

.app-container {
  background-color: #f0f2f5;
  padding: 16px;
}

/* 图表容器：宽度 80%，内部两个卡片垂直排列 */
.chart-wrapper {
  width: 90%;
  margin: 20px auto 0; /* 上边距，与表单分开 */
  margin-top: -20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  padding-left: 15px;
  border-left: 4px solid #409eff;
  margin-bottom: 16px;
  line-height: 1.4;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  flex-shrink: 0; /* 防止标题被压缩 */
}

.chart {
  width: 100%;
  height: 280px; /* 关键：明确高度！ */
}

/* 响应式：小屏幕下高度自适应 */
@media (max-width: 1440px) {
  .app-container {
    padding: 12px;
    background-color: #f0f2f5;
  }
  .chart-wrapper {
    width: 95%;
    gap: 16px;
  }
  .chart-card {
    padding: 12px;
    border-radius: 10px;
  }
  .chart-title {
    font-size: 16px;
    margin-bottom: 12px;
  }
}

</style>

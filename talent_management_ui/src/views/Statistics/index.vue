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

      <!-- 最底部又新增了计费情况表格，炸开啊 -->
      <div class="chart-card" v-loading="loading3" v-if="deptLevel === 1">
        <div class="chart-title">计费情况</div>
        <el-table :data="billingTableData" border style="width: 100%" :summary-method="getSummaries" show-summary>
          <el-table-column prop="locationName" label="点位" align="center" min-width="120" />
          <el-table-column prop="totalSent" label="总送人数" align="center" min-width="100" />
          <el-table-column prop="hardRequirementNotMet" label="硬性条件不符" align="center" min-width="120" />
          <el-table-column prop="remainingDenominator" label="剩余分母人数" align="center" min-width="120" />
          <el-table-column prop="billedCount" label="计费人数" align="center" min-width="100" />
          <el-table-column prop="billingRate" label="计费率" align="center" min-width="100">
            <template #default="scope">
              {{ scope.row.billingRate }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

  </div>
</template>

<script>
import { allListNoDept } from "@/api/location";
import { listDept } from "@/api/system/dept";
import {deptBillingCount, personBillingCount, rateCalculation} from "@/api/statistics";
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
      loading3: false,
      chart1: null,
      chart2: null,
      flowInterval: null,

      chartData1:{
        x: [],
        y: [],
      },

      chartData2:{
        /** 招聘人 */
        createNameData: [],
        /** 硬性条件人数 */
        hardRequirementsData: [],
        /** 不合格人数 */
        unqualifiedData: [],
        /** 通过人数 */
        passedData: [],
        /** 总送人数 */
        totalData: [],
        /** 计费率 */
        billingRateData: [],
      },

      // 计费情况表格...
      billingTableData: []
    };
  },
  created() {
    this.getLocationList();
    this.getDeptLevel3List();

    this.$nextTick(() => {
      this.getList();
    });
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

            this.chartData2.createNameData = response.data?.createNameData || [];
            this.chartData2.hardRequirementsData = response.data?.hardRequirementsData || [];
            this.chartData2.unqualifiedData = response.data?.unqualifiedData || [];
            this.chartData2.passedData = response.data?.passedData || [];
            this.chartData2.totalData = response.data?.totalData || [];
            this.chartData2.billingRateData = response.data?.billingRateData || [];

            this.initChart2();
            this.loading2 = false;
          });
        }else{
          this.chartData2.createNameData = [];
          this.chartData2.hardRequirementsData = [];
          this.chartData2.unqualifiedData = [];
          this.chartData2.passedData = [];
          this.chartData2.totalData = [];
          this.chartData2.billingRateData = [];
          this.initChart2();
        }

        // 计费率表格
        this.loading3 = true;
        this.billingTableData = [];
        rateCalculation(this.queryParams).then(response => {
          this.billingTableData = response.data?.billingTableData || [];
          this.loading3 = false;
        });
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
      /** 招聘人 */
      let createNameData = this.chartData2.createNameData;
      /** 硬性条件人数 */
      let hardRequirementsData = this.chartData2.hardRequirementsData;
      /** 不合格人数 */
      let unqualifiedData = this.chartData2.unqualifiedData;
      /** 通过人数 */
      let passedData = this.chartData2.passedData;
      /** 总人数 */
      let totalData = this.chartData2.totalData;
      /** 计费率 */
      let billingRateData = this.chartData2.billingRateData;

      // 构建横坐标数据（格式：轩轩:44人，计费率为65%）
      let xAxisData = [];
      for (let i = 0; i < createNameData.length; i++) {
        xAxisData.push(`${createNameData[i]}:${totalData[i]}人,计费率${billingRateData[i]}%`);
      }

      // ========== 配置 ==========
      const option = {
        tooltip: {
          trigger: 'axis',
        },
        grid: {
          top: '12%',      // 上边距（给 xAxis 标签和 label 留空间）
          bottom: '10%',   // 下边距（给 x 轴文字留空间）
          left: '8%',      // 左边距（给 y 轴数值留空间）
          right: '4%',     // 右边距（通常可小一点）
          containLabel: true // 自动包含 axisLabel，防止文字被裁剪
        },
        legend: {
          icon: 'roundRect',
          top: 5,
          itemHeight: 10,
          itemWidth: 10,
          textStyle: {
            color: '#000'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLabel: {
            fontSize: 12,
            color: '#444',
            interval: 0, // 0 表示显示所有，1 表示隔一个显示一个，以此类推
            // 不设 interval:0，让 ECharts 自动优化
            // 但配合 formatter 实现“省略+提示”
            formatter: function(value) {
              return value.length > 6 ? value.slice(0, 14) + '...' : value;
            }
          },
          axisTick: {
            show: false // 可选：隐藏刻度线更干净
          },
          axisLine: {
            lineStyle: {
              color: '#ccc'
            }
          },
          splitLine: {
            show: false,
          }
        },
        yAxis: {
          type: 'value',
          offset: 0,
          name: '',
          axisLabel: {
            show:true,
            textStyle: {
              color: '#9eaaba'
            },
            color: '#666'
          },
          nameTextStyle: {
            color: '#9eaaba',
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
          },
        },
        series: [
          {
            name: '通过人数 ',
            type: 'bar',
            stack: '策略变更',
            barWidth: '40%',
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#18FF80'},
                  {offset: 1, color: 'rgba(24, 255, 182, 0.35)'}
                ], false),
              }
            },
            data: passedData,
          },
          {
            name: '不合格人数',
            type: 'bar',
            stack: '策略变更',
            barWidth: '40%',  //柱子宽度
            itemStyle: {  //柱子颜色
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#FFBA18'},
                  {offset: 1, color: 'rgba(255, 151, 24, 0.35)'}
                ], false),
              }
            },
            data: unqualifiedData
          },
          {
            name: '硬性条件人数',
            type: 'bar',
            stack: '策略变更',
            barWidth: '40%',
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#1890FF'},
                  {offset: 1, color: 'rgba(24, 144, 255, 0.35)'}

                ], false),
              }
            },
            data: hardRequirementsData,

            // 4. 在每个柱子顶部显示总人数
            label: {
              show: true,
              position: 'top',
              formatter: function(params) {
                // 显示该柱子的总人数（通过+不合格+硬性条件）
                const total = totalData[params.dataIndex];
                return `${total}人`;
              },
              color: '#666',
              fontWeight: 'bold'
            }
          }
        ]
      };

      this.chart2.setOption(option);
    },

    // 计费率表格求和
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];

      // 先计算合计值（用于计费率）
      const totalSentSum = data.reduce((sum, item) => sum + (Number(item.totalSent) || 0), 0);
      const hardRequirementNotMetSum = data.reduce((sum, item) => sum + (Number(item.hardRequirementNotMet) || 0), 0);
      const billedCountSum = data.reduce((sum, item) => sum + (Number(item.billedCount) || 0), 0);
      const remainingDenominatorSum = totalSentSum - hardRequirementNotMetSum; // 或直接 sum(item.remainingDenominator)

      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计';
          return;
        }

        const prop = column.property;

        if (prop === 'billingRate') {
          // 计费率 = 合计计费人数 / 合计剩余分母人数
          if (remainingDenominatorSum > 0) {
            const rate = (billedCountSum / remainingDenominatorSum * 100).toFixed(2);
            sums[index] = `${rate}%`;
          } else {
            sums[index] = '0.00%';
          }
        } else if (prop === 'locationName') {
          sums[index] = '';
        } else {
          // 其他数值列直接求和
          const sum = data.reduce((acc, item) => {
            const val = Number(item[prop]) || 0;
            return acc + val;
          }, 0);
          sums[index] = sum;
        }
      });

      return sums;
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

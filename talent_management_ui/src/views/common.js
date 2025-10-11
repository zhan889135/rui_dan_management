export const RET_CODE_0 = "0";
export const RET_CODE_1 = "1";
export const RET_CODE_2 = "2";
export const YYMMDDHHMMSS = "YYYY-MM-DD HH:mm:ss";
export const LastAndNext = {
  LastDay: "lastday",
  NextDay: "nextday",
  LastWeek: "lastweek",
  NextWeek: "nextweek",
  LastMonth: "lastmonth",
  NextMonth: "nextmonth",
  LastSeason: "lastseason",
  NextSeason: "nextseason",
};
// "#32A9D6",//蓝色-一般
// "#FF9900",//橙色-严重
// "#7F00BE",//紫色-危急
// "#FF7D7D",//橙红-未处理
// "#EE84E3",//粉色-瞬时报警
// "#D84275",//深粉-持续报警
export const ALARM_COLOR = [
  "#32A9D6",
  "#FF9900",
  "#7F00BE",
  "#FF7D7D",
  "#EE84E3",
  "#D84275",
];
// ABC三相颜色
export const ABC_COLORS = ["#FFB300", "#17B847", "#E40505"];
// ABC图例名称
export const ABC_LEGEND = ["A相", "B相", "C相"];
// ABC
export const ABC = ["A", "B", "C"];
// 默认分部
export const DEFAULT_BRANCH = "辽阳"

function createObject(keys) {
  return keys.reduce((obj, key) => {
    obj[key] = key;
    return obj;
  }, {});
}

export const RELOGIN_KEY = "reLogin";

export const CONFIG_AND_JOB_BTN = "阈值和定时任务设置";

export const JOB_BTN = "定时任务设置";

// 阈值前缀-需与config配置表中保持一致
export const CONFIG_KEY_MAPPING = createObject([
  "circuitBreaker",
  "oilFilling",
  "trend",
  "threeCurrents",
  "telemetry",
  "parallelOperation",
  "ctCircuit",
  "recordingWaves",
  "positionIncorrect",
  "brakeUnSync",
  "empty",
  "gis",
  "lowVoltageGrounding",
  "substationDataError",
  "cvtVoltageError",
  "circuit",
  "everydayLine"
]);

// 定时任务方法名映射
export const JOB_NAME_MAPPING = {
  circuitBreaker: "断路器储能",
  oilFilling: "充油",
  threeCurrents: "三相电流不平衡度",
  telemetry: "遥测数据",
  switchNoChange: "开关无变化",
  everydayLine: "每日潮流",
  parallelOperation: "主变并列",
  ctCircuit: "CT回路",
  recordingWaves: "录波器",
  trend:"断路器打压趋势",
  positionIncorrect: "断路器分合闸位置不正确",
  brakeUnSync: "断路器分合闸严重不同期",
  empty: "空载站用变",
  substationDataErrors: "变电站至集控主站数据异常",
  gis: "GIS母线双跨",
  lowVoltageGrounding: "低压侧小电流接地",
  cvtVoltageError: "CVT电压互感器",
  circuit: "断路器开合次数分类统计",
};

// 路由key 需与菜单表中path属性(首字母大写后)相同
export const Menu_Key = createObject([
  // 三相电流不平衡度
  "ThreeCurrents",
  "ThreeCurrentsDetail",
  "ThreeCurrentsCompare",
  // 断路器储能机构异常
  "CircuitBreaker",
  "CircuitBreakerDetail",
  // 断路器分合闸位置不正确
  "PositionIncorrect",
  "PositionIncorrectDetail",
  "PositionIncorrectCompare",
  // 断路器打压趋势
  "Trend",
  "TrendDetail",
  // 断路器分合闸严重不同期
  "BrakeUnSync",
  "BrakeUnSyncDetail",
  // 变压器油温及绕组温度异常
  "OilFilling",
  "OilFillingDetail",
  // 并列运行主变电压比不一致
  "ParallelOperation",
  "ParallelOperationDetail",
  // 空载站用变高压侧断线
  "EmptyLoadTransformerDisconnection",
  "EmptyLoadTransformerDisconnectionDetail",
  // GIS母线双跨开关分合不到位
  "GisSwitchIncorrect",
  "GisSwitchIncorrectDetail",
  // 500千伏主变低压侧小电流单相接地
  "LowVoltageGrounding",
  "LowVoltageGroundingDetail",
  // CVT电压互感器异常
  "CvtVoltageError",
  "CvtVoltageErrorDetail",
  // 测控装置异常
  "Telemetry",
  "TelemetryDetail",
  // 变电站至集控主站数据异常
  "SubstationDataError",
  "SubstationDataErrorDetail",
  // CT回路二次侧断线预警
  "CtCircuit",
  "CtCircuitDetail",
  "CtCircuitDetailList",
  // 录波器监视
  "RecordingWaves",
  "RecordingWavesDetail",
  // 主变承受短路电流统计
  "TransCurrent",
  "TransCurrentDetail",
  // 断路器隔离开关未变位统计
  "SwitchNoChange",
  "SwitchNoChangeDetail",
  // 断路器开断短路电流统计
  "FaultCurrent",
  // 断路器开合次数分类统计
  "CircuitBreakerClassifyCount",
  "CircuitBreakerClassifyCountDetail",
  // 异常信息自动统计
  "ExceptionInfo",
  "EverydayLine",
  // 告警信息
  "Alarm",
  // 服务监控
  // 集控系统智能运维
  "Server",
]);

export const ExceptionInfolevel = { 0: "一般", 1: "严重", 2: "危急" };

export const TableHeight5Row = 276;

export const TableHeight10Row = 555;

export const CardInfos = {
  RecordingWaves: {
    title: "录波器简介",
    content:
      "录波频繁启动预警”对录波器频繁启动进行预警。每天执行一次，统计每台设备的每天录波启动次数；超过阀值次数进行报警； 可以查询报警历史数据及制定录波器历史周期的动作曲线；录波启动次数要减去其和分闸动作次数。 对长时间启动未复归的录波器进行列表统计及排序功能。",
  },
  CtCircuit: {
    title: "测量回路CT回路二次侧断线预警简介",
    content: `“测量回路CT回路二次侧断线预警”
    采取轮巡方式，一旦满足预警条件，对CT回路二次侧断线进行预警。要求一小时轮巡一次；对指定变电站及设备进行随时检测，并进行预警。报警信息进行储存及相关电气量查看。
    可以查看历史周期相关电气量情况。`,
  },
  Trend: {
    title: "断路器打压趋势知识简介",
    content: `“断路器作为保障电力网络稳定运行的核心设备，其性能和可靠性备受关注。对断路器的打压进行监测不仅能够及时发现潜在故障，还能有效延长设备使用寿命，确保电力系统的安全与高效运行。"`,
  },
  GIS: {
    title: "刀闸双跨方式下刀闸合分不到位预警简介",
    content: `“刀闸双跨方式下刀闸合分不到位预警"在热倒母线操作刀闸双跨过程中，对刀闸双跨设备的某把母线刀闸三相中任意一相合分不到位进行预警。要求制定变站中的热倒母线设备，一旦出现刀闸双跨情况进行实时展开监测，并根据算法进行预警分析，一旦发现三相中任意一相合分不到位立即预警`,
  },
  LowVoltageGrounding: {
    title: "500kV主变低压侧小电流单相接地预警简介",
    content: `“500kV主变低压侧小电流单相接地预警分析"对500kV变电站、66(35)kV小电流接地系统单相接地进行预警;出现单相接地时，对接地故障进行预警，对出现接地的设备进行识别，并进行设备报警要求在计划固定周期内，对设备电气是进行轮巡分析，若符合报警条件，进行报警，对报警信息进行储存及支持相关信息查看`,
  },
  BrakeUnSync: {
    title: "断路器分合闸严重不同期简介",
    content: `“断路器分合闸严重不同期"在多相电力系统中，断路器的三相（即 A、B、C 三相）不能在规定的时间范围内同时完成分合闸操作。会导致系统中不平衡电流的产生，可能引发振荡或谐波，进而影响电网的稳定性。`,
  },
  PositionIncorrect: {
    title: "断路器分合闸位置不正确简介",
    content: `“断路器分合闸位置不正确"断路器在执行分闸或合闸操作后，其机械位置指示与实际状态不一致。这种情况会影响电力系统的安全性和稳定性，可能导致误操作或延迟故障处理。`,
  },
  FaultCurrent: {
    title: "断路器开断短路电流次数及累计电流值统计",
    content: `“断路器开断短路电流次数及累计电流值统计"断路器在电力系统中用于切断和接通电流，特别是在短路情况下，断路器需要迅速切断异常的短路电流，以保护设备和电力系统的安全。断路器的性能和寿命会受到它开断短路电流的次数和累计电流值的影响。`,
  },
  TransCurrent: {
    title: "主变承受短路电流次数及累计电流值统计",
    content: `“主变承受短路电流次数及累计电流值统计"断路器在电力系统中用于切断和接通电流，特别是在短路情况下，断路器需要迅速切断异常的短路电流，以保护设备和电力系统的安全。断路器的性能和寿命会受到它开断短路电流的次数和累计电流值的影响。`,
  },
  EmptyLoadTransformerDisconnection: {
    title: "空载站用变高压侧断线简介",
    content:
      "“空载站用变高压侧断线预警”功能是通过监测空载站用变压器低压侧的电压情况来判断高压侧的断线状态。当空载站用变压器处于运行状态时，系统会持续监测低压侧的电压。如果低压侧的电压低于预设的阈值，这表明高压侧可能出现了断线情况。\n" +
      "\n" +
      "此预警机制的核心在于通过低压侧电压的实时监测来间接判断高压侧的连接状态。一旦系统检测到低压侧电压低于设定值，将立即发出预警，提示运维人员检查高压侧的连接状态。这种监测方式能够快速识别潜在的断线问题，防止设备在异常状态下运行，确保电力系统的安全性和稳定性。",
  },
};

// 通用图表报警种类
export const AnalysisLabel = {
  NORMAL: "一般",
  SERIOUS: "严重",
  DANGER: "危急",
  UNTREATED: "未处理",
  ALARM: "瞬时报警",
  FAULT: "持续报警",
  COUNT: "总数",
};

// 功能functionKey与菜单表中menuKey属性对应
export const FunctionType = createObject([
  "CircuitBreaker",
  "CircuitBreakerTrend",
  "OilFilling",
  "ThreeCurrents",
  "Telemetry",
  "ParallelOperation",
  "CtCircuit",
  "RecordingWaves",
  "SubstationDataError",
  "LowVoltageGrounding",
  "CvtVoltageError",
  "GisSwitchIncorrect",
  "BrakeUnSync",
  "PositionIncorrect",
  "CircuitBreakerClassifyCount",
  "EmptyLoad",
]);

// export const Menu_Prefix_DataMonitor = "/dataMonitor";
// export const Menu_Prefix_StatisticalAnalysis = "/statisticalAnalysis";
// export const Menu_Prefix_Ops = "/ops";

export const dateLabel = {
  day: [
    { label: "前一天", value: LastAndNext.LastDay },
    { label: "后一天", value: LastAndNext.NextDay },
  ],
  week: [
    { label: "前一周", value: LastAndNext.LastWeek },
    { label: "后一周", value: LastAndNext.NextWeek },
  ],
  month: [
    { label: "前一月", value: LastAndNext.LastMonth },
    { label: "后一月", value: LastAndNext.NextMonth },
  ],
  season: [
    { label: "前一季", value: LastAndNext.LastSeason },
    { label: "后一季", value: LastAndNext.NextSeason },
  ],
};

export const nameplateInfos = {
  CircuitBreaker: {
    nameplateForm: {
      deviceName: "断路器开关",
      deviceNumber: "SF6-874",
      manufacturer: "csp",
      manufactureDate: "2022-01-01",
      ratedVoltage: "220V",
      ratedPower: "500kW",
    },
    maintenanceRecords: [
      {
        date: "2023-06-15",
        type: "定期检修",
        description: "更换老化部件",
        inspector: "刘强",
        location: "厂站",
        duration: "2小时",
        tools: "扳手",
        cost: "200元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-07-10",
        type: "临时检修",
        description: "修复机器部件",
        inspector: "王力国",
        location: "厂站",
        duration: "3小时",
        tools: "螺丝刀",
        cost: "300元",
        result: "正常",
        notes: "无",
      },
    ],
    defectRecords: [
      {
        date: "2023-06-20",
        description: "传动轴故障",
        status: "已修复",
        responsible: "鄂文鹏",
        severity: "中等",
        actions: "更换",
        dueDate: "2023-06-22",
        actualRepairDate: "2023-06-22",
        notes: "无",
      },
      {
        date: "2023-07-05",
        description: "杠杆轻微磨损",
        status: "待处理",
        responsible: "冯浩",
        severity: "轻微",
        actions: "待观察",
        dueDate: "2023-07-10",
        actualRepairDate: "",
        notes: "无",
      },
    ],
  },

  OilFilling: {
    nameplateForm: {
      deviceName: "高压变压器",
      deviceNumber: "TWTM-001",
      manufacturer: "温控科技公司",
      manufactureDate: "2022-06-01",
      ratedVoltage: "220kV",
      ratedPower: "300MVA",
    },
    maintenanceRecords: [
      {
        date: "2018-06-15",
        type: "定期检修",
        description: "检查油温传感器和冷却装置",
        inspector: "李国强",
        location: "220kV 华南变电站",
        duration: "4小时",
        tools: "油温表、冷却剂",
        cost: "1500元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2018-06-20",
        type: "异常检修",
        description: "处理冷却系统故障",
        inspector: "王小军",
        location: "220kV 华南变电站",
        duration: "6小时",
        tools: "扳手、油泵",
        cost: "5000元",
        result: "修复完成",
        notes: "油泵更换",
      },
    ],
    defectRecords: [
      {
        date: "2018-06-18",
        description: "油温异常上升",
        status: "已修复",
        responsible: "王小军",
        severity: "严重",
        actions: "停电检修，发现油泵故障",
        dueDate: "2018-06-20",
        actualRepairDate: "2018-06-20",
        notes: "更换油泵",
      },
      {
        date: "2018-07-05",
        description: "冷却风扇故障",
        status: "待处理",
        responsible: "李国强",
        severity: "中等",
        actions: "待检修",
        dueDate: "2018-07-10",
        actualRepairDate: "",
        notes: "等待更换风扇",
      },
    ],
  },

  ThreeCurrents: {
    nameplateForm: {
      deviceName: "变电站线路",
      deviceNumber: "3PHC-002",
      manufacturer: "电力设备公司",
      manufactureDate: "2021-05-10",
      ratedVoltage: "220kV",
      ratedPower: "400MVA",
    },
    maintenanceRecords: [
      {
        date: "2021-08-12",
        type: "定期检修",
        description: "检查三相电流平衡度",
        inspector: "张伟",
        location: "东部变电站",
        duration: "3小时",
        tools: "电流测试仪",
        cost: "1000元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2021-09-15",
        type: "异常检修",
        description: "处理电流不平衡问题",
        inspector: "李华",
        location: "西部变电站",
        duration: "5小时",
        tools: "调节器",
        cost: "2000元",
        result: "已修复",
        notes: "调整三相电流",
      },
    ],
    defectRecords: [
      {
        date: "2021-09-10",
        description: "三相电流不平衡",
        status: "已修复",
        responsible: "李华",
        severity: "严重",
        actions: "停电调节",
        dueDate: "2021-09-15",
        actualRepairDate: "2021-09-15",
        notes: "问题解决",
      },
      {
        date: "2021-10-01",
        description: "电流波动异常",
        status: "待处理",
        responsible: "张伟",
        severity: "中等",
        actions: "等待检查",
        dueDate: "2021-10-05",
        actualRepairDate: "",
        notes: "待处理",
      },
    ],
  },

  Telemetry: {
    nameplateForm: {
      deviceName: "220kV 变电站集控",
      deviceNumber: "RTU-220-XY001",
      manufacturer: "集控系统设备有限公司",
      manufactureDate: "2021-12-15",
      ratedVoltage: "220kV",
      ratedPower: "1000MW",
    },
    maintenanceRecords: [
      {
        date: "2023-04-10",
        type: "定期检修",
        description: "遥测数据准确度校验",
        inspector: "李勇",
        location: "西南变电站",
        duration: "2小时",
        tools: "数据分析仪",
        cost: "1500元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-05-18",
        type: "异常检修",
        description: "处理数据传输故障",
        inspector: "张强",
        location: "东部变电站",
        duration: "4小时",
        tools: "网络测试仪",
        cost: "3000元",
        result: "已修复",
        notes: "更换了传输设备",
      },
    ],
    defectRecords: [
      {
        date: "2023-05-15",
        description: "遥测信息传输失效",
        status: "已修复",
        responsible: "张强",
        severity: "严重",
        actions: "更换传输模块",
        dueDate: "2023-05-18",
        actualRepairDate: "2023-05-18",
        notes: "问题解决",
      },
      {
        date: "2023-06-01",
        description: "测控装置死机",
        status: "待处理",
        responsible: "李勇",
        severity: "严重",
        actions: "等待更换",
        dueDate: "2023-06-05",
        actualRepairDate: "",
        notes: "待处理",
      },
    ],
  },

  ParallelOperation: {
    nameplateForm: {
      deviceName: "高压变压器",
      deviceNumber: "TR-PAR-003",
      manufacturer: "电力系统设备有限公司",
      manufactureDate: "2020-11-01",
      ratedVoltage: "220kV",
      ratedPower: "600MVA",
    },
    maintenanceRecords: [
      {
        date: "2022-07-10",
        type: "定期检修",
        description: "检查并列主变无功功率分配",
        inspector: "陈伟",
        location: "南方变电站",
        duration: "3小时",
        tools: "无功功率测试仪",
        cost: "2000元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2022-09-12",
        type: "异常检修",
        description: "处理无功功率分配不均",
        inspector: "王强",
        location: "北方变电站",
        duration: "5小时",
        tools: "无功调节器",
        cost: "4000元",
        result: "已修复",
        notes: "无功分配已平衡",
      },
    ],
    defectRecords: [
      {
        date: "2022-08-05",
        description: "无功功率不平衡",
        status: "已修复",
        responsible: "王强",
        severity: "严重",
        actions: "调整无功分配，修复不平衡",
        dueDate: "2022-08-10",
        actualRepairDate: "2022-08-10",
        notes: "已处理",
      },
      {
        date: "2022-10-01",
        description: "主变运行异常，无功分配偏差",
        status: "待处理",
        responsible: "陈伟",
        severity: "中等",
        actions: "计划调整无功分配",
        dueDate: "2022-10-05",
        actualRepairDate: "",
        notes: "待调整",
      },
    ],
  },

  CtCircuit: {
    nameplateForm: {
      deviceName: "变电线路",
      deviceNumber: "CTW-004",
      manufacturer: "高压电力设备有限公司",
      manufactureDate: "2020-10-10",
      ratedVoltage: "220kV",
      ratedPower: "1000MVA",
    },
    maintenanceRecords: [
      {
        date: "2022-01-15",
        type: "定期检修",
        description: "检查CT回路连接情况",
        inspector: "张伟",
        location: "南方变电站",
        duration: "2小时",
        tools: "电流测试仪",
        cost: "1200元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2022-02-10",
        type: "异常检修",
        description: "处理CT回路断线故障",
        inspector: "李强",
        location: "北方变电站",
        duration: "3小时",
        tools: "断路器测试仪",
        cost: "2000元",
        result: "已修复",
        notes: "更换断线部分",
      },
    ],
    defectRecords: [
      {
        date: "2022-02-05",
        description: "CT回路二次侧断线",
        status: "已修复",
        responsible: "李强",
        severity: "严重",
        actions: "更换二次侧断线",
        dueDate: "2022-02-10",
        actualRepairDate: "2022-02-10",
        notes: "问题已解决",
      },
      {
        date: "2022-03-01",
        description: "CT回路电流异常",
        status: "待处理",
        responsible: "张伟",
        severity: "中等",
        actions: "等待检测",
        dueDate: "2022-03-05",
        actualRepairDate: "",
        notes: "检测中",
      },
    ],
  },

  RecordingWaves: {
    nameplateForm: {
      deviceName: "录波器",
      deviceNumber: "RWB-005",
      manufacturer: "自动化设备有限公司",
      manufactureDate: "2021-05-20",
      ratedVoltage: "66kV",
      ratedPower: "200MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-03-10",
        type: "定期检修",
        description: "检查录波器启动情况",
        inspector: "张三",
        location: "华南变电站",
        duration: "3小时",
        tools: "录波器测试仪",
        cost: "1800元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-04-15",
        type: "异常检修",
        description: "处理录波器频繁启动问题",
        inspector: "李四",
        location: "华北变电站",
        duration: "4小时",
        tools: "断路器测试设备",
        cost: "2500元",
        result: "已修复",
        notes: "调整了启动参数",
      },
    ],
    defectRecords: [
      {
        date: "2023-04-01",
        description: "录波器频繁启动",
        status: "已修复",
        responsible: "李四",
        severity: "严重",
        actions: "调整启动参数并测试",
        dueDate: "2023-04-15",
        actualRepairDate: "2023-04-15",
        notes: "问题解决",
      },
      {
        date: "2023-05-01",
        description: "录波器长时间未复归",
        status: "待处理",
        responsible: "张三",
        severity: "中等",
        actions: "等待处理",
        dueDate: "2023-05-10",
        actualRepairDate: "",
        notes: "检测中",
      },
    ],
  },

  LowVoltageGrounding: {
    nameplateForm: {
      deviceName: "母线",
      deviceNumber: "GND-66KV-006",
      manufacturer: "高压设备制造有限公司",
      manufactureDate: "2022-08-15",
      ratedVoltage: "66kV",
      ratedPower: "600MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-05-05",
        type: "定期检修",
        description: "检查小电流接地系统工作状态",
        inspector: "张强",
        location: "南部变电站",
        duration: "3小时",
        tools: "接地电流测试仪",
        cost: "2000元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-06-12",
        type: "异常检修",
        description: "处理接地故障",
        inspector: "李明",
        location: "北方变电站",
        duration: "5小时",
        tools: "接地故障分析仪",
        cost: "3500元",
        result: "已修复",
        notes: "接地故障排除",
      },
    ],
    defectRecords: [
      {
        date: "2023-06-10",
        description: "小电流单相接地故障",
        status: "已修复",
        responsible: "李明",
        severity: "严重",
        actions: "处理接地故障",
        dueDate: "2023-06-12",
        actualRepairDate: "2023-06-12",
        notes: "问题解决",
      },
      {
        date: "2023-07-01",
        description: "低压侧接地异常",
        status: "待处理",
        responsible: "张强",
        severity: "中等",
        actions: "检测接地情况",
        dueDate: "2023-07-05",
        actualRepairDate: "",
        notes: "等待处理",
      },
    ],
  },

  CvtVoltageError: {
    nameplateForm: {
      deviceName: "母线",
      deviceNumber: "CVT-66KV-007",
      manufacturer: "电力电容设备公司",
      manufactureDate: "2021-04-25",
      ratedVoltage: "66kV",
      ratedPower: "300MVA",
    },
    maintenanceRecords: [
      {
        date: "2022-07-15",
        type: "定期检修",
        description: "检查CVT电压数据",
        inspector: "李伟",
        location: "中南变电站",
        duration: "2小时",
        tools: "电压测试仪",
        cost: "1200元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-03-10",
        type: "异常检修",
        description: "处理CVT电压波动异常",
        inspector: "王小明",
        location: "东南变电站",
        duration: "4小时",
        tools: "CVT电压分析仪",
        cost: "2800元",
        result: "已修复",
        notes: "更换了故障部件",
      },
    ],
    defectRecords: [
      {
        date: "2023-03-05",
        description: "CVT电压波动异常",
        status: "已修复",
        responsible: "王小明",
        severity: "严重",
        actions: "更换故障部件",
        dueDate: "2023-03-10",
        actualRepairDate: "2023-03-10",
        notes: "问题解决",
      },
      {
        date: "2023-04-01",
        description: "CVT二次电压异常升高",
        status: "待处理",
        responsible: "李伟",
        severity: "中等",
        actions: "等待分析",
        dueDate: "2023-04-05",
        actualRepairDate: "",
        notes: "待分析",
      },
    ],
  },

  GisSwitchIncorrect: {
    nameplateForm: {
      deviceName: "母线",
      deviceNumber: "DSC-220KV-008",
      manufacturer: "电力设备控制有限公司",
      manufactureDate: "2021-11-30",
      ratedVoltage: "220kV",
      ratedPower: "1000MVA",
    },
    maintenanceRecords: [
      {
        date: "2022-05-10",
        type: "定期检修",
        description: "检查刀闸操作情况",
        inspector: "张宏伟",
        location: "西北变电站",
        duration: "3小时",
        tools: "刀闸检测仪",
        cost: "1500元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2022-11-20",
        type: "异常检修",
        description: "处理刀闸合分不到位问题",
        inspector: "李建",
        location: "华东变电站",
        duration: "5小时",
        tools: "刀闸调整设备",
        cost: "3000元",
        result: "已修复",
        notes: "调整刀闸位置",
      },
    ],
    defectRecords: [
      {
        date: "2022-11-15",
        description: "刀闸合分不到位",
        status: "已修复",
        responsible: "李建",
        severity: "严重",
        actions: "调整刀闸操作位置",
        dueDate: "2022-11-20",
        actualRepairDate: "2022-11-20",
        notes: "问题解决",
      },
      {
        date: "2023-01-10",
        description: "刀闸三相中一相未合到位",
        status: "待处理",
        responsible: "张宏伟",
        severity: "中等",
        actions: "等待处理",
        dueDate: "2023-01-15",
        actualRepairDate: "",
        notes: "检测中",
      },
    ],
  },

  BrakeUnSync: {
    nameplateForm: {
      deviceName: "断路器",
      deviceNumber: "CBP-220kV-010",
      manufacturer: "高压设备制造厂",
      manufactureDate: "2020-09-30",
      ratedVoltage: "220kV",
      ratedPower: "1200MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-01-05",
        type: "定期检修",
        description: "检查断路器三相分合闸操作时间",
        inspector: "张三",
        location: "华南变电站",
        duration: "4小时",
        tools: "同步测试仪",
        cost: "2000元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-02-12",
        type: "异常检修",
        description: "处理断路器严重不同期问题",
        inspector: "李四",
        location: "华北变电站",
        duration: "6小时",
        tools: "同期调整设备",
        cost: "3000元",
        result: "已修复",
        notes: "调整分合闸时间",
      },
    ],
    defectRecords: [
      {
        date: "2023-02-10",
        description: "断路器三相分合闸不同期",
        status: "已修复",
        responsible: "李四",
        severity: "严重",
        actions: "调整三相同期设备",
        dueDate: "2023-02-12",
        actualRepairDate: "2023-02-12",
        notes: "问题解决",
      },
      {
        date: "2023-03-01",
        description: "断路器合闸时间超限",
        status: "待处理",
        responsible: "张三",
        severity: "中等",
        actions: "等待检测",
        dueDate: "2023-03-05",
        actualRepairDate: "",
        notes: "待检测",
      },
    ],
  },

  PositionIncorrect: {
    nameplateForm: {
      deviceName: "断路器",
      deviceNumber: "CBP-220kV-009",
      manufacturer: "电力机械设备有限公司",
      manufactureDate: "2021-06-15",
      ratedVoltage: "220kV",
      ratedPower: "1200MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-01-10",
        type: "定期检修",
        description: "检查断路器机械位置与电气状态",
        inspector: "李明",
        location: "北方变电站",
        duration: "3小时",
        tools: "位置传感器测试仪",
        cost: "1800元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-02-15",
        type: "异常检修",
        description: "处理断路器分合闸位置不一致问题",
        inspector: "张强",
        location: "华东变电站",
        duration: "4小时",
        tools: "断路器调整工具",
        cost: "2500元",
        result: "已修复",
        notes: "调整了位置传感器",
      },
    ],
    defectRecords: [
      {
        date: "2023-02-10",
        description: "断路器分闸后位置不正确",
        status: "已修复",
        responsible: "张强",
        severity: "严重",
        actions: "调整位置传感器并测试",
        dueDate: "2023-02-15",
        actualRepairDate: "2023-02-15",
        notes: "问题解决",
      },
      {
        date: "2023-03-01",
        description: "断路器合闸位置异常",
        status: "待处理",
        responsible: "李明",
        severity: "中等",
        actions: "等待进一步检测",
        dueDate: "2023-03-05",
        actualRepairDate: "",
        notes: "待分析",
      },
    ],
  },

  CircuitBreakerClassifyCount: {
    nameplateForm: {
      deviceName: "断路器",
      deviceNumber: "CBT-220kV-011",
      manufacturer: "高压电器设备有限公司",
      manufactureDate: "2021-07-01",
      ratedVoltage: "220kV",
      ratedPower: "1500MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-02-15",
        actions: "电气分合操作",
        description: "接通主回路电流",
        inspector: "张强",
        location: "厂站",
        duration: "2小时",
        tools: "检测",
        cost: "300元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-03-10",
        actions: "机械分合操作",
        description: "进行断路器维护测试",
        inspector: "李明",
        location: "厂站",
        duration: "3小时",
        tools: "检测",
        cost: "200元",
        result: "正常",
        notes: "无",
      },
    ],
    defectRecords: [
      {
        date: "2023-03-05",
        description: "电气分合次数过多导致磨损",
        status: "待处理",
        responsible: "李明",
        severity: "严重",
        actions: "更换磨损触头",
        dueDate: "2023-03-12",
        actualRepairDate: "",
        notes: "更换中",
      },
      {
        date: "2023-04-01",
        description: "机械分合操作失效",
        status: "已修复",
        responsible: "张强",
        severity: "中等",
        actions: "调整机械操作机构",
        dueDate: "2023-04-05",
        actualRepairDate: "2023-04-04",
        notes: "已修复",
      },
    ],
  },

  EmptyLoad: {
    nameplateForm: {
      deviceName: "站用变-变压器",
      deviceNumber: "NCB-220kV-012",
      manufacturer: "变压器制造厂",
      manufactureDate: "2022-05-01",
      ratedVoltage: "220kV",
      ratedPower: "2000MVA",
    },
    maintenanceRecords: [
      {
        date: "2023-06-01",
        type: "定期检修",
        description: "检查高压侧连接情况",
        inspector: "李强",
        location: "东南变电站",
        duration: "2小时",
        tools: "电压测试仪",
        cost: "1800元",
        result: "正常",
        notes: "无",
      },
      {
        date: "2023-07-10",
        type: "异常检修",
        description: "处理高压侧断线预警问题",
        inspector: "王伟",
        location: "华北变电站",
        duration: "4小时",
        tools: "高压测试仪",
        cost: "2500元",
        result: "已修复",
        notes: "更换了高压侧连接线",
      },
    ],
    defectRecords: [
      {
        date: "2023-07-05",
        description: "高压侧断线",
        status: "已修复",
        responsible: "王伟",
        severity: "严重",
        actions: "更换高压侧断线",
        dueDate: "2023-07-10",
        actualRepairDate: "2023-07-10",
        notes: "问题解决",
      },
      {
        date: "2023-08-01",
        description: "低压侧电压异常",
        status: "待处理",
        responsible: "李强",
        severity: "中等",
        actions: "检查高压侧连接",
        dueDate: "2023-08-05",
        actualRepairDate: "",
        notes: "待检测",
      },
    ],
  },
};

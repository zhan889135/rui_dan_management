// export function generateRandomID(length = 19) {
//   const chars = '0123456789';
//   let result = '';
//   for (let i = 0; i < length; i++) {
//     result += chars.charAt(Math.floor(Math.random() * chars.length));
//   }
//   return result;
// }

import {Message} from "element-ui";

/**
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 */

// 日期格式化 {y}-{m}-{d}
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/').replace('T', ' ').replace(new RegExp(/\.[\d]{3}/gm), '');
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value] }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params;
  search.params = typeof (search.params) === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {};
  dateRange = Array.isArray(dateRange) ? dateRange : [];
  if (typeof (propName) === 'undefined') {
    search.params['beginTime'] = dateRange[0];
    search.params['endTime'] = dateRange[1];
  } else {
    search.params['begin' + propName] = dateRange[0];
    search.params['end' + propName] = dateRange[1];
  }
  return search;
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return "";
  }
  var actions = [];
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label);
      return true;
    }
  })
  if (actions.length === 0) {
    actions.push(value);
  }
  return actions.join('');
}

// 回显数据字典（字符串、数组）
export function selectDictLabels(datas, value, separator) {
  if (value === undefined || value.length ===0) {
    return "";
  }
  if (Array.isArray(value)) {
    value = value.join(",");
  }
  var actions = [];
  var currentSeparator = undefined === separator ? "," : separator;
  var temp = value.split(currentSeparator);
  Object.keys(value.split(currentSeparator)).some((val) => {
    var match = false;
    Object.keys(datas).some((key) => {
      if (datas[key].value == ('' + temp[val])) {
        actions.push(datas[key].label + currentSeparator);
        match = true;
      }
    })
    if (!match) {
      actions.push(temp[val] + currentSeparator);
    }
  })
  return actions.join('').substring(0, actions.join('').length - 1);
}

// 字符串格式化(%s )
export function sprintf(str) {
  var args = arguments, flag = true, i = 1;
  str = str.replace(/%s/g, function () {
    var arg = args[i++];
    if (typeof arg === 'undefined') {
      flag = false;
      return '';
    }
    return arg;
  });
  return flag ? str : '';
}

// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return "";
  }
  return str;
}

// 数据合并
export function mergeRecursive(source, target) {
  for (var p in target) {
    try {
      if (target[p].constructor == Object) {
        source[p] = mergeRecursive(source[p], target[p]);
      } else {
        source[p] = target[p];
      }
    } catch (e) {
      source[p] = target[p];
    }
  }
  return source;
};

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
export function handleTree(data, id, parentId, children) {
  let config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  };

  var childrenListMap = {};
  var nodeIds = {};
  var tree = [];

  for (let d of data) {
    let parentId = d[config.parentId];
    if (childrenListMap[parentId] == null) {
      childrenListMap[parentId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[parentId].push(d);
  }

  for (let d of data) {
    let parentId = d[config.parentId];
    if (nodeIds[parentId] == null) {
      tree.push(d);
    }
  }

  for (let t of tree) {
    adaptToChildrenList(t);
  }

  function adaptToChildrenList(o) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]];
    }
    if (o[config.childrenList]) {
      for (let c of o[config.childrenList]) {
        adaptToChildrenList(c);
      }
    }
  }
  return tree;
}

/**
* 参数处理
* @param {*} params  参数
*/
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    var part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            let params = propName + '[' + key + ']';
            var subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result
}

// 验证是否为blob格式
export function blobValidate(data) {
  return data.type !== 'application/json'
}

// 成功提示框
export function successBox(msg) {
  this.$notify({
    title: '成功',
    message: msg,
    type: 'success'
  });
}
// 警告提示框
export function warningBox(msg) {
  this.$notify({
    title: '警告',
    message: msg,
    type: 'warning'
  });
}
// 错误提示框
export function errorBox(msg) {
  this.$notify.error({
    title: '错误',
    message: msg
  });
}

export function getNickNameByUserId(userIds, userList) {
  if (!userList || !Array.isArray(userList) || userList.length === 0) return '';

  return String(userIds || '')
    .split(',')
    .map(id => {
      const trimmedId = String(id).trim(); // 强制转为字符串
      if (trimmedId === '1' || trimmedId === 1) return '超级管理员';
      const user = userList.find(u => String(u.userId) === trimmedId);
      return user ? user.nickName : trimmedId;
    })
    .filter(Boolean)
    .join(', ');
}


// 下面是兼容单个和多个 ID 的统一版本
export function getNickNameByUserName(userNames, userList) {
  if (!userList || !Array.isArray(userList) || userList.length === 0) return '';

  return String(userNames || '')
    .split(',')
    .map(name => {
      const trimmedName = name.trim();
      if (trimmedName === 'admin') return '超级管理员';
      const user = userList.find(u => u.userName === trimmedName);
      return user ? user.nickName : trimmedName;
    })
    .filter(Boolean)
    .join(', ');
}

// 限制只能输入正数
export function validatePositiveInteger(val) {
  const str = String(val).trim();
  return /^[1-9]\d*$/.test(str) ? Number(str) : '';
}

/*
限制只能输入小数
formData：form
fieldName：属性名称
maxInt：最大整数
maxDec：最大小数
*/
export function validateDecimalField(formData, fieldName, value, maxInt, maxDec) {
  value = String(value ?? '');

  // ==================== 新增逻辑：maxDec = 0 只允许整数 ====================
  if (maxDec === 0) {
    const intRegex = new RegExp(`^\\d{0,${maxInt}}$`);

    if (intRegex.test(value)) {
      // 合法整数
      formData[fieldName] = value.slice(0, maxInt);
    } else {
      // 不合法时裁剪为前 maxInt 位整数
      const match = value.match(new RegExp(`^(\\d{0,${maxInt}})`));
      formData[fieldName] = match ? match[1] : '';
    }
    return; // 🔥 必须 return，避免走后面小数逻辑
  }

  const regex = new RegExp(`^\\d{0,${maxInt}}(\\.\\d{0,${maxDec}})?$`);
  if (regex.test(value)) {
    // 合法时保留当前输入
    const parts = value.split('.');
    const intPart = parts[0].slice(0, maxInt);
    const decPart = parts[1] ?? ''; // 允许没有小数部分
    formData[fieldName] = parts.length > 1 ? `${intPart}.${decPart}` : intPart;
  } else {
    // 非法则清空或裁剪（建议裁剪而非清空）
    const match = value.match(new RegExp(`^(\\d{0,${maxInt}})(?:\\.(\\d{0,${maxDec}})?)?`));

    if (match) {
      const intPart = match[1];
      const decPart = match[2] ?? '';
      formData[fieldName] = decPart ? `${intPart}.${decPart}` : intPart;
    } else {
      formData[fieldName] = '';
    }
  }
}

/*
限制只能输入负数或正数小数
formData：form
fieldName：属性名称
maxInt：最大整数位数（不含负号）
maxDec：最大小数位数
*/
export function validateDecimalFieldAllowNegative(formData, fieldName, value, maxInt, maxDec) {
  value = String(value ?? '');

  const regex = new RegExp(`^-?\\d{0,${maxInt}}(\\.\\d{0,${maxDec}})?$`);
  if (regex.test(value)) {
    const sign = value.startsWith('-') ? '-' : '';
    const numeric = sign ? value.slice(1) : value;
    const parts = numeric.split('.');
    const intPart = parts[0].slice(0, maxInt);
    const decPart = parts[1] ?? '';

    formData[fieldName] = sign + (parts.length > 1 ? `${intPart}.${decPart}` : intPart);
  } else {
    const match = value.match(new RegExp(`^(-?)(\\d{0,${maxInt}})(?:\\.(\\d{0,${maxDec}})?)?`));
    if (match) {
      const sign = match[1];
      const intPart = match[2];
      const decPart = match[3] ?? '';
      formData[fieldName] = sign + (decPart ? `${intPart}.${decPart}` : intPart);
    } else {
      formData[fieldName] = '';
    }
  }
}


// 去掉末尾多余0，0或无效值返回空
export function fmtNum(value) {
  if (value == null) return '';

  const num = Number(value);
  if (isNaN(num) || num === 0) return '';

  // 精确控制小数去 0，只保留整数部分或精简小数位
  return parseFloat(num.toString()).toString();
}

// 百分比格式化（带 %）
export function fmtPct(value) {
  if (value == null) return '';

  const num = Number(value);
  if (isNaN(num) || num === 0) return '';

  return parseFloat(num.toString()).toString() + '%';
}


// 根据 预算 核算 计算偏差额 偏差率
export function calcDeviation(budgetValue, accountingValue) {
  const budget = parseFloat(budgetValue); // 预算
  const accounting = parseFloat(accountingValue); // 核算

  if (!isNaN(budget) && budget !== 0 && !isNaN(accounting) && accounting !== 0) {
    const deviationAmt = parseFloat((accounting - budget).toFixed(3)); // 核算 - 预算
    const deviationRate = parseFloat(((accounting - budget) / budget * 100).toFixed(3)); // 偏差额 / 预算 * 100
    return { deviationAmt, deviationRate };
  } else {
    return { deviationAmt: undefined, deviationRate: undefined };
  }
}


// 获取文件名称
// "http://x.com/a/b/c.jpg"      	"c.jpg"
// "C:\\folder\\abc.doc"      	  "abc.doc"
// "filename.txt"v	              "filename.txt"
// "http://x.com/path/to/"      	"to"
// null / undefined	              ""
export function getFileName(filePath) {
  if (typeof filePath !== 'string') return '';

  // 替换反斜杠为斜杠，统一处理（支持 Windows 路径）
  const normalizedPath = filePath.replace(/\\/g, '/');

  // 去除末尾斜杠
  const cleanPath = normalizedPath.replace(/\/+$/, '');

  const lastSlash = cleanPath.lastIndexOf('/');
  return lastSlash >= 0 ? cleanPath.slice(lastSlash + 1) : cleanPath;
}

/**
 * 校验上传文件大小和格式
 * @param {File} file - 文件对象
 * @param {Object} options - 配置项
 * @param {number} options.maxSizeMB - 最大文件大小（单位：MB，默认 20）
 * @param {string[]} options.allowTypes - 允许的文件扩展名（默认办公常用格式）
 * @returns {boolean} 是否通过校验
 */
export function validateFile(file, options = {}) {
  const maxSizeMB = options.maxSizeMB || 200;
  // const fileTypeGroups = {
  //   '文档类': ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'csv'],
  //   '图片类': ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'],
  //   '音视频类': ['mp4', 'avi', 'mov', 'flv', 'wmv', 'mkv', 'mp3', 'wav'],
  //   '压缩包类': ['zip', 'rar', '7z']
  // };
  //
  // // 合并所有允许类型（用于校验）
  // const allowTypes = options.allowTypes ||
  //   Object.values(fileTypeGroups).flat();

  const isLtMax = file.size / 1024 / 1024 < maxSizeMB;
  if (!isLtMax) {
    Message.error(`上传文件大小不能超过 ${maxSizeMB}MB!`);
    return false;
  }

  // const fileExt = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();
  // if (!allowTypes.includes(fileExt)) {
  //   // 拼接分组格式提示
  //   const groupedMsg = Object.entries(fileTypeGroups)
  //     .map(([group, types]) => `${group}：${types.join('/')}`)
  //     .join('\n');
  //
  //   Message.error(`文件格式不正确，请上传以下类型文件：\n${groupedMsg}`);
  //   return false;
  // }

  return true;
}

// 通用的折叠动画对象
export const foldTransition = {
  beforeEnter(el) {
    el.style.height = '0';
    el.style.opacity = '0';
  },
  enter(el) {
    el.style.transition = 'all 0.3s ease';
    el.style.height = el.scrollHeight + 'px';
    el.style.opacity = '1';
  },
  afterEnter(el) {
    el.style.height = 'auto';
  },
  beforeLeave(el) {
    el.style.height = el.scrollHeight + 'px';
    el.style.opacity = '1';
  },
  leave(el) {
    el.style.transition = 'all 0.3s ease';
    void el.offsetHeight; // 强制 reflow
    el.style.height = '0';
    el.style.opacity = '0';
  },
  afterLeave(el) {
    el.style.height = 'auto';
  }
};

/**
 * 从树中查找节点路径，返回 idPath 和 labelPath（都以 / 拼接）
 * @param {Array} nodes - 树结构
 * @param {String|Number} targetId - 要查找的目标 id
 * @param {Array} path - 用于递归记录路径
 * @returns {{ idPath: string, labelPath: string } | null}
 */
export function findTreePathInfoById(nodes, targetId, path = []) {
  for (const node of nodes) {
    const currentPath = [...path, { id: node.id, label: node.label ?? node.name }];

    if (node.id === targetId || node.value === targetId) {
      const idPath = currentPath.map(item => item.id).join('/');
      const labelPath = currentPath.map(item => item.label).join('/');
      return { idPath, labelPath };
    }

    if (Array.isArray(node.children) && node.children.length > 0) {
      const result = findTreePathInfoById(node.children, targetId, currentPath);
      if (result) return result;
    }
  }
  return null;
}

/**
 * 通用联想搜索下拉（支持 nickName 转换字段）
 * @param {Array<Object>} list - 原始数据列表
 * @param {String} queryString - 用户输入
 * @param {Function} callback - el-autocomplete 的 callback
 * @param {Array<String>|String} fieldNames - 展示字段名（1个或多个）
 * @param {Array<Object>} userList - 用户列表（用于 userName 转 nickName）
 * @param {Array<String>} needConvertFields - 需要 userName → nickName 的字段名数组（如 ['projectManager']）
 */
export function suggestFieldHistory(list, queryString, callback, fieldNames, userList = [], needConvertFields = []) {

  const rawList = list || [];
  const fields = Array.isArray(fieldNames) ? fieldNames : [fieldNames];
  const convertSet = new Set(needConvertFields); // 转为 Set，方便判断

  // 去重（以第一个字段为 key）
  const uniqueMap = new Map();
  rawList.forEach(item => {
    const key = item[fields[0]];
    if (key) {
      uniqueMap.set(key, item);
    }
  });

  const values = Array.from(uniqueMap.values());

  // 模糊匹配（支持转换字段）
  const results = queryString
    ? values.filter(item =>
      fields.some(field => {
        const rawVal = item[field];
        const displayVal = convertSet.has(field)
          ? getNickNameByUserName(rawVal, userList)
          : rawVal;
        return String(displayVal || '').toLowerCase().includes(queryString.toLowerCase());
      })
    )
    : values;

  // 拼接展示内容
  const formatted = results.map(item => {
    const label = fields
      .map(field => {
        const rawVal = item[field];
        return convertSet.has(field)
          ? getNickNameByUserName(rawVal, userList)
          : rawVal;
      })
      .filter(Boolean)
      .join(' - ');

    return {
      value: label,
      ...item
    };
  });

  callback(formatted);
}

/**
 * 求数组中指定属性的数值和（保留两位小数）
 * @param {Array} list - 源数组
 * @param {String} prop - 属性名
 * @returns {Number} - 求和后的结果，保留两位小数
 */
export function sumProperty(list, prop) {
  if (!Array.isArray(list) || !prop) {
    return 0;
  }
  const total = list.reduce((sum, item) => {
    const val = Number(item[prop]);
    return sum + (isNaN(val) ? 0 : val);
  }, 0);
  return Number(total.toFixed(2));
}

/**
 * 播放系统提示音
 * @param {string} path 可选，音频路径（默认是 /assets/mp3/4089.wav）
 */
let lastPlayTime = 0;

/**
 * 播放系统提示音
 * @param {string} path 可选，自定义音频路径
 */
export function playAudio(path) {
  const now = Date.now();
  if (now - lastPlayTime < 800) return; // 防止短时间重复播放
  lastPlayTime = now;

  try {
    const audio = new Audio(require('@/assets/mp3/4089.wav'));
    if (path) audio.src = path;

    audio.play().catch(() => {
      console.warn('音频自动播放被浏览器阻止，请在用户交互后触发');
    });
  } catch (e) {
    console.error('播放音效失败：', e);
  }
}

let blinkTimer = null
let blinkFlag = false
let originalTitle = document.title

/**
 * 开始闪烁浏览器标签标题
 * @param {string} text 自定义提示文本（默认：'【新消息】'）
 */
export function startBlinkTitle(text = '【新消息】') {
  if (blinkTimer) return  // 已经在闪烁中则不重复

  originalTitle = document.title
  blinkTimer = setInterval(() => {
    blinkFlag = !blinkFlag
    document.title = blinkFlag ? `${text}${originalTitle}` : `【　　　】${originalTitle}`
  }, 800)
}

/**
 * 停止闪烁，恢复原标题
 */
export function stopBlinkTitle() {
  if (blinkTimer) {
    clearInterval(blinkTimer)
    blinkTimer = null
  }
  document.title = originalTitle
}

import request from '@/utils/request'

// *****************************************统计分析*****************************************
// 统计分析 - 供应商 计费人数
export function deptBillingCount(query) {
  return request({
    url: '/chart/statistics/deptBillingCount',
    method: 'get',
    params: query
  })
}

// 统计分析 - 招聘员工 计费人数
export function personBillingCount(query) {
  return request({
    url: '/chart/statistics/personBillingCount',
    method: 'get',
    params: query
  })
}

// 统计分析 - 后加的计费率
export function rateCalculation(query) {
  return request({
    url: '/chart/statistics/rateCalculation',
    method: 'get',
    params: query
  })
}

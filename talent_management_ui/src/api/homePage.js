import request from '@/utils/request'

// *****************************************面试点位*****************************************

// 查询邀约量
export function invitationCount(query) {
  return request({
    url: '/chart/homePage/invitationCount',
    method: 'get',
    params: query
  })
}

// 邀约量top5
export function invitationTop5(query) {
  return request({
    url: '/chart/homePage/invitationTop5',
    method: 'get',
    params: query
  })
}

// 供应商公告
export function sysNoticeList(query) {
  return request({
    url: '/chart/homePage/sysNoticeList',
    method: 'get',
    params: query
  })
}

// 统计供应商明日报备点位人数
export function tomorrowReportCount(query) {
  return request({
    url: '/chart/homePage/tomorrowReportCount',
    method: 'get',
    params: query
  })
}


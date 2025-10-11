import request from '@/utils/request'

// 查询在线用户列表
export function list(query) {
  return request({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  })
}

// 强退用户
export function forceLogout(tokenId) {
  return request({
    url: '/monitor/online/' + tokenId,
    method: 'delete'
  })
}

// 查询全部在线用户列表
export function allOnlineList(query) {
  return request({
    url: '/monitor/online/allOnlineList',
    method: 'get',
    params: query
  })
}

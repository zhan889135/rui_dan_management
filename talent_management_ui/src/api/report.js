import request from '@/utils/request'

// *****************************************面试报备*****************************************
// 分页查询
export function list(query) {
  return request({
    url: '/interview/report/query',
    method: 'get',
    params: query
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/interview/report/save',
    method: 'post',
    data: data
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/interview/report/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/interview/report/${id}`,
    method: 'get'
  })
}

// 查询全部
export function listAll() {
  return request({
    url: '/interview/report/allList',
    method: 'get',
  })
}

import request from '@/utils/request'

// *****************************************面试点位*****************************************
// 查询全部（不做供应商数据权限）为了公司名称下拉历史输入
export function allListNoDept(query) {
  return request({
    url: '/interview/location/allListNoDept',
    method: 'get',
    params: query
  })
}
// 分页查询
export function list(query) {
  return request({
    url: '/interview/location/query',
    method: 'get',
    params: query
  })
}

// 查询全部
export function listAll() {
  return request({
    url: '/interview/location/allList',
    method: 'get',
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/interview/location/save',
    method: 'post',
    data: data
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/interview/location/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/interview/location/${id}`,
    method: 'get'
  })
}

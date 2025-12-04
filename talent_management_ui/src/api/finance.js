import request from '@/utils/request'

// *****************************************招聘需求*****************************************
// 分页查询
export function list(query) {
  return request({
    url: '/interview/finance/query',
    method: 'get',
    params: query
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/interview/finance/save',
    method: 'post',
    data: data
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/interview/finance/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/interview/finance/${id}`,
    method: 'get'
  })
}

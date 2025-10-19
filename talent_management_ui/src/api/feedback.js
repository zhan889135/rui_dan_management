import request from '@/utils/request'

// *****************************************面试反馈*****************************************
// 分页查询
export function list(query) {
  return request({
    url: '/interview/feedback/query',
    method: 'get',
    params: query
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/interview/feedback/save',
    method: 'post',
    data: data
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/interview/feedback/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/interview/feedback/${id}`,
    method: 'get'
  })
}

// 二级一键推送数据
export function secondPushData(query) {
  return request({
    url: '/interview/feedback/secondPushData',
    method: 'get',
    params: query
  })
}

// 校验手机号是否存在
export function verifyIsExist(data) {
  return request({
    url: '/interview/feedback/verifyIsExist',
    method: 'post',
    data: data
  })
}

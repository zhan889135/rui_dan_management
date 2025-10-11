import request from '@/utils/request'

// *****************************************供应商面试点位*****************************************
// 查询全部
export function listAll() {
  return request({
    url: '/sys/location/allList',
    method: 'get',
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/sys/location/save',
    method: 'post',
    data: data
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/sys/location/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/sys/location/${id}`,
    method: 'get'
  })
}

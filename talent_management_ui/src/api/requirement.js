import request from '@/utils/request'

// *****************************************招聘需求*****************************************
// 分页查询
export function list(query) {
  return request({
    url: '/interview/requirement/query',
    method: 'get',
    params: query
  })
}

// 保存或更新
export function save(data) {
  return request({
    url: '/interview/requirement/save',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除
export function delData(ids) {
  return request({
    url: `/interview/requirement/${ids}`,
    method: 'delete'
  })
}

// 根据ID获取详细信息
export function getInfo(id) {
  return request({
    url: `/interview/requirement/${id}`,
    method: 'get'
  })
}

// 查询全部
export function listAll() {
  return request({
    url: '/interview/requirement/allList',
    method: 'get',
  })
}

/**
 * 获取文件
 */
export function getItemPic(id) {
  return request({
    url: '/interview/requirement/getItemPic/' + id,
    method: "GET",
    responseType: 'blob',
  })
}


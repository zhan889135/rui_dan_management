import request from '@/utils/request'

// 查询部门列表
export function listDept(query) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params: query
  })
}

// 查询部门列表（排除节点）
export function listDeptExcludeChild(deptId) {
  return request({
    url: '/system/dept/list/exclude/' + deptId,
    method: 'get'
  })
}

// 查询部门详细
export function getDept(deptId) {
  return request({
    url: '/system/dept/' + deptId,
    method: 'get'
  })
}

// 新增部门
export function addDept(data) {
  return request({
    url: '/system/dept',
    method: 'post',
    data: data
  })
}

// 修改部门
export function updateDept(data) {
  return request({
    url: '/system/dept',
    method: 'put',
    data: data
  })
}

// 删除部门
export function delDept(deptId) {
  return request({
    url: '/system/dept/' + deptId,
    method: 'delete'
  })
}

/**
 * 查询全部供应商，多级的按照/拼接
 * 微兴
 * 微兴/招聘一部
 * 微兴/招聘二部
 * 微兴/招聘三部
 * 佳新
 * 佳新/综合部门
 * 佳新/综合部门/招投标组
 * ......
 */
export function selectLevel2DeptName(query) {
  return request({
    url: '/system/dept/selectLevel2DeptName',
    method: 'get',
    params: query
  })
}

/**
 * 查询当前供应商存在的点位
 */
// export function selectSysLocationByDeptId(query) {
//   return request({
//     url: '/system/dept/selectSysLocationByDeptId',
//     method: 'get',
//     params: query
//   })
// }

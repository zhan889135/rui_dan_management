import request from '@/utils/request'

// *****************************************总部*****************************************
// 群列表
export function groupList() {
  return request({
    url: '/api/group/selectGroup',
    method: 'get'
  })
}

// 创建群
export function createGroup(data) {
  return request({
    url: '/api/group/addGroup',
    method: 'post',
    data: data
  })
}

// 修改群
export function updateGroupName(data) {
  return request({
    url: '/api/group/updateName',
    method: 'post',
    data: data
  })
}

// 解散群
export function deleteGroup(id) {
  return request({
    url: `/api/group/delGroup/${id}`,
    method: 'delete'
  })
}


// 添加成员（data 形如 {id:1,userIdParam:['u1','u2']}）
export function addMember(data) {
  return request({
    url: '/api/group/addMember',
    method: 'post',
    data: data
  })
}

// 删除成员（data 形如 {id:1,userIdParam:['u1']}）
export function removeMember(data) {
  return request({
    url: '/api/group/delMember',
    method: 'delete',
    data: data
  })
}

/**
 * 查询群历史消息
 */
export function getHistory(groupId) {
  return request({
    url: `/api/group/history/${groupId}`,
    method: 'get'
  })
}

/**
 * 标记为已读
 */
export function markRead(groupId) {
  return request({
    url: `/api/group/${groupId}/read`,
    method: 'post'
  })
}

/**
 * 查询供应商邀约总数
 */
export function selectDeptInvitationCount(query) {
  return request({
    url: '/interview/speak/selectDeptInvitationCount',
    method: 'get',
    params: query
  })
}

/**
 * 查询邀约明细
 */
export function selectInvitationInfo(query) {
  return request({
    url: '/interview/speak/selectInvitationInfo',
    method: 'get',
    params: query
  })
}

/**
 * 查询员工邀约总数
 */
export function selectPeopleInvitationCount(query) {
  return request({
    url: '/interview/speak/selectPeopleInvitationCount',
    method: 'get',
    params: query
  })
}

// 员工保存邀约信息
export function saveInvitationInfo(data) {
  return request({
    url: '/interview/speak/saveInvitationInfo',
    method: 'post',
    data: data
  })
}

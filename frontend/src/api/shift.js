import request from '@/utils/request'

export const shiftApi = {
  page(params) {
    return request({
      url: '/shift/page',
      method: 'get',
      params
    })
  },
  getDetail(id) {
    return request({
      url: `/shift/${id}`,
      method: 'get'
    })
  },
  create(data) {
    return request({
      url: '/shift',
      method: 'post',
      data
    })
  },
  updateMembers(id, data) {
    return request({
      url: `/shift/${id}/members`,
      method: 'put',
      data
    })
  },
  publish(id, supervisorId) {
    return request({
      url: `/shift/${id}/publish`,
      method: 'put',
      params: { supervisorId }
    })
  },
  complete(id, supervisorId) {
    return request({
      url: `/shift/${id}/complete`,
      method: 'put',
      params: { supervisorId }
    })
  },
  recommendMembers(id, params) {
    return request({
      url: `/shift/${id}/recommend-members`,
      method: 'get',
      params
    })
  },
  recommendTeam(id, data) {
    return request({
      url: `/shift/${id}/recommend-team`,
      method: 'post',
      data
    })
  },
  delete(id) {
    return request({
      url: `/shift/${id}`,
      method: 'delete'
    })
  }
}

export const shiftEvaluationApi = {
  getByShift(shiftId) {
    return request({
      url: `/shift-evaluation/shift/${shiftId}`,
      method: 'get'
    })
  },
  getByUser(userId) {
    return request({
      url: `/shift-evaluation/user/${userId}`,
      method: 'get'
    })
  },
  add(data, evaluatorId) {
    return request({
      url: '/shift-evaluation',
      method: 'post',
      data,
      params: { evaluatorId }
    })
  },
  batchEvaluate(shiftId, data) {
    return request({
      url: `/shift-evaluation/batch/${shiftId}`,
      method: 'post',
      data
    })
  }
}

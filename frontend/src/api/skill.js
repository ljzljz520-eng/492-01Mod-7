import request from '@/utils/request'

export const skillApi = {
  list() {
    return request({
      url: '/skill/list',
      method: 'get'
    })
  },
  getById(id) {
    return request({
      url: `/skill/${id}`,
      method: 'get'
    })
  }
}

export const userSkillApi = {
  getMatrix(params) {
    return request({
      url: '/user-skill/matrix',
      method: 'get',
      params
    })
  },
  getMatrixPage(params) {
    return request({
      url: '/user-skill/matrix/page',
      method: 'get',
      params
    })
  },
  getRiskWarning() {
    return request({
      url: '/user-skill/risk-warning',
      method: 'get'
    })
  },
  recommendWorkers(params) {
    return request({
      url: '/user-skill/recommend',
      method: 'get',
      params
    })
  },
  save(data) {
    return request({
      url: '/user-skill',
      method: 'post',
      params: data
    })
  }
}

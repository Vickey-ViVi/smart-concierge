export const COMPLAINT_TYPES = {
  ac: '空调问题',
  clean: '卫生问题',
  lost: '失物招领',
  other: '其他'
}

export const COMPLAINT_STATUS = {
  0: '待处理',
  1: '处理中',
  2: '已完成'
}

export const INTENT_LABELS = {
  food: '美食',
  entertainment: '玩乐',
  shopping: '购物',
  gift: '购物',
  route: '路线'
}

export const ROLE_MENU = {
  客服: ['complaint', 'lost'],
  运营: ['dashboard', 'question', 'shop', 'activity'],
  招商: ['proposal'],
  管理员: ['dashboard', 'question', 'complaint', 'lost', 'proposal', 'shop', 'activity', 'user', 'config', 'audit', 'monitor']
}

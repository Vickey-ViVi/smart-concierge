import { ROLE_MENU } from './constants'

export function hasMenu(role, menuKey) {
  const menus = ROLE_MENU[role] || []
  return menus.includes(menuKey)
}

export function getDefaultRoute(role) {
  const menus = ROLE_MENU[role] || []
  const first = menus[0]
  const map = {
    dashboard: '/dashboard',
    question: '/question',
    complaint: '/complaint',
    lost: '/lost',
    proposal: '/proposal',
    shop: '/shop',
    activity: '/activity',
    user: '/user',
    config: '/config',
    audit: '/audit',
    monitor: '/monitor'
  }
  return map[first] || '/login'
}

export function statusTagType(status) {
  if (status === 0) return 'danger'
  if (status === 1) return 'warning'
  return 'success'
}

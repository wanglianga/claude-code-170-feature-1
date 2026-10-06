// 平台字典
export const SOURCE_LABEL = {
  SMART_BUTTON: '智能按钮',
  PHONE: '电话',
  MINI_APP: '社区小程序'
}

export const TRIAGE_LABEL = {
  LIFE_ASSIST: '生活协助',
  BODY_DISCOMFORT: '身体不适',
  FALL_RISK: '跌倒风险',
  EMOTIONAL_CRISIS: '情绪危机',
  FALSE_TRIGGER: '误触'
}

export const SEVERITY_LABEL = {
  LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急'
}

export const SEVERITY_TAG = {
  LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger'
}

export const TARGET_LABEL = {
  GRID_WORKER: '网格员',
  NEIGHBOR_VOLUNTEER: '邻里志愿者',
  FAMILY_DOCTOR: '家庭医生',
  EMERGENCY_120: '120急救'
}

export const SERVICE_LABEL = {
  DAILY_VISIT: '日常巡访',
  MEAL_SERVICE: '助餐',
  AGING_RETROFIT: '适老化改造',
  PSYCH_COMPANION: '心理陪伴',
  TEMP_CARE: '临时照护',
  HOSPITAL_ESCORT: '送医陪诊',
  OTHER: '其他服务'
}

export const FOLLOW_RESULT_LABEL = {
  IMPROVED: '好转',
  STABLE: '平稳',
  WORSENED: '恶化',
  NO_ANSWER: '未联系上'
}

export const RISK_LABEL = {
  CANNOT_OPEN_DOOR: '无法开门',
  LOCATION_OFFSET: '定位偏移',
  FAMILY_UNREACHABLE: '家属长期不接电话',
  NEIGHBOR_INFO_UNCLEAR: '邻居代报信息不清',
  REFUSE_HOSPITAL: '老人拒绝就医',
  HOME_SAFETY_HAZARD: '家中存在安全隐患',
  FREQUENT_NIGHT_HELP: '频繁夜间求助',
  DOOR_BREAK_RESCUE: '已破门/锁匠救助'
}

export const RISK_TAG = {
  CANNOT_OPEN_DOOR: 'danger',
  LOCATION_OFFSET: 'warning',
  FAMILY_UNREACHABLE: 'danger',
  NEIGHBOR_INFO_UNCLEAR: 'warning',
  REFUSE_HOSPITAL: 'danger',
  HOME_SAFETY_HAZARD: 'danger',
  FREQUENT_NIGHT_HELP: 'danger',
  DOOR_BREAK_RESCUE: 'danger'
}

/* 无法开门多方确认 */
export const DOOR_PARTY_LABEL = {
  FAMILY: '家属',
  PROPERTY: '物业',
  NEIGHBOR: '邻居',
  POLICE: '派出所'
}
export const DOOR_PARTY_ORDER = ['FAMILY', 'PROPERTY', 'NEIGHBOR', 'POLICE']

export const DOOR_ACTION_LABEL = {
  BREAK_DOOR: '破门入户',
  LOCKSMITH: '请锁匠开锁',
  BOTH: '破门并换锁'
}

export const AUTH_LABEL = {
  FAMILY_AUTHORIZED: '家属授权',
  POLICE_ORDER: '民警现场决定',
  COMMUNITY_EMERGENCY: '社区紧急避险决定',
  OTHER: '其他依据'
}

export const EXPENSE_TYPE_LABEL = {
  LOCK_OPEN: '开锁费',
  LOCK_REPLACE: '锁具更换费',
  DOOR_REPAIR: '门体修复费',
  WINDOW_REPAIR: '窗户修复费',
  TEMP_CARE: '临时陪护费',
  OTHER: '其他费用'
}

export const BEARER_LABEL = {
  FAMILY: '家庭承担',
  COMMUNITY_AID: '社区救助',
  INSURANCE: '保险理赔',
  PENDING: '待家属确认'
}

export const BEARER_TAG = {
  FAMILY: 'warning',
  COMMUNITY_AID: 'success',
  INSURANCE: 'primary',
  PENDING: 'info'
}

export const ELDER_PLACEMENT_OPTIONS = ['送医救治', '社区临时照护点', '家属接走', '邻居暂为照护', '居家（已恢复安全）']

export const STATUS_LABEL = {
  NEW: '待研判',
  TRIAGED: '已研判',
  DISPATCHED: '已转介',
  ONSITE: '已上门',
  SERVICED: '已服务',
  COMMUNICATED: '已沟通家属',
  FOLLOWED_UP: '待回访归档',
  CLOSED: '已归档'
}

export const STATUS_TAG = {
  NEW: 'danger',
  TRIAGED: 'warning',
  DISPATCHED: 'warning',
  ONSITE: 'primary',
  SERVICED: 'primary',
  COMMUNICATED: 'primary',
  FOLLOWED_UP: 'warning',
  CLOSED: 'success'
}

export const RISK_LEVEL_LABEL = {
  LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '极高风险'
}

export const RISK_LEVEL_TAG = {
  LOW: 'success', MEDIUM: 'info', HIGH: 'warning', CRITICAL: 'danger'
}

export const ENTRY_METHODS = ['钥匙开门', '门禁密码', '智能锁授权', '邻居协助开门', '家属到场开门', '破锁入户', '未能入户']

export const ELDER_STATUS_OPTIONS = ['清醒正常', '身体不适', '已跌倒', '意识不清', '情绪激动', '未见到老人']

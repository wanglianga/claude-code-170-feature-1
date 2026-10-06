<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/elders')" style="margin-bottom:12px">
      <template #content>
        <b style="font-size:16px">{{ data.elder?.name }}</b>
        <el-tag :type="RISK_LEVEL_TAG[data.elder?.riskLevel]" effect="dark" style="margin-left:10px">
          {{ RISK_LEVEL_LABEL[data.elder?.riskLevel] }}
        </el-tag>
      </template>
    </el-page-header>

    <el-row :gutter="12">
      <el-col :span="15">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">照护画像</span></template>
          <el-descriptions v-if="data.elder" :column="2" border size="small">
            <el-descriptions-item label="性别年龄">{{ data.elder.gender }} · {{ data.elder.age }}岁</el-descriptions-item>
            <el-descriptions-item label="电话">{{ data.elder.phone }}</el-descriptions-item>
            <el-descriptions-item label="居住状态">{{ data.elder.livingStatus }}</el-descriptions-item>
            <el-descriptions-item label="长期独居">
              <el-tag size="small" :type="data.elder.livingAloneLongTerm ? 'danger' : 'info'">
                {{ data.elder.livingAloneLongTerm ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="住址" :span="2">{{ data.elder.address }}</el-descriptions-item>
            <el-descriptions-item label="基础病" :span="2">{{ data.elder.chronicDiseases || '无' }}</el-descriptions-item>
            <el-descriptions-item label="病史用药" :span="2">{{ data.elder.medicalNote || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门禁信息" :span="2">
              <span style="color:#b88230">{{ data.elder.doorAccessInfo || '无' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="安全设施" :span="2">
              燃气报警器
              <el-icon v-if="data.elder.gasAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
              ｜独立烟感
              <el-icon v-if="data.elder.smokeAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
              ｜SOS设备
              <el-icon v-if="data.elder.sosDeviceInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
            </el-descriptions-item>
            <el-descriptions-item label="最近家属探访">{{ data.elder?.lastFamilyVisitDate || '无记录' }}</el-descriptions-item>
            <el-descriptions-item label="最近医生随访">{{ data.elder?.lastDoctorFollowUpDate || '无记录' }}</el-descriptions-item>
            <el-descriptions-item label="随访结论" :span="2">{{ data.elder?.doctorFollowUpConclusion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="家属响应评分">
              <el-rate :model-value="Math.round((data.elder?.familyResponsivenessScore||0)/20)" disabled />
              {{ data.elder?.familyResponsivenessScore }}
              <div class="muted">{{ data.elder?.familyResponsivenessNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="邻里协助可信度">
              <el-rate :model-value="Math.round((data.elder?.neighborReliabilityScore||0)/20)" disabled />
              {{ data.elder?.neighborReliabilityScore }}
            </el-descriptions-item>
            <el-descriptions-item label="过往误触记录">
              {{ data.elder?.falseTriggerCount }} 次
              <div class="muted">{{ data.elder?.falseTriggerNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="服务建议">
              <el-space wrap>
                <el-tag v-if="data.elder?.suggestDailyVisit" type="danger" effect="plain">增加日常巡访</el-tag>
                <el-tag v-if="data.elder?.suggestMealService" type="warning" effect="plain">助餐服务</el-tag>
                <el-tag v-if="data.elder?.suggestAgingRetrofit" type="info" effect="plain">适老化改造</el-tag>
                <el-tag v-if="data.elder?.suggestPsychCompanion" type="success" effect="plain">心理陪伴</el-tag>
              </el-space>
            </el-descriptions-item>
            <el-descriptions-item label="下一次主动关怀" :span="2">
              <el-tag type="warning">{{ data.elder?.proactiveCareType || '未排期' }}</el-tag>
              {{ data.elder?.nextProactiveCareAt ? fmt(data.elder.nextProactiveCareAt) : '' }}
              <div class="muted">{{ data.elder?.proactiveCareNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ data.elder?.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never">
          <template #header><span class="section-title">历史求助事件（{{ events.length }}）</span></template>
          <el-table :data="events" size="small" @row-click="r => $router.push(`/events/${r.id}`)" style="cursor:pointer">
            <el-table-column prop="eventNo" label="编号" width="125" />
            <el-table-column prop="createdAt" label="时间" width="150">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="研判" width="100">
              <template #default="{ row }">{{ row.triageType ? TRIAGE_LABEL[row.triageType] : '—' }}</template>
            </el-table-column>
            <el-table-column label="严重度" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.severity" size="small" :type="SEVERITY_TAG[row.severity]">{{ SEVERITY_LABEL[row.severity] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="风险标记" min-width="180">
              <template #default="{ row }">
                <el-tag v-for="f in (row.riskFlags||'').split(',').filter(Boolean)" :key="f" size="small"
                        :type="RISK_TAG[f]" style="margin:1px">{{ RISK_LABEL[f] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="回访" width="80">
              <template #default="{ row }">{{ row.followUpResult ? FOLLOW_RESULT_LABEL[row.followUpResult] : '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="STATUS_TAG[row.status]">{{ STATUS_LABEL[row.status] }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="9">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">紧急联系人</span>
              <el-button size="small" type="danger" plain @click="openContact()">添加</el-button>
            </div>
          </template>
          <div v-for="c in data.contacts" :key="c.id" class="person">
            <div>
              <el-tag size="small" :type="c.primaryContact ? 'danger' : 'info'">{{ c.relationship }}</el-tag>
              <b style="margin:0 6px">{{ c.name }}</b>
              <span class="muted">{{ c.phone }}</span>
              <el-tag v-if="c.oftenUnreachable" size="small" type="danger" effect="dark" style="margin-left:6px">长期不接</el-tag>
              <div class="muted">联系 {{ c.contactAttempts }} 次 / 接通 {{ c.contactReached }} 次 {{ c.reachabilityNote }}</div>
            </div>
            <el-button link type="danger" size="small" @click="delContact(c.id)">删除</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">邻里响应人</span>
              <el-button size="small" type="danger" plain @click="openNeighbor()">添加</el-button>
            </div>
          </template>
          <div v-for="n in data.neighbors" :key="n.id" class="person">
            <div>
              <b>{{ n.name }}</b>
              <span class="muted" style="margin-left:6px">{{ n.phone }} · {{ n.proximity }}</span>
              <div>
                <el-rate :model-value="Math.round(n.reliabilityScore/20)" disabled size="small" />
                <span class="muted">响应 {{ n.effectiveCount }}/{{ n.callCount }} 次</span>
                <span class="muted" v-if="n.note"> · {{ n.note }}</span>
              </div>
            </div>
            <el-button link type="danger" size="small" @click="delNeighbor(n.id)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="contactDialog" title="添加紧急联系人" width="440px">
      <el-form :model="contactForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="contactForm.name" /></el-form-item>
        <el-form-item label="关系"><el-input v-model="contactForm.relationship" placeholder="儿子/女儿" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="contactForm.phone" /></el-form-item>
        <el-form-item label="主要联系人"><el-switch v-model="contactForm.primaryContact" /></el-form-item>
        <el-form-item label="长期不接"><el-switch v-model="contactForm.oftenUnreachable" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactDialog = false">取消</el-button>
        <el-button type="danger" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="neighborDialog" title="添加邻里响应人" width="440px">
      <el-form :model="neighborForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="neighborForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="neighborForm.phone" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="neighborForm.proximity" placeholder="如 对门502" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="neighborDialog = false">取消</el-button>
        <el-button type="danger" @click="saveNeighbor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import api from '../api'
import {
  RISK_LEVEL_LABEL, RISK_LEVEL_TAG, TRIAGE_LABEL, SEVERITY_LABEL, SEVERITY_TAG,
  RISK_LABEL, RISK_TAG, STATUS_LABEL, STATUS_TAG, FOLLOW_RESULT_LABEL
} from '../api/dict'

const route = useRoute()
const id = route.params.id
const data = ref({})
const events = ref([])
const loading = ref(false)
const contactDialog = ref(false)
const neighborDialog = ref(false)
const contactForm = reactive({})
const neighborForm = reactive({})

async function load() {
  loading.value = true
  try {
    data.value = await api.get(`/elders/${id}`)
    events.value = await api.get('/events', { params: { elderId: id } })
  } finally {
    loading.value = false
  }
}

function openContact() {
  Object.assign(contactForm, { name: '', relationship: '', phone: '', primaryContact: true, oftenUnreachable: false })
  contactDialog.value = true
}
async function saveContact() {
  await api.post(`/elders/${id}/contacts`, contactForm)
  ElMessage.success('已添加')
  contactDialog.value = false
  load()
}
async function delContact(cid) {
  await ElMessageBox.confirm('删除该联系人？', '确认', { type: 'warning' })
  await api.delete(`/elders/contacts/${cid}`)
  load()
}

function openNeighbor() {
  Object.assign(neighborForm, { name: '', phone: '', proximity: '' })
  neighborDialog.value = true
}
async function saveNeighbor() {
  await api.post(`/elders/${id}/neighbors`, neighborForm)
  ElMessage.success('已添加')
  neighborDialog.value = false
  load()
}
async function delNeighbor(nid) {
  await ElMessageBox.confirm('删除该邻里响应人？', '确认', { type: 'warning' })
  await api.delete(`/elders/neighbors/${nid}`)
  load()
}

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.person { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px dashed #ebeef5; }
</style>

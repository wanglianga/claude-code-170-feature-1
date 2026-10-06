<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="section-title" style="margin:0">独居老人档案</span>
        <div>
          <el-input v-model="keyword" placeholder="按姓名搜索" clearable style="width:180px;margin-right:8px"
                    @keyup.enter="load" @clear="load" />
          <el-select v-model="riskLevel" placeholder="风险等级" clearable style="width:130px;margin-right:8px"
                     @change="load">
            <el-option v-for="(v,k) in RISK_LEVEL_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
          <el-button type="danger" :icon="Plus" @click="openEdit()">新建档案</el-button>
        </div>
      </div>
    </template>

    <el-table :data="elders" stripe v-loading="loading"
              @row-click="row => $router.push(`/elders/${row.id}`)" style="cursor:pointer">
      <el-table-column label="老人" width="150">
        <template #default="{ row }">
          <b>{{ row.name }}</b>
          <span class="muted"> {{ row.gender }} · {{ row.age }}岁</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="住址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="chronicDiseases" label="基础病" min-width="150" show-overflow-tooltip />
      <el-table-column label="风险" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="RISK_LEVEL_TAG[row.riskLevel]" effect="dark">
            {{ RISK_LEVEL_LABEL[row.riskLevel] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="安全设施" width="170">
        <template #default="{ row }">
          <el-icon v-if="row.gasAlarmInstalled" color="#67c23a" title="燃气报警器"><CircleCheck /></el-icon>
          <el-icon v-else color="#c0c4cc" title="无燃气报警器"><CircleClose /></el-icon>
          <el-icon style="margin-left:8px" v-if="row.smokeAlarmInstalled" color="#67c23a" title="烟感"><CircleCheck /></el-icon>
          <el-icon style="margin-left:8px" v-if="row.sosDeviceInstalled" color="#67c23a" title="SOS设备"><CircleCheck /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="家属/邻里评分" width="150">
        <template #default="{ row }">
          <span>家属 {{ row.familyResponsivenessScore }}</span>
          <el-divider direction="vertical" />
          <span>邻里 {{ row.neighborReliabilityScore }}</span>
        </template>
      </el-table-column>
      <el-table-column label="误触" width="70">
        <template #default="{ row }">{{ row.falseTriggerCount }} 次</template>
      </el-table-column>
      <el-table-column label="下次关怀" width="170">
        <template #default="{ row }">
          {{ row.proactiveCareType || '—' }}
          <span class="muted" v-if="row.nextProactiveCareAt">{{ fmt(row.nextProactiveCareAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editDialog" :title="form.id ? '编辑老人档案' : '新建老人档案'" width="680px" top="5vh">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="8">
          <el-col :span="8"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-select v-model="form.gender" style="width:100%"><el-option label="女" value="女" /><el-option label="男" value="男" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="50" :max="120" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="居住状态"><el-input v-model="form.livingStatus" placeholder="长期独居" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="住址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="住址经度"><el-input v-model.number="form.homeLongitude" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="住址纬度"><el-input v-model.number="form.homeLatitude" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="基础病"><el-input v-model="form.chronicDiseases" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="病史用药"><el-input v-model="form.medicalNote" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="门禁信息"><el-input v-model="form.doorAccessInfo" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="燃气报警器"><el-switch v-model="form.gasAlarmInstalled" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="烟感"><el-switch v-model="form.smokeAlarmInstalled" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="SOS设备"><el-switch v-model="form.sosDeviceInstalled" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最近家属探访"><el-date-picker v-model="form.lastFamilyVisitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最近医生随访"><el-date-picker v-model="form.lastDoctorFollowUpDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="随访结论"><el-input v-model="form.doctorFollowUpConclusion" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import { RISK_LEVEL_LABEL, RISK_LEVEL_TAG } from '../api/dict'

const elders = ref([])
const loading = ref(false)
const keyword = ref('')
const riskLevel = ref('')
const editDialog = ref(false)
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const params = {}
    if (keyword.value) params.keyword = keyword.value
    if (riskLevel.value) params.riskLevel = riskLevel.value
    elders.value = await api.get('/elders', { params })
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) Object.assign(form, JSON.parse(JSON.stringify(row)))
  else Object.assign(form, {
    name: '', gender: '女', age: 80, phone: '', address: '', livingStatus: '长期独居',
    chronicDiseases: '', medicalNote: '', doorAccessInfo: '', riskLevel: 'MEDIUM',
    gasAlarmInstalled: false, smokeAlarmInstalled: false, sosDeviceInstalled: true,
    homeLongitude: null, homeLatitude: null, remark: ''
  })
  editDialog.value = true
}

async function save() {
  if (!form.name || !form.address) { ElMessage.warning('姓名和住址必填'); return }
  if (form.id) await api.put(`/elders/${form.id}`, form)
  else await api.post('/elders', form)
  ElMessage.success('已保存')
  editDialog.value = false
  load()
}

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

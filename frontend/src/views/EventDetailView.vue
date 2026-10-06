<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/workbench')" style="margin-bottom:12px">
      <template #content>
        <span style="font-weight:700;font-size:16px">{{ d.event?.eventNo }}</span>
        <el-tag :type="STATUS_TAG[d.event?.status]" style="margin-left:10px">{{ STATUS_LABEL[d.event?.status] }}</el-tag>
        <el-tag v-if="d.event?.nightHelp" type="warning" effect="dark" style="margin-left:6px">夜间求助</el-tag>
        <el-tag v-for="f in flags" :key="f" :type="RISK_TAG[f]" effect="dark" size="small" style="margin-left:6px">
          {{ RISK_LABEL[f] }}
        </el-tag>
      </template>
      <template #extra>
        <el-button-group>
          <el-button :icon="Plus" @click="flagDialog = true">补充风险标记</el-button>
        </el-button-group>
      </template>
    </el-page-header>

    <el-row :gutter="12">
      <!-- 左：处置区 -->
      <el-col :span="17">
        <!-- 求助信息 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">求助信息</span></template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="求助来源">
              {{ SOURCE_LABEL[d.event?.source] }}
              <span v-if="d.event?.sourceDetail" class="muted">（{{ d.event.sourceDetail }}）</span>
            </el-descriptions-item>
            <el-descriptions-item label="求助时间">{{ fmt(d.event?.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="定位">
              {{ d.event?.callAddress || '—' }}
              <span class="muted" v-if="d.event?.callLongitude">
                （{{ d.event.callLongitude }}, {{ d.event.callLatitude }}）
              </span>
              <el-tag v-if="d.event?.locationOffset" type="danger" size="small" style="margin-left:6px">
                偏移 {{ d.event.locationOffsetMeters }} 米
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="响应时长">
              <span v-if="d.event?.responseMinutes != null">{{ d.event.responseMinutes }} 分钟到场</span>
              <span v-else class="muted">尚未到场</span>
            </el-descriptions-item>
            <el-descriptions-item label="当前语音内容" :span="2">
              {{ d.event?.voiceContent || '（无语音内容）' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 操作按钮 -->
        <el-card shadow="never" style="margin-bottom:12px" v-if="d.event?.status !== 'CLOSED'">
          <template #header><span class="section-title">处置操作</span></template>
          <el-space wrap>
            <el-button type="danger" :icon="Search" @click="openTriage"
                       v-if="!d.event?.triageType">① 值班研判</el-button>
            <el-button type="warning" :icon="Phone" @click="openDispatch"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">② 转介通知</el-button>
            <el-button type="primary" :icon="House" @click="openOnsite"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">③ 上门核实</el-button>
            <el-button type="info" :icon="ChatDotRound" @click="openComm"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">家属沟通</el-button>
            <el-button type="success" :icon="Files" @click="openService"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">④ 后续服务</el-button>
            <el-button type="danger" plain :icon="CircleCheck" @click="openFollow">
              {{ d.event?.triageType === 'FALSE_TRIGGER' ? '② 回访归档' : '⑤ 回访归档' }}
            </el-button>
            <el-button :icon="EditPen" @click="noteDialog = true">添加备注</el-button>
          </el-space>
        </el-card>

        <!-- 处置记录 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">转介与到场（{{ d.dispatches?.length || 0 }}）</span></template>
          <el-empty v-if="!d.dispatches?.length" :image-size="50" description="尚未转介" />
          <el-timeline v-else>
            <el-timeline-item v-for="x in d.dispatches" :key="x.id" :timestamp="fmt(x.notifiedAt)"
                              :type="x.effectiveResponse ? 'success' : 'primary'">
              <el-tag size="small">{{ TARGET_LABEL[x.targetType] }}</el-tag>
              <b style="margin:0 6px">{{ x.targetName }}</b>
              <span class="muted">{{ x.targetPhone }}</span>
              <el-tag size="small" :type="dispatchTag(x.status)" style="margin-left:8px">
                {{ DISPATCH_STATUS[x.status] }}
              </el-tag>
              <el-tag v-if="x.neighborReported" size="small"
                      :type="x.neighborInfoClear ? 'success' : 'danger'" style="margin-left:6px">
                邻居代报·{{ x.neighborInfoClear ? '信息清晰' : '信息不清' }}
              </el-tag>
              <div class="muted" style="margin-top:4px">
                通知人 {{ x.notifiedByName }} · {{ x.notifyChannel }}
                <el-button v-if="x.status !== 'ARRIVED' && x.status !== 'REJECTED'" link type="primary"
                           size="small" style="margin-left:10px"
                           @click="changeDispatch(x.id, 'ACCEPTED')">受理</el-button>
                <el-button v-if="x.status === 'ACCEPTED' || x.status === 'NOTIFIED'" link type="success"
                           size="small" @click="changeDispatch(x.id, 'ARRIVED')">到场</el-button>
                <el-button v-if="x.status !== 'REJECTED' && x.status !== 'ARRIVED'" link type="danger"
                           size="small" @click="changeDispatch(x.id, 'REJECTED')">无法响应</el-button>
              </div>
              <div v-if="x.note" class="muted">{{ x.note }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">上门核实记录（{{ d.visits?.length || 0 }}）</span></template>
          <el-empty v-if="!d.visits?.length" :image-size="50" description="尚未上门" />
          <div v-for="v in d.visits" :key="v.id" class="record-box">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="入户方式">
                {{ v.entryMethod }}
                <el-tag v-if="!v.entered" type="danger" size="small">未能入户</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="到场时间">{{ fmt(v.arrivedAt) }}（{{ v.visitorName }}）</el-descriptions-item>
              <el-descriptions-item label="老人状态" :span="2">{{ v.elderStatus }}</el-descriptions-item>
              <el-descriptions-item label="联系家属">
                <el-tag size="small" :type="v.familyContacted ? 'success' : 'info'">
                  {{ v.familyContacted ? '已联系' : '未联系' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="处置判断">
                <el-tag v-if="v.hospitalNeeded" type="danger" size="small">需送医</el-tag>
                <el-tag v-if="v.refuseMedical" type="danger" size="small" effect="plain">拒绝就医</el-tag>
                <el-tag v-if="v.temporaryCareNeeded" type="warning" size="small">需临时照护</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="后续服务建议" :span="2">{{ v.followServiceSuggestions || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家中安全隐患" :span="2">
                <span :style="{ color: v.safetyHazards ? '#f56c6c' : '' }">{{ v.safetyHazards || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="v.photoUrls" label="现场照片" :span="2">
                <el-image v-for="(u, i) in v.photoUrls.split(',').filter(Boolean)" :key="i"
                          :src="u" :preview-src-list="v.photoUrls.split(',')" fit="cover"
                          style="width:88px;height:88px;border-radius:6px;margin-right:8px"
                          preview-teleported />
                <span class="muted">（演示环境为示例路径）</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="v.note" label="备注" :span="2">{{ v.note }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <!-- 无法开门 · 多方确认处置 -->
        <el-card shadow="never" style="margin-bottom:12px"
                 v-if="flags.includes('CANNOT_OPEN_DOOR') || door.escalation">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title">无法开门 · 多方确认处置</span>
              <el-tag v-if="door.escalation" effect="dark" size="small"
                      :type="door.escalation.status === 'CONTACTING' ? 'danger' : 'success'">
                {{ door.escalation.status === 'CONTACTING' ? '多方联系确认中' : '已结案 · ' + DOOR_RESOLUTION_LABEL[door.escalation.resolution] }}
              </el-tag>
            </div>
          </template>

          <!-- 尚未启动 -->
          <template v-if="!door.escalation">
            <el-alert type="error" :closable="false" show-icon
                      title="老人无响应且无法入户，请启动多方确认流程"
                      description="平台将引导依次联系 家属 → 物业 → 邻居 → 派出所，逐步记录联系人、时间与反馈；如需破门或请锁匠，必须登记授权依据与现场见证人，避免后续责任争议。" />
            <el-button v-if="d.event?.status !== 'CLOSED'" type="danger" style="margin-top:10px"
                       :icon="Bell" @click="openDoorStart">启动多方确认</el-button>
          </template>

          <template v-else>
            <!-- 现场情况 -->
            <div style="margin-bottom:10px">
              <el-tag size="small" effect="plain" :type="door.escalation.elderNoResponse ? 'danger' : 'info'">
                老人{{ door.escalation.elderNoResponse ? '无响应' : '响应情况不明' }}
              </el-tag>
              <el-tag v-if="door.escalation.lightOnInside" size="small" type="warning" effect="plain"
                      style="margin-left:6px">屋内有灯光</el-tag>
              <span v-if="door.escalation.sceneNote" class="muted" style="margin-left:8px">{{ door.escalation.sceneNote }}</span>
              <div class="muted" style="margin-top:4px">
                启动于 {{ fmt(door.escalation.startedAt) }} · {{ door.escalation.startedByName }}
                <template v-if="door.escalation.resolvedAt">；结案于 {{ fmt(door.escalation.resolvedAt) }}</template>
              </div>
            </div>

            <!-- 四步引导：家属 → 物业 → 邻居 → 派出所 -->
            <el-steps :active="doorNextIdx" align-center finish-status="success"
                      style="margin-bottom:14px">
              <el-step v-for="(p, i) in DOOR_PARTY_ORDER" :key="p"
                       :title="`${i + 1}. 联系${DOOR_PARTY_LABEL[p]}`" :description="partySummary(p)" />
            </el-steps>

            <!-- 联系记录 -->
            <el-table v-if="door.steps.length" :data="door.steps" size="small" style="margin-bottom:10px">
              <el-table-column label="对象" width="76">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain">{{ DOOR_PARTY_LABEL[row.partyType] }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="contactName" label="联系人" width="100" />
              <el-table-column prop="contactPhone" label="电话" width="118" />
              <el-table-column label="结果" width="140">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.reached ? 'success' : 'danger'">
                    {{ row.reached ? '已联系上' : '未联系上' }}
                  </el-tag>
                  <el-tag v-if="row.willComeToScene" size="small" type="warning"
                          style="margin-left:4px">将到场</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="feedback" label="反馈" show-overflow-tooltip />
              <el-table-column label="时间" width="145">
                <template #default="{ row }"><span class="muted">{{ fmt(row.contactedAt) }}</span></template>
              </el-table-column>
            </el-table>

            <!-- 联系中操作 -->
            <el-space v-if="door.escalation.status === 'CONTACTING' && d.event?.status !== 'CLOSED'" wrap>
              <el-button type="primary" :icon="Phone" @click="openDoorContact">
                登记联系{{ doorNextIdx < 4 ? `（下一步：${DOOR_PARTY_LABEL[DOOR_PARTY_ORDER[doorNextIdx]]}）` : '' }}
              </el-button>
              <el-button type="success" :icon="Key" @click="openResolve">已开门 · 结案</el-button>
              <el-button type="danger" :icon="Unlock" @click="openEntry">破门 / 请锁匠</el-button>
            </el-space>

            <!-- 破门 / 锁匠处置 -->
            <template v-if="door.entry">
              <el-divider content-position="left">破门 / 锁匠处置（授权与见证留痕）</el-divider>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="处置方式">
                  <el-tag size="small" type="danger">{{ ENTRY_METHOD_LABEL[door.entry.method] }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="授权依据">
                  <el-tag size="small" type="danger" effect="plain">{{ ENTRY_AUTH_LABEL[door.entry.authorizationBasis] }}</el-tag>
                  <span style="margin-left:6px">授权人：{{ door.entry.authorizedBy }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="授权说明" :span="2">{{ door.entry.authorizationDetail || '—' }}</el-descriptions-item>
                <el-descriptions-item label="执行人">
                  {{ door.entry.executorName }}<span class="muted"> {{ door.entry.executorPhone }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="执行时间">{{ fmt(door.entry.executedAt) }}</el-descriptions-item>
                <el-descriptions-item label="现场见证人" :span="2">{{ door.entry.witnessNames }}</el-descriptions-item>
                <el-descriptions-item label="物业见证">{{ door.entry.propertyWitnessName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="民警见证">{{ door.entry.policeWitnessName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="损坏情况" :span="2">{{ door.entry.damageDescription || '—' }}</el-descriptions-item>
                <el-descriptions-item label="锁具更换">
                  <el-tag size="small" :type="door.entry.lockReplaced ? 'success' : 'warning'">
                    {{ door.entry.lockReplaced ? '已更换' : '待更换' }}
                  </el-tag>
                  <span class="muted" style="margin-left:6px">{{ door.entry.lockReplaceNote }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="门窗修复">
                  <el-tag size="small" :type="door.entry.doorWindowRepaired ? 'success' : 'warning'">
                    {{ door.entry.doorWindowRepaired ? '已修复' : '待修复' }}
                  </el-tag>
                  <span class="muted" style="margin-left:6px">{{ door.entry.repairNote }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="老人后续安置" :span="2">
                  <el-tag v-if="door.entry.elderPlacement" size="small" type="warning" effect="plain">
                    {{ PLACEMENT_LABEL[door.entry.elderPlacement] }}
                  </el-tag>
                  <span class="muted" style="margin-left:6px">{{ door.entry.placementNote || '—' }}</span>
                </el-descriptions-item>
              </el-descriptions>
              <el-button v-if="d.event?.status !== 'CLOSED'" size="small" type="primary" plain
                         style="margin-top:8px" :icon="Tools" @click="openEntryEdit">补录修复与安置</el-button>

              <!-- 费用明细 -->
              <el-divider content-position="left">费用明细（垫付与承担）</el-divider>
              <el-empty v-if="!door.costs.length" :image-size="40" description="尚未登记费用" />
              <el-table v-else :data="door.costs" size="small">
                <el-table-column label="项目" width="110">
                  <template #default="{ row }">{{ COST_TYPE_LABEL[row.itemType] }}</template>
                </el-table-column>
                <el-table-column prop="description" label="说明" show-overflow-tooltip />
                <el-table-column label="金额" width="90">
                  <template #default="{ row }">¥ {{ row.amount }}</template>
                </el-table-column>
                <el-table-column label="垫付方" width="96">
                  <template #default="{ row }">{{ COST_ADVANCE_LABEL[row.advanceBy] }}</template>
                </el-table-column>
                <el-table-column label="承担方" width="104">
                  <template #default="{ row }">
                    <el-tag size="small" :type="COST_BEARER_TAG[row.bearer]">{{ COST_BEARER_LABEL[row.bearer] }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column v-if="d.event?.status !== 'CLOSED'" label="操作" width="70">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="openCost(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button v-if="d.event?.status !== 'CLOSED'" size="small" type="warning" plain
                         style="margin-top:8px" :icon="Money" @click="openCost(null)">登记费用</el-button>
            </template>

            <!-- 家属到场确认 -->
            <template v-if="door.escalation.status === 'RESOLVED'">
              <el-divider content-position="left">家属到场确认</el-divider>
              <el-empty v-if="!door.reviews.length" :image-size="40" description="家属尚未到场确认" />
              <div v-for="r in door.reviews" :key="r.id" class="record-box">
                <el-tag size="small" :type="COST_OPINION_TAG[r.costOpinion]">{{ COST_OPINION_LABEL[r.costOpinion] }}</el-tag>
                <b style="margin:0 6px">{{ r.familyName }}</b>
                <span class="muted">{{ r.relationship }} · {{ fmt(r.arrivedAt) }} 到场</span>
                <el-tag v-if="r.viewedTrajectory" size="small" type="success" effect="plain"
                        style="margin-left:6px">已查看完整处置轨迹</el-tag>
                <div v-if="r.comment" class="muted" style="margin-top:4px">{{ r.comment }}</div>
              </div>
              <el-button v-if="d.event?.status !== 'CLOSED'" size="small" type="success" plain
                         :icon="User" @click="openReview">家属到场确认</el-button>
            </template>
          </template>
        </el-card>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header><span class="section-title">后续服务（{{ d.services?.length || 0 }}）</span></template>
              <el-empty v-if="!d.services?.length" :image-size="40" description="无" />
              <el-table v-else :data="d.services" size="small">
                <el-table-column label="类型" width="100">
                  <template #default="{ row }">{{ SERVICE_LABEL[row.serviceType] }}</template>
                </el-table-column>
                <el-table-column prop="providerName" label="提供方" />
                <el-table-column prop="content" label="内容" show-overflow-tooltip />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header><span class="section-title">家属沟通（{{ d.communications?.length || 0 }}）</span></template>
              <el-empty v-if="!d.communications?.length" :image-size="40" description="无" />
              <div v-for="c in d.communications" :key="c.id" style="margin-bottom:8px">
                <el-tag size="small" :type="c.reached ? 'success' : 'danger'">
                  {{ c.reached ? '已接通' : '未接通' }}
                </el-tag>
                <b style="margin:0 6px">{{ c.contactName }}</b>
                <span class="muted">{{ c.attitude }} · {{ fmt(c.communicatedAt) }}</span>
                <div v-if="c.note" class="muted" style="padding-left:4px">{{ c.note }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" v-if="d.followUps?.length">
          <template #header><span class="section-title">回访记录</span></template>
          <div v-for="f in d.followUps" :key="f.id" class="record-box">
            <el-tag :type="f.result === 'IMPROVED' ? 'success' : f.result === 'WORSENED' ? 'danger' : 'info'">
              {{ FOLLOW_RESULT_LABEL[f.result] }}
            </el-tag>
            <span style="margin:0 8px">{{ f.method }} · 满意度 {{ f.satisfaction || '—' }}/5 · {{ fmt(f.followedUpAt) }}</span>
            <el-tag type="warning" effect="plain" v-if="f.nextCareType">
              下一次关怀：{{ SERVICE_LABEL[f.nextCareType] }} · {{ fmt(f.nextCareAt) }}
            </el-tag>
            <div v-if="f.note" class="muted" style="margin-top:4px">{{ f.note }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右：老人画像 + 时间线 -->
      <el-col :span="7">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">老人照护画像</span>
              <el-link type="primary" @click="$router.push(`/elders/${d.elder?.id}`)">完整档案</el-link>
            </div>
          </template>
          <template v-if="d.elder">
            <div class="elder-head">
              <el-avatar :size="46" style="background:#c45656">{{ d.elder.name?.[0] }}</el-avatar>
              <div>
                <div style="font-weight:700;font-size:16px">
                  {{ d.elder.name }}
                  <el-tag size="small" :type="RISK_LEVEL_TAG[d.elder.riskLevel]" effect="dark">
                    {{ RISK_LEVEL_LABEL[d.elder.riskLevel] }}
                  </el-tag>
                </div>
                <div class="muted">{{ d.elder.gender }} · {{ d.elder.age }}岁 · {{ d.elder.livingStatus }}</div>
              </div>
            </div>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="基础病">{{ d.elder.chronicDiseases || '无' }}</el-descriptions-item>
              <el-descriptions-item label="用药备注">{{ d.elder.medicalNote || '—' }}</el-descriptions-item>
              <el-descriptions-item label="住址">{{ d.elder.address }}</el-descriptions-item>
              <el-descriptions-item label="门禁信息">
                <span style="color:#b88230">{{ d.elder.doorAccessInfo || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="安全设施">
                <el-icon :color="d.elder.gasAlarmInstalled ? '#67c23a' : '#f56c6c'">
                  <CircleCheck v-if="d.elder.gasAlarmInstalled" /><CircleClose v-else />
                </el-icon> 燃气报警器
                <el-icon style="margin-left:10px" v-if="d.elder.smokeAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon> 烟感
                <el-icon style="margin-left:10px" v-if="d.elder.sosDeviceInstalled" color="#67c23a"><CircleCheck /></el-icon> SOS设备
              </el-descriptions-item>
              <el-descriptions-item label="最近家属探访">{{ d.elder.lastFamilyVisitDate || '无记录' }}</el-descriptions-item>
              <el-descriptions-item label="医生随访">{{ d.elder.lastDoctorFollowUpDate || '无记录' }}</el-descriptions-item>
              <el-descriptions-item label="随访结论">{{ d.elder.doctorFollowUpConclusion || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家属响应评分">
                <el-rate :model-value="Math.round((d.elder.familyResponsivenessScore||0)/20)" disabled size="small" />
                {{ d.elder.familyResponsivenessScore }}
              </el-descriptions-item>
              <el-descriptions-item label="邻里可信度">
                <el-rate :model-value="Math.round((d.elder.neighborReliabilityScore||0)/20)" disabled size="small" />
                {{ d.elder.neighborReliabilityScore }}
              </el-descriptions-item>
              <el-descriptions-item label="历史误触">{{ d.elder.falseTriggerCount }} 次</el-descriptions-item>
              <el-descriptions-item label="服务建议">
                <el-tag v-if="d.elder.suggestDailyVisit" size="small" type="danger" effect="plain">巡访</el-tag>
                <el-tag v-if="d.elder.suggestMealService" size="small" type="warning" effect="plain">助餐</el-tag>
                <el-tag v-if="d.elder.suggestAgingRetrofit" size="small" type="info" effect="plain">适老化改造</el-tag>
                <el-tag v-if="d.elder.suggestPsychCompanion" size="small" type="success" effect="plain">心理陪伴</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="下次主动关怀">
                {{ d.elder.proactiveCareType || '—' }}
                <span class="muted" v-if="d.elder.nextProactiveCareAt">{{ fmt(d.elder.nextProactiveCareAt) }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <div style="margin-top:10px">
              <div class="muted" style="margin-bottom:4px">紧急联系人</div>
              <div v-for="c in d.contacts" :key="c.id" class="person-row">
                <el-tag size="small" :type="c.primaryContact ? 'danger' : 'info'" effect="plain">
                  {{ c.relationship }}
                </el-tag>
                <b style="margin:0 6px">{{ c.name }}</b>
                <span class="muted">{{ c.phone }}</span>
                <el-tag v-if="c.oftenUnreachable" size="small" type="danger" style="margin-left:auto">长期不接</el-tag>
              </div>
            </div>
            <div style="margin-top:10px">
              <div class="muted" style="margin-bottom:4px">邻里响应人</div>
              <div v-for="n in d.neighbors" :key="n.id" class="person-row">
                <el-icon color="#409eff"><UserFilled /></el-icon>
                <b style="margin:0 6px">{{ n.name }}</b>
                <span class="muted">{{ n.phone }} · {{ n.proximity }}</span>
                <el-tag size="small" type="success" effect="plain" style="margin-left:auto">可信度 {{ n.reliabilityScore }}</el-tag>
              </div>
            </div>
          </template>
        </el-card>

        <el-card shadow="never">
          <template #header><span class="section-title">事件全流程时间线</span></template>
          <el-timeline class="timeline-dense">
            <el-timeline-item v-for="l in d.logs" :key="l.id" :timestamp="fmt(l.createdAt)"
                              :type="LOG_TYPE[l.type] || 'info'" size="large">
              <el-tag size="small" effect="plain" style="margin-right:6px">{{ LOG_LABEL[l.type] || l.type }}</el-tag>
              <span style="font-size:13px">{{ l.content }}</span>
              <div class="muted">{{ l.operatorName }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 研判 ===== -->
    <el-dialog v-model="triageDialog" title="值班社工研判" width="520px">
      <el-form :model="triage" label-width="92px">
        <el-form-item label="研判类型">
          <el-radio-group v-model="triage.triageType">
            <el-radio v-for="(v, k) in TRIAGE_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-radio-group v-model="triage.severity">
            <el-radio v-for="(v, k) in SEVERITY_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="研判说明">
          <el-input v-model="triage.triageNote" type="textarea" :rows="3"
                    placeholder="结合语音内容、档案基础病、历史求助与误触记录综合判断" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triageDialog = false">取消</el-button>
        <el-button type="danger" @click="submitTriage">提交研判</el-button>
      </template>
    </el-dialog>

    <!-- ===== 转介 ===== -->
    <el-dialog v-model="dispatchDialog" title="转介 / 通知" width="540px">
      <el-form :model="dispatch" label-width="100px">
        <el-form-item label="通知对象">
          <el-radio-group v-model="dispatch.targetType" @change="onTargetChange">
            <el-radio v-for="(v, k) in TARGET_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="dispatch.targetName" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="dispatch.targetPhone" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="dispatch.notifyChannel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="平台推送" value="平台推送" />
            <el-option label="短信" value="短信" />
          </el-select>
        </el-form-item>
        <el-form-item label="邻居代报">
          <el-switch v-model="dispatch.neighborReported" />
        </el-form-item>
        <el-form-item v-if="dispatch.neighborReported" label="代报信息">
          <el-radio-group v-model="dispatch.neighborInfoClear">
            <el-radio :value="true">信息清晰</el-radio>
            <el-radio :value="false">信息不清</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dispatch.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialog = false">取消</el-button>
        <el-button type="warning" @click="submitDispatch">发出通知</el-button>
      </template>
    </el-dialog>

    <!-- ===== 上门 ===== -->
    <el-dialog v-model="onsiteDialog" title="上门核实记录" width="640px" top="5vh">
      <el-form :model="onsite" label-width="110px">
        <el-form-item label="入户方式">
          <el-select v-model="onsite.entryMethod" style="width:100%">
            <el-option v-for="m in ENTRY_METHODS" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否入户">
          <el-switch v-model="onsite.entered" active-text="已入户" inactive-text="未能入户" />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" name="file"
                     :on-success="onUploaded" :show-file-list="true" accept="image/*">
            <el-button :icon="Picture">上传现场照片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="老人状态">
          <el-select v-model="onsite.elderStatus" style="width:100%" allow-create filterable>
            <el-option v-for="s in ELDER_STATUS_OPTIONS" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="联系家属"><el-switch v-model="onsite.familyContacted" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="需要送医"><el-switch v-model="onsite.hospitalNeeded" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="拒绝就医"><el-switch v-model="onsite.refuseMedical" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="临时照护"><el-switch v-model="onsite.temporaryCareNeeded" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="后续服务建议">
          <el-checkbox-group v-model="onsite.suggestions">
            <el-checkbox value="日常巡访">日常巡访</el-checkbox>
            <el-checkbox value="助餐">助餐</el-checkbox>
            <el-checkbox value="适老化改造">适老化改造</el-checkbox>
            <el-checkbox value="心理陪伴">心理陪伴</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="家中安全隐患">
          <el-input v-model="onsite.safetyHazards" type="textarea" :rows="2"
                    placeholder="如：地滑、电线老化、燃气隐患、照明不足、无扶手等" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="onsite.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onsiteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOnsite">提交上门记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 家属沟通 ===== -->
    <el-dialog v-model="commDialog" title="家属沟通记录" width="500px">
      <el-form :model="comm" label-width="92px">
        <el-form-item label="联系人">
          <el-select v-model="comm.contactId" style="width:100%" @change="onContactChange">
            <el-option v-for="c in d.contacts" :key="c.id"
                       :label="`${c.relationship} ${c.name} ${c.phone}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="comm.contactName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="comm.contactPhone" /></el-form-item>
        <el-form-item label="方式">
          <el-select v-model="comm.channel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="短信" value="短信" />
            <el-option label="微信" value="微信" />
            <el-option label="当面" value="当面" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否接通">
          <el-switch v-model="comm.reached" active-text="已接通" inactive-text="未接通" />
        </el-form-item>
        <el-form-item v-if="comm.reached" label="家属态度">
          <el-radio-group v-model="comm.attitude">
            <el-radio value="积极配合">积极配合</el-radio>
            <el-radio value="一般">一般</el-radio>
            <el-radio value="推诿拒绝">推诿拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="comm.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commDialog = false">取消</el-button>
        <el-button type="info" @click="submitComm">保存沟通记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 后续服务 ===== -->
    <el-dialog v-model="serviceDialog" title="后续服务安排" width="500px">
      <el-form :model="service" label-width="92px">
        <el-form-item label="服务类型">
          <el-select v-model="service.serviceType" style="width:100%">
            <el-option v-for="(v, k) in SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="提供方"><el-input v-model="service.providerName" placeholder="如 康泰养老服务中心 / 助餐点" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="service.providerPhone" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="service.status">
            <el-radio value="PENDING">待服务</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="DONE">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服务内容"><el-input v-model="service.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialog = false">取消</el-button>
        <el-button type="success" @click="submitService">保存服务</el-button>
      </template>
    </el-dialog>

    <!-- ===== 回访归档 ===== -->
    <el-dialog v-model="followDialog" title="回访并归档（联动更新老人画像与关怀计划）" width="540px">
      <el-alert type="info" :closable="false" style="margin-bottom:12px"
                title="归档后将更新老人风险等级、家属响应表现、邻里协助可信度，并生成下一次主动关怀计划" />
      <el-form :model="follow" label-width="100px">
        <el-form-item label="回访方式">
          <el-radio-group v-model="follow.method">
            <el-radio value="电话回访">电话回访</el-radio>
            <el-radio value="上门回访">上门回访</el-radio>
            <el-radio value="小程序">小程序</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回访结果">
          <el-radio-group v-model="follow.result">
            <el-radio v-for="(v, k) in FOLLOW_RESULT_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="满意度">
          <el-rate v-model="follow.satisfaction" />
        </el-form-item>
        <el-form-item label="回访备注">
          <el-input v-model="follow.note" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="关怀类型">
          <el-select v-model="follow.nextCareType" clearable placeholder="留空则系统按风险自动排期" style="width:100%">
            <el-option v-for="(v, k) in SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="关怀时间">
          <el-date-picker v-model="follow.nextCareAt" type="datetime" placeholder="留空则自动排期"
                          value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followDialog = false">取消</el-button>
        <el-button type="danger" @click="submitFollow">完成回访并归档</el-button>
      </template>
    </el-dialog>

    <!-- ===== 启动多方确认 ===== -->
    <el-dialog v-model="doorStartDialog" title="启动无法开门多方确认" width="480px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:12px"
                title="启动后请按 家属 → 物业 → 邻居 → 派出所 顺序依次联系并登记反馈" />
      <el-form :model="doorStart" label-width="110px">
        <el-form-item label="关联上门记录">
          <el-select v-model="doorStart.onsiteVisitId" clearable placeholder="选择未能入户的上门记录" style="width:100%">
            <el-option v-for="v in (d.visits || []).filter(x => !x.entered)" :key="v.id"
                       :label="`${fmt(v.arrivedAt)} · ${v.entryMethod}`" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="老人无响应">
          <el-switch v-model="doorStart.elderNoResponse" active-text="敲门/呼喊/电话均无回应" />
        </el-form-item>
        <el-form-item label="屋内有灯光">
          <el-switch v-model="doorStart.lightOnInside" active-text="有灯光/电视声等有人迹象" />
        </el-form-item>
        <el-form-item label="现场情况">
          <el-input v-model="doorStart.sceneNote" type="textarea" :rows="2"
                    placeholder="如：屋内灯亮着，敲门无应答，邻居称今早未见老人出门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doorStartDialog = false">取消</el-button>
        <el-button type="danger" @click="submitDoorStart">启动多方确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 登记联系步骤 ===== -->
    <el-dialog v-model="doorContactDialog" title="登记多方联系" width="500px">
      <el-form :model="doorContact" label-width="92px">
        <el-form-item label="联系对象">
          <el-radio-group v-model="doorContact.partyType" @change="onDoorPartyChange">
            <el-radio-button v-for="p in DOOR_PARTY_ORDER" :key="p" :value="p">{{ DOOR_PARTY_LABEL[p] }}</el-radio-button>
          </el-radio-group>
          <div class="muted" style="font-size:12px">须按 家属 → 物业 → 邻居 → 派出所 顺序联系，同一对象可多次登记</div>
        </el-form-item>
        <el-form-item label="联系人">
          <el-select v-if="doorContact.partyType === 'FAMILY' && d.contacts?.length"
                     v-model="doorContact._pick" placeholder="从紧急联系人选择" style="width:100%"
                     @change="pickFamilyContact" clearable>
            <el-option v-for="c in d.contacts" :key="c.id"
                       :label="`${c.relationship} ${c.name} ${c.phone}`" :value="c.id" />
          </el-select>
          <el-select v-else-if="doorContact.partyType === 'NEIGHBOR' && d.neighbors?.length"
                     v-model="doorContact._pick" placeholder="从邻里响应人选择" style="width:100%"
                     @change="pickNeighbor" clearable>
            <el-option v-for="n in d.neighbors" :key="n.id"
                       :label="`${n.name} ${n.phone}（${n.proximity}）`" :value="n.id" />
          </el-select>
          <el-input v-model="doorContact.contactName" placeholder="姓名 / 单位名称" style="margin-top:6px" />
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="doorContact.contactPhone" /></el-form-item>
        <el-form-item label="联系方式">
          <el-select v-model="doorContact.channel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="上门" value="上门" />
            <el-option label="微信" value="微信" />
            <el-option label="短信" value="短信" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否联系上">
          <el-switch v-model="doorContact.reached" active-text="已联系上" inactive-text="未联系上" />
        </el-form-item>
        <el-form-item label="将到场协助">
          <el-switch v-model="doorContact.willComeToScene" />
        </el-form-item>
        <el-form-item label="对方反馈">
          <el-input v-model="doorContact.feedback" type="textarea" :rows="2"
                    placeholder="如：儿子在外地无法赶回 / 物业无备用钥匙 / 邻居有钥匙 5 分钟到 / 民警建议破门并到场见证" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doorContactDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDoorContact">保存联系记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 已开门结案 ===== -->
    <el-dialog v-model="resolveDialog" title="已开门 · 结案" width="440px">
      <el-form :model="resolve" label-width="92px">
        <el-form-item label="开门方式">
          <el-radio-group v-model="resolve.resolution" style="display:flex;flex-direction:column;gap:8px;align-items:flex-start">
            <el-radio value="FAMILY_OPENED">家属到场开门</el-radio>
            <el-radio value="PROPERTY_OPENED">物业协助开门</el-radio>
            <el-radio value="NEIGHBOR_OPENED">邻居协助开门</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="resolve.resolveNote" type="textarea" :rows="2"
                    placeholder="如：女儿 20:40 赶到开门，老人在卧室熟睡，身体无碍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialog = false">取消</el-button>
        <el-button type="success" @click="submitResolve">确认结案</el-button>
      </template>
    </el-dialog>

    <!-- ===== 破门 / 请锁匠 ===== -->
    <el-dialog v-model="entryDialog" title="破门 / 请锁匠处置登记" width="620px" top="4vh">
      <el-alert type="error" :closable="false" show-icon style="margin-bottom:12px"
                title="须先完成四方联系（家属→物业→邻居→派出所），并留存授权依据与现场见证人，避免救助后责任争议" />
      <el-form :model="entry" label-width="110px">
        <el-form-item label="处置方式">
          <el-radio-group v-model="entry.method">
            <el-radio value="LOCKSMITH">请锁匠开锁</el-radio>
            <el-radio value="FORCED_ENTRY">破门进入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="授权依据">
          <el-select v-model="entry.authorizationBasis" style="width:100%">
            <el-option v-for="(v, k) in ENTRY_AUTH_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="授权人">
          <el-input v-model="entry.authorizedBy" placeholder="如：儿子周建国（电话授权）/ 民警王警官" />
        </el-form-item>
        <el-form-item label="授权说明">
          <el-input v-model="entry.authorizationDetail" type="textarea" :rows="2"
                    placeholder="如：20:55 与周建国通话获口头授权，通话录音编号 R20261006-03；民警警号 012345" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="执行人"><el-input v-model="entry.executorName" placeholder="锁匠/施工人员" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行人电话" label-width="90px"><el-input v-model="entry.executorPhone" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="现场见证人">
          <el-input v-model="entry.witnessNames" placeholder="多人用逗号分隔，如：赵网格员、孙阿姨（邻居）" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物业见证人"><el-input v-model="entry.propertyWitnessName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民警见证" label-width="90px"><el-input v-model="entry.policeWitnessName" placeholder="姓名/警号" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="损坏情况">
          <el-input v-model="entry.damageDescription" type="textarea" :rows="2"
                    placeholder="如：入户门锁芯损毁，门框轻微变形，无其他财物损失" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="锁具已更换"><el-switch v-model="entry.lockReplaced" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门窗已修复" label-width="90px"><el-switch v-model="entry.doorWindowRepaired" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="老人后续安置">
          <el-select v-model="entry.elderPlacement" clearable style="width:100%">
            <el-option v-for="(v, k) in PLACEMENT_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="安置说明">
          <el-input v-model="entry.placementNote" type="textarea" :rows="2"
                    placeholder="如：老人意识模糊，已送市一院急诊观察，女儿正赶往医院" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialog = false">取消</el-button>
        <el-button type="danger" @click="submitEntry">提交处置登记</el-button>
      </template>
    </el-dialog>

    <!-- ===== 补录修复与安置 ===== -->
    <el-dialog v-model="entryEditDialog" title="补录后续修复与安置" width="520px">
      <el-form :model="entryEdit" label-width="110px">
        <el-form-item label="锁具已更换">
          <el-switch v-model="entryEdit.lockReplaced" />
          <el-input v-model="entryEdit.lockReplaceNote" placeholder="如：已更换 C 级锁芯，新钥匙 2 把交家属" style="margin-top:6px" />
        </el-form-item>
        <el-form-item label="门窗已修复">
          <el-switch v-model="entryEdit.doorWindowRepaired" />
          <el-input v-model="entryEdit.repairNote" placeholder="如：门框已修复加固，当日完工" style="margin-top:6px" />
        </el-form-item>
        <el-form-item label="物业见证人">
          <el-input v-model="entryEdit.propertyWitnessName" />
        </el-form-item>
        <el-form-item label="损坏情况">
          <el-input v-model="entryEdit.damageDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="老人后续安置">
          <el-select v-model="entryEdit.elderPlacement" clearable style="width:100%">
            <el-option v-for="(v, k) in PLACEMENT_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="安置说明">
          <el-input v-model="entryEdit.placementNote" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEntryEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 费用登记 ===== -->
    <el-dialog v-model="costDialog" :title="cost.id ? '编辑费用' : '登记费用'" width="480px">
      <el-form :model="cost" label-width="92px">
        <el-form-item label="费用项目">
          <el-select v-model="cost.itemType" style="width:100%">
            <el-option v-for="(v, k) in COST_TYPE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额（元）">
          <el-input-number v-model="cost.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="cost.description" placeholder="如：夜间开锁服务费 / C 级锁芯含安装" />
        </el-form-item>
        <el-form-item label="垫付方">
          <el-radio-group v-model="cost.advanceBy">
            <el-radio-button v-for="(v, k) in COST_ADVANCE_LABEL" :key="k" :value="k">{{ v }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="承担方">
          <el-radio-group v-model="cost.bearer">
            <el-radio-button v-for="(v, k) in COST_BEARER_LABEL" :key="k" :value="k">{{ v }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="cost.note" type="textarea" :rows="2"
                    placeholder="如：待家属确认后由社区救助金结算 / 已提交意外险理赔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="costDialog = false">取消</el-button>
        <el-button type="warning" @click="submitCost">保存费用</el-button>
      </template>
    </el-dialog>

    <!-- ===== 家属到场确认 ===== -->
    <el-dialog v-model="reviewDialog" title="家属到场确认" width="480px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"
                title="请家属先查看本页完整处置轨迹（联系记录、破门授权、费用明细），再确认费用承担方案" />
      <el-form :model="review" label-width="100px">
        <el-form-item label="家属姓名">
          <el-select v-model="review._pick" placeholder="从紧急联系人选择" clearable style="width:100%"
                     @change="pickReviewFamily">
            <el-option v-for="c in d.contacts" :key="c.id"
                       :label="`${c.relationship} ${c.name} ${c.phone}`" :value="c.id" />
          </el-select>
          <el-input v-model="review.familyName" style="margin-top:6px" />
        </el-form-item>
        <el-form-item label="与老人关系"><el-input v-model="review.relationship" /></el-form-item>
        <el-form-item label="已查看轨迹">
          <el-switch v-model="review.viewedTrajectory" active-text="已查看完整处置轨迹" />
        </el-form-item>
        <el-form-item label="费用意见">
          <el-radio-group v-model="review.costOpinion" style="display:flex;flex-direction:column;gap:8px;align-items:flex-start">
            <el-radio value="AGREE">认可费用分担方案</el-radio>
            <el-radio value="PARTIAL">部分认可（有项目待核实）</el-radio>
            <el-radio value="DISAGREE">有异议（需进一步协商）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="意见说明">
          <el-input v-model="review.comment" type="textarea" :rows="2"
                    placeholder="如：开锁费与锁具费由家庭承担，门窗修复申请社区救助" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="success" @click="submitReview">保存确认记录</el-button>
      </template>
    </el-dialog>

    <!-- 风险标记 / 备注 -->
    <el-dialog v-model="flagDialog" title="补充风险标记" width="420px">
      <el-radio-group v-model="manualFlag" style="display:flex;flex-direction:column;gap:8px">
        <el-radio v-for="(v, k) in RISK_LABEL" :key="k" :value="k">{{ v }}</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="flagDialog = false">取消</el-button>
        <el-button type="danger" @click="submitFlag">添加标记</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="noteDialog" title="添加事件备注" width="440px">
      <el-input v-model="noteContent" type="textarea" :rows="3" placeholder="其他需要留痕的信息" />
      <template #footer>
        <el-button @click="noteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Phone, House, ChatDotRound, Files, CircleCheck, CircleClose,
  EditPen, Picture, UserFilled, Bell, Key, Unlock, Tools, Money, User
} from '@element-plus/icons-vue'
import api from '../api'
import {
  SOURCE_LABEL, TRIAGE_LABEL, SEVERITY_LABEL, SEVERITY_TAG, TARGET_LABEL,
  SERVICE_LABEL, FOLLOW_RESULT_LABEL, RISK_LABEL, RISK_TAG, STATUS_LABEL, STATUS_TAG,
  RISK_LEVEL_LABEL, RISK_LEVEL_TAG, ENTRY_METHODS, ELDER_STATUS_OPTIONS,
  DOOR_PARTY_LABEL, DOOR_PARTY_ORDER, DOOR_RESOLUTION_LABEL, ENTRY_METHOD_LABEL,
  ENTRY_AUTH_LABEL, COST_TYPE_LABEL, COST_ADVANCE_LABEL, COST_BEARER_LABEL,
  COST_BEARER_TAG, PLACEMENT_LABEL, COST_OPINION_LABEL, COST_OPINION_TAG
} from '../api/dict'

const route = useRoute()
const d = ref({})
const loading = ref(false)

const DISPATCH_STATUS = { NOTIFIED: '已通知', ACCEPTED: '已受理', ARRIVED: '已到场', REJECTED: '无法响应' }
const LOG_LABEL = {
  HELP: '求助', TRIAGE: '研判', DISPATCH: '转介', ONSITE: '上门', SERVICE: '服务',
  FAMILY_COMM: '家属沟通', FOLLOW_UP: '回访', CLOSE: '归档', SYSTEM: '系统', NOTE: '备注',
  DOOR: '多方确认'
}
const LOG_TYPE = {
  HELP: 'danger', TRIAGE: 'warning', DISPATCH: 'warning', ONSITE: 'primary',
  SERVICE: 'success', FAMILY_COMM: 'info', FOLLOW_UP: 'success', CLOSE: 'success',
  SYSTEM: 'info', NOTE: 'info', DOOR: 'warning'
}

const flags = computed(() => (d.value.event?.riskFlags || '').split(',').filter(Boolean))
const uploadUrl = '/api/files/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

async function load() {
  loading.value = true
  try {
    d.value = await api.get(`/events/${route.params.id}`)
  } finally {
    loading.value = false
  }
}
function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
function dispatchTag(s) {
  return { NOTIFIED: 'info', ACCEPTED: 'warning', ARRIVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

/* ---- 表单状态 ---- */
const triageDialog = ref(false)
const triage = reactive({ triageType: 'BODY_DISCOMFORT', severity: 'MEDIUM', triageNote: '' })
function openTriage() { Object.assign(triage, { triageType: 'BODY_DISCOMFORT', severity: 'MEDIUM', triageNote: '' }); triageDialog.value = true }
async function submitTriage() {
  await api.post(`/events/${route.params.id}/triage`, triage)
  ElMessage.success('研判已提交')
  triageDialog.value = false
  load()
}

const dispatchDialog = ref(false)
const dispatch = reactive({ targetType: 'GRID_WORKER', targetName: '', targetPhone: '', notifyChannel: '电话', neighborReported: false, neighborInfoClear: true, note: '' })
function openDispatch() {
  Object.assign(dispatch, { targetType: 'GRID_WORKER', targetName: '', targetPhone: '', notifyChannel: '电话', neighborReported: false, neighborInfoClear: true, note: '' })
  dispatchDialog.value = true
}
function onTargetChange(t) {
  if (t === 'NEIGHBOR_VOLUNTEER' && d.value.neighbors?.[0]) {
    dispatch.targetName = d.value.neighbors[0].name
    dispatch.targetPhone = d.value.neighbors[0].phone
  }
}
async function submitDispatch() {
  if (!dispatch.targetName) { ElMessage.warning('请填写姓名'); return }
  await api.post(`/events/${route.params.id}/dispatches`, dispatch)
  ElMessage.success('已通知')
  dispatchDialog.value = false
  load()
}
async function changeDispatch(id, status) {
  await api.put(`/events/dispatches/${id}/status`, null, { params: { status } })
  load()
}

const onsiteDialog = ref(false)
const onsite = reactive({})
function openOnsite() {
  Object.assign(onsite, {
    entryMethod: d.value.elder?.doorAccessInfo ? '智能锁授权' : '钥匙开门',
    entered: true, photoUrls: '', elderStatus: '清醒正常',
    familyContacted: false, hospitalNeeded: false, refuseMedical: false,
    temporaryCareNeeded: false, suggestions: [], safetyHazards: '', note: ''
  })
  onsiteDialog.value = true
}
function onUploaded(res) {
  if (res.code === 0) {
    onsite.photoUrls = onsite.photoUrls ? onsite.photoUrls + ',' + res.data.url : res.data.url
    ElMessage.success('照片已上传')
  }
}
async function submitOnsite() {
  if (!onsite.elderStatus) { ElMessage.warning('请填写老人状态'); return }
  await api.post(`/events/${route.params.id}/onsite`, {
    ...onsite,
    followServiceSuggestions: onsite.suggestions.join(',')
  })
  ElMessage.success('上门记录已提交')
  onsiteDialog.value = false
  await load()
  // 未能入户：引导启动多方确认流程
  if (!onsite.entered && !d.value.doorEscalation) {
    try {
      await ElMessageBox.confirm(
        '本次未能入户。若老人无响应但屋内有灯光，建议立即启动「无法开门多方确认」流程，依次联系家属→物业→邻居→派出所。',
        '启动多方确认？',
        { type: 'warning', confirmButtonText: '立即启动', cancelButtonText: '稍后处理' }
      )
      openDoorStart()
    } catch { /* 用户选择稍后处理 */ }
  }
}

/* ---- 无法开门 · 多方确认 ---- */
const door = computed(() => ({
  escalation: d.value.doorEscalation || null,
  steps: d.value.doorSteps || [],
  entry: d.value.forcedEntry || null,
  costs: d.value.doorCosts || [],
  reviews: d.value.doorReviews || []
}))
/** 下一个应联系的对象下标（0家属 1物业 2邻居 3派出所，4=均已联系） */
const doorNextIdx = computed(() => {
  let max = -1
  for (const s of door.value.steps) {
    const i = DOOR_PARTY_ORDER.indexOf(s.partyType)
    if (i > max) max = i
  }
  return Math.min(max + 1, 4)
})
function partySummary(p) {
  const list = door.value.steps.filter(s => s.partyType === p)
  if (!list.length) return '尚未联系'
  const last = list[list.length - 1]
  return `${last.contactName} · ${last.reached ? '已联系上' : '未联系上'}（共 ${list.length} 次）`
}

const doorStartDialog = ref(false)
const doorStart = reactive({ onsiteVisitId: null, elderNoResponse: true, lightOnInside: true, sceneNote: '' })
function openDoorStart() {
  const failed = (d.value.visits || []).filter(v => !v.entered)
  Object.assign(doorStart, {
    onsiteVisitId: failed.length ? failed[failed.length - 1].id : null,
    elderNoResponse: true, lightOnInside: true, sceneNote: ''
  })
  doorStartDialog.value = true
}
async function submitDoorStart() {
  await api.post(`/events/${route.params.id}/door-escalation`, doorStart)
  ElMessage.success('多方确认已启动，请依次联系家属→物业→邻居→派出所')
  doorStartDialog.value = false
  load()
}

const doorContactDialog = ref(false)
const doorContact = reactive({})
function openDoorContact() {
  const next = DOOR_PARTY_ORDER[Math.min(doorNextIdx.value, 3)]
  Object.assign(doorContact, {
    partyType: next, contactName: '', contactPhone: '', channel: '电话',
    reached: true, willComeToScene: false, feedback: '', _pick: null
  })
  onDoorPartyChange(next)
  doorContactDialog.value = true
}
function onDoorPartyChange(p) {
  doorContact._pick = null
  if (p === 'FAMILY') {
    const c = d.value.contacts?.find(x => x.primaryContact) || d.value.contacts?.[0]
    doorContact.contactName = c?.name || ''
    doorContact.contactPhone = c?.phone || ''
  } else if (p === 'NEIGHBOR') {
    const n = d.value.neighbors?.[0]
    doorContact.contactName = n?.name || ''
    doorContact.contactPhone = n?.phone || ''
  } else if (p === 'PROPERTY') {
    doorContact.contactName = '小区物业'
    doorContact.contactPhone = ''
  } else if (p === 'POLICE') {
    doorContact.contactName = '辖区派出所'
    doorContact.contactPhone = '110'
  }
}
function pickFamilyContact(id) {
  const c = d.value.contacts?.find(x => x.id === id)
  if (c) { doorContact.contactName = c.name; doorContact.contactPhone = c.phone }
}
function pickNeighbor(id) {
  const n = d.value.neighbors?.find(x => x.id === id)
  if (n) { doorContact.contactName = n.name; doorContact.contactPhone = n.phone }
}
async function submitDoorContact() {
  if (!doorContact.contactName) { ElMessage.warning('请填写联系人'); return }
  await api.post(`/door-escalations/${door.value.escalation.id}/contacts`, doorContact)
  ElMessage.success('联系记录已保存')
  doorContactDialog.value = false
  load()
}

const resolveDialog = ref(false)
const resolve = reactive({ resolution: 'FAMILY_OPENED', resolveNote: '' })
function openResolve() {
  Object.assign(resolve, { resolution: 'FAMILY_OPENED', resolveNote: '' })
  resolveDialog.value = true
}
async function submitResolve() {
  await api.post(`/door-escalations/${door.value.escalation.id}/resolve`, resolve)
  ElMessage.success('已结案：' + DOOR_RESOLUTION_LABEL[resolve.resolution])
  resolveDialog.value = false
  load()
}

const entryDialog = ref(false)
const entry = reactive({})
function openEntry() {
  Object.assign(entry, {
    method: 'LOCKSMITH', authorizationBasis: 'FAMILY_PHONE', authorizationDetail: '',
    authorizedBy: '', executorName: '', executorPhone: '',
    witnessNames: '', propertyWitnessName: '', policeWitnessName: '',
    damageDescription: '', lockReplaced: false, lockReplaceNote: '',
    doorWindowRepaired: false, repairNote: '', elderPlacement: null, placementNote: ''
  })
  entryDialog.value = true
}
async function submitEntry() {
  if (!entry.authorizedBy) { ElMessage.warning('请填写授权人'); return }
  if (!entry.executorName) { ElMessage.warning('请填写执行人'); return }
  if (!entry.witnessNames) { ElMessage.warning('请填写现场见证人'); return }
  await ElMessageBox.confirm('确认已留存授权依据与现场见证人？提交后处置单将结案', '破门/锁匠处置确认', { type: 'warning' })
  await api.post(`/door-escalations/${door.value.escalation.id}/forced-entry`, entry)
  ElMessage.success('破门/锁匠处置已登记')
  entryDialog.value = false
  load()
}

const entryEditDialog = ref(false)
const entryEdit = reactive({})
function openEntryEdit() {
  const e = door.value.entry
  Object.assign(entryEdit, {
    lockReplaced: e.lockReplaced, lockReplaceNote: e.lockReplaceNote || '',
    doorWindowRepaired: e.doorWindowRepaired, repairNote: e.repairNote || '',
    propertyWitnessName: e.propertyWitnessName || '',
    damageDescription: e.damageDescription || '',
    elderPlacement: e.elderPlacement || null, placementNote: e.placementNote || ''
  })
  entryEditDialog.value = true
}
async function submitEntryEdit() {
  await api.put(`/door-escalations/${door.value.escalation.id}/forced-entry`, entryEdit)
  ElMessage.success('后续修复与安置已更新')
  entryEditDialog.value = false
  load()
}

const costDialog = ref(false)
const cost = reactive({ id: null, itemType: 'LOCKSMITH_FEE', description: '', amount: 0, advanceBy: 'COMMUNITY', bearer: 'PENDING', note: '' })
function openCost(row) {
  if (row) {
    Object.assign(cost, {
      id: row.id, itemType: row.itemType, description: row.description || '',
      amount: Number(row.amount), advanceBy: row.advanceBy, bearer: row.bearer, note: row.note || ''
    })
  } else {
    Object.assign(cost, { id: null, itemType: 'LOCKSMITH_FEE', description: '', amount: 0, advanceBy: 'COMMUNITY', bearer: 'PENDING', note: '' })
  }
  costDialog.value = true
}
async function submitCost() {
  if (cost.amount == null || cost.amount < 0) { ElMessage.warning('请填写正确金额'); return }
  if (cost.id) {
    await api.put(`/door-costs/${cost.id}`, cost)
  } else {
    await api.post(`/forced-entries/${door.value.entry.id}/costs`, cost)
  }
  ElMessage.success('费用已保存')
  costDialog.value = false
  load()
}

const reviewDialog = ref(false)
const review = reactive({ _pick: null, familyName: '', relationship: '', viewedTrajectory: true, costOpinion: 'AGREE', comment: '' })
function openReview() {
  const c = d.value.contacts?.find(x => x.primaryContact) || d.value.contacts?.[0]
  Object.assign(review, {
    _pick: c?.id || null, familyName: c?.name || '', relationship: c?.relationship || '',
    viewedTrajectory: true, costOpinion: 'AGREE', comment: ''
  })
  reviewDialog.value = true
}
function pickReviewFamily(id) {
  const c = d.value.contacts?.find(x => x.id === id)
  if (c) { review.familyName = c.name; review.relationship = c.relationship }
}
async function submitReview() {
  if (!review.familyName) { ElMessage.warning('请填写家属姓名'); return }
  await api.post(`/door-escalations/${door.value.escalation.id}/family-review`, review)
  ElMessage.success('家属确认已记录')
  reviewDialog.value = false
  load()
}

const commDialog = ref(false)
const comm = reactive({})
function openComm() {
  const primary = d.value.contacts?.find(c => c.primaryContact) || d.value.contacts?.[0]
  Object.assign(comm, {
    contactId: primary?.id || null, contactName: primary?.name || '',
    contactPhone: primary?.phone || '', channel: '电话', reached: true, attitude: '积极配合', note: ''
  })
  commDialog.value = true
}
function onContactChange(id) {
  const c = d.value.contacts.find(x => x.id === id)
  if (c) { comm.contactName = c.name; comm.contactPhone = c.phone }
}
async function submitComm() {
  if (!comm.contactName) { ElMessage.warning('请填写联系人'); return }
  await api.post(`/events/${route.params.id}/family-communications`, comm)
  ElMessage.success('沟通记录已保存')
  commDialog.value = false
  load()
}

const serviceDialog = ref(false)
const service = reactive({})
function openService() {
  Object.assign(service, { serviceType: 'DAILY_VISIT', providerName: '', providerPhone: '', status: 'DONE', content: '' })
  serviceDialog.value = true
}
async function submitService() {
  if (!service.providerName) { ElMessage.warning('请填写提供方'); return }
  await api.post(`/events/${route.params.id}/services`, service)
  ElMessage.success('服务已保存')
  serviceDialog.value = false
  load()
}

const followDialog = ref(false)
const follow = reactive({})
function openFollow() {
  Object.assign(follow, { result: 'STABLE', method: '电话回访', satisfaction: 4, note: '', nextCareType: null, nextCareAt: null })
  followDialog.value = true
}
async function submitFollow() {
  await ElMessageBox.confirm('确认完成回访并归档？归档将联动更新老人画像', '归档确认', { type: 'warning' })
  await api.post(`/events/${route.params.id}/follow-up`, follow)
  ElMessage.success('事件已归档，画像与关怀计划已更新')
  followDialog.value = false
  load()
}

const flagDialog = ref(false)
const manualFlag = ref('CANNOT_OPEN_DOOR')
async function submitFlag() {
  await api.post(`/events/${route.params.id}/flags`, null, { params: { flag: manualFlag.value } })
  flagDialog.value = false
  load()
}
const noteDialog = ref(false)
const noteContent = ref('')
async function submitNote() {
  if (!noteContent.value) return
  await api.post(`/events/${route.params.id}/notes`, { content: noteContent.value })
  noteDialog.value = false
  noteContent.value = ''
  load()
}

function onBus() { if (d.value.event && d.value.event.status !== 'CLOSED') load() }
onMounted(() => {
  load()
  window.addEventListener('workbench-reload', onBus)
})
</script>

<style scoped>
.record-box { margin-bottom: 10px; }
.elder-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.person-row { display: flex; align-items: center; gap: 2px; padding: 5px 0; font-size: 13px; border-bottom: 1px dashed #ebeef5; }
</style>

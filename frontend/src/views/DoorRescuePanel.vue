<template>
  <el-card shadow="never" style="margin-bottom:12px" v-if="visible">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="section-title" style="margin:0">
          <el-icon color="#f56c6c"><WarningFilled /></el-icon>
          无法开门 · 多方确认与破门救助
        </span>
        <el-button v-if="!data.doorRescue && canOperate" type="danger" :icon="Unlock" @click="openRescue">
          登记破门 / 锁匠救助
        </el-button>
      </div>
    </template>

    <!-- 引导提示 -->
    <el-alert type="error" :closable="false" show-icon style="margin-bottom:12px">
      <template #title>
        社工到场后老人无响应、屋内有灯光：请按平台引导依次联系
        <b>家属 → 物业 → 邻居 → 派出所</b>，每步记录联系人、时间与反馈；仍无法入户时，
        破门/请锁匠必须留存<b>授权依据</b>与<b>现场见证人</b>，并登记修复费用与老人安置。
      </template>
    </el-alert>

    <!-- 多方联系步骤条 -->
    <el-steps :active="contactStep" align-center finish-status="success" style="margin-bottom:14px">
      <el-step v-for="p in DOOR_PARTY_ORDER" :key="p" :title="DOOR_PARTY_LABEL[p]"
               :description="stepDesc(p)" />
    </el-steps>

    <div style="margin-bottom:10px">
      <el-button v-if="canOperate" type="primary" plain :icon="PhoneFilled" @click="openContact">
        {{ data.doorContacts?.length ? '记录下一步联系（' + nextPartyLabel + '）' : '记录第 1 步联系（家属）' }}
      </el-button>
    </div>

    <el-empty v-if="!data.doorContacts?.length" :image-size="46" description="尚未进行多方联系确认" />
    <el-timeline v-else style="margin-bottom:4px">
      <el-timeline-item v-for="c in data.doorContacts" :key="c.id" :timestamp="fmt(c.contactedAt)"
                        :type="c.reached ? 'success' : 'danger'">
        <el-tag size="small" effect="dark">第{{ c.seq }}步 · {{ DOOR_PARTY_LABEL[c.partyType] }}</el-tag>
        <b style="margin:0 6px">{{ c.partyName }}</b>
        <span class="muted">{{ c.partyPhone }} · {{ c.channel }}</span>
        <el-tag size="small" :type="c.reached ? 'success' : 'danger'" style="margin-left:8px">
          {{ c.reached ? '已联系上' : '未联系上' }}
        </el-tag>
        <el-tag v-if="c.willArrive" size="small" type="warning" style="margin-left:6px">
          承诺到场{{ c.etaMinutes != null ? `（约${c.etaMinutes}分钟）` : '' }}
        </el-tag>
        <div v-if="c.feedback" style="margin-top:2px">反馈：{{ c.feedback }}</div>
        <div class="muted" style="font-size:12px">记录人 {{ c.operatorName }}<span v-if="c.note"> · {{ c.note }}</span></div>
      </el-timeline-item>
    </el-timeline>

    <!-- 破门救助记录 -->
    <template v-if="r">
      <el-divider content-position="left">破门 / 锁匠救助记录</el-divider>

      <el-alert v-if="r.familyConfirmed" type="success" :closable="false" show-icon style="margin-bottom:10px">
        <template #title>
          家属 <b>{{ r.familyConfirmerName }}</b> 已于 {{ fmt(r.familyConfirmedAt) }}
          到场查看完整处置轨迹并确认费用承担{{ r.familyConfirmNote ? '：' + r.familyConfirmNote : '' }}
        </template>
      </el-alert>
      <el-alert v-else type="warning" :closable="false" show-icon style="margin-bottom:10px"
                title="家属尚未到场确认；费用承担需家属到场查看完整处置轨迹后逐项确认" />

      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="处置方式">
          <el-tag type="danger" effect="dark">{{ DOOR_ACTION_LABEL[r.actionType] }}</el-tag>
          <span style="margin-left:8px">{{ r.providerName || '—' }} <span class="muted">{{ r.providerPhone }}</span></span>
        </el-descriptions-item>
        <el-descriptions-item label="实施时间">{{ fmt(r.rescuedAt) }}（{{ r.operatorName }}）</el-descriptions-item>

        <el-descriptions-item label="授权依据">
          <el-tag type="warning">{{ AUTH_LABEL[r.authorizationType] }}</el-tag>
          <span style="margin-left:6px">授权人 {{ r.authorizerName || '—' }} <span class="muted">{{ r.authorizerPhone }}</span></span>
          <div class="muted" style="font-size:12px">授权时间 {{ fmt(r.authorizedAt) }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="授权凭证">
          <span style="color:#409eff;word-break:break-all">{{ r.authorizationEvidence }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="r.authorizationNote" label="授权原话/紧急理由" :span="2">
          {{ r.authorizationNote }}
        </el-descriptions-item>

        <el-descriptions-item label="现场见证人" :span="2">
          <el-tag v-for="w in r.witnesses" :key="w.id" size="small" type="info" effect="plain"
                  style="margin-right:8px">
            {{ w.role }} {{ w.name }}<span v-if="w.phone"> {{ w.phone }}</span>
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item v-if="r.sceneDescription" label="现场情况" :span="2">{{ r.sceneDescription }}</el-descriptions-item>
        <el-descriptions-item v-if="r.scenePhotoUrls" label="现场照片/视频" :span="2">
          <el-image v-for="(u, i) in splitUrls(r.scenePhotoUrls)" :key="i" :src="u"
                    :preview-src-list="splitUrls(r.scenePhotoUrls)" fit="cover"
                    style="width:72px;height:72px;border-radius:6px;margin-right:6px" preview-teleported />
        </el-descriptions-item>

        <el-descriptions-item label="锁具更换">
          <el-tag size="small" :type="r.lockReplaced ? 'success' : 'info'">
            {{ r.lockReplaced ? '已更换' : '未更换' }}
          </el-tag>
          <span v-if="r.lockModel" style="margin-left:6px">{{ r.lockModel }}</span>
          <div v-if="r.newKeyKeptBy" class="muted" style="font-size:12px">新钥匙/门禁保管：{{ r.newKeyKeptBy }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="物业见证">
          <el-tag size="small" :type="r.propertyWitnessed ? 'success' : 'info'">
            {{ r.propertyWitnessed ? '已见证' : '未见证' }}
          </el-tag>
          <span v-if="r.propertyWitnessName" style="margin-left:6px">{{ r.propertyWitnessName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="门窗修复" :span="2">
          {{ r.repairArrangement || '待安排' }}
          <span v-if="r.repairedAt" class="muted">（完成于 {{ fmt(r.repairedAt) }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="费用垫付">
          {{ r.advancedBy || '—' }}
          <b v-if="r.advancedAmount != null" style="margin-left:6px">¥{{ r.advancedAmount }}</b>
        </el-descriptions-item>
        <el-descriptions-item label="老人后续安置">
          {{ r.elderPlacement || '待安置' }}
          <span v-if="r.placementNote" class="muted">（{{ r.placementNote }}）</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 费用清单 -->
      <div style="display:flex;align-items:center;margin:12px 0 6px">
        <b>救助费用清单（{{ r.expenses?.length || 0 }} 项）</b>
        <el-button v-if="canOperate" link type="primary" :icon="Plus" style="margin-left:8px"
                   @click="openExpense">追加费用</el-button>
      </div>
      <el-table :data="r.expenses || []" size="small" border>
        <el-table-column label="项目" width="120">
          <template #default="{ row }">{{ EXPENSE_TYPE_LABEL[row.itemType] || row.itemType }}</template>
        </el-table-column>
        <el-table-column prop="itemDetail" label="说明" show-overflow-tooltip />
        <el-table-column label="金额" width="90">
          <template #default="{ row }">¥{{ row.amount ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="advancedBy" label="垫付方" width="100" />
        <el-table-column label="承担方" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="BEARER_TAG[row.bearer]">{{ BEARER_LABEL[row.bearer] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="claimNo" label="理赔/救助单号" width="130" />
      </el-table>

      <div style="margin-top:10px" v-if="canOperate">
        <el-button @click="openUpdate">补录修复 / 换锁 / 费用垫付 / 安置</el-button>
        <el-button type="danger" plain :icon="CircleCheck" @click="openConfirm">
          家属到场确认处置轨迹与费用承担
        </el-button>
      </div>
    </template>

    <!-- ===== 多方联系 ===== -->
    <el-dialog v-model="contactDialog" :title="`多方联系确认 · 第${contact.seq}步（${DOOR_PARTY_LABEL[contact.partyType]}）`"
               width="520px" append-to-body>
      <el-form :model="contact" label-width="92px">
        <el-form-item label="联系方">
          <el-radio-group v-model="contact.partyType">
            <el-radio v-for="p in DOOR_PARTY_ORDER" :key="p" :value="p">{{ DOOR_PARTY_LABEL[p] }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人/单位">
          <el-input v-model="contact.partyName"
                    :placeholder="contact.partyType === 'POLICE' ? '如 幸福路派出所值班民警' : '联系人姓名/单位名称'" />
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="contact.partyPhone" placeholder="可填值班电话" /></el-form-item>
        <el-form-item label="联系方式">
          <el-select v-model="contact.channel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="短信" value="短信" />
            <el-option label="微信" value="微信" />
            <el-option label="上门" value="上门" />
            <el-option label="楼宇对讲" value="楼宇对讲" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否联系上">
          <el-switch v-model="contact.reached" active-text="已联系上" inactive-text="未联系上" />
        </el-form-item>
        <el-form-item label="反馈">
          <el-input v-model="contact.feedback" type="textarea" :rows="2"
                    placeholder="如：无人接听/同意破门但赶不回来/拒绝承担/民警20分钟到场" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="承诺到场"><el-switch v-model="contact.willArrive" /></el-form-item>
          </el-col>
          <el-col :span="12" v-if="contact.willArrive">
            <el-form-item label="约多少分钟"><el-input-number v-model="contact.etaMinutes" :min="1" :max="600" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="contact.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactDialog = false">取消</el-button>
        <el-button type="primary" @click="submitContact">保存联系记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 破门救助登记 ===== -->
    <el-dialog v-model="rescueDialog" title="登记破门 / 锁匠救助（须留存授权依据与见证人）" width="720px"
               top="4vh" append-to-body>
      <el-alert type="error" :closable="false" show-icon style="margin-bottom:12px"
                title="强制留痕：必须填写授权依据（录音/截图/执法记录号/会议纪要）并登记至少 1 名现场见证人，否则不能提交" />
      <el-form :model="rescue" label-width="118px">
        <el-form-item label="处置方式">
          <el-radio-group v-model="rescue.actionType">
            <el-radio v-for="(v, k) in DOOR_ACTION_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="锁匠/施工方"><el-input v-model="rescue.providerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="rescue.providerPhone" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">授权依据</el-divider>
        <el-form-item label="授权类型" required>
          <el-radio-group v-model="rescue.authorizationType">
            <el-radio v-for="(v, k) in AUTH_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="授权人"><el-input v-model="rescue.authorizerName" placeholder="家属/民警姓名" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="授权人电话"><el-input v-model="rescue.authorizerPhone" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="授权凭证" required>
          <el-input v-model="rescue.authorizationEvidence" type="textarea" :rows="2"
                    placeholder="电话录音/微信截图 URL、民警执法记录号、社区紧急会议纪要（必填，多个用逗号分隔）" />
          <el-upload :action="uploadUrl" :headers="uploadHeaders" name="file" :show-file-list="false"
                     :on-success="(res) => onEvidenceUploaded(res, 'authorizationEvidence')" style="margin-top:4px">
            <el-button size="small" :icon="Picture">上传授权凭证</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="授权原话/理由">
          <el-input v-model="rescue.authorizationNote" type="textarea" :rows="2"
                    placeholder="如：屋内灯光亮、长时间敲门无应答，家属电话同意紧急破门" />
        </el-form-item>

        <el-divider content-position="left">现场见证人（至少 1 人）</el-divider>
        <div v-for="(w, i) in rescue.witnesses" :key="i" style="display:flex;gap:8px;margin-bottom:8px">
          <el-input v-model="w.name" placeholder="姓名" style="width:130px" />
          <el-select v-model="w.role" placeholder="身份" style="width:150px" allow-create filterable>
            <el-option label="物业工作人员" value="物业工作人员" />
            <el-option label="民警" value="民警" />
            <el-option label="邻居" value="邻居" />
            <el-option label="社区工作者" value="社区工作者" />
            <el-option label="家属" value="家属" />
          </el-select>
          <el-input v-model="w.phone" placeholder="电话" style="width:150px" />
          <el-button type="danger" link @click="rescue.witnesses.splice(i, 1)">删除</el-button>
        </div>
        <el-button size="small" :icon="Plus" @click="rescue.witnesses.push({ name: '', role: '物业工作人员', phone: '' })">
          添加见证人
        </el-button>

        <el-divider content-position="left">现场与修复、安置</el-divider>
        <el-form-item label="现场情况"><el-input v-model="rescue.sceneDescription" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="现场照片">
          <el-input v-model="rescue.scenePhotoUrls" placeholder="照片 URL，多个逗号分隔" />
          <el-upload :action="uploadUrl" :headers="uploadHeaders" name="file" :show-file-list="false"
                     :on-success="(res) => onEvidenceUploaded(res, 'scenePhotoUrls')" style="margin-top:4px">
            <el-button size="small" :icon="Picture">上传现场照片</el-button>
          </el-upload>
        </el-form-item>
        <el-row>
          <el-col :span="8"><el-form-item label="更换锁具"><el-switch v-model="rescue.lockReplaced" /></el-form-item></el-col>
          <el-col :span="16"><el-form-item label="锁具型号"><el-input v-model="rescue.lockModel" :disabled="!rescue.lockReplaced" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="新钥匙保管">
          <el-select v-model="rescue.newKeyKeptBy" clearable placeholder="家属/物业/社区/邻居" style="width:100%">
            <el-option label="家属" value="家属" />
            <el-option label="物业" value="物业" />
            <el-option label="社区" value="社区" />
            <el-option label="邻居" value="邻居" />
          </el-select>
        </el-form-item>
        <el-form-item label="门窗修复安排"><el-input v-model="rescue.repairArrangement" placeholder="如：当日下午修复门框并更换新门" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="费用垫付方">
            <el-select v-model="rescue.advancedBy" clearable style="width:100%">
              <el-option v-for="x in ['社区', '物业', '家属', '施工方挂账']" :key="x" :label="x" :value="x" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="垫付金额"><el-input-number v-model="rescue.advancedAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="物业已见证">
          <el-switch v-model="rescue.propertyWitnessed" />
          <el-input v-if="rescue.propertyWitnessed" v-model="rescue.propertyWitnessName" placeholder="物业见证人姓名"
                    style="width:220px;margin-left:10px" />
        </el-form-item>
        <el-form-item label="老人安置">
          <el-select v-model="rescue.elderPlacement" allow-create filterable clearable style="width:100%">
            <el-option v-for="x in ELDER_PLACEMENT_OPTIONS" :key="x" :label="x" :value="x" />
          </el-select>
        </el-form-item>
        <el-form-item label="安置说明"><el-input v-model="rescue.placementNote" type="textarea" :rows="2" /></el-form-item>

        <el-divider content-position="left">费用项（可选，家属到场后确认承担方）</el-divider>
        <div v-for="(x, i) in rescue.expenses" :key="i" style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap">
          <el-select v-model="x.itemType" placeholder="项目" style="width:130px">
            <el-option v-for="(v, k) in EXPENSE_TYPE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
          <el-input v-model="x.itemDetail" placeholder="说明" style="width:160px" />
          <el-input-number v-model="x.amount" :min="0" :precision="2" placeholder="金额" style="width:120px" />
          <el-select v-model="x.advancedBy" placeholder="垫付方" style="width:120px" clearable>
            <el-option v-for="p in ['社区', '物业', '家属', '施工方挂账']" :key="p" :label="p" :value="p" />
          </el-select>
          <el-button type="danger" link @click="rescue.expenses.splice(i, 1)">删除</el-button>
        </div>
        <el-button size="small" :icon="Plus" @click="rescue.expenses.push({ itemType: 'LOCK_OPEN', itemDetail: '', amount: undefined, advancedBy: rescue.advancedBy, bearer: 'PENDING' })">
          添加费用项
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="rescueDialog = false">取消</el-button>
        <el-button type="danger" @click="submitRescue">提交救助登记</el-button>
      </template>
    </el-dialog>

    <!-- ===== 补录 ===== -->
    <el-dialog v-model="updateDialog" title="补录修复 / 换锁 / 费用垫付 / 安置" width="560px" append-to-body>
      <el-form :model="upd" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="更换锁具"><el-switch v-model="upd.lockReplaced" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="锁具型号"><el-input v-model="upd.lockModel" :disabled="!upd.lockReplaced" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="新钥匙保管">
          <el-select v-model="upd.newKeyKeptBy" clearable style="width:100%">
            <el-option v-for="x in ['家属', '物业', '社区', '邻居']" :key="x" :label="x" :value="x" />
          </el-select>
        </el-form-item>
        <el-form-item label="门窗修复安排"><el-input v-model="upd.repairArrangement" /></el-form-item>
        <el-form-item label="修复完成时间">
          <el-date-picker v-model="upd.repairedAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="费用垫付方">
            <el-select v-model="upd.advancedBy" clearable style="width:100%">
              <el-option v-for="x in ['社区', '物业', '家属', '施工方挂账']" :key="x" :label="x" :value="x" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="垫付金额"><el-input-number v-model="upd.advancedAmount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="物业已见证">
          <el-switch v-model="upd.propertyWitnessed" />
          <el-input v-if="upd.propertyWitnessed" v-model="upd.propertyWitnessName" placeholder="物业见证人"
                    style="width:220px;margin-left:10px" />
        </el-form-item>
        <el-form-item label="老人安置">
          <el-select v-model="upd.elderPlacement" allow-create filterable clearable style="width:100%">
            <el-option v-for="x in ELDER_PLACEMENT_OPTIONS" :key="x" :label="x" :value="x" />
          </el-select>
        </el-form-item>
        <el-form-item label="安置说明"><el-input v-model="upd.placementNote" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate">保存补录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 追加费用 ===== -->
    <el-dialog v-model="expenseDialog" title="追加救助费用" width="480px" append-to-body>
      <el-form :model="exp" label-width="92px">
        <el-form-item label="费用项目">
          <el-select v-model="exp.itemType" style="width:100%">
            <el-option v-for="(v, k) in EXPENSE_TYPE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="exp.itemDetail" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="exp.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="垫付方">
          <el-select v-model="exp.advancedBy" clearable style="width:100%">
            <el-option v-for="x in ['社区', '物业', '家属', '施工方挂账']" :key="x" :label="x" :value="x" />
          </el-select>
        </el-form-item>
        <el-form-item label="承担方">
          <el-radio-group v-model="exp.bearer">
            <el-radio v-for="(v, k) in BEARER_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="理赔/救助单号"><el-input v-model="exp.claimNo" placeholder="保险报案号或社区救助单号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expenseDialog = false">取消</el-button>
        <el-button type="primary" @click="submitExpense">保存费用</el-button>
      </template>
    </el-dialog>

    <!-- ===== 家属确认 ===== -->
    <el-dialog v-model="confirmDialog" title="家属到场确认完整处置轨迹与费用承担" width="680px" append-to-body>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"
                title="请家属查看上方完整处置轨迹（多方联系、授权依据、见证人、破门过程、修复与费用），逐项确认费用承担方" />
      <el-form :model="cf" label-width="100px">
        <el-form-item label="家属姓名"><el-input v-model="cf.confirmerName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="cf.confirmerPhone" /></el-form-item>
        <el-form-item label="确认意见">
          <el-input v-model="cf.confirmNote" type="textarea" :rows="2" placeholder="认可处置 / 异议说明" />
        </el-form-item>
        <el-form-item label="签字/合影">
          <el-input v-model="cf.evidenceUrl" placeholder="凭证 URL" />
          <el-upload :action="uploadUrl" :headers="uploadHeaders" name="file" :show-file-list="false"
                     :on-success="(res) => { if (res.code === 0) cf.evidenceUrl = res.data.url }" style="margin-top:4px">
            <el-button size="small" :icon="Picture">上传签字/合影</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <el-table :data="confirmRows" size="small" border>
        <el-table-column label="项目" width="110">
          <template #default="{ row }">{{ EXPENSE_TYPE_LABEL[row.itemType] || row.itemType }}</template>
        </el-table-column>
        <el-table-column prop="itemDetail" label="说明" show-overflow-tooltip />
        <el-table-column label="金额" width="80">
          <template #default="{ row }">¥{{ row.amount ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="承担方确认" width="300">
          <template #default="{ row }">
            <el-radio-group v-model="row.bearer" size="small">
              <el-radio value="FAMILY">家庭</el-radio>
              <el-radio value="COMMUNITY_AID">社区救助</el-radio>
              <el-radio value="INSURANCE">保险理赔</el-radio>
              <el-radio value="PENDING">待定</el-radio>
            </el-radio-group>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!confirmRows.length" class="muted" style="margin:8px 0">暂无费用项，可先追加费用后再确认。</div>
      <template #footer>
        <el-button @click="confirmDialog = false">取消</el-button>
        <el-button type="danger" @click="submitConfirm">家属确认并留痕</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, PhoneFilled, Unlock, CircleCheck, Picture, WarningFilled } from '@element-plus/icons-vue'
import api from '../api'
import {
  DOOR_PARTY_LABEL, DOOR_PARTY_ORDER, DOOR_ACTION_LABEL, AUTH_LABEL,
  EXPENSE_TYPE_LABEL, BEARER_LABEL, BEARER_TAG, ELDER_PLACEMENT_OPTIONS
} from '../api/dict'

const props = defineProps({
  eventId: { type: [String, Number], required: true },
  data: { type: Object, required: true },
  canOperate: { type: Boolean, default: true }
})
const emit = defineEmits(['changed'])

const uploadUrl = '/api/files/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const r = computed(() => props.data.doorRescue)
const hasCannotOpen = computed(() =>
  (props.data.event?.riskFlags || '').split(',').includes('CANNOT_OPEN_DOOR')
  || (props.data.visits || []).some(v => !v.entered || v.entryMethod === '未能入户' || v.entryMethod === '破锁入户'))
// 已存在多方记录/救助记录时也保持面板可见
const visible = computed(() => hasCannotOpen.value
  || (props.data.doorContacts && props.data.doorContacts.length > 0)
  || !!props.data.doorRescue)

const contactStep = computed(() => Math.min((props.data.doorContacts || []).length, DOOR_PARTY_ORDER.length))
const nextPartyLabel = computed(() => DOOR_PARTY_LABEL[DOOR_PARTY_ORDER[Math.min(contactStep.value, 3)]])

function stepDesc(p) {
  const list = props.data.doorContacts || []
  const hit = list.find(c => c.partyType === p)
  if (!hit) return '未联系'
  return hit.reached ? '已联系上' : '未联系上'
}
function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
function splitUrls(s) { return (s || '').split(',').map(x => x.trim()).filter(Boolean) }

async function refresh() { emit('changed') }

/* ---- 多方联系 ---- */
const contactDialog = ref(false)
const contact = reactive({})
function openContact() {
  const seq = (props.data.doorContacts || []).length + 1
  const party = DOOR_PARTY_ORDER[Math.min(seq - 1, 3)]
  // 预填建议联系人
  let name = '', phone = ''
  if (party === 'FAMILY') {
    const c = (props.data.contacts || []).find(x => x.primaryContact) || (props.data.contacts || [])[0]
    name = c?.name || ''; phone = c?.phone || ''
  } else if (party === 'NEIGHBOR') {
    const n = (props.data.neighbors || [])[0]
    name = n ? `${n.name}（${n.proximity}）` : ''; phone = n?.phone || ''
  } else if (party === 'PROPERTY') {
    name = '幸福社区物业服务中心'; phone = '025-88880000'
  } else {
    name = '幸福路派出所'; phone = '110'
  }
  Object.assign(contact, { seq, partyType: party, partyName: name, partyPhone: phone, channel: '电话',
    reached: true, feedback: '', willArrive: false, etaMinutes: null, note: '' })
  contactDialog.value = true
}
async function submitContact() {
  if (!contact.partyName) { ElMessage.warning('请填写联系人/单位'); return }
  await api.post(`/events/${props.eventId}/door-contacts`, { ...contact, seq: undefined })
  ElMessage.success('联系记录已保存')
  contactDialog.value = false
  refresh()
}

/* ---- 破门救助登记 ---- */
const rescueDialog = ref(false)
const rescue = reactive({})
function openRescue() {
  Object.assign(rescue, {
    actionType: 'BREAK_DOOR', providerName: '', providerPhone: '',
    authorizationType: 'FAMILY_AUTHORIZED', authorizerName: '', authorizerPhone: '',
    authorizationEvidence: '', authorizationNote: '',
    sceneDescription: '', scenePhotoUrls: '',
    lockReplaced: false, lockModel: '', newKeyKeptBy: '物业',
    repairArrangement: '', advancedBy: '社区', advancedAmount: undefined,
    propertyWitnessed: true, propertyWitnessName: '',
    elderPlacement: '', placementNote: '',
    witnesses: [{ name: '', role: '物业工作人员', phone: '' }],
    expenses: []
  })
  rescueDialog.value = true
}
function onEvidenceUploaded(res, field) {
  if (res.code === 0) {
    rescue[field] = rescue[field] ? rescue[field] + ',' + res.data.url : res.data.url
    ElMessage.success('凭证已上传')
  }
}
async function submitRescue() {
  if (!rescue.authorizationEvidence?.trim()) { ElMessage.error('必须填写授权凭证'); return }
  const ws = rescue.witnesses.filter(w => w.name && w.role)
  if (!ws.length) { ElMessage.error('必须登记至少一名现场见证人'); return }
  await api.post(`/events/${props.eventId}/door-rescue`, { ...rescue, witnesses: ws })
  ElMessage.success('破门/锁匠救助已登记并全程留痕')
  rescueDialog.value = false
  refresh()
}

/* ---- 补录 ---- */
const updateDialog = ref(false)
const upd = reactive({})
function openUpdate() {
  Object.assign(upd, {
    lockReplaced: r.value.lockReplaced, lockModel: r.value.lockModel || '',
    newKeyKeptBy: r.value.newKeyKeptBy || '', repairArrangement: r.value.repairArrangement || '',
    repairedAt: r.value.repairedAt || null,
    advancedBy: r.value.advancedBy || '', advancedAmount: r.value.advancedAmount ?? undefined,
    propertyWitnessed: r.value.propertyWitnessed, propertyWitnessName: r.value.propertyWitnessName || '',
    elderPlacement: r.value.elderPlacement || '', placementNote: r.value.placementNote || ''
  })
  updateDialog.value = true
}
async function submitUpdate() {
  const body = {}
  Object.entries(upd).forEach(([k, v]) => { if (v !== '' && v !== null && v !== undefined) body[k] = v })
  await api.put(`/events/${props.eventId}/door-rescue`, body)
  ElMessage.success('补录已保存')
  updateDialog.value = false
  refresh()
}

/* ---- 追加费用 ---- */
const expenseDialog = ref(false)
const exp = reactive({})
function openExpense() {
  Object.assign(exp, { itemType: 'LOCK_OPEN', itemDetail: '', amount: undefined,
    advancedBy: r.value.advancedBy || '', bearer: 'PENDING', claimNo: '' })
  expenseDialog.value = true
}
async function submitExpense() {
  if (!exp.itemType) { ElMessage.warning('请选择费用项目'); return }
  await api.post(`/events/${props.eventId}/door-rescue/expenses`, exp)
  ElMessage.success('费用已登记')
  expenseDialog.value = false
  refresh()
}

/* ---- 家属确认 ---- */
const confirmDialog = ref(false)
const cf = reactive({})
const confirmRows = ref([])
function openConfirm() {
  Object.assign(cf, { confirmerName: '', confirmerPhone: '', confirmNote: '', evidenceUrl: '' })
  confirmRows.value = (r.value.expenses || []).map(x => ({ ...x }))
  confirmDialog.value = true
}
async function submitConfirm() {
  if (!cf.confirmerName) { ElMessage.warning('请填写家属姓名'); return }
  const bearers = {}
  confirmRows.value.forEach(x => { bearers[x.id] = x.bearer || 'PENDING' })
  await api.post(`/events/${props.eventId}/door-rescue/family-confirm`, { ...cf, expenseBearers: bearers })
  ElMessage.success('家属确认已留痕')
  confirmDialog.value = false
  refresh()
}
</script>

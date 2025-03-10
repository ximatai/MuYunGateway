<template>
    <el-card class="box-card" shadow="never">
        <template #header>
            <div class="card-header">
                <span>基本设置</span>
            </div>
        </template>
        <el-form-item prop="port">
            <template #label>
                <div class="label-desc">
                    <span>端口</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="gateway渲染站点入口"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input-number
                v-model="dataForm.port"
                :min="1"
                :max="65535"
                controls-position="right"
            />
        </el-form-item>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>开启HTTPS</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="使用HTTPS协议"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-switch
                v-model="dataForm.ssl.use"
                inline-prompt
                active-text="开"
                inactive-text="关"
            />
        </el-form-item>
        <template v-if="dataForm.ssl.use">
            <el-form-item prop="ssl.certPath">
                <template #label>
                    <div class="label-desc">
                        <span>证书路径</span>
                        <el-tooltip
                            class="box-item"
                            effect="dark"
                            content="SSL证书绝对路径"
                            placement="top"
                        >
                            <el-icon class="icon" size="15"><Warning /></el-icon>
                        </el-tooltip>
                        <span>:</span>
                    </div>
                </template>
                <el-input v-model="dataForm.ssl.cert_path" placeholder="请输入" />
            </el-form-item>
            <el-form-item prop="ssl.keyPath">
                <template #label>
                    <div class="label-desc">
                        <span>密钥路径</span>
                        <el-tooltip
                            class="box-item"
                            effect="dark"
                            content="SSL证书密钥绝对路径"
                            placement="top"
                        >
                            <el-icon class="icon" size="15"><Warning /></el-icon>
                        </el-tooltip>
                        <span>:</span>
                    </div>
                </template>
                <el-input v-model="dataForm.ssl.key_path" placeholder="请输入" />
            </el-form-item>
        </template>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>首页</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="访问空路径默认跳转地址"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input v-model="dataForm.index" placeholder="请输入" />
        </el-form-item>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>登录路由</span>
                    <el-tooltip class="box-item" effect="dark" content="登录路径" placement="top">
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input v-model="dataForm.login.path" placeholder="请输入" />
        </el-form-item>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>登录页</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="无权限时强制跳转地址"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input v-model="dataForm.login.page" placeholder="请输入" />
        </el-form-item>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>登录接口</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="后端提供登录接口"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input v-model="dataForm.login.api" placeholder="请输入" />
        </el-form-item>

        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>开启JWT</span>
                    <el-tooltip class="box-item" effect="dark" content="开启JWT" placement="top">
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-switch
                v-model="dataForm.jwt.use"
                inline-prompt
                active-text="开"
                inactive-text="关"
            />
        </el-form-item>
        <el-form-item v-if="dataForm.jwt.use">
            <template #label>
                <div class="label-desc">
                    <span>验证JWT过期</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="验证JWT过期时间"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-switch
                v-model="dataForm.jwt.checkExpiration"
                inline-prompt
                active-text="开"
                inactive-text="关"
            />
        </el-form-item>
        <el-form-item prop="jwt.expiresHour" v-if="dataForm.jwt.use && dataForm.jwt.checkExpiration">
            <template #label>
                <div class="label-desc">
                    <span>JWT过期</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="JWT过期时间，超过则Token失效"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input-number
                v-model="dataForm.jwt.expiresHour"
                controls-position="right"
                :min="1"
                :max="43200"
                class="input-number"
            >
            </el-input-number>
        </el-form-item>
        <el-form-item>
            <template #label>
                <div class="label-desc">
                    <span>启用Session</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="验证Session过期时间"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-switch
                v-model="dataForm.session.use"
                inline-prompt
                active-text="开"
                inactive-text="关"
            />
        </el-form-item>
        <el-form-item prop="session.timeoutHour" v-if="dataForm.session.use">
            <template #label>
                <div class="label-desc">
                    <span>Session过期</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="超过此时间不访问就自动退出"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-input-number
                v-model="dataForm.session.timeoutHour"
                :min="1"
                :max="43200"
                class="input-number"
                controls-position="right"
            >
            </el-input-number>
        </el-form-item>
      <el-form-item :rules="headersRule">
        <template #label>
          <div class="label-desc">
            <span>自定义Header</span>
            <el-tooltip
                class="box-item"
                effect="dark"
                content="追加自定义Header"
                placement="top"
            >
              <el-icon class="icon" size="15"><Warning /></el-icon>
            </el-tooltip>
            <span>:</span>
          </div>
        </template>
        <el-row v-for="(item, index) in dataForm.headers" style="margin-bottom: 20px">
          <el-col :span="10">
            <el-form-item
                :prop="`headers.${index}.name`"
                :rules="headerItemRules.name"
                required
            >
              <el-input v-model="item.name" placeholder="name">
                <template #prepend>名称</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="10" style="margin-left: 5px">
            <el-form-item
                :prop="`headers.${index}.value`"
                :rules="headerItemRules.value"
                required
            >
              <el-input v-model="item.value" placeholder="value">
                <template #prepend>值</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="3" style="margin-left: 5px">
            <el-button
                @click="dataForm.headers.splice(index, 1)"
                :icon="Minus"
                circle
                size="small"
            />
          </el-col>
        </el-row>
        <p class="add-btn" @click="addHeaders">
          <el-icon> <Plus /> </el-icon>追加Header
        </p>
      </el-form-item>
        <el-form-item :rules="redirectRules">
            <template #label>
                <div class="label-desc">
                    <span>302跳转</span>
                    <el-tooltip
                        class="box-item"
                        effect="dark"
                        content="预制302跳转规则"
                        placement="top"
                    >
                        <el-icon class="icon" size="15"><Warning /></el-icon>
                    </el-tooltip>
                    <span>:</span>
                </div>
            </template>
            <el-row v-for="(item, index) in dataForm.redirects" style="margin-bottom: 20px">
                <el-col :span="10">
                    <el-form-item
                        :prop="`redirects.${index}.from`"
                        :rules="redirectItemRules.from"
                        required
                    >
                        <el-input v-model="item.from" placeholder="/source">
                            <template #prepend>拦截</template>
                        </el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="10" style="margin-left: 5px">
                    <el-form-item
                        :prop="`redirects.${index}.to`"
                        :rules="redirectItemRules.to"
                        required
                    >
                        <el-input v-model="item.to" placeholder="/target">
                            <template #prepend>跳转</template>
                        </el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="3" style="margin-left: 5px">
                    <el-button
                        @click="dataForm.redirects.splice(index, 1)"
                        :icon="Minus"
                        circle
                        size="small"
                    />
                </el-col>
            </el-row>
            <p class="add-btn" @click="addAutoRedirects">
                <el-icon> <Plus /> </el-icon>追加302跳转
            </p>
        </el-form-item>
    </el-card>
</template>

<script setup lang="ts">
import { Minus } from '@element-plus/icons-vue'
const props = defineProps({
    dataForm: {
        type: Object,
        default: () => {},
    },
})

// 追加自定义header
const addHeaders = () => {
  props.dataForm.headers.push({
    name: '',
    value: '',
  })
}

// 追加302跳转规则
const addAutoRedirects = () => {
    props.dataForm.redirects.push({
        from: '',
        to: '',
    })
}
// 单个跳转项的校验规则
const redirectItemRules = {
    from: [
        {
            required: true,
            message: '拦截路径不能为空',
            trigger: 'blur',
        },
        {
            pattern: /^\/\S+$/,
            message: '必须以/开头且不能为空路径',
            trigger: 'blur',
        },
    ],
    to: [
        {
            required: true,
            message: '跳转目标不能为空',
            trigger: 'blur',
        },
    ],
}
// 整个redirects数组的校验
const redirectRules = [
    {
        validator: (_: any, value: any, callback: any) => {
            //   if (!value || value.length === 0) {
            //     callback(new Error("至少需要一条跳转规则"));
            //     return;
            //   }
            const hasEmpty = value.some((item: any) => !item.from?.trim() || !item.to?.trim())
            if (hasEmpty) {
                callback(new Error('存在未填写的跳转规则'))
            } else {
                callback()
            }
        },
        trigger: 'blur',
    },
]

// 单个header项的校验规则
const headerItemRules = {
  name: [
    {
      required: true,
      message: 'Header 名称不能位空',
      trigger: 'blur',
    }
  ],
  value: [
    {
      required: true,
      message: 'Header 值不能为空',
      trigger: 'blur',
    },
  ],
}

// 整个headers数组的校验
const headersRule = [
  {
    validator: (_: any, value: any, callback: any) => {
      //   if (!value || value.length === 0) {
      //     callback(new Error("至少需要一条跳转规则"));
      //     return;
      //   }
      const hasEmpty = value.some((item: any) => !item.name?.trim() || !item.value?.trim())
      if (hasEmpty) {
        callback(new Error('Header 设置不完整'))
      } else {
        callback()
      }
    },
    trigger: 'blur',
  },
]
</script>

<style scoped lang="less">
.card-header {
    font-size: 16px;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
    text-align: left;
}
:deep(.el-card__body) {
    height: 610px;
    overflow: scroll;
}
.label-desc {
    height: 32px;
    display: flex;
    align-items: center;
    .icon {
        margin: 0 4px;
    }
}
.input-number {
    :deep(.el-input) {
        position: relative;
        &::before {
            // 也可以将此处的分钟换成其他单位
            content: '小时';
            z-index: 2;
            position: absolute;
            top: 0;
            right: 42px;
            height: 32px;
            line-height: 32px;
            color: #999;
            font-size: 12px;
        }
    }

    :deep(.el-input__inner) {
        box-sizing: border-box;
        padding-right: 25px;
    }
}
.desc-item {
    width: 100%;
    display: flex;
    justify-content: space-between;
}
:deep(.el-input-group__prepend) {
    padding: 0 10px;
}
.add-btn {
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    padding: 0 15px;
    font-size: 14px;
    box-sizing: border-box;
    border: 1px dashed #d9d9d9;
    text-align: center;
    line-height: 32px;
    width: 84.8%;
    background-color: #fff;
    //margin-top: 10px;
    cursor: pointer;
    transition: all 0.5s;
    &:hover {
        border-color: #409eff;
        color: #409eff;
    }
}
</style>

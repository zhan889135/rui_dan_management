import Dict from './Dict'
import { mergeOptions } from './DictOptions'

export default function(Vue, options) {
  mergeOptions(options)
  Vue.mixin({
    data() {
      if (this.$options === undefined || this.$options.dicts === undefined || this.$options.dicts === null) {
        return {}
      }
      const dict = new Dict()
      dict.owner = this
      return {
        dict
      }
    },
    created() {
      if (!(this.dict instanceof Dict)) {
        return
      }
      options.onCreated && options.onCreated(this.dict)
      this.dict.init(this.$options.dicts).then(() => {
        options.onReady && options.onReady(this.dict)
        this.$nextTick(() => {
          this.$emit('dictReady', this.dict)
          if (this.$options.methods && this.$options.methods.onDictReady instanceof Function) {
            this.$options.methods.onDictReady.call(this, this.dict)
          }
        })
      })
    },
  })
}

/**
 * 根据字典 value 查找 label
 * @param {String|Number} value 要匹配的值
 * @param {Array} dict 字典数组，如 [{value: '0', label: '成功'}]
 * @returns {String} 对应的 label（找不到返回 '未知'）
 */
export function getDictLabel(value, dict = []) {
  const match = dict.find(item => String(item.value) === String(value))
  return match ? match.label : ''
}

/**
 * 根据字典 value 查找 el-tag 的类型
 * @param {String|Number} value 要匹配的值
 * @param {Array} dict 字典数组，如 [{value: '0', elTagType: 'success'}]
 * @returns {String} elTagType，如 'success', 'danger'，默认返回 'info'
 */
export function getDictTagType(value, dict = []) {
  const match = dict.find(item => String(item.value) === String(value))
  return match ? match.raw.listClass : ''
}


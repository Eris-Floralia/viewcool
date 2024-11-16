<template>
  <div class="expression-editor">
    <div class="expression-box">
      <div class="expression-con">
        <div style="display: inline">
          <span class="expression-filed">计算公式</span>
          <span class="expression-filed">=</span>
        </div>
        <el-scrollbar style="flex-grow: 1; width: 100px">
          <div class="formula-edit-box">
            <div class="formula-item">
              <span class="expression-filed" @click="onClickItem(-1)"></span>
              <span v-if="currentIndex === 0" class="cursor" />
            </div>
            <div
              class="formula-item"
              v-for="(fitem, index) in fieldList"
              :key="fitem.itemName + '_' + index"
            >
              <span
                :style="{
                  'background-color': fitem.itemKind == FormulaItemKind.VARABLE ? '#eee' : '',
                  padding: fitem.itemKind == FormulaItemKind.VARABLE ? '2px 8px' : '2px 0'
                }"
                class="expression-filed"
                @click="onClickItem(index)"
              >
                {{ fitem.itemName }}
              </span>
              <span v-if="currentIndex === index + 1" class="cursor" />
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div class="expression-toolbar">
        <el-button type="text" icon="el-icon-back" @click="deleteField()" />
        <el-button type="text" icon="el-icon-delete" @click="clearField()" />
      </div>
    </div>
    <div class="expression-tips">
      编辑计算公式可用来完成审批单内数据的自动结算，例如：采购单内设置计算公式“合计=单价x数量”，发起人填写单价、数量后，组件将自动计算出合计金额，免手动计算
    </div>
    <div class="expression-form">
      <div class="expression-form-row">
        <div class="expression-form-label">计算对象：</div>
        <div class="expression-form-inline">
          <span
            v-for="(eItem, eIndex) in variableList"
            :key="eIndex"
            class="expression-form-value"
            @click="addField(eItem)"
          >
            {{ eItem.itemName }}
          </span>
        </div>
      </div>
      <div class="expression-form-row">
        <div class="expression-form-label">计算符号：</div>
        <div class="expression-form-inline">
          <span
            v-for="(eItem, eIndex) in operatorList"
            :key="eIndex"
            class="expression-form-value"
            @click="addField(eItem)"
          >
            {{ eItem.itemName }}
          </span>
        </div>
      </div>
      <div class="expression-form-row">
        <div class="expression-form-label">数字键盘：</div>
        <div class="expression-form-inline">
          <span
            v-for="(eItem, eIndex) in numberList"
            :key="eIndex"
            class="expression-form-value"
            @click="addField(eItem)"
            >{{ eItem.itemName }}</span
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { FormulaItemKind } from '@/staticDict/index';

// 运算符号
const DefaultOperator = [
  { itemName: '+', itemCode: '+', itemKind: FormulaItemKind.OPERATOR },
  { itemName: '-', itemCode: '-', itemKind: FormulaItemKind.OPERATOR },
  { itemName: '×', itemCode: '*', itemKind: FormulaItemKind.OPERATOR },
  { itemName: '÷', itemCode: '/', itemKind: FormulaItemKind.OPERATOR },
  { itemName: '(', itemCode: '(', itemKind: FormulaItemKind.OPERATOR },
  { itemName: ')', itemCode: ')', itemKind: FormulaItemKind.OPERATOR }
];
// 数字键盘
const DefaultNumberKeyword = [
  { itemName: '1', itemCode: '1', itemKind: FormulaItemKind.NUMBER },
  { itemName: '2', itemCode: '2', itemKind: FormulaItemKind.NUMBER },
  { itemName: '3', itemCode: '3', itemKind: FormulaItemKind.NUMBER },
  { itemName: '4', itemCode: '4', itemKind: FormulaItemKind.NUMBER },
  { itemName: '5', itemCode: '5', itemKind: FormulaItemKind.NUMBER },
  { itemName: '6', itemCode: '6', itemKind: FormulaItemKind.NUMBER },
  { itemName: '7', itemCode: '7', itemKind: FormulaItemKind.NUMBER },
  { itemName: '8', itemCode: '8', itemKind: FormulaItemKind.NUMBER },
  { itemName: '9', itemCode: '9', itemKind: FormulaItemKind.NUMBER },
  { itemName: '0', itemCode: '0', itemKind: FormulaItemKind.NUMBER },
  { itemName: '.', itemCode: '.', itemKind: FormulaItemKind.NUMBER }
];
export default {
  name: 'FormulaEditor',
  props: {
    value: {
      type: String,
      default: ''
    },
    options: {
      type: Object
    }
  },
  data() {
    return {
      fieldList: [],
      currentIndex: 0,
      currentValue: undefined
    };
  },
  computed: {
    variableList() {
      return this.options?.variableList || [];
    },
    operatorList() {
      return this.options?.operatorList || DefaultOperator;
    },
    numberList() {
      return this.options?.numberList || DefaultNumberKeyword;
    },
    currentField() {
      return this.fieldList[this.currentIndex - 1];
    }
  },
  methods: {
    escapeRegExp(string) {
      return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& 表示整个匹配的字符串
    },
    // 默认解析
    defaultParse(value) {
      if (value == null || value === '') return [];
      let result = [];
      try {
        result = JSON.parse(value);
      } catch (e) {
        console.error('parse error', e);
      }
      if (Array.isArray(result)) {
        let defaultTime = new Date().getTime();
        result.forEach(item => {
          item.id = defaultTime++;
        });
      }
      return result;
    },
    // 默认格式化
    defaultFormat(data) {
      if (Array.isArray(data) && data.length > 0) {
        return JSON.stringify(data);
      } else {
        return '';
      }
    },
    // 删除元素
    deleteField() {
      if (this.currentIndex === 0 || this.currentField == null) return;
      if (
        this.currentField.itemKind === FormulaItemKind.NUMBER ||
        this.currentField.itemKind === FormulaItemKind.STRING
      ) {
        if (this.currentField.itemName.length > 1) {
          this.currentField.itemName = this.currentField.itemName.slice(
            0,
            this.currentField.itemName.length - 1
          );
          return;
        }
      }
      this.fieldList.splice(this.currentIndex - 1, 1);
      this.currentIndex = this.currentIndex - 1;
    },
    // 清空元素
    clearField() {
      this.fieldList = [];
      this.currentIndex = 0;
    },
    // 插入元素
    appendField(item) {
      if (item == null) return;
      this.fieldList.splice(this.currentIndex, 0, item);
      this.currentIndex = this.currentIndex + 1;
    },
    // 添加元素
    addField(item) {
      if (item == null) return;
      item = {
        ...item
      };
      if (this.currentField == null) {
        this.appendField(item);
        return;
      }
      if (this.currentField.itemKind === FormulaItemKind.NUMBER) {
        if (item.itemKind === FormulaItemKind.NUMBER || item.itemKind === FormulaItemKind.STRING) {
          this.currentField.itemName += item.itemCode;
          // 如果拼接上字符串，元素类型变成字符串元素
          if (item.itemKind === FormulaItemKind.STRING)
            this.currentField.itemKind = FormulaItemKind.STRING;
          return;
        }
      } else if (this.currentField.itemKind === FormulaItemKind.STRING) {
        if (item.itemKind === FormulaItemKind.NUMBER || item.itemKind === FormulaItemKind.STRING) {
          this.currentField.itemName += item.itemCode;
          return;
        }
      }
      this.appendField(item);
    },
    // 选中元素
    onClickItem(index) {
      this.currentIndex = index + 1;
    },
    // 当公式变化时
    onChange() {
      let temp;
      if (this.options?.stringify != null) {
        temp = this.options?.stringify(this.fieldList);
      } else {
        temp = this.defaultFormat(this.fieldList);
      }
      this.currentValue = temp;
      this.$emit('input', temp);
      this.$emit('change', temp);
    }
  },
  watch: {
    value: {
      handler(value) {
        if (this.currentValue === this.value) return;
        if (this.options?.parse != null) {
          this.fieldList = this.options.parse(this.value);
        } else {
          this.fieldList = this.defaultParse(this.value);
        }
        this.currentIndex = this.fieldList.length;
      },
      immediate: true
    },
    fieldList: {
      handler() {
        this.onChange();
      },
      deep: true,
      immediate: true
    }
  }
};
</script>

<style lang="css" scoped>
.expression-editor {
  --border: 1px solid #dedede;
  --border-radius: 5px;
  background: white;
  display: flex;
  flex-direction: column;
}

.expression-editor {
  width: 100%;
  max-width: 1000px;
  height: 550px;
  margin: 0 auto;
  box-sizing: border-box;
  font-size: 14px;
  /*background-color: #fff;*/
}
.expression-editor * {
  box-sizing: border-box;
}

.expression-box {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 200px;
  border: var(--border);
  border-radius: var(--border-radius);
  padding: 10px 20px 1px 20px;
}

.expression-con {
  display: flex;
  width: 100%;
  height: calc(100% - 40px);
  overflow-y: auto;
  flex-wrap: wrap;
}

.expression-filed {
  display: inline-flex;
  font-size: 12px;
  color: #242424;
  border-radius: 4px;
  cursor: pointer;
  min-height: 26px;
  min-width: 2px;
  align-items: center;
  justify-content: center;
  margin-right: 5px;
  margin-bottom: 5px;
}

.expression-toolbar {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-direction: row;
}

.expression-btn {
  color: #000;
  padding: 5px 10px;
  cursor: pointer;
}

.expression-tips {
  padding: 10px 0;
  line-height: 1.5;
  font-size: 12px;
  color: #666;
}

.expression-form-row {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  flex-direction: row;
  margin: 10px 0;
}

.expression-form-inline {
  display: inline-block;
  flex-wrap: wrap;
  flex-grow: 1;
}

.expression-form-label {
  color: #242424;
  font-size: 12px;
  flex-shrink: 0;
}
.expression-form-value {
  display: inline-flex;
  padding: 2px 8px;
  background-color: #eee;
  font-size: 12px;
  color: #242424;
  border-radius: 4px;
  cursor: pointer;
  min-width: 36px;
  min-height: 36px;
  align-items: center;
  justify-content: center;
  margin: 0 15px 15px 0;
}

.expression-form-value:hover {
  background-color: #dddddd;
}

.btn-save {
  display: inline-block;
  margin-right: 15px;
  padding: 5px 15px;
  background-color: #3b64da;
  color: #fff;
  width: 100px;
  text-align: center;
  font-size: 20px;
  line-height: 1.5;
  cursor: pointer;
}

.filedString {
  line-height: 2;
  font-size: 16px;
  color: orange;
}

.filedString span {
  color: red;
}

.formula-edit-box {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
}

.formula-item {
  display: inline-block;
  position: relative;
  height: 26px;
  line-height: 26px;
  margin-bottom: 16px;
}

.cursor {
  display: inline-block;
  position: absolute;
  width: 1.5px; /* 光标的宽度 */
  height: 12px; /* 光标的高度，通常与文本行高一致 */
  background-color: black; /* 光标的颜色 */
  animation: blink 1s step-end infinite; /* 使用动画 */
  right: 2px;
  top: 9px;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>

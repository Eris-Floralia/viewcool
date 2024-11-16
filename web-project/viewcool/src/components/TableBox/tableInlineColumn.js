export const TableInlintColumnMixins = {
  props: {
    /**
     * 是否支持多选
     */
    multiple: {
      type: Boolean,
      default: false
    }
  },
  /**
   * 行内编辑配置信息
   * rowData: 正在编辑的行数据
   * rowCopy: 正在编辑的行数据的副本
   * isDirty: 是否有修改
   * autoEditNext: 是否自动编辑下一行数据
   */
  inject: ['inlineEditConfig'],
  computed: {
    editConfig() {
      return this.inlineEditConfig;
    },
    getRowData() {
      let row = (this.editConfig || {}).rowCopy;
      if (row == null || this.$attrs.field == null) {
        return undefined;
      }
      let path = this.$attrs.field.split('.');
      let temp = row;
      for (let i = 0; i < path.length; i++) {
        temp = temp[path[i]];
        if (temp == null) break;
      }
      return temp;
    }
  },
  methods: {
    getRowValue(row) {
      if (row == null || this.$attrs.field == null) {
        return undefined;
      }
      let path = this.$attrs.field.split('.');
      let temp = row;
      for (let i = 0; i < path.length; i++) {
        temp = temp[path[i]];
        if (temp == null) break;
      }
      return temp;
    },
    isEdit(row) {
      return this.editConfig && this.editConfig.rowData === row;
    },
    reset() {
      // 重置组件数据
    },
    setRowData(value, dictData) {
      if (this.$attrs.field != null && this.$attrs.field !== '') {
        this.editConfig.isDirty = true;
        let path = this.$attrs.field.split('.');
        let temp = this.editConfig.rowCopy;
        for (let i = 0; i < path.length; i++) {
          let key = path[i];
          if (i !== path.length - 1) {
            if (temp[key] == null) {
              temp[key] = {};
            }
          } else {
            temp[key] = value;
            if (this.multiple) {
              temp[key + 'DictMapList'] = dictData;
            } else {
              temp[key + 'DictMap'] = dictData;
            }
          }
          temp = temp[key];
        }
      }
    },
    getValue(value) {
      return value;
    },
    getDictData(value) {
      return undefined;
    },
    onChange(value) {
      let finalValue = this.getValue(value);
      let dictData = this.getDictData(value);
      this.setRowData(finalValue, dictData);
      this.$emit('change', finalValue, this.editConfig.rowCopy);
    },
    focus() {
      if (this.$refs.inputWidget) this.$refs.inputWidget.focus();
    }
  }
};

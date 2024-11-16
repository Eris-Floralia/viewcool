export default {
  computed: {
    dataObject() {
      if (Array.isArray(this.data)) {
        return this.data[0];
      } else {
        return this.data;
      }
    }
  },
  methods: {
    getRowDataByColumnName(row, columnName) {
      if (row == null) return null;
      if (!Array.isArray(columnName)) {
        let dictName = columnName + '__DictMap';
        return row[dictName] ? row[dictName].name : row[columnName];
      } else {
        let dataValue = columnName.length > 0 ? row : undefined;
        for (let i = 0; i < columnName.length; i++) {
          let name = columnName[i];
          if (name == null || dataValue == null) {
            dataValue = undefined;
            break;
          }
          dataValue = dataValue[name];
        }
        return dataValue;
      }
    },
    getRowDataNameValueByColumnName(row, valueColumnName, nameColumnName) {
      if (row == null) return null;
      return {
        value: this.getRowDataByColumnName(row, valueColumnName),
        name: nameColumnName
      };
    },
    getRowDataByValueColumns(row, valueColumnList) {
      let temp = [];
      valueColumnList.forEach(valueColumn => {
        temp.push(this.fixedValue(row[valueColumn.columnName], valueColumn.fixed));
      });
      return temp;
    },
    getColumnName(columnName) {
      return Array.isArray(columnName) ? columnName.join('__') : columnName;
    },
    getCategoryColumnValue(row, categoryColumnList) {
      let columnValue = [];
      categoryColumnList.forEach(categoryColumnItem => {
        columnValue.push(this.getColumnValue(row, categoryColumnItem.columnName));
      });

      return columnValue.join('\r\n');
    },
    /*
     * 取出配置的数据字段
     * @params columnName 组件配置 datasetInfo 中的配置名称
     */
    getDataPropertyName(columnName) {
      if (
        this.options.datasetInfo[columnName] &&
        Array.isArray(this.options.datasetInfo[columnName]) &&
        this.options.datasetInfo[columnName].length > 0
      ) {
        return this.options.datasetInfo[columnName][0].columnName;
      } else {
        return undefined;
      }
    },
    /*
     * @param {object} row  是当前行数据比如 {a: { b: 'xxxx' }}
     * @param {array} columnNameList 列名数组，如：['a','b'];
     * @returns {object} 取出的值
     */
    getColumnValue(row, columnNameList) {
      if (row == null) return undefined;
      if (Array.isArray(columnNameList)) {
        let dataValue = columnNameList.length > 0 ? row : undefined;

        for (let i = 0; i < columnNameList.length; i++) {
          let name = columnNameList[i];
          if (name == null || dataValue == null) {
            dataValue = undefined;
            break;
          }
          let dictName = name + '__DictMap';
          dataValue = dataValue[dictName] ? dataValue[dictName].name : dataValue[name];
        }
        return dataValue;
      } else {
        let columnName = columnNameList;
        let dictName = columnName + '__DictMap';
        return row[dictName] ? row[dictName].name : row[columnName];
      }
    },
    // 根据配置的字段取字符串数据
    getDataString(columnName, defaultString, data) {
      let propertyName;
      if (typeof columnName === 'string') {
        propertyName = this.getDataPropertyName(columnName);
      } else {
        propertyName = columnName.columnName;
      }
      if (!propertyName) return defaultString;
      let val = this.getColumnValue(data || this.dataObject, propertyName);

      return val || defaultString;
    },
    // 根据配置的字段去数字数据
    getDataNumber(columnName, data) {
      let val = this.getDataString(columnName, '0', data);

      if (typeof val === 'string') {
        return isNaN(val) ? 0 : Number(val);
      } else {
        return val;
      }
    },
    fixedValue(value, fix) {
      if (fix === undefined) return value;
      return Number(value.toFixed(fix));
    }
  }
};

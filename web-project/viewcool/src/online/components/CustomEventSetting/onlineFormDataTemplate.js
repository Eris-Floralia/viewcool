/*
const queryFrom = `
export default {
  props: {
  },
  data () {
    return {
      // 过滤组件字段
      formData: {
        <%_ if (Array.isArray(form.widgetList)) { -%>
          <%_ form.widgetList.forEach(widget => { -%>
            <%_ if (widget.bindData.dataType === 0) { -%>
        // <%= widget.showName %>
        <%= widget.variableName %>: undefined,
            <%_ } -%>
          <%_ }); -%>
        <%_ } -%>
        // 自定义字段
        customField: {
          <%_ if (Array.isArray(form.customFieldList)) { -%>
            <%_ form.customFieldList.forEach(field => { -%>
          <%= field.fieldName %>: undefined,
            <%_ }); -%>
            <%_ } -%>
        }
      }
    };
  }
};
`;
*/
const queryFrom = `
export default {
  props: {
  },
  data () {
    return {
      formData: {
        <%_ if (Array.isArray(form.tableList)) { -%>
          <%_ form.tableList.forEach(table => { -%>
        // <%= table.showName %>
            <%_ if (table.isObject) { -%>
        <%= table.variableName %>: {
              <%_ table.columnList.forEach(column => { -%>
          // <%= column.columnComment %>
          <%= column.columnName %>: undefined,
              <%_ }) -%>
        },
            <%_ } else { -%>
              <%_ table.columnList.forEach(column => { -%>
        // <%= column.columnName %>: <%= column.columnComment %>
              <%_ }) -%>
        <%= table.variableName %>: [],
            <%_ } -%>
          <%_ }) -%>
        <%_ } -%>
        // 自定义字段
        customField: {
          <%_ if (Array.isArray(form.customFieldList)) { -%>
            <%_ form.customFieldList.forEach(field => { -%>
          <%= field.fieldName %>: undefined,
            <%_ }); -%>
          <%_ } -%>
        }
      }
    };
  }
};
`;

export default {
  queryFrom
};

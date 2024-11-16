import { nameTranslate } from '@/utils';
import { FilterValueKind, CalculateType } from '@/staticDict/reportStaticDict.js';
import { CriteriaFilterType } from '@/staticDict/index.js';
/**
 * 获取可用的过滤类型
 * @param {*} fieldType 字段类型
 * @returns
 */
export function getFilterTypeByFieldType(fieldType) {
  return CriteriaFilterType.getList().filter(item => {
    switch (fieldType) {
      case 'Boolean':
        return [CriteriaFilterType.EQ, CriteriaFilterType.NOT_EQ].indexOf(item.id) !== -1;
      case 'String':
        return (
          [
            CriteriaFilterType.EQ,
            CriteriaFilterType.NOT_EQ,
            CriteriaFilterType.LIKE,
            CriteriaFilterType.NOT_NULL,
            CriteriaFilterType.IS_NULL,
            CriteriaFilterType.IN,
            CriteriaFilterType.NOT_IN
          ].indexOf(item.id) !== -1
        );
      case 'Date':
        return (
          [
            CriteriaFilterType.EQ,
            CriteriaFilterType.GE,
            CriteriaFilterType.GT,
            CriteriaFilterType.LE,
            CriteriaFilterType.LT,
            CriteriaFilterType.BETWEEN,
            CriteriaFilterType.NOT_NULL,
            CriteriaFilterType.IS_NULL
          ].indexOf(item.id) !== -1
        );
      case 'Integer':
      case 'Long':
      case 'Double':
      case 'BigDecimal':
        return (
          [
            CriteriaFilterType.EQ,
            CriteriaFilterType.NOT_EQ,
            CriteriaFilterType.GE,
            CriteriaFilterType.GT,
            CriteriaFilterType.LE,
            CriteriaFilterType.LT,
            CriteriaFilterType.NOT_NULL,
            CriteriaFilterType.IS_NULL,
            CriteriaFilterType.IN,
            CriteriaFilterType.NOT_IN
          ].indexOf(item.id) !== -1
        );
      default:
        return [];
    }
  });
}
/**
 * 获取过滤可用的参数值类型
 * @param {*} filterValueList 参数值类型列表
 * @param {*} filterType 过滤类型
 * @param {*} column 过滤字段
 * @returns
 */
export function getValidFilterValueTypeByFieldType(filterValueList, filterType, column) {
  if (!Array.isArray(filterValueList)) return [];
  return filterValueList
    .filter(item => {
      switch (column.fieldType) {
        case 'Boolean':
          return (
            [
              FilterValueKind.FORM_PARAM,
              FilterValueKind.WIDGET_DATA,
              FilterValueKind.DICT_DATA,
              FilterValueKind.PRINT_INPUT_PARAM,
              FilterValueKind.INPUT_DATA
            ].indexOf(item) !== -1
          );
        case 'String':
          return (
            [
              FilterValueKind.FORM_PARAM,
              FilterValueKind.WIDGET_DATA,
              FilterValueKind.DICT_DATA,
              FilterValueKind.COLUMN_DATA,
              FilterValueKind.PRINT_INPUT_PARAM,
              FilterValueKind.INPUT_DATA
            ].indexOf(item) !== -1
          );
        case 'Date':
          return (
            [
              FilterValueKind.FORM_PARAM,
              FilterValueKind.WIDGET_DATA,
              FilterValueKind.DICT_DATA,
              FilterValueKind.COLUMN_DATA,
              FilterValueKind.PRINT_INPUT_PARAM,
              FilterValueKind.INNER_VARIABLE,
              FilterValueKind.INPUT_DATA
            ].indexOf(item) !== -1
          );
        case 'Integer':
        case 'Long':
        case 'Double':
        case 'BigDecimal':
          return (
            [
              FilterValueKind.FORM_PARAM,
              FilterValueKind.WIDGET_DATA,
              FilterValueKind.DICT_DATA,
              FilterValueKind.COLUMN_DATA,
              FilterValueKind.PRINT_INPUT_PARAM,
              FilterValueKind.INPUT_DATA
            ].indexOf(item) !== -1
          );
        default:
          return [];
      }
    })
    .filter(item => {
      // 包含和不包含类型只能使用字典和字段值过滤
      if (filterType === CriteriaFilterType.IN || filterType === CriteriaFilterType.NOT_IN) {
        return item === FilterValueKind.DICT_DATA || item === FilterValueKind.COLUMN_DATA;
      } else {
        return true;
      }
    });
}

export function getValueColumnName(calculateType, columnName) {
  let calculateName = null;
  if (columnName == null || columnName === '') return calculateName;
  switch (calculateType) {
    case CalculateType.SUM:
      calculateName = 'sumOf';
      break;
    case CalculateType.COUNT:
      calculateName = 'countOf';
      break;
    case CalculateType.AVG:
      calculateName = 'avgOf';
      break;
    case CalculateType.MIN_BY:
      calculateName = 'minOf';
      break;
    case CalculateType.MAX_BY:
      calculateName = 'maxOf';
      break;
    case CalculateType.STD_DEV:
      calculateName = 'stddevOf';
      break;
    case CalculateType.MEAN_DEV:
      calculateName = 'varpopOf';
      break;
  }
  return calculateName + nameTranslate(columnName, 1);
}

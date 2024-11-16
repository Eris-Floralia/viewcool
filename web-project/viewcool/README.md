## 橙单代码生成器
### 构建命令
``` bash
# install dependencies
npm install

# serve with hot reload at localhost:8080
npm run dev

# build for production with minification
npm run build

# run all tests
npm test
```

### 第三方接入
#### 在线表单接入地址
- 数据库链接：http://localhost:8085/#/thirdParty/thirdOnlineDblink   
- 字典管理：http://localhost:8085/#/thirdParty/thirdFormOnlineDict   
- 表单管理：http://localhost:8085/#/thirdParty/thirdOnlinePage   
- 在线表单：http://localhost:8085/#/thirdParty/thirdOnlineForm?formId=XXXXX   
  只允许接入主表查询页面，接入地址可以在查询表单配置页面表单属性面板中复制。   
- 工单列表：http://localhost:8085/#/thirdParty/thirdOnlineForm?formId=XXXXX&entryId=XXXXX   
  接入工单列表页面的时候，需要在url内拼上使用这个表单的流程定义的id
#### 打印模块接入地址
- 打印管理：http://localhost:8085/#/thirdParty/thirdPrint
- 数据库链接：http://localhost:8085/#/thirdParty/thirdReportDblink
- 数据集：http://localhost:8085/#/thirdParty/thirdReportDataset
- 报表字典：http://localhost:8085/#/thirdParty/thirdReportDict
- 报表页面配置：http://localhost:8085/#/thirdParty/thirdReportPage
- 报表页面渲染：http://localhost:8085/#/thirdParty/thirdReport?pageId=XXXXX
#### 工作流
- 流程分类：http://localhost:8085/#/thirdParty/thirdFormFlowCategory
- 流程设计：http://localhost:8085/#/thirdParty/thirdFormFlowEntry
- 流程实例：http://localhost:8085/#/thirdParty/thirdFormAllInstance
- 待办任务：http://localhost:8085/#/thirdParty/thirdFormMyTask
- 历史任务：http://localhost:8085/#/thirdParty/thirdFormMyHistoryTask
- 已办任务：http://localhost:8085/#/thirdParty/thirdFormMyApprovedTask
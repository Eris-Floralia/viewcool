<template>
  <el-row type="flex" :justify="align">
    <img :src="imageUrl" :style="getStyle" />
  </el-row>
</template>

<script>
import { uploadMixin } from '@/core/mixins';

export default {
  name: 'onlineCustomImage',
  props: {
    value: {
      type: String
    },
    src: {
      type: String
    },
    fit: {
      type: String
    },
    align: {
      type: String,
      default: 'left'
    },
    width: {
      type: String
    },
    height: {
      type: String
    },
    radius: {
      type: Number,
      default: 5
    },
    round: {
      type: Boolean,
      default: false
    },
    widget: {
      type: Object
    }
  },
  mixins: [uploadMixin],
  inject: ['form'],
  data() {
    return {
      fileList: []
    };
  },
  methods: {
    isBase64(src) {
      return /^data:image\/\w+;base64,/.test(src);
    }
  },
  computed: {
    getStyle() {
      let temp = this.round ? '50%' : this.radius + 'px';
      return {
        width: this.width != null ? this.width : '200px',
        height: this.height != null ? this.height : '200px',
        'object-fit': this.fit,
        'border-radius': temp
      };
    },
    buildFlowParam() {
      let flowParam = {};
      let flowData = this.form().flowData;
      if (flowData) {
        if (flowData.processDefinitionKey)
          flowParam.processDefinitionKey = flowData.processDefinitionKey;
        if (flowData.processInstanceId) flowParam.processInstanceId = flowData.processInstanceId;
        if (flowData.taskId) flowParam.taskId = flowData.taskId;
      }

      return flowParam;
    },
    imageUrl() {
      if (Array.isArray(this.fileList) && this.fileList.length > 0) {
        return this.fileList[0].url;
      } else {
        return this.isBase64(this.src) ? this.src : this.getDownloadUrl;
      }
    },
    getDownloadUrl() {
      if (this.value != null) {
        if (this.widget.relation) {
          return (
            '/admin/online/onlineOperation/downloadOneToManyRelation/' +
            (this.widget.datasource || {}).variableName
          );
        } else {
          return (
            '/admin/online/onlineOperation/downloadDatasource/' +
            (this.widget.datasource || {}).variableName
          );
        }
      } else {
        let imgUrl;
        try {
          imgUrl = this.src ? JSON.parse(this.src) : undefined;
          if (imgUrl) {
            imgUrl = this.getUploadFileUrl(imgUrl, { filename: imgUrl.filename });
          }
        } catch (e) {
          imgUrl = null;
        }
        return imgUrl;
      }
    }
  },
  watch: {
    value: {
      handler(newValue) {
        setTimeout(() => {
          this.fileList = [];
          if (newValue != null) {
            let downloadParams = {
              ...this.buildFlowParam,
              datasourceId: (this.widget.datasource || {}).datasourceId,
              fieldName: this.widget.column.columnName,
              asImage: true,
              dataId: this.form().getPrimaryData(this.widget) || ''
            };
            if (this.widget.relation) downloadParams.relationId = this.widget.relation.relationId;
            let temp = JSON.parse(newValue);
            temp = Array.isArray(temp)
              ? temp.map(item => {
                  return {
                    ...item,
                    downloadUri: this.getDownloadUrl
                  };
                })
              : [];
            this.fileList = this.parseUploadData(JSON.stringify(temp), downloadParams);
          }
        }, 30);
      },
      immediate: true
    }
  }
};
</script>

<style></style>

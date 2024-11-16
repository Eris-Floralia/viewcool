<template>
  <div class="online-custom-upload">
    <el-upload
      v-if="!readOnly"
      :class="{
        'upload-image-item': isImage,
        'upload-image-multi': maxFileCount !== 1
      }"
      :name="widget.props.fileFieldName"
      :headers="getUploadHeaders"
      :action="getActionUrl"
      :data="getUploadData"
      :on-success="onUploadSuccess"
      :on-remove="onRemoveFile"
      :on-error="onUploadError"
      :on-exceed="onUploadLimit"
      :limit="maxFileCount"
      :show-file-list="maxFileCount !== 1 || !isImage"
      :list-type="getUploadListType"
      :file-list="getUploadFileList"
      :disabled="getDisabledStatus()"
    >
      <!-- 上传图片 -->
      <template v-if="isImage && maxFileCount === 1">
        <div v-if="getUploadFileList && getUploadFileList[0] != null" style="position: relative">
          <img class="upload-image-show" :src="getUploadFileList[0].url" />
          <div class="upload-img-del el-icon-close" @click.stop="onRemoveFile(null, null)" />
        </div>
        <i v-else class="el-icon-plus upload-image-item"></i>
      </template>
      <!-- 上传文件 -->
      <template v-else-if="!isImage">
        <el-button :size="defaultFormItemSize" type="primary">点击上传</el-button>
      </template>
    </el-upload>
    <template v-else>
      <template v-if="isImage">
        <el-image
          v-for="item in uploadWidgetImpl.fileList"
          :preview-src-list="(uploadWidgetImpl.fileList || []).map(imgItem => imgItem.url)"
          class="table-cell-image"
          :key="item.url"
          :src="item.url"
          fit="fill"
        >
        </el-image>
      </template>
      <a
        v-else
        v-for="item in uploadWidgetImpl.fileList"
        :key="item.url"
        href="javascript:void(0);"
        @click="downloadFile(item.url, item.name)"
      >
        {{ item.name }}
      </a>
    </template>
  </div>
</template>

<script>
import { UploadWidget } from '@/utils/widget.js';
import { uploadMixin } from '@/core/mixins';

export default {
  name: 'onlineCustomUpload',
  props: {
    value: {
      type: String
    },
    widget: {
      type: Object,
      required: true
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  mixins: [uploadMixin],
  inject: ['form'],
  data() {
    return {
      isImage: false,
      uploadWidgetImpl: new UploadWidget(this.widget.column ? this.widget.column.maxFileCount : 0)
    };
  },
  methods: {
    getDisabledStatus() {
      if (this.form().isEdit) return true;
      if (this.widget.eventInfo && this.widget.eventInfo[this.OnlineFormEventType.DISABLE]) {
        return this.widget.eventInfo[this.OnlineFormEventType.DISABLE]();
      } else {
        return this.widget.props.disabled;
      }
    },
    onValueChange() {
      this.$emit('input', this.fileListToJson(this.uploadWidgetImpl.fileList), this.widgetConfig);
    },
    onUploadSuccess(response, file, fileList) {
      if (response.success) {
        file.filename = response.data.filename;
        file.url = URL.createObjectURL(file.raw);
        this.uploadWidgetImpl.onFileChange(file, fileList);
        this.onValueChange();
      } else {
        this.$message.error(response.message);
      }
    },
    onRemoveFile(file, fileList) {
      this.uploadWidgetImpl.onFileChange(file, fileList);
      this.onValueChange();
    },
    onUploadError(e, file, fileList) {
      this.$message.error('文件上传失败');
    },
    onUploadLimit(files, fileList) {
      if (this.maxFileCount != null && this.maxFileCount > 0) {
        this.$message.error('已经超出最大上传个数限制');
      }
    }
  },
  computed: {
    /*
    isImage () {
      return this.widget && this.widget.column && this.widget.column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE;
    },
    */
    maxFileCount() {
      return this.widget.column ? this.widget.column.maxFileCount : 0;
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
    getActionUrl() {
      if (this.widget.props.actionUrl == null || this.widget.props.actionUrl === '') {
        if (this.widget.relation) {
          return this.getUploadActionUrl(
            '/admin/online/onlineOperation/uploadOneToManyRelation/' +
              (this.widget.datasource || {}).variableName
          );
        } else {
          return this.getUploadActionUrl(
            '/admin/online/onlineOperation/uploadDatasource/' +
              (this.widget.datasource || {}).variableName
          );
        }
      } else {
        return this.getUploadActionUrl(this.widget.props.actionUrl);
      }
    },
    getDownloadUrl() {
      if (this.widget.props.downloadUrl == null || this.widget.props.downloadUrl === '') {
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
        return this.widget.props.downloadUrl;
      }
    },
    getUploadData() {
      let temp = {
        ...this.buildFlowParam,
        datasourceId: (this.widget.datasource || {}).datasourceId,
        asImage: this.isImage,
        fieldName: (this.widget.column || {}).columnName
      };
      if ((this.widget.relation || {}).relationId)
        temp.relationId = (this.widget.relation || {}).relationId;
      let flowData = this.form().flowData;
      if (flowData && flowData.processDefinitionKey)
        temp.processDefinitionKey = flowData.processDefinitionKey;
      return temp;
    },
    getUploadListType() {
      if (this.maxFileCount !== 1 && this.isImage) {
        return 'picture-card';
      }
      return 'text';
    },
    getUploadFileList() {
      return this.uploadWidgetImpl ? this.uploadWidgetImpl.fileList : [];
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
  },
  watch: {
    value: {
      handler(newValue) {
        setTimeout(() => {
          this.uploadWidgetImpl.fileList = [];
          if (newValue != null) {
            let downloadParams = {
              ...this.buildFlowParam,
              datasourceId: (this.widget.datasource || {}).datasourceId,
              fieldName: this.widget.column.columnName,
              asImage: this.isImage,
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
            this.uploadWidgetImpl.fileList = this.parseUploadData(
              JSON.stringify(temp),
              downloadParams
            );
          }
        }, 30);
      },
      immediate: true
    },
    maxFileCount: {
      handler() {
        if (this.uploadWidgetImpl) this.uploadWidgetImpl.maxCount = this.maxFileCount;
      },
      immediate: true
    },
    'widget.column': {
      handler() {
        let column = this.widget.bindData.column || this.widget.column;
        this.isImage = column ? column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE : false;
      },
      immediate: true
    },
    'widget.bindData.columnId': {
      handler() {
        let column = this.widget.bindData.column || this.widget.column;
        this.isImage = column ? column.fieldKind === this.SysOnlineFieldKind.UPLOAD_IMAGE : false;
      },
      immediate: true
    }
  }
};
</script>

<style></style>

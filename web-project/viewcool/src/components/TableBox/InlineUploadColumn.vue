<template>
  <vxe-column v-bind="$attrs" v-on="$listeners">
    <template slot-scope="scope">
      <el-form-item v-if="isEdit(scope.row)" label="" :prop="$attrs.field" style="width: 100%">
        <template v-if="fileListType === 'dropdown'">
          <el-dropdown
            size="mini"
            split-button
            type="default"
            trigger="click"
            :class="{ 'hide-list': uploadWidget.fileList.length <= 0 }"
          >
            <el-upload
              name="uploadFile"
              style="width: 100%; height: 13px"
              :headers="getUploadHeaders"
              :action="getUploadActionUrl(action)"
              :data="data"
              :limit="limit"
              :show-file-list="false"
              :file-list="uploadWidget.fileList"
              :on-success="onUploadSuccess"
              :on-remove="onRemoveFile"
              :on-error="onUploadError"
              :on-exceed="onUploadLimit"
            >
              <el-row type="flex" align="middle">
                <span>上传文件</span>
                <div
                  v-if="uploadWidget.fileList.length > 0"
                  style="
                    height: 12px;
                    line-height: 12px;
                    background: #f56c6c;
                    border-radius: 6px;
                    padding: 0px 6px;
                    color: white;
                    min-width: 12px;
                    font-size: 11px;
                    margin-left: 5px;
                  "
                >
                  {{ uploadWidget.fileList.length }}
                </div>
              </el-row>
            </el-upload>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="item in uploadWidget.fileList" :key="item.url">
                <el-row type="flex" justify="space-between" align="middle" style="height: 30px">
                  <span>{{ item.name }}</span>
                  <i
                    class="el-icon-error"
                    style="margin-left: 6px"
                    @click.stop="onRemoveFile(item, uploadWidget.fileList)"
                  />
                </el-row>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <el-upload
          v-else
          name="uploadFile"
          style="width: 100%"
          class="table-inline-upload"
          :headers="getUploadHeaders"
          :action="getUploadActionUrl(action)"
          :data="data"
          :limit="limit"
          :show-file-list="false"
          :file-list="uploadWidget.fileList"
          :on-success="onUploadSuccess"
          :on-remove="onRemoveFile"
          :on-error="onUploadError"
          :on-exceed="onUploadLimit"
        >
          <el-row v-if="type === 'image'" type="flex" style="flex-wrap: wrap">
            <!-- 上传图片 -->
            <div v-for="item in uploadWidget.fileList" :key="item.url" style="position: relative">
              <el-image
                :preview-src-list="uploadWidget.fileList"
                class="table-cell-image"
                :src="item.url"
              />
              <div
                class="upload-img-del el-icon-close"
                @click.stop="onRemoveFile(item, uploadWidget.fileList)"
              />
            </div>
            <i
              v-if="uploadWidget.fileList.length < limit || limit === 0 || limit == null"
              class="table-cell-image el-icon-plus upload-image-item"
            ></i>
          </el-row>
          <!-- 上传文件 -->
          <div v-else class="upload-file-list">
            <el-button size="mini" type="default">点击上传</el-button>
            <div
              class="file-list-item"
              v-for="item in uploadWidget.fileList"
              :key="item.url"
              tabindex="-1"
            >
              <a class="ellipsis-item" :href="item.url" target="_blank" tabindex="-1">
                {{ item.name }}
              </a>
              <el-button
                type="danger"
                icon="el-icon-close"
                circle
                style="margin-left: 6px; margin-top: 1px; padding: 1px; font-size: 10px"
                tabindex="-1"
                @click.stop="onRemoveFile(item, uploadWidget.fileList)"
              />
            </div>
          </div>
        </el-upload>
      </el-form-item>
      <!-- 判断是否有default的slot -->
      <slot v-else-if="$slots.default || $scopedSlots.default" :row="scope.row" />
      <!-- 默认显示内容 -->
      <span v-else>{{ getRowValue(scope.row) }}</span>
    </template>
  </vxe-column>
</template>

<script>
import { TableInlintColumnMixins } from './tableInlineColumn.js';
import { uploadMixin } from '@/core/mixins';
import { UploadWidget } from '@/utils/widget.js';

export default {
  name: 'InlineUploadColumn',
  props: {
    /**
     * 上传文件类型，image/file
     * image: 图片
     * file: 文件
     */
    type: {
      type: String,
      default: 'file'
    },
    /**
     * 文件列表类型，dropdown/expand
     * dropdown: 列表在下拉框内
     * expand: 列表展开
     */
    fileListType: {
      type: String,
      default: 'expand'
    },
    /**
     * 上传的地址
     */
    action: {
      type: String,
      default: ''
    },
    /**
     * 上传时附带的额外参数
     */
    data: {
      type: Object,
      default: () => ({})
    },
    /**
     * 上传文件个数限制
     */
    limit: {
      type: Number
    },
    /**
     * 主键字段名
     */
    rowKey: {
      type: String
    }
  },
  mixins: [TableInlintColumnMixins, uploadMixin],
  data() {
    return {
      showPopover: false,
      uploadWidget: new UploadWidget(this.limit)
    };
  },
  methods: {
    getValue(value) {
      return this.fileListToJson(value);
    },
    onUploadSuccess(response, file, fileList) {
      if (response.success) {
        file.downloadUri = response.data.downloadUri;
        file.filename = response.data.filename;
        file.uploadPath = response.data.uploadPath;
        file.url = URL.createObjectURL(file.raw);
        this.uploadWidget.onFileChange(file, fileList);
        this.onChange(this.uploadWidget.fileList);
      } else {
        this.$message.error(response.message);
      }
    },
    onRemoveFile(file, fileList) {
      if (file != null && fileList != null) {
        fileList = fileList.filter(item => item !== file);
      }
      this.uploadWidget.onFileChange(file, fileList);
      this.onChange(this.uploadWidget.fileList);
    },
    onUploadError(e, file, fileList) {
      this.$message.error('文件上传失败');
    },
    onUploadLimit(file, fileList) {
      this.$message.error('已经超出最大上传个数限制');
    }
  },
  watch: {
    getRowData: {
      handler(value) {
        let row = (this.editConfig || {}).rowCopy;
        let downloadParams = {
          ...this.data
        };
        if (row && row[this.rowKey]) downloadParams[this.rowKey] = row[this.rowKey];
        console.log('getRowData', value, downloadParams);
        this.uploadWidget.fileList = this.parseUploadData(value, downloadParams);
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.upload-image-item {
  height: 65px;
  width: 65px;
  line-height: 65px;
  text-align: center;
  font-size: 28px !important;
  border-radius: 4px;
  border: 1px dashed #d9d9d9;
}
.ellipsis-item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.hide-list /deep/ .el-dropdown__caret-button {
  display: none;
}
.upload-file-list {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 10px 0px;
}
.file-list-item {
  display: flex;
  align-items: center;
  margin-top: 5px;
  width: 100%;
  text-align: left;
}
.table-inline-upload /deep/ .el-upload {
  width: 100%;
}
</style>

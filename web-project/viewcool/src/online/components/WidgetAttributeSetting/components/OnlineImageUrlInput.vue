<template>
  <div>
    <el-input :value="value" @input="onValueChange" :size="size" :disabled="disabled">
      <el-button slot="append" icon="el-icon-upload2" @click="onSelect" :disabled="disabled">
      </el-button>
    </el-input>
    <input
      type="file"
      ref="fileSelect"
      v-show="false"
      accept="image/*"
      @change="onSelectImageChange"
    />
  </div>
</template>

<script>
// import { fileToBase64 } from '@/utils';
import { uploadMixin } from '@/core/mixins';

export default {
  props: {
    value: {
      type: String
    },
    size: {
      type: String,
      default: 'small'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  mixins: [uploadMixin],
  methods: {
    onSelect() {
      this.$refs.fileSelect.click();
    },
    onSelectImageChange(e) {
      let file = e.target.files[0];
      if (file != null) {
        let uploadUrl = this.getUploadActionUrl('/admin/commonext/util/uploadImage');
        this.upload(uploadUrl, {
          uploadFile: file
        })
          .then(res => {
            console.log(res);
            console.log(res.data);
            this.onValueChange(JSON.stringify(res.data));
          })
          .catch(e => {
            console.log(e);
          });
        /*
        fileToBase64(file).then(res => {
          this.onValueChange(res);
        }).catch(e => {
          console.log(e);
        });
        */
      }
    },
    onValueChange(val) {
      this.$emit('input', val);
    }
  }
};
</script>

<style></style>

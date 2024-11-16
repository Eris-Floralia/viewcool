<template>
  <div ref="root" style="position: absolute; font-size: 14px" />
</template>

<script>
const DEFAULT_OPTIONS = {
  useWrapMode: false,
  tabSize: 2
};

export default {
  props: {
    value: {
      type: String
    },
    type: {
      type: String,
      default: 'javascript'
    },
    options: {
      type: Object,
      default: () => {
        return DEFAULT_OPTIONS;
      }
    }
  },
  data() {
    return {
      editor: undefined
    };
  },
  mounted() {
    let finalOptions = {
      ...DEFAULT_OPTIONS,
      ...this.options
    };
    this.editor = window.ace.edit(this.$el);
    this.editor.getSession().setMode('ace/mode/' + this.type);
    this.editor.session.setTabSize(finalOptions.tabSize || 2);
    this.editor.session.setUseWrapMode(finalOptions.useWrapMode || false);
    this.editor.getSession().on('change', e => {
      this.$emit('input', this.editor.getValue());
    });
    this.editor.setReadOnly(finalOptions.readOnly);
    if (this.value) this.editor.setValue(this.value);
    this.editor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true
    });
  },
  destroyed() {
    this.editor.destroy();
    this.editor.container.remove();
    this.editor = undefined;
  },
  watch: {
    value: {
      handler(newValue) {
        if (this.editor && this.editor.getValue() !== newValue) {
          this.editor.setValue(newValue);
          this.editor.moveCursorTo(0, 0);
        }
      },
      immediate: true
    }
  }
};
</script>

<style></style>

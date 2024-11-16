module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: ['plugin:vue/essential', 'eslint:recommended', '@vue/standard', '@vue/prettier'],
  parserOptions: {
    parser: 'babel-eslint'
  },
  rules: {
    'no-console': 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    // semi: ['off', 'always'],
    'prefer-promise-reject-errors': ['error', { allowEmptyReject: true }],
    'no-trailing-spaces': ['error', { skipBlankLines: true }],
    'prefer-const': ['off'],
    'quote-props': ['off'],
    'object-curly-spacing': ['off'],
    'dot-notation': ['off'],
    'lines-between-class-members': ['off'],
    // 'no-undef': ['off', 'always'],
    'no-unused-vars': ['off', 'always'],
    'no-new-func': ['off', 'always'],
    'vue/no-mutating-props': 0,
    'space-before-function-paren': 0,
    'vue/multi-word-component-names': 'off'
  },

  overrides: [
    {
      files: ['**/__tests__/*.{j,t}s?(x)', '**/tests/unit/**/*.spec.{j,t}s?(x)'],
      env: {
        jest: true
      }
    },
    // 这里是添加的代码
    {
      files: ['src/views/**/index.vue', 'src/layout/**/index.vue', 'src/components/**/index.vue'], // 匹配views和二级目录中的index.vue
      rules: {
        'vue/multi-word-component-names': 'off'
      } // 给上面匹配的文件指定规则
    }
  ]
};

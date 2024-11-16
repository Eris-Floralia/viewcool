module.exports = (file, root = 'views') => {
  return require('../' + root + '/' + file + '.vue').default;
};

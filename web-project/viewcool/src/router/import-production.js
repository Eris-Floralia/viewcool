module.exports = (file, root = 'views') => {
  return function () {
    return import('../' + root + '/' + file + '.vue');
  };
};

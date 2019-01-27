var vm = new Vue({
  el: '#example',
  data: {
    message: 'Hello'
  },
  computed: {
    // 计算属性的 getter
    reversedMessage: function () {
      // `this` 指向 vm 实例
      return this.message.split('').reverse().join('')
    }
  },
  methods: {
    reversedMessage: function () {
      // `this` 指向 vm 实例
      return this.message.split('').reverse().join('')
    }
  },

  // 我们可以将同一函数定义为一个方法而不是一个计算属性。
  // 两种方式的最终结果确实是完全相同的。
  // 然而，不同的是计算属性是基于它们的依赖进行缓存的。

})
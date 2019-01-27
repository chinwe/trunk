var obj = {
  foo: 'bar'
}

Object.freeze(obj)

new Vue({
  el: '#app',
  data: obj,
  created: function () {
    // `this` 指向 vm 实例
    console.log('foo is: ' + this.foo)
  }
})
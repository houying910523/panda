import Vue from 'vue'
import 'bootstrap/dist/js/bootstrap.js'
import "bootstrap/dist/css/bootstrap.css"
import router from './router.js'
import app from './components/app.vue'

new Vue({
    el: '#index',
    router,
    render(h) {
        return h(app);
    }
});


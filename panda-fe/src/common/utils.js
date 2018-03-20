import $ from 'jquery'
import router from '../router.js'
export default {

    alert(message) {
        this.notify("alert", message);
    },

    success(message) {
        this.notify("success", message);
    },

    notify(type, message) {
        const colorClass = {
            "success": "alert-success",
            "alert": "alert-danger"
        }[type];
        const box = $("#alertMsgBox");
        box.css({
            display: 'block'
        });
        box.removeClass("alert-success");
        box.removeClass("alert-danger");
        box.addClass(colorClass);
        $("#alertMsg").text(message);
        window.setTimeout(function() {
            box.css({
                display: 'none'
            })
        }, 10000);
    },

    isLogin () {
        var username = this.user();
        return username != null && username !== "null";
    },

    user () {
        var username = window.localStorage.getItem("user");
        if (username != null && username != "null") {
            return username;
        }
        $.ajax({
            url: "/api/user",
            type: 'get',
            async: false,
            success: function(data) {
                if (data.status == 0) {
                    username = data.data;
                }
            }
        });
        window.localStorage.setItem("user", username);
        return username;
    },

    fetchSync(url, data) {
        if (data) {
            var i = 0;
            for (var key in data) {
                if (i == 0) {
                    url = url + "?" + key + "=" + data[key];
                } else {
                    url = url + "&" + key + "=" + data[key];
                }
                i = i + 1;
            }
        }
        var ret = undefined;
        $.ajax({
            url: url,
            type: 'get',
            async: false,
            success: function(json) {
                if (json.status == 0) {
                    ret = json.data;
                } else {
                    ret = json.message;
                }
            }
        });
        return ret;
    },

    fetch (url, data, success, error) {
        if (arguments.length == 3) {
            success = arguments[1];
            error = arguments[2];
            data = {}
        }
        var i = 0;
        for (var key in data) {
            if (i == 0) {
                url = url + "?" + key + "=" + data[key];
            } else {
                url = url + "&" + key + "=" + data[key];
            }
            i = i + 1;
        }
        $.get(url, function(data) {
            if (data.status == 0) {
                success(data);
            } else if (data.status == 302) {
                router.push({path: '/login'})
            } else {
                error(data);
            }
        })
    },

    post (url, data, success, error) {
        if (arguments.length == 3) {
            data = {};
            success = arguments[1];
            error = arguments[2];
        }

        $.post(url, data, function(data) {
            if (data.status == 0) {
                success(data);
            } else if (data.status == 302) {
                router.push({path: '/login'})
            } else {
                error(data);
            }
        })
    },

    postJson (url, data, success, error) {
        if (arguments.length == 3) {
            data = {};
            success = arguments[1];
            error = arguments[2];
        }

        $.ajax({
            type: "POST",
            url: url,
            contentType: "application/json; charset=UTF-8",
            data: JSON.stringify(data),
            dataType: "json",
            success: function(data) {
                if (data.status == 0) {
                    success(data);
                } else if (data.status == 302) {
                    router.push({path: '/login'})
                } else {
                    error(data);
                }
            }
        });
    }
}

$(function () {
    $('[data-toggle="tooltip"]').tooltip();
});

function err(msg, f) {
    layer.alert(msg, {icon: 2, closeBtn: 0, title: false, offset: '100px'}, f);
}

function info(msg, f) {
    layer.alert(msg, {icon: 0, closeBtn: 0, title: false, offset: '100px'}, f);
}

function succ(msg, f) {
    layer.msg(msg, {
        icon: 1,
        time: 1500,
        offset: '100px'
    }, f);
}

function long_succ(msg, f) {
    layer.msg(msg, {
        icon: 1,
        time: 2500,
        offset: '100px'
    }, f);
}

function handle_json(json, f) {
    if (json.msg.length > 0) {
        err(json.msg);
    } else {
        succ('恭喜，操作成功：）', f);
    }
}


function forNum(a) {
    return 10 > a ? "0" + a: a
}
function countdown(a, b) {
    var c = new Date(b).getTime() / 1e3,
    d = c - a,
    e = parseInt(d % 60),
    f = parseInt(d / 60 % 60),
    g = parseInt(d / 3600 % 24),
    h = parseInt(d / 3600 / 24),
    i = [e.toString(), f.toString(), g.toString()];
    return 0 >= d ? ["00小时00分00秒", !0] : h > 0 ? (window._timestr = h + "天" + forNum(i[2]) + "时" + forNum(i[1]) + "分" + forNum(i[0]) + "秒", [h + "<span>天</span>" + forNum(i[2]) + "<span>时</span>" + forNum(i[1]) + "<span>分</span>" + forNum(i[0]) + "<span>秒</span>", !1]) : (window._timestr = forNum(i[2]) + "时" + forNum(i[1]) + "分" + forNum(i[0]) + "秒", [forNum(i[2]) + "<span>时</span>" + forNum(i[1]) + "<span>分</span>" + forNum(i[0]) + "<span>秒</span>", !1])
}
function WebEventTrigger(a, b) {
    try {
        if (WE && WE.trigger) return "string" != typeof b && (b = JSON.stringify(b)),
        WE.trigger(a, b)
    } catch(c) {}
    return ! 1
}
function isApp() {
    try {
        if (WE) return ! 0
    } catch(a) {}
    return ! 1
}
var quickBuy = {
    init: function() {
        this.config = {},
        this.config.hdurl = {
            miphone: "gen/130927/20130925113229/tip_SaledOverAll.html",
            mibox: "hezi/130927/20130925113340/tip_SaledOverAll.html"
        },
        this.config.mode = "",
        this.mode = {
            miphone: !1,
            mibox: !1
        },
        this.modeModals = {
            miphone: "#modal_miphone",
            mibox: "#modal_mibox"
        },
        this.modeCookies = {
            miphone: "xm_hd_so_phone",
            mibox: "xm_hd_so_box"
        },
        this.config.startDate = +new Date("09/27/2013 12:00:00"),
        this.maskLoad = $("#maskLoad"),
        this.config.minSignTime = 120,
        this.results = null,
        this.timer = null,
        this.timeCount = Math.floor(Math.abs(10 * Math.random() - 5)) + 5;
        var a = !0;
        for (var b in this.mode) $.cookie(this.modeCookies[b]) || (a = !1),
        $.cookie(this.modeCookies[b]) && this.saleOut();
        a || ($.cookie("xm_diff_hd") ? (this.config.serverTime = this.localTime() + parseInt($.cookie("xm_diff_hd"), 10), this.config.serverTime >= parseInt(this.config.startDate / 1e3, 10) && !$.cookie("xm_xt_hd") && ($.cookie("xm_hd_so_phone") && $.cookie("xm_hd_so_box") || this.getData(!0), this.goBuy()), this.launch()) : this.getData())
    },
    getData: function(a, b) {
        var c = this,
        a = a || null;
        b && (b = b),
        $.ajax({
            url: "http://tc.hd.xiaomi.com/hdget",
            dataType: "jsonp",
            jsonpCallback: "hdcontrol",
            timeout: 7e3,
            beforeSend: function() {
                c.maskLoad.css("display", "block")
            },
            error: function() {
                c.maskLoad.css("display", "none"),
                alert("服务器压力山大，请您重新刷新页面！")
            },
            success: function(d) {
                if (c.maskLoad.css("display", "none"), "undefined" == typeof d) return alert("服务器压力山大，请您重新刷新页面！"),
                void 0;
                if (c.results = d, a || (c.config.serverTime = c.results.stime), !$.cookie("xm_diff_hd")) {
                    var e = parseInt(c.config.serverTime - c.localTime(), 10);
                    $.cookie("xm_diff_hd", e, {
                        path: "/",
                        domain: ".xiaomi.com",
                        expires: 1
                    })
                }
                if (a) {
                    c.initialData();
                    for (var f in c.mode) if (c.config[f].hdstart === !1 && c.config[f].hdstop === !0) {
                        var g = c.modeCookies[f];
                        $.cookie(g, 1, {
                            path: "/",
                            domain: ".xiaomi.com",
                            expires: 1
                        }),
                        c.saleOut()
                    } else b && b.call()
                }
                c.launch()
            }
        })
    },
    initialData: function() {
        this.config.allow = this.results.status.allow,
        this.config.mibox = this.results.status.mibox,
        this.config.miphone = this.results.status.miphone
    },
    launch: function() {
        this.timeStart(),
        this.countdown()
    },
    timeStart: function() {
        var a = this,
        b = 1e3 * a.config.serverTime,
        c = a.config.startDate,
        d = c - 1e3 * 60 * a.config.minSignTime;
        b >= d && c > b && !isApp() ? $.cookie("userId") ? $("#buy_btn_2").html('<a href="javascript:;" class="btn_buy btn_02">已经登录</a>') : a.toLogin() : b >= c && a.toBuy()
    },
    toLogin: function() {
        $("#buy_btn_2").html('<a href="javascript:;" class="btn_buy btn_02">提前登录</a>'),
        $("#buy_btn_2").find(".btn_02").on("click",
        function() {
            WebEventTrigger("login", null) || (window.location.href = "http://m.xiaomi.com/mshopapi/index.php/v1/authorize/sso?client_id=180100031013&callback=" + encodeURIComponent(window.location.href))
        })
    },
    countdown: function() {
        var a = this,
        b = null,
        c = a.config.startDate;
        b = a.config.serverTime ? a.config.serverTime: parseInt( + new Date / 1e3, 10);
        var d = c - 1e3 * 60 * a.config.minSignTime,
        e = setInterval(function() {
            var f = countdown(b, c);
            f[1] ? (clearInterval(e), $("#countdown").css("display", "none"), a.toBuy()) : (1e3 * b != d || isApp() || ($.cookie("userId") ? $("#buy_btn_2").html('<a href="javascript:;" class="btn_buy btn_02">已经登录</a>') : a.toLogin()), b++),
            $("#countdown").html(f[0])
        },
        1e3)
    },
    toBuy: function() {
        var a = this;
        a.goBuy();
        var b = a.modeModals[a.config.mode];
        $(b).find(".close").on("click",
        function() {
            $(b).css("display", "none"),
            window.clearTimeout(a.timer),
            a.timer = null
        })
    },
    saleOut: function() {
        var a = {
            miphone: "#buy_btn_1",
            mibox: "#buy_btn_2"
        },
        b = {
            miphone: "http://t.hd.xiaomi.com/r/?_a=payment_check&_m=1",
            mibox: "http://t.hd.xiaomi.com/r/?_a=payment_check_box&_m=1"
        },
        c = {
            miphone: "支付小米手机",
            mibox: "支付小米盒子"
        };
        for (var d in this.mode) $.cookie(this.modeCookies[d]) && ($(a[d]).html('<a href="' + b[d] + '" class="btn_buy">' + c[d] + "</a>"), $(this.modeModals[d]).is(":visible") && $(this.modeModals[d]).find(".close").click())
    },
    localTime: function() {
        var a = parseInt( + new Date / 1e3, 10);
        return a
    },
    toProcess: function(a) {
        var b = this,
        c = b.timer,
        d = b.timeCount || 10,
        e = $(b.modeModals[a]).find(".re_countdown"),
        f = $(b.modeModals[a]).find(".modal_btn"),
        g = function() {
            0 == d ? (f.html('<a href="javascript:;" class="into_buy_btn">进入活动</a>'), b.toBuyGo(b.config.mode), c && window.clearTimeout(c), c = null) : (e.html("(" + d + ")"), d--, b.timer = window.setTimeout(function() {
                g()
            },
            1e3))
        };
        c && window.clearTimeout(c),
        g()
    },
    goBuy: function() {
        var a = this,
        b = {
            miphone: "购买小米手机",
            mibox: "购买小米盒子"
        },
        c = {
            miphone: "#buy_btn_1",
            mibox: "#buy_btn_2"
        };
        for (var d in a.mode) $.cookie(a.modeCookies[d]) || $(c[d]).html('<a href="javascript:;" class="btn_buy btn_buy_go" data-type="' + d + '">' + b[d] + "</a>");
        $("#open_button").find(".btn_buy_go").on("click",
        function() {
            a.mode[$(this).attr("data-type")] = !0;
            var b = a.config.mode = $(this).attr("data-type");
            if ($.cookie("userId")) a.getData(!0,
            function() {
                $(a.modeModals[b]).css("display", "block"),
                a.toProcess(b)
            });
            else {
                if (WebEventTrigger("login", null)) return;
                window.location.href = "http://m.xiaomi.com/mshopapi/index.php/v1/authorize/sso?client_id=180100031013&callback=" + encodeURIComponent(window.location.href)
            }
        })
    },
    toBuyGo: function(a) {
        var b = this;
        window.clearTimeout(b.timer),
        b.timer = null,
        $(b.modeModals[a]).find(".modal_btn").find(".into_buy_btn").on("click",
        function(c) {
            c.preventDefault(),
            b.getData(!0,
            function() {
                if ($(b.modeModals[a]).css("display", "block"), b.config.allow) if (b.config[a].hdurl) {
                    var c = "http://t.hd.xiaomi.com/s/" + b.config[a].hdurl + "&_m=1";
                    window.setTimeout(function() {
                        window.location.href = c
                    },
                    500)
                } else {
                    var d = "http://p.www.xiaomi.com/m/activities/open/" + b.config.hdurl[a];
                    window.setTimeout(function() {
                        window.location.href = d
                    },
                    500)
                } else if (b.config[a].hdstart === !1 && b.config[a].hdstop === !0) {
                    var d = "http://p.www.xiaomi.com/m/activities/open/" + b.config.hdurl[a];
                    window.setTimeout(function() {
                        window.location.href = d
                    },
                    500)
                } else $(b.modeModals[a]).find(".modal_btn").html('<a href="javascript:;" class="re_enter">重新进入<span class="re_countdown"></span></a>'),
                b.toProcess(a)
            })
        })
    }
};
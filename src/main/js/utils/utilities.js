function trimToNull(value) {
	return (value && value.length > 0) ? value : null;
}

(function(a) {
    a.fn.scrollIntoView = function(f, j, c) {
        var b = a.extend({}, a.fn.scrollIntoView.defaults);
        if (a.type(f) == "object") {
            a.extend(b, f)
        } else {
            if (a.type(f) == "number") {
                a.extend(b, {
                    duration: f,
                    easing: j,
                    complete: c
                })
            } else {
                if (f == false) {
                    b.smooth = false
                }
            }
        }
        var h = Infinity, e = 0;
        if (this.size() == 1) {
            ((h = this.get(0).offsetTop) == null || (e = h + this.get(0).offsetHeight))
        } else {
            this.each(function(m, n) {
                (n.offsetTop < h ? h = n.offsetTop : n.offsetTop + n.offsetHeight > e ? e = n.offsetTop + n.offsetHeight : null)
            })
        }
        e -= h;
        var k = this.commonAncestor().get(0);
        var g = a(window).height();
        while (k) {
            var d = k.scrollTop, l = k.clientHeight;
            if (l > g) {
                l = g
            }
            if (l == 0 && k.tagName == "BODY") {
                l = g
            }
            if ((k.scrollTop != ((k.scrollTop += 1) == null || k.scrollTop) && (k.scrollTop -= 1) != null) || (k.scrollTop != ((k.scrollTop -= 1) == null || k.scrollTop) && (k.scrollTop += 1) != null)) {
                if (h <= d) {
                    i(k, h)
                } else {
                    if ((h + e) > (d + l)) {
                        i(k, h + e - l)
                    } else {
                        i(k, undefined)
                    }
                }
                return 
            }
            k = k.parentNode
        }
        function i(n, m) {
            if (m === undefined) {
                if (a.isFunction(b.complete)) {
                    b.complete.call(n)
                }
            } else {
                if (b.smooth) {
                    a(n).stop().animate({
                        scrollTop: m
                    }, b)
                } else {
                    n.scrollTop = m;
                    if (a.isFunction(b.complete)) {
                        b.complete.call(n)
                    }
                }
            }
        }
        return this
    };
    a.fn.scrollIntoView.defaults = {
        smooth: true,
        duration: null,
        easing: a.easing && a.easing.easeOutExpo ? "easeOutExpo": null,
        complete: a.noop(),
        step: null,
        specialEasing: {}
    };
    a.fn.isOutOfView = function(b) {
        var c = true;
        this.each(function() {
            var h = this.parentNode, d = h.scrollTop, g = h.clientHeight, f = this.offsetTop, e = this.offsetHeight;
            if (b ? (f) > (d + g) : (f + e) > (d + g)) {} else {
                if (b ? (f + e) < d : f < d) {} else {
                    c = false
                }
            }
        });
        return c
    };
    a.fn.commonAncestor = function() {
        var c = [];
        var f = Infinity;
        a(this).each(function() {
            var g = a(this).parents();
            c.push(g);
            f = Math.min(f, g.length)
        });
        for (var d = 0; d < c.length; d++) {
            c[d] = c[d].slice(c[d].length - f)
        }
        for (var d = 0; d < c[0].length; d++) {
            var e = true;
            for (var b in c) {
                if (c[b][d] != c[0][d]) {
                    e = false;
                    break
                }
            }
            if (e) {
                return a(c[0][d])
            }
        }
        return a([])
    }
})(jQuery);
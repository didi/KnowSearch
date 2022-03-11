function clamp(n, min, max) {
  if (n < min) return min;
  if (n > max) return max;
  return n;
}

function toBarPerc(n) {
  return (-1 + n) * 100;
}

let queue = (function () {
  let pending = [];

  function next() {
    let fn = pending.shift();
    if (fn) {
      fn(next);
    }
  }

  return function (fn) {
    pending.push(fn);
    if (pending.length == 1) next();
  };
})();

let css = (function () {
  let cssPrefixes = ["Webkit", "O", "Moz", "ms"],
    cssProps = {};

  function camelCase(string) {
    return string.replace(/^-ms-/, "ms-").replace(/-([\da-z])/gi, function (match, letter) {
      return letter.toUpperCase();
    });
  }

  function getVendorProp(name) {
    let style = document.body.style;
    if (name in style) return name;

    let i = cssPrefixes.length,
      capName = name.charAt(0).toUpperCase() + name.slice(1),
      vendorName;
    while (i--) {
      vendorName = cssPrefixes[i] + capName;
      if (vendorName in style) return vendorName;
    }

    return name
  }

  function getStyleProp(name) {
    name = camelCase(name);
    return cssProps[name] || (cssProps[name] = getVendorProp(name));
  }

  function applyCss(element, prop, value) {
    prop = getStyleProp(prop);
    element.style[prop] = value;
  }

  return function (element, properties) {
    let args = arguments,
      prop,
      value;

    if (args.length == 2) {
      for (prop in properties) {
        value = properties[prop];
        if (value !== undefined && properties.hasOwnProperty(prop)) applyCss(element, prop, value);
      }
    } else {
      applyCss(element, args[1], args[2]);
    }
  };
})();

function hasClass(element, name) {
  let list = typeof element == "string" ? element : classList(element);
  return list.indexOf(" " + name + " ") >= 0;
}

function addClass(element, name) {
  let oldList = classList(element),
    newList = oldList + name;

  if (hasClass(oldList, name)) return;

  // Trim the opening space.
  element.className = newList.substring(1);
}

function removeClass(element, name) {
  let oldList = classList(element),
    newList;

  if (!hasClass(element, name)) return;

  // Replace the class name.
  newList = oldList.replace(" " + name + " ", " ");

  // Trim the opening and closing spaces.
  element.className = newList.substring(1, newList.length - 1);
}

function classList(element) {
  return (" " + (element.className || "") + " ").replace(/\s+/gi, " ");
}

function removeElement(element) {
  element && element.parentNode && element.parentNode.removeChild(element);
}

class NProgress {
  constructor(settings) {
    this.status = null;
    this.settings = settings || {
      minimum: 0.08,
      easing: "ease",
      positionUsing: "",
      speed: 200,
      trickle: true,
      trickleRate: 0.02,
      trickleSpeed: 800,
      showSpinner: true,
      barSelector: '[role="bar"]',
      spinnerSelector: '[role="spinner"]',
      parent: "body",
      template:
        '<div class="bar" role="bar"><div class="peg"></div></div><div class="spinner" role="spinner"><div class="spinner-icon"></div></div>',
    };
  }

  configure(options) {
    let key, value;
    for (key in options) {
      value = options[key];
      if (value !== undefined && options.hasOwnProperty(key)) this.settings[key] = value;
    }

    return this;
  }
  set(n) {
    let started = this.isStarted();

    n = clamp(n, this.settings.minimum, 1);
    this.status = n === 1 ? null : n;

    let progress = this.render(!started),
      bar = progress.querySelector(this.settings.barSelector),
      speed = this.settings.speed,
      ease = this.settings.easing;
    progress.offsetWidth;
    let self = this;
    queue(function (next) {
      // this.set positionUsing if it hasn't already been this.set
      if (self.settings.positionUsing === "") self.settings.positionUsing = self.getPositioningCSS();

      // Add transition
      css(bar, self.barPositionCSS(n, speed, ease));

      if (n === 1) {
        // Fade out
        css(progress, {
          transition: "none",
          opacity: 1,
        });
        progress.offsetWidth; /* Repaint */

        setTimeout(function () {
          css(progress, {
            transition: "all " + speed + "ms linear",
            opacity: 0,
          });
          setTimeout(function () {
            self.remove();
            next();
          }, speed);
        }, speed);
      } else {
        setTimeout(next, speed);
      }
    });

    return this;
  }

  isStarted() {
    return typeof this.status === "number";
  }

  start() {
    let self = this;
    if (!self.status) self.set(0);

    let work = function () {
      setTimeout(function () {
        if (!self.status) return;
        self.trickle();
        work();
      }, self.settings.trickleSpeed);
    };

    if (self.settings.trickle) work();

    return self;
  }

  done(force) {
    if (!force && !this.status) return this;

    return this.inc(0.3 + 0.5 * Math.random()).set(1);
  }

  inc(amount) {
    let n = this.status;

    if (!n) {
      return this.start();
    } else {
      if (typeof amount !== "number") {
        amount = (1 - n) * clamp(Math.random() * n, 0.1, 0.95);
      }

      n = clamp(n + amount, 0, 0.994);
      return this.set(n);
    }
  }

  trickle() {
    return this.inc(Math.random() * this.settings.trickleRate);
  }

  render(fromStart) {
    if (this.isRendered()) return document.getElementById("nprogress");

    addClass(document.documentElement, "this-busy");

    let progress = document.createElement("div");
    progress.id = "nprogress";
    progress.innerHTML = this.settings.template;

    let bar = progress.querySelector(this.settings.barSelector),
      perc = fromStart ? "-100" : toBarPerc(this.status || 0),
      parent = document.querySelector(this.settings.parent),
      spinner;

    css(bar, {
      transition: "all 0 linear",
      transform: "translate3d(" + perc + "%,0,0)",
    });

    if (!this.settings.showSpinner) {
      spinner = progress.querySelector(this.settings.spinnerSelector);
      spinner && removeElement(spinner);
    }

    if (parent != document.body) {
      addClass(parent, "this-custom-parent");
    }

    parent.appendChild(progress);
    return progress;
  }

  remove() {
    removeClass(document.documentElement, "this-busy");
    removeClass(document.querySelector(this.settings.parent), "this-custom-parent");
    let progress = document.getElementById("nprogress");
    progress && removeElement(progress);
  }
  isRendered() {
    return !!document.getElementById("nprogress");
  }
  getPositioningCSS() {
    // Sniff on document.body.style
    let bodyStyle = document.body.style;

    // Sniff prefixes
    let vendorPrefix =
      "WebkitTransform" in bodyStyle
        ? "Webkit"
        : "MozTransform" in bodyStyle
        ? "Moz"
        : "msTransform" in bodyStyle
        ? "ms"
        : "OTransform" in bodyStyle
        ? "O"
        : "";

    if (vendorPrefix + "Perspective" in bodyStyle) {
      // Modern browsers with 3D support, e.g. Webkit, IE10
      return "translate3d";
    } else if (vendorPrefix + "Transform" in bodyStyle) {
      // Browsers without 3D support, e.g. IE9
      return "translate";
    } else {
      // Browsers without translate() support, e.g. IE7-8
      return "margin";
    }
  }
  barPositionCSS(n, speed, ease) {
    let barCSS;
    if (this.settings.positionUsing === "translate3d") {
      barCSS = { transform: "translate3d(" + toBarPerc(n) + "%,0,0)" };
    } else if (this.settings.positionUsing === "translate") {
      barCSS = { transform: "translate(" + toBarPerc(n) + "%,0)" };
    } else {
      barCSS = { "margin-left": toBarPerc(n) + "%" };
    }

    barCSS.transition = "all " + speed + "ms " + ease;

    return barCSS;
  }
}

export default NProgress;
 
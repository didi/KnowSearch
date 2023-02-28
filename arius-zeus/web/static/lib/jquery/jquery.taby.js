/*!
 * jQuery Taby - A Textarea Tabulator
 * ----------------------------------------------------------------
 *
 * jQuery Taby is a plugin to enable tabulation in textarea fields.
 *
 * Licensed under The MIT License
 *
 * @version        1.0.0
 * @since          2012.01.10
 * @author         Washington Botelho
 * @documentation  wbotelhos.com/taby
 *
 * ----------------------------------------------------------------
 *
 *  $('textarea').taby();
 *
 *  <textarea></textarea>
 *
 */

;(function($) {

  var methods = {
    init: function(settings) {
      return this.each(function() {
        this.opt = $.extend({}, $.fn.taby.defaults, settings);

        var that = $(this).off('.taby');

        methods._adjustTab.call(this);
        methods._bind.call(this);

        that.data({ 'settings': this.opt, 'taby': true });
      });
    }, _adjustTab: function() {
      this.opt['tab'] = '';

      for (var i = 0; i < this.opt.space; i++) {
        this.opt.tab += ' ';
      }
    }, _backspace: function() {
      var opt   = $(this).data('settings'),
          start = this.selectionStart;

      if (opt.backspace && this.value.slice(start - opt.tab.length, start) == opt.tab) {
        this.evt.preventDefault();

        this.value = this.value.slice(0, start - opt.tab.length) + this.value.slice(start);

        this.selectionStart = start - opt.tab.length;
        this.selectionEnd   = start - opt.tab.length;
      }
    }, _bind: function() {
      $(this).on(methods._eventName(), function(evt) {
        var key = evt.keyCode || evt.which;

        if (evt.metaKey) {
          return;
        }

        this.evt = evt;

        if (key == 9) {
          if (evt.shiftKey) {
            methods._shiftTab.call(this);
          } else {
            methods._tab.call(this);
          }
        } else if (key == 8) {
          methods._backspace.call(this);
        } else if (key == 46) {
          methods._del.call(this);
        } else if (key == 37) {
          methods._left.call(this);
        } else if (key == 39) {
          methods._right.call(this);
        }
      });
    }, _del: function() {
      var opt   = $(this).data('settings'),
          start = this.selectionStart,
          end   = this.selectionEnd;

      if (opt.del && this.value.slice(start, start + opt.tab.length) == opt.tab) {
        this.evt.preventDefault();

        this.value = this.value.slice(0, start) + this.value.slice(start + opt.tab.length);

        this.selectionStart = start;
        this.selectionEnd   = end;
      }
    }, _eventName: function() {
      return methods._isFirefox() ? 'keypress.taby' : 'keydown.taby'
    }, _isFirefox: function() {
      return /firefox/.test(navigator.userAgent.toLowerCase());
    }, _left: function() {
      var opt   = $(this).data('settings'),
          start = this.selectionStart,
          end   = this.selectionEnd;

      if (!this.evt.shiftKey) {
        start = end;
      }

      if (opt.left === true && this.value.slice(start - opt.tab.length, start) == opt.tab) {
        this.evt.preventDefault();

        var toStartTake = opt.tab.length,
            toEndTake   = opt.tab.length;

        if (this.evt.shiftKey) {
          toEndTake = 0;
        }

        this.selectionStart = start - toStartTake;
        this.selectionEnd   = end - toEndTake;
      }
    }, _right: function() {
      var opt    = $(this).data('settings'),
          start  = this.selectionStart,
          end    = this.selectionEnd;

      if (!this.evt.shiftKey) {
        end = start;
      }

      if (opt.right === true && this.value.slice(end, end + opt.tab.length) == opt.tab) {
        this.evt.preventDefault();

        this.selectionStart = start + (this.evt.shiftKey ? 0 : opt.tab.length);
        this.selectionEnd   = end + opt.tab.length;
      }
    }, _shiftTab: function() {
      this.evt.preventDefault();

      var opt               = $(this).data('settings'),
          start             = this.selectionStart,
          end               = this.selectionEnd;
          preselection      = this.value.slice(0, start),
          selection         = this.value.slice(start, end),
          postselection     = this.value.slice(end),
          isMultipleLine    = selection.indexOf("\n") >= 0,
          lineStart         = preselection.lastIndexOf("\n"),
          lineEnd           = end + postselection.indexOf("\n"),
          isFirst           = start == 0,
          previousCharacter = (isFirst) ? '' : this.value.slice(start - 1, start);

      if (lineStart < 0) {
        lineStart = 0;
      } else {
        lineStart++;
      }

      if (lineEnd < 0) {
        lineEnd = this.value.length;
      }

      if (isMultipleLine) {
        if (selection.lastIndexOf("\n") == selection.length - 1) {
          lineEnd       = end - 1;
          postselection = "\n" + postselection;
        }

        var line    = this.value.slice(lineStart, lineEnd),
            result  = line;

        result = result.replace(new RegExp('^' + opt.tab), '').replace(new RegExp("\n" + opt.tab, 'g'), "\n");

        if (line == result) {
          return;
        }

        this.value = this.value.slice(0, lineStart) + result + this.value.slice(lineEnd);

        var blankRemoved  = line.length - result.length,
            startTaked    = start,
            endTaked      = end - blankRemoved;

        if (!isFirst && previousCharacter != "\n") {
          startTaked -= opt.tab.length;
        }

        if (startTaked < lineStart) {
          startTaked = lineStart;
        }

        var toStartTake = (startTaked < 0) ? 0 : startTaked,
            toEndTake   = (endTaked < 0) ? 0 : endTaked;

        this.selectionStart = toStartTake;
        this.selectionEnd   = toEndTake;
      } else {
        var line    = this.value.slice(lineStart, lineEnd),
            result  = line;

        if (line.indexOf(opt.tab) == 0) {
          result = line.slice(opt.tab.length);
        }

        if (line == result) {
          return;
        }

        this.value = this.value.slice(0, lineStart) + result + this.value.slice(lineEnd);

        var takedStart = takedEnd = opt.tab.length;

        if (start - lineStart < opt.tab.length) {
          takedStart = start - lineStart;

          if (start == end) {
            takedEnd = takedStart;
          }
        }

        this.selectionStart = start - takedStart;
        this.selectionEnd   = end - takedEnd;
      }
    }, _tab: function() {
      this.evt.preventDefault();

      var opt               = $(this).data('settings'),
          start             = this.selectionStart,
          end               = this.selectionEnd,
          preselection      = this.value.slice(0, start),
          selection         = this.value.slice(start, end),
          postselection     = this.value.slice(end),
          lineStart         = preselection.lastIndexOf("\n"),
          lineEnd           = end + postselection.indexOf("\n"),
          isFirst           = start == 0,
          isLast            = end == this.value.length,
          previousCharacter = (isFirst) ? '' : this.value.slice(start - 1, start),
          nextCharacter     = (isLast) ? '' : this.value.slice(end, end + 1);

      if (lineStart < 0) {
        lineStart = 0;
      } else {
        lineStart++;
      }

      if (lineEnd < 0) {
        lineEnd = this.value.length;
      }

      if (selection.indexOf("\n") >= 0) {
          if (selection.lastIndexOf("\n") == selection.length - 1) {
            lineEnd       = end - 1;
            postselection = "\n" + postselection;
          }

          var line    = this.value.slice(lineStart, lineEnd),
              result  = line;

          result = opt.tab + result.replace(new RegExp("\n", 'g'), "\n" + opt.tab);

          if (line == result) {
            return;
          }

          this.value = this.value.slice(0, lineStart) + result + this.value.slice(lineEnd);

        var blankAdded  = result.length - line.length,
            startAdded  = start,
            endAdded    = end + blankAdded;

        if (!isFirst && previousCharacter != "\n") {
          startAdded += opt.tab.length;
        }

        this.selectionStart = startAdded;
        this.selectionEnd   = endAdded;
      } else if ((isFirst || previousCharacter == "\n") && (isLast || nextCharacter == "\n") && selection != '') {
        var line = this.value.slice(lineStart, lineEnd);

        this.value          = preselection + opt.tab + line + postselection;
        this.selectionStart = start;
        this.selectionEnd   = end + opt.tab.length;
      } else {
        this.value          = preselection + opt.tab + postselection;
        this.selectionStart = start + opt.tab.length;
        this.selectionEnd   = start + opt.tab.length;
      }
    }, goTo: function(position) {
      return this.each(function() {
        this.focus();
        this.selectionStart = position;
        this.selectionEnd   = position;
      });
    }, select: function(start, end) {
      return this.each(function() {
        this.focus();
        this.selectionStart = start;
        this.selectionEnd   = (end === undefined) ? this.value.length : end;
      });
    }, set: function(settings) {
      return this.each(function() {
        var that   = $(this),
            actual = that.data('settings'),
            news   = $.extend({}, actual, settings);

        that.taby(news);
      });
    }
  };

  $.fn.taby = function(method) {
    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist!');
    }
  };

  $.fn.taby.defaults = {
    backspace : true,
    del       : true,
    left      : true,
    right     : true,
    space     : 4
  };

})(jQuery);
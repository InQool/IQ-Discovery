'use strict';

let _classes = [];

export class Page {
    constructor($rootScope) {
        'ngInject';
        this.scope = $rootScope;
    }

    _setClassName() {
        this.scope.bodyClass = _classes.join(' ');
    }

    setClass(className) {
        _classes = className.split(' ');
        this._setClassName();
    }

    addClass(className) {
        _classes.push(className);
        _classes = _.uniq(_classes);
        this._setClassName();
    }

    removeClass(className) {
        _classes = _.remove(_classes, className);
        this._setClassName();
    }

    setPrint() {
        this.addClass('print');
    }

    unsetPrint() {
        this.removeClass('print');
    }

    triggerResize() {
        var timeout = setTimeout(() => {
            $(window).trigger('resize');
            clearTimeout(timeout);
        }, 300);
    }
}
'use strict';

let _title = undefined;

export class Page {
    setTitle(title) {
        _title = title;
    }

    getTitle() {
        return _title;
    }
}
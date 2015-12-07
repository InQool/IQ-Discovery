'use strict';
export class CSV {
    constructor(Restangular) {
        'ngInject';
        this.csv = Restangular.one('csv');
    }

    create(filename, data) {
        let blob = new Blob([data]);

        if (window.navigator.msSaveOrOpenBlob) {
            window.navigator.msSaveOrOpenBlob(blob, filename);
        } else if (window.navigator.msSaveBlob) {
            window.navigator.msSaveBlob(blob, filename);
        } else {
            let URL = window.URL || window.webkitURL, downloadUrl = URL.createObjectURL(blob);

            if (filename) {
                let a = document.createElement("a");
                // safari doesn't support this yet
                if (a.download === undefined) {
                    window.location = "data:application/csv;charset=utf-8," + data;
                } else {
                    a.href = downloadUrl;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();
                }
            } else {
                window.location = downloadUrl;
            }

            var timeout = setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
                clearTimeout(timeout);
            }, 100);
        }
    }
}
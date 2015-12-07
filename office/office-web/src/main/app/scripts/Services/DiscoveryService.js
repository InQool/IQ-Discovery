export class DiscoveryService {
    /**
     * @ngInject
     * @param Restangular
     */
    constructor(Restangular) {
        this.portal = Restangular.one('portal');
        this.settings = this.portal.one('settings');
        this.dataLoad = Restangular.all('dataLoad');
        this.progress = Restangular.one('var/kdrLoadProgress');
    }

    find() {
        return this.settings.get();
    }

    update(data) {
        return this.settings.doPUT(data);
    }

    runImport(type) {
        return this.dataLoad.one(type).post();
    }

    getImportProgress() {
        return this.progress.get();
    }

    uploadFile(type, file) {
        const formData = new FormData();
        formData.append('file', file);

        return this.portal.withHttpConfig({
            transformRequest: angular.identity,
            transformResponse: undefined
        }).customPOST(formData, type, undefined, {
            'Content-Type': undefined
        });
    }

    removeFile(type) {
        return this.portal.one(type).remove();
    }
}

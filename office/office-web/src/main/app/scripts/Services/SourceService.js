'use strict';
export class SourceService {
    /**
     * @ngInject
     * @param Restangular
     */
    constructor(Restangular) {
        let source = Restangular.all('source');
        this.oai = source.all('oai');
        this.sru = source.all('sru');
        this.z3950 = source.all('z3950');
    }

    findAllOAI() {
        return this.oai.getList();
    }

    createOAI(data) {
        return this.oai.post(data);
    }

    findAllSRU() {
        return this.sru.getList();
    }

    createSRU(data) {
        return this.sru.post(data);
    }

    findAllZ3950() {
        return this.z3950.getList();
    }

    createZ3950(data) {
        return this.z3950.post(data);
    }
}
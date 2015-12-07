'use strict';
export class SuggestService {
    /**
     * @ngInject
     * @param RestangularCachable
     */
    constructor(RestangularCachable) {
        let nk = RestangularCachable.all('nkLists');
        this.spatial = nk.all('geo/search');
        this.temporal = nk.all('chro/search');
        this.subject = nk.all('topic/search');
        this.type = nk.all('genre/search');
    }

    fetch(prop, query) {
        return this[prop].getList({name: query});
    }
}
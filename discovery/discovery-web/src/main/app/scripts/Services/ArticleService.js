'use strict';
export class ArticleService {
    constructor(Restangular) {
        'ngInject';
        this.article = Restangular.all('article');
    }

    findAll() {
        return this.article.getList();
    }

    find(id) {
        return this.article.one(id).get();
    }
}
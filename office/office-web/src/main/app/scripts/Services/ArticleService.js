export class ArticleService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.rest = Restangular;
		this.article = Restangular.service('article');
	}

	findAll() {
		return this.article.getList();
	}

	find(id) {
		return this.article.one(id).get();
	}

	create(article) {
		return this.article.post(article);
	}

	uploadImage(article, file) {
		let formData = new FormData();
		formData.append('file', file);

		return article.withHttpConfig({transformRequest: angular.identity}).customPOST(formData, 'image', undefined, {'Content-Type': undefined});
	}

	remove(ids) {
		return this.rest.all('article/delete').post(ids);
	}
}
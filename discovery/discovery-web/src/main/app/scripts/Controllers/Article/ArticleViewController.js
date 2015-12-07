export class ArticleViewController {
	/**
	 * @ngInject
	 * @param article
	 */
	constructor(article) {
		let vm = this;

		vm.article = article;
	}
}
export class ArticleController {
	/**
	 * @ngInject
	 * @param {ArticleService} ArticleService
	 */
	constructor(ArticleService) {
		let vm = this;

		load();

		function load() {
			ArticleService.findAll().then(success);

			function success(articles) {
				vm.articles = articles;
			}
		}
	}
}
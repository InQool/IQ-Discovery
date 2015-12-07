export class HistoryController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {User} User
	 * @param {SearchService} SearchService
	 * @param {Notifications} Notifications
	 */
	constructor($state, SearchService, User, Notifications) {
		let vm = this;

		vm.history = User.getLocalHistory();
		vm.isLoggedIn = User.isLoggedIn();

		vm.save = save;
		vm.clear = clear;
		vm.getReadableQuery = getReadableQuery;
		vm.getReadableFacets = getReadableFacets;

		function getReadableQuery(query) {
			return SearchService.getReadableQuery(query);
		}

		function getReadableFacets(facetQuery) {
			return SearchService.getReadableFacets(facetQuery);
		}

		function save(history) {
			User.saveSearch(history.query, history.facetQuery).then(success);

			function success() {
				Notifications.addSuccess(__('Hledání bylo uloženo.'));
			}
		}

		function clear() {
			User.removeLocalHistory();
			$state.reload();
		}
	}
}
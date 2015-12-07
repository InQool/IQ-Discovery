export class AccountSettingsSearchController {
	/**
	 * @ngInject
	 * @param {User} User
	 * @param {SearchService} SearchService
	 * @param {Notifications} Notifications
	 */
	constructor(User, SearchService, Notifications) {
		let vm = this;

		vm.remove = remove;
		vm.getReadableQuery = getReadableQuery;
		vm.getReadableFacets = getReadableFacets;

		load();

		function load() {
			User.getSavedSearch().then(results => {
				vm.results = results;
			});
		}

		function getReadableQuery(result) {
			return SearchService.getReadableQuery(result.solrQuery);
		}

		function getReadableFacets(result) {
			return SearchService.getReadableFacets(result.restrictions);
		}

		function remove(result) {
			result.remove();
			_.remove(vm.results, result);
			Notifications.addSuccess(__('Hledání bylo odstraněno.'));
		}
	}
}
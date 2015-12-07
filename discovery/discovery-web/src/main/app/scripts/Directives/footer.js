export function footer() {
	return {
		restrict: 'E',
		controller: controller,
		controllerAs: 'vm',
		templateUrl: 'views/directives/Footer/footer.html'
	};

	/**
	 * @ngInject
	 * @param Configuration
	 */
	function controller(Configuration) {
		let vm = this;

		vm.limit = 9;
		vm.footer = Configuration.footer;
		vm.isArray = isArray;

		function isArray(value) {
			return _.isArray(value);
		}
	}
}
export function flashes() {
	return {
		restrict: 'E',
		replace: true,
		template: '<div class="alert" ng-if="vm.getMessage()" ng-class="vm.getMessage().type" ng-bind="vm.getMessage().message"></div>',
		controller: controller,
		controllerAs: 'vm'
	};

	/**
	 * @ngInject
	 * @param {Notifications} Notifications
	 */
	function controller(Notifications) {
		let vm = this;

		vm.getMessage = getMessage;

		function getMessage() {
			return Notifications.getMessage();
		}
	}
}
export default function () {
	return {
		restrict: 'A',
		replace: true,
		controller: controller
	};

	/**
	 * @ngInject
	 * @param $scope
	 * @param SCREEN
	 */
	function controller($scope, SCREEN) {
		$scope.closed = angular.element(window) < SCREEN.SM_MAX;
		$scope.toggle = toggle;

		function toggle() {
			$scope.closed = !$scope.closed;
		}
	}
}
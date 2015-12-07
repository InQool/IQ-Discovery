export function documentType() {
	return {
		restrict: 'E',
		replace: true,
		scope: {
			document: '=',
			isPublished: '=',
			isPrint: '='
		},
		templateUrl: 'views/directives/documentType.html',
		controller: controller,
		controllerAs: 'vm'
	};

	/**
	 * @ngInject
	 * @param $scope
	 * @param {TypeService} TypeService
	 */
	function controller($scope, TypeService) {
		let vm = this;

		vm.document = $scope.document;
		vm.isPrint = $scope.isPrint || false;
		vm.isPublished = $scope.isPublished || false;

		vm.fetchSubtypes = fetchSubtypes;

		loadTypes();

		function fetchSubtypes(type, event) {
			type.get().then(success);
			if (angular.isDefined(event)) {
				vm.document.documentSubType = null;
			}

			function success(type) {
				vm.subtypes = type.subTypes;
				vm.getSubType = getSubType;
			}
		}

		function loadTypes() {
			TypeService.findAll().then(types => {
				vm.types = _.filter(types, type => type.subtypeCount > 0);
				vm.getType = getType;

				if ($scope.document.documentType) {
					let type = _.find(types, {id: parseInt(vm.document.documentType)});
					fetchSubtypes(type);
				}
			});
		}

		function getType() {
			return _.find(vm.types, {id: parseInt(vm.document.documentType)});
		}

		function getSubType() {
			return _.find(vm.subtypes, {id: parseInt(vm.document.documentSubType)});
		}
	}
}
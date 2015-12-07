export function carousel($timeout) {
	'ngInject';
	return {
		scope: true,
		restrict: 'C',
		link: (scope, el, attrs) => {
			el.carousel();

			scope.current = 1;

			attrs.$observe('count', () => {
				if (attrs.count) {
					scope.total = attrs.count;
				}
			});

			el.on('slid.bs.carousel', () => {
				scope.$broadcast('slid.bs.carousel');
				var timeout = $timeout(() => {
					scope.current = el.find('.active').index() + 1;
					$timeout.cancel(timeout);
				});
			});

			scope.next = () => {
				el.carousel('next');
			};

			scope.prev = () => {
				el.carousel('prev');
			};
		}
	};
}
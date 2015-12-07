const EXPO_HEIGHT = 297 - 30; // minus padding

export function exposition($timeout) {
	'ngInject';
	return {
		restrict: 'A',
		link: (scope, el) => {
			let overlay = el.find('.overlay'),
				title = overlay.find('.title');

			var timeout = $timeout(onTimeout);

			el.hover(() => {
				setTopProperty(0);
			}, () => {
				setTopProperty(calculateTop(title));
			});

			scope.$on('slid.bs.carousel', onCarouselSlide);

			function onTimeout() {
				onCarouselSlide();
				$timeout.cancel(timeout);
			}

			function onCarouselSlide() {
				setTopProperty(calculateTop(title));
			}

			function calculateTop(element) {
				let height = element.outerHeight();
				return height === 0 ? '100%' : overlay.height() - height;
			}

			function setTopProperty(value) {
				overlay.css('top', value);
			}
		}
	};
}
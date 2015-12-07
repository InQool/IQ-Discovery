require('openseadragon');

/**
 * @ngInject
 */
export function seadragon($timeout) {
	return {
		restrict: 'A',
		replace: true,
		scope: {
			tiles: '=',
			model: '='
		},
		link: (scope, element) => {
			var timeout = $timeout(() => {
				let content = element.parents('.content'), height = content.height() - content.find('> .controls').outerHeight();


				element.height(height);
				element.parent().height(height);

				$timeout.cancel(timeout);
			}, 0);

			let wrapper,
				pageChange,
				opts = {
					id: 'openseadragon-' + Math.random(),
					element: element[0],
					previousButton: 'viewer-btn-previous',
					homeButton: 'viewer-btn-home',
					nextButton: 'viewer-btn-next',
					zoomInButton: 'viewer-btn-zoomin',
					zoomOutButton: 'viewer-btn-zoomout',
					fullPageButton: 'viewer-btn-fullscreen',
					prefixUrl: 'components/openseadragon/built-openseadragon/openseadragon/images/',
					tileSources: _.map(scope.tiles, id => `/dcap/discovery/data/image/${id}/image.dzi`)
				};

			//Create the viewer
			scope.osd = OpenSeadragon(opts);

			element.find('.openseadragon-container').on('contextmenu', () => false);

			//Create a wrapper
			wrapper = {
				setPage: page => {
					scope.osd.goToPage(page);
					wrapper.currentPage = page;
				},
				mouse: {
					position: null,
					imageCoord: null,
					viewportCoord: null
				},
				currentPage: 0,
				zoom: 0,
				viewport: {
					bounds: null,
					center: null,
					rotation: 0,
					zoom: 0
				}
			};

			if (angular.isDefined(scope.model)) {
				scope.model = wrapper;

				pageChange = event => { // on page change
					timeout = $timeout(() => {
						wrapper.currentPage = event.page;
						$timeout.cancel(timeout);
					});
				};

				scope.osd.addHandler('page', pageChange);
			}

			scope.$on('$destroy', () => {
				if (scope.osd) {
					scope.osd.removeHandler('page', pageChange);
				}

				element.find('.openseadragon-container').off('contextmenu');

				scope.osd.destroy();
			});
		}
	};
}
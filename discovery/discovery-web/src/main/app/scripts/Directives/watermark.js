const PointerEvents = require('pointerEvents');

/**
 * @ngInject
 */
export function watermark($timeout) {
	return {
		restrict: 'A',
		scope: {},
		link: (scope, el, attrs) => {
			el.addClass('watermark');
			el[0].onload = onLoad;
			el[0].src = `/dcap/discovery/data/file/${attrs.watermark}`;

			function onLoad() {
				var timeout = $timeout(() => {
					let width = el.width(), height = el.height(), parent = el.parent();
					if (width > parent.width()) {
						width = parent.width();
					}

					if (height > parent.height()) {
						height = parent.height();
					}

					PointerEvents.initialize({
						selector: '.watermark'
					});

					el.css('margin-left', -(width / 2));
					el.css('margin-top', -(height / 2));
					el.addClass('show');

					$timeout.cancel(timeout);
				}, 0);
			}
		}
	};
}
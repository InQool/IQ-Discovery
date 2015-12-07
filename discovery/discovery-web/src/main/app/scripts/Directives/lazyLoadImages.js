require('jquery-lazyload');

/**
 * @ngInject
 * @param $timeout
 */
export function lazyLoadImages($timeout) {
	return {
		restrict: 'A',
		link: link
	};

	function link(scope, el) {
		var timeout = $timeout(() => {
			el.css('height', el.outerHeight());
			el.find('img').lazyload({
				effect: 'fadeIn',
				container: el
			});

			$timeout.cancel(timeout);
		});
	}
}
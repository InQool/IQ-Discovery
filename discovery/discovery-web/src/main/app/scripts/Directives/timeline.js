const STEP = 20, COLOR = '#4a89dc';

/**
 * @ngInject
 * @param $state
 * @param $timeout
 * @param {SearchService} SearchService
 */
export function timeline($state, $timeout, SearchService) {
	return {
		restrict: 'E',
		replace: true,
		scope: {},
		templateUrl: 'views/directives/Homepage/timeline.html',
		link: (scope, el) => {
			let graph = el.find('.graph'),
				plot,
				options = {
					grid: {
						show: true,
						color: '#bbbbbb',
						hoverable: true,
						axisMargin: 0,
						borderWidth: 0,
						borderColor: null,
						autoHighlight: true,
						mouseActiveRadius: 20
					},
					series: {
						bars: {
							show: true,
							fill: true,
							fillColor: COLOR,
							barWidth: 1
						}
					},
					xaxis: {
						show: true,
						tickDecimals: 0
					},
					yaxis: {
						show: false
					},
					colors: [COLOR],
					selection: {
						mode: 'x'
					}
				};

			generateDataAndCreatePlot();

			graph.on('plotselected', onPlotSelected);

			scope.yearStart = null;
			scope.yearEnd = null;

			scope.zoomIn = () => {
				zoom('in');
			};

			scope.zoomOut = () => {
				zoom('out');
			};

			scope.moveLeft = () => {
				move('left');
			};

			scope.moveRight = () => {
				move('right');
			};

			scope.search = search;

			function search() {
				$state.go('secured.search', {
					yearStart: scope.yearStart,
					yearEnd: scope.yearEnd
				});
			}

			function loadFacet() {
				return SearchService.findFacets();
			}

			function generateData() {
				return loadFacet().then(result => {
					_.remove(result.yearStart, {name: SearchService.StringUnknown});
					return _.map(result.yearStart, map);
				});

				function map(obj) {
					return [Number(obj.name), obj.count];
				}
			}

			function generateDataAndCreatePlot() {
				generateData().then(data => {
					createPlot(data, options);
				});
			}

			function createPlot(data, options) {
				plot = $.plot(graph, [data], options);
			}

			function onPlotSelected(event, ranges) {
				var timeout = $timeout(() => {
					scope.yearStart = parseInt(ranges.xaxis.from);
					scope.yearEnd = parseInt(ranges.xaxis.to);
					$timeout.cancel(timeout);
				});
			}

			function getAxisRange() {
				let xaxis = plot.getAxes().xaxis;
				return [xaxis.min, xaxis.max];
			}

			function createPlotWithRange(min, max) {
				generateData().then(data => {
					createPlot(data, _.merge(options, {
						xaxis: {
							min: min,
							max: max
						}
					}));
				});
			}

			function getMinAndMaxYears() {
				let data = plot.getData()[0].data;
				return [_.first(data)[0], _.last(data)[0]];
			}

			function zoom(direction) {
				let [min, max] = getAxisRange(), [YEAR_MIN, YEAR_MAX] = getMinAndMaxYears();

				if (direction === 'in') {
					min = min + STEP;
					max = max - STEP;
				} else {
					min = min - STEP;
					max = max + STEP;
				}

				if (min < YEAR_MIN) { // min should never be less than YEAR_MIN, but just in case
					min = YEAR_MIN;
				}

				if (max > YEAR_MAX) {
					max = YEAR_MAX;
				}

				if (min > max) {
					return;
				}

				createPlotWithRange(min, max);
			}

			function move(direction) {
				let [min, max] = getAxisRange(), [YEAR_MIN, YEAR_MAX] = getMinAndMaxYears();

				if (direction === 'left' && min <= YEAR_MIN || direction === 'right' && max >= YEAR_MAX) {
					return;
				}

				if (direction === 'left') {
					min = min - STEP;
					max = max - STEP;
				} else {
					min = min + STEP;
					max = max + STEP;
				}

				createPlotWithRange(min, max);
			}
		}
	};
}
export class CatalogController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {SearchService} SearchService
	 * @param {DocumentService} DocumentService
	 */
	constructor($state, SearchService, DocumentService) {
		let vm = this;

		vm.search = search;
		vm.selectFacet = selectFacet;
		vm.getAlphabet = getAlphabet;
		vm.selectLetter = selectLetter;
		vm.getValuesForLetter = getValuesForLetter;
		vm.getFacetName = (name) => SearchService.FacetNames[name];

		loadFacets();

		function loadFacets() {
			SearchService.findFacets(true).then(facets => {
				delete facets.documentSubType;
				delete facets.datePublished;
				vm.facets = facets;
			});
		}

		function selectFacet(facet) {
			vm.facet = facet;
			vm.letter = null;
		}

		function transformValues(values, facet) {
			if (facet === 'language') {
				values = _.map(values, value => {
					value.translated = DocumentService.Languages[value.name];
					return value;
				});
			}

			return values;
		}

		function getAlphabet() {
			let values = vm.facets[vm.facet].slice(), result = {};
			values = transformValues(values, vm.facet);
			_.each(values, value => {
				if (value.hasOwnProperty('translated')) {
					result[value.translated.charAt(0).toUpperCase()] = true;
				} else {
					result[value.name.charAt(0).toUpperCase()] = true;
				}
			});

			return _.keys(result).sort();
		}

		function selectLetter(letter) {
			vm.letter = letter;
		}

		function getValuesForLetter() {
			let values = vm.facets[vm.facet];
			values = transformValues(values, vm.facet);
			return _.filter(values, filter);

			function filter(value) {
				let letter;
				if (value.hasOwnProperty('translated')) {
					letter = value.translated.charAt(0).toUpperCase();
				} else {
					letter = value.name.charAt(0).toUpperCase();
				}

				return letter === vm.letter;
			}
		}

		function search(value) {
			let params = {};
			if (vm.facet === 'yearStart') {
				params.yearStart = value;
			} else {
				params['fq[]'] = [SearchService.formatFacetQuery(vm.facet, value)];
			}

			$state.go('secured.search', params);
		}
	}
}
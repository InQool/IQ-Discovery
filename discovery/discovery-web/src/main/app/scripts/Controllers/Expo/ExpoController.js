export class ExpoController {
    /**
     * @ngInject
     * @param $state
     * @param $stateParams
     * @param expo
     * @param facetQuery
     * @param {DocumentService} DocumentService
     * @param {SearchService} SearchService
     * @Param {Translator} Translator
     */
    constructor($state, $stateParams, expo, facetQuery, DocumentService, SearchService, Translator) {
        let vm = this;

        vm.expo = expo;
        vm.external = [];
        vm.loading = false;
        vm.layout = $stateParams.layout || 'grid';
        vm.term = $stateParams.term || '';
        vm.page = $stateParams.page || SearchService.PageDefault;
        vm.orderBy = $stateParams.orderBy || 'score';
        vm.orderDir = $stateParams.orderDir || 'DESC';
        vm.maxCount = $stateParams.maxCount || SearchService.MaxCountDefault;
        vm.yearStart = $stateParams.yearStart;
        vm.yearEnd = $stateParams.yearEnd;
        vm.options = SearchService.Options;

        vm.isGridLayout = isGridLayout;
        vm.getStateData = getStateData;
        vm.isSelected = isSelected;
        vm.select = select;
        vm.setOrderBy = setOrderBy;
        vm.setOrderDir = setOrderDir;
        vm.setMaxCount = setMaxCount;

        vm.setLayout = setLayout;
        vm.setPage = setPage;
        vm.getFacetName = getFacetName;
        vm.getFacetValue = getFacetValue;
        vm.getFacetValues = getFacetValues;
        vm.submitYears = submitYears;
        vm.getValue = getValue;

        search();

        function reload(params = {}) {
            $state.go('.', params);
        }

        function isGridLayout() {
            return vm.layout === 'grid';
        }

        function setOrderBy(value) {
            reload({orderBy: value === 'score' ? null : value});
        }

        function setOrderDir(value) {
            reload({orderDir: value === 'DESC' ? null : value});
        }

        function setMaxCount(value) {
            value = Number(value);
            reload({maxCount: value === SearchService.MaxCountDefault ? null : value});
        }

        function setLayout(layout) {
            reload({layout: layout});
        }

        function setPage(value) {
            reload({page: value === 1 ? null : value});
        }

        function getFacetName(name) {
            return SearchService.FacetNames[name];
        }

        function getFacetValue(facet, value) {
            if (SearchService.FacetValues.hasOwnProperty(facet) && SearchService.FacetValues[facet].hasOwnProperty(value)) {
                return SearchService.FacetValues[facet][value];
            }

            if (value === 'unknown') {
                return Translator.translate(SearchService.StringUnknown);
            }

            if (facet === 'language') {
                if (DocumentService.Languages.hasOwnProperty(value)) {
                    return DocumentService.Languages[value];
                }
            }

            return value;
        }

        /**
         * @param {Object} facet
         * @param {String} facet.name
         * @param {Array} facet.hitMap
         */
        function getFacetValues(facet) {
            if (facet.name === 'datePublished') {
                let hitMap = {};
                angular.forEach(SearchService.FacetValues.datePublished, (value, key) => {
                    hitMap[key] = facet.hitMap[key];
                });

                return hitMap;
            }

            return facet.hitMap;
        }

        function select(facet, value) {
            if (vm.isSelected(facet, value)) { // clicked facet is selected, de-select
                removeFacetValue(facet, value);
            } else {
                if (isSelectedFacet(facet)) { // facet is not selected, but is from the same group
                    let index = _.indexOf(facetQuery, getFacetByName(facet));
                    if (index !== -1) {
                        facetQuery.splice(index, 1);
                    }
                }

                facetQuery.push(SearchService.formatFacetQuery(facet, value));
            }

            let params = {'fq[]': facetQuery};
            if (vm.page > 1) {
                params.page = null; // reset to page 1
            }

            reload(params);
        }

        function removeFacetValue(facet, value) {
            let index = _.indexOf(facetQuery, SearchService.formatFacetQuery(facet, value));
            if (index !== -1) {
                facetQuery.splice(index, 1);
            }
        }

        function isSelected(facet, value) {
            return _.indexOf(facetQuery, SearchService.formatFacetQuery(facet, value)) !== -1;
        }

        function isSelectedFacet(facet) {
            return angular.isDefined(getFacetByName(facet));
        }

        function getFacetByName(facet) {
            return _.find(facetQuery, entry => _.startsWith(entry, facet));
        }

        function getQuery() {
            return SearchService.getQuery(expo.documents, 'inventoryId', 'invId');
        }

        function getFacetQuery() {
            return facetQuery.concat(SearchService.formatFacetQuery('yearStart', [vm.yearStart, vm.yearEnd]));
        }

        function search() {
            vm.loading = true;

            SearchService.search(getQuery(), getFacetQuery(), {
                maxCount: vm.maxCount,
                orderBy: vm.orderBy,
                orderDir: vm.orderDir,
                start: (vm.page - 1) * vm.maxCount
            }).then(searchComplete);

            /**
             * @param {Object} response
             * @param {Array} response.searchResults
             * @param {Number} response.numResults
             * @param {Array} response.facets
             */
            function searchComplete(response) {
                vm.results = response.searchResults;
                vm.resultsCount = response.numResults;

                let facets = response.facets;
                if (isSelectedFacet('documentType') === false) {
                    _.remove(facets, {name: 'documentSubType'});
                }

                vm.facets = facets;
                vm.loading = false;
            }
        }

        function getStateData(result) {
            return {
                id: result.inventoryId,
                maxCount: vm.maxCount !== SearchService.MaxCountDefault ? vm.maxCount : null,
                numResults: vm.resultsCount,
                page: vm.page !== SearchService.PageDefault ? vm.page : null,
                'fq[]': facetQuery,
                yearStart: vm.yearStart,
                yearEnd: vm.yearEnd,
                expoId: expo.id
            };
        }

        function submitYears() {
            reload({
                'fq[]': facetQuery,
                yearStart: vm.yearStart,
                yearEnd: vm.yearEnd
            });
        }

        function getValue(result, property) {
            if (_.indexOf(result[property], 'unknown') !== -1) {
                return Translator.translate(SearchService.StringUnknown);
            }

            return result[property].join(', ');
        }
    }
}

import {stripHtml, escapeQuery} from '../Services/Utils.js';

export function headerSearch() {
    return {
        scope: {
            query: '@',
            image: '=',
            logo: '='
        },
        restrict: 'AE',
        replace: true,
        templateUrl: 'views/directives/Header/search.html',
        controller: controller,
        controllerAs: 'vm'
    };

    /**
     * @ngInject
     * @param $scope
     * @param $state
     * @param {DataService} DataService
     * @param {SearchService} SearchService
     * @param {Configuration} Configuration
     * @param {Page} Page
     */
    function controller($scope, $state, DataService, SearchService, Configuration, Page) {
        const TOKEN_SEARCH_EVERYWHERE = __('Prohledat vše');

        let vm = this;
        vm.subtitle = Configuration.subtitle;
        vm.query = $scope.query;
        vm.suggest = [];

        vm.searchQuery = searchQuery;
        vm.submit = submit;
        vm.selectType = selectType;
        vm.getTitle = getTitle;

        loadSettings();

        function loadSettings() {
            DataService.getPortalSettings().then(settings => {
                vm.settings = settings;
                vm.getBackground = getBackground;

                if ($scope.logo) {
                    vm.logo = $scope.logo;
                } else if (settings.logoId) {
                    vm.logo = settings.logoId;
                } else {
                    vm.logo = null;
                }
            });
        }

        function getBackground() {
            let image;
            if ($scope.image) {
                image = $scope.image;
            } else if (vm.settings.headerId) {
                image = vm.settings.headerId;
            }

            return image ? {'background-image': 'url(/dcap/discovery/data/file/' + image + ')'} : {};
        }

        function getTitle() {
            return Page.getTitle();
        }

        vm.onChange = _.debounce(() => {
            SearchService.findSuggestions(vm.query).then(suggest => {
                vm.suggest = _.uniq(suggest);
            });
        }, 500);

        SearchService.findFacets().then(facets => {
            let types = facets.documentType;

            types.unshift({name: TOKEN_SEARCH_EVERYWHERE, isDefault: true});
            vm.type = types[0];
            vm.types = types;
        });

        function selectType(type) {
            vm.type = type;
        }

        function searchQuery(query) {
            vm.query = stripHtml(query);
            vm.submit();
        }

        function submit() {
            let query = [], facetQuery = [];
            if (vm.query) {
                query.push(
                    escapeQuery(vm.query)
                );
            }

            if (vm.type.hasOwnProperty('isDefault') === false) {
                facetQuery.push(`documentType:"${vm.type.name}"`);
            }

            $state.go('secured.search', {term: vm.query, query: `${query.join(' AND ')}`, 'fq[]': facetQuery});
        }
    }
}
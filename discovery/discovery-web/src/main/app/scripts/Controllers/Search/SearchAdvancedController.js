import {escapeQuery} from '../../Services/Utils.js';

export class SearchAdvancedController {
    /**
     * @ngInject
     * @param $state
     * @param {SearchService} SearchService
     * @param {DocumentService} DocumentService
     */
    constructor($state, SearchService, DocumentService) {
        let vm = this;

        vm.language = [];
        vm.languages = [];

        vm.documentType = [];
        vm.documentTypes = [];

        vm.searchOptions = _.merge({
            '': __('Všechna pole')
        }, DocumentService.DCTerms);

        vm.groups = [
            createGroup()
        ];

        vm.termOptions = {
            '+': __('všechny termíny'),
            '': __('jakékoliv termíny'),
            '-': __('žádné termíny')
        };

        vm.matchOptions = {
            '+': __('všechny skupiny'),
            '': __('jakékoliv skupiny')
        };

        vm.yearStart = null;
        vm.yearEnd = null;

        vm.values = {
            match: ''
        };

        vm.submit = submit;
        vm.getLanguage = lang => DocumentService.Languages[lang];
        vm.addGroup = addGroup;
        vm.removeGroup = removeGroup;
        vm.addField = addField;

        findFacets();

        function createFields() {
            return [{key: '', value: ''}, {key: '', value: ''}, {key: '', value: ''}];
        }

        function createGroup() {
            return {
                match: '',
                fields: createFields()
            };
        }

        function getQuery() {
            let query = [];
            _.each(vm.groups, group => {
                let groupQuery = [];

                _.each(group.fields, field => {
                    field = createFieldQuery(field.value, field.key);
                    if (field) {
                        groupQuery.push(field);
                    }
                });


                if (groupQuery.length !== 0) {
                    groupQuery = group.match + groupQuery.join(' ' + group.match);
                    query.push(`(${groupQuery})`);
                }
            });

            if (query.length !== 0) {
                if (vm.groups.length === 1) {
                    query = '+' + query.join();
                } else {
                    query = vm.values.match + query.join(` ${vm.values.match}`);
                }
            } else {
                query = '';
            }

            return query;

            function createFieldQuery(value, key) {
                if (!value) {
                    return;
                }

                var tmp = [];
                if (key) {
                    tmp.push(key);
                }

                tmp.push(escapeQuery(value));
                return tmp.join(':');
            }
        }

        function getFacetQuery() {
            let query = [];
            if (vm.language.length > 0) {
                //query = query.concat(_.map(vm.language, l => SearchService.formatFacetQuery('language', l)));
                query.push(
                    SearchService.formatFacetQuery('language', vm.language)
                );
            }

            if (vm.documentType.length > 0) {
                //query = query.concat(_.map(vm.type, t => SearchService.formatFacetQuery('documentType', t)));
                query.push(
                    SearchService.formatFacetQuery('documentType', vm.documentType)
                );
            }

            return query;
        }

        function submit() {
            $state.go('secured.search', {
                query: getQuery(),
                'fq[]': getFacetQuery(),
                yearStart: vm.yearStart,
                yearEnd: vm.yearEnd
            });
        }

        function findFacets() {
            SearchService.findFacets().then(facets => {
                vm.languages = facets.language;
                vm.documentTypes = facets.documentType;
            });
        }

        function addGroup() {
            vm.groups.push(
                createGroup()
            );
        }

        function removeGroup(index) {
            if (vm.groups.length > 1) {
                vm.groups.splice(index, 1);
            }
        }

        function addField(index) {
            vm.groups[index].fields.push({key: ''});
        }
    }
}
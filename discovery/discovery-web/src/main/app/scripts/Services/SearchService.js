const BASIC_FACETS = [
    'documentType',
    'documentSubType',
    'language',
    'yearStart'
];

const FACET_NAMES = {
    'documentType': __('Typ dokumentu'),
    'documentSubType': __('Podtyp dokumentu'),
    'creatorStr': __('Autor'),
    'language': __('Jazyk'),
    'organization': __('Organizace'),
    'datePublished': __('Novinky'),
    'spatial': __('Místo'),
    'subject': __('Téma'),
    'temporal': __('Období'),
    'type': __('Žánr'),
    'yearStart': __('Rok vzniku')
};

const FACET_VALUES = {
    datePublished: {
        '[NOW-7DAY TO NOW]': __('Posledních 7 dní'),
        '[NOW-1MONTH TO NOW]': __('Poslední měsíc'),
        '[NOW-6MONTH TO NOW]': __('Posledních 6 měsíců'),
        '[NOW-1YEAR TO NOW]': __('Poslední rok')
    }
};

const STRING_UNKNOWN = __('Neznámo');

const MAX_COUNT_DEFAULT = 20;
const PAGE_DEFAULT = 1;
const FACET_SEPARATOR = '|';

export class SearchService {
    /**
     * @ngInject
     * @param RestangularCachable
     * @param {DocumentService} DocumentService
     * @param {Translator} Translator
     * @param {User} User
     */
    constructor(RestangularCachable, DocumentService, Translator, User) {
        this.user = User;
        this.translator = Translator;
        this.document = DocumentService;

        this.rest = RestangularCachable;
        this.suggest = RestangularCachable.all('suggest');
        this.oai = RestangularCachable.all('search/external/oai');
        this.z3950 = RestangularCachable.all('search/external/z3950');
    }

    get FacetNames() {
        return FACET_NAMES;
    }

    get FacetValues() {
        return FACET_VALUES;
    }

    get MaxCountDefault() {
        return MAX_COUNT_DEFAULT;
    }

    get PageDefault() {
        return PAGE_DEFAULT;
    }

    get StringUnknown() {
        return STRING_UNKNOWN;
    }

    get Options() {
        return {
            orderBy: {
                score: __('Relevance'),
                datePublished: __('Naposledy přidáno')
            },
            orderDir: {
                ASC: __('vzest.'),
                DESC: __('sestup.')
            },
            maxCount: {
                20: 20,
                50: 50,
                100: 100
            }
        };
    }

    search(query, facetsQuery, extraQueries = {}) {
        extraQueries = _.merge(extraQueries, {query: query, fq: facetsQuery});
        return this.rest.one('search').get(extraQueries);
    }

    findSuggestions(query) {
        return this.suggest.getList({query: query});
    }

    findFacets(all = false, facets = []) {
        return this.search(null, null, {
            facf: all ? [] : (facets.length > 0 ? facets : BASIC_FACETS),
            maxCount: 0
        }).then(searchComplete);

        /**
         * @param {Object} response
         * @param {{hitMap: Array}[]} response.facets
         */
        function searchComplete(response) {
            let result = {};
            for (let facet of response.facets) {
                result[facet.name] = _.map(facet.hitMap, mapFacet);
            }

            return result;

            function mapFacet(count, name) {
                return {
                    name,
                    count
                };
            }
        }
    }

    searchChildren(query, parent) {
        return this.rest.all('search/detail').getList({
            query,
            parentId: parent,
            maxCount: 1
        });
    }

    getTree(parent) {
        return this.rest.all('search/tree').getList({parentId: parent});
    }

    findZ3950Sources() {
        return this.z3950.getList();
    }

    findOaiSources() {
        return this.oai.getList();
    }

    searchOaiSources(query) {
        return this.oai.all('all').getList({query});
    }

    /**
     * @param {Array} documents
     * @param {String} property
     * @param {String} field
     * @return {String}
     */
    getQuery(documents, property, field) {
        let fields = documents.map(d => `${property}:\\"${d[field]}\\"`);
        return fields.join(' OR ');
    }

    formatFacetQuery(facet, value) {
        if (facet === 'yearStart') {
            if (angular.isUndefined(value[0]) && angular.isUndefined(value[1])) {
                return '';
            }

            let query;

            if (angular.isUndefined(value[1])) { // only FROM year filled
                query = [
                    `yearStart:[${value[0]} TO *]`,
                    `(yearStart:[* TO ${value[0]}] NOT yearEnd:[* TO ${value[0]}])`
                ];
            } else if (angular.isUndefined(value[0])) { // only TO year filled
                query = [
                    `yearStart:[* TO ${value[1]}]`
                ];
            } else {
                query = [
                    `yearStart:[${value[0]} TO ${value[1]}]`,
                    `yearEnd:[${value[0]} TO ${value[1]}]`,
                    `(yearStart:[* TO ${value[0]}] NOT yearEnd:[* TO ${value[1]}])`
                ];
            }

            return query.join(' OR ');
        }

        if (facet === 'datePublished') {
            return `${facet}:${value}`;
        }

        if (value === STRING_UNKNOWN) {
            return `-${facet}:["" TO *]`;
        }

        return `${facet}:"${value}"`;
    }

    getReadableQuery(query) {
        if (query) {
            return query.replace(/(\w+):/g, replacement => {
                replacement = replacement.substring(0, replacement.length - 1);
                if (this.document.DCTerms.hasOwnProperty(replacement)) {
                    return this.translator.translate(this.document.DCTerms[replacement]) + ':';
                }

                return replacement;
            });
        } else {
            return this.translator.translate(__('(prázdný dotaz)'));
        }
    }

    getReadableFacets(facets) {
        if (angular.isString(facets)) {
            facets = facets.split(FACET_SEPARATOR);
        }

        facets = facets.join(', ').replace(/(\w+)/g, replacement => {
            if (FACET_NAMES.hasOwnProperty(replacement)) {
                return FACET_NAMES[replacement];
            }

            return replacement;
        });

        facets = facets.replace(/:\["" TO \*]+/g, () => {
            return ':' + this.translator.translate(STRING_UNKNOWN);
        });

        return facets;
    }
}

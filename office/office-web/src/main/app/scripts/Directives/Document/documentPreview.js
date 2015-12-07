/**
 * @param $modal
 * @param {DocumentService} DocumentService
 */
export function documentPreview($modal, DocumentService) {
    'ngInject';

    return {
        restrict: 'A',
        scope: {
            document: '=',
            documentId: '@'
        },
        link: (scope, el) => {
            el.on('click', () => {
                if (_.isUndefined(scope.document)) {
                    DocumentService.find(scope.documentId).then(doc => {
                        openModal(doc.concept);
                    });
                } else {
                    openModal(scope.document);
                }
            });
        }
    };

    /**
     * @param {Object} doc
     * @param {Array} doc.isPartOf
     */
    function openModal(doc) {
        $modal.open({
            size: 'lg',
            windowClass: 'modal-preview',
            templateUrl: 'views/controllers/Document/preview.html',
            controller: controller
        });

        /**
         *
         * @param $scope
         * @param {TypeService} TypeService
         * @param {Configuration} Configuration
         * @param {User} User
         */
        function controller($scope, TypeService, Configuration, User) {
            const clone = _.clone(doc);
            if (_.isArray(clone.zdoType)) {
                clone.zdoType = clone.zdoType[0];
            }

            $scope.token = User.getToken();
            $scope.document = clone;
            $scope.getChoices = getChoices;
            $scope.getValue = getValue;
            $scope.allowDocumentTypeChange = Configuration.document.allowDocumentTypeChange;

            fetchTypes();

            if (clone.zdoType === 'issue') {
                DocumentService.find(DocumentService.getIdFromUrl(clone.isPartOf[0])).then(volume => {
                    $scope.volume = volume.concept;
                    DocumentService.find(DocumentService.getIdFromUrl(volume.concept.isPartOf[0])).then(periodical => {
                        $scope.periodical = periodical.concept;
                    });
                });
            }

            function fetchTypes() {
                TypeService.findAll().then(types => {
                    $scope.getType = _.memoize(getType);

                    function getType(type) {
                        type = _.find(types, {
                            id: Number(type)
                        });

                        if (Configuration.document.allowDocumentTypeChange) {
                            TypeService.find(type.id).then(fetchedType => {
                                $scope.getSubType = _.memoize(getSubType);

                                function getSubType(subtype) {
                                    return _.find(fetchedType.subTypes, {
                                        id: Number(subtype)
                                    }).name;
                                }
                            });
                        }

                        return type.name;
                    }
                });
            }

            function getChoices() {
                return DocumentService.DCTerms;
            }

            function getValue(obj, key) {
                let values = obj[key];
                if (values.length === 0 || values[0] === '') {
                    values = ['Neznámo'];
                }

                if (key === 'language') {
                    values = _.map(values, value => DocumentService.Languages[value]);
                }

                return values;
            }
        }
    }
}

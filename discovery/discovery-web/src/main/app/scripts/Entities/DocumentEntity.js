export class DocumentEntity {
    /**
     * @ngInject
     */
    constructor(document) {
        this.document = document;
    }

    /**
     * @param {String} property
     * @return {Boolean}
     */
    hasOwnProperty(property) {
        return this.document.hasOwnProperty(property);
    }

    getProperty(property) {
        return this.document[property];
    }

    get customAttributes() {
        let result = {};
        angular.forEach(this.document.plain(), (values, key) => {
            if (_.startsWith(key, 'customField_')) {
                key = key.replace(/customField_(.*)/, '$1');
                result[key] = values;
            }
        });

        return result;
    }

    /**
     * @return {String}
     */
    get id() {
        return this.document.id;
    }

    /**
     * @return {String}
     */
    get title() {
        return this.document.title[0];
    }

    get alternative() {
        return this.document.alternative;
    }

    /**
     * @return {String}
     */
    get inventoryId() {
        return this.getProperty('inventoryId');
    }

    /**
     * @return {String}
     */
    get imgThumb() {
        return this.getProperty('imgThumb');
    }

    /**
     * @return {String}
     */
    get zdoType() {
        return this.getProperty('zdoType');
    }

    /**
     * @return {Array}
     */
    get imageIds() {
        return this.getProperty('imageIds');
    }

    /**
     * @return {Array}
     */
    get thumbIds() {
        return this.getProperty('thumbIds');
    }

    /**
     * @return {String}
     */
    get pdfId() {
        return this.getProperty('pdfId');
    }

    /**
     * @return {String}
     */
    get epubId() {
        return this.getProperty('epubId');
    }

    /**
     * @return {String}
     */
    get orgIdmId() {
        return this.getProperty('orgIdmId');
    }

    /**
     * @return {String}
     */
    get videoId() {
        return this.getProperty('videoId');
    }

    /**
     * @return {Boolean}
     */
    get allowContentPublicly() {
        return this.getProperty('allowContentPublicly');
    }

    /**
     * @return {Boolean}
     */
    get watermark() {
        return this.getProperty('watermark');
    }
}

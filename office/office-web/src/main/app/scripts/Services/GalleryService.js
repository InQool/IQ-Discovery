export class GalleryService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.gallery = Restangular.service('expo');
	}

	findAll() {
		return this.gallery.getList();
	}

	find(id) {
		return this.gallery.one(id).get();
	}

	create(data) {
		return this.gallery.post(data);
	}

	uploadImage(gallery, file) {
		let formData = new FormData();
		formData.append('file', file);

		return gallery.withHttpConfig({transformRequest: angular.identity}).customPOST(formData, 'image', undefined, {'Content-Type': undefined});
	}
}
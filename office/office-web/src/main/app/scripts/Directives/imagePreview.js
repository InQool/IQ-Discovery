export function imagePreview() {
	return {
		restrict: 'E',
		replace: true,
		scope: {
			image: '='
		},
		template: '<img class="img-responsive">',
		link: (scope, el) => {
			scope.$watch('image', () => {
				if (scope.image instanceof File) {
					let fileReader = new FileReader();
					fileReader.readAsDataURL(scope.image);
					fileReader.onload = () => {
						el.prop('src', fileReader.result);
					};
				}
			});
		}
	};
}
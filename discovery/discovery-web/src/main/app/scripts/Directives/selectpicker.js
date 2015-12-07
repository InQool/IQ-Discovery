export function selectpicker() {
    return {
        restrict: 'AE',
        scope: {
            label: '@',
            items: '=',
            onChange: '&'
        },
        replace: true,
        require: 'ngModel',
        templateUrl: 'views/directives/Selectpicker/select.html',
        link: (scope, el, attrs, ngModel) => {
            scope.getValue = () => scope.items[ngModel.$viewValue];
            scope.setValue = setValue;

            function setValue(value) {
                scope.onChange({value: value});
                ngModel.$setViewValue(value);
            }
        }
    };
}
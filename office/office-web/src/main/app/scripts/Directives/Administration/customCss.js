const DEFAULT_CSS = `
body {
    /* color: #000; */ /* barva textu */
}

a {
    /* color: #00a3b5; */ /* barva odkazu */
}

#search .header h1 {
    /* color: #fff; */ /* barva nadpisu */
}

.btn-primary { /* tlacitka na strance, napr. pro stazeni PDF
    /* color: #fff */ /* barva textu */
    /* background-color: #00a3b5; */ /* barva pozadi */
    /* border-color: #00a3b5; */ /* barva okraje tlacitka */
}

.btn-primary:hover { /* barvy pri najeti kurzorem na tlacitko */
    /* color: #fff; */ /* barva textu */
    /* background-color: #007582; */ /* barva pozadi */
    /* border-color: #006c78; */ /* barva pozadi */
}`;

export function customCss() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            css: '='
        },
        templateUrl: 'views/forms/Administration/customCss.html',
        controller: controller
    };

	/**
	 * @ngInject
	 */
    function controller($scope, $window) {
        $scope.reset = reset;

        function reset() {
            if ($window.confirm('Opravdu chcete vrátit CSS kód na původní hodnotu?')) {
                resetCss();
            }
        }

        function resetCss() {
            $scope.css = DEFAULT_CSS;
            $scope.$broadcast('reset');
        }
    }
}

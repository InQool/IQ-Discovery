'use strict';
export class NotificationEditController {
    /**
     * @ngInject
     * @param $scope
     * @param notification
     * @param {Notifications} Notifications
     */
    constructor($scope, notification, Notifications) {
        $scope.notification = notification;
        $scope.submit = () => {
            $scope.loading = true;
            $scope.notification.save().then(() => {
                Notifications.changesSaved();
                $scope.reloadTable();
                $scope.$dismiss();
            });
        };
    }
}
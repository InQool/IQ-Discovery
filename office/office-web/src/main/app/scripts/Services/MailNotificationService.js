'use strict';
export class MailNotificationService {
    /**
     * @ngInject
     * @param Restangular
     */
    constructor(Restangular) {
        this.notifications = Restangular.all('mailNotification');
    }

    findAll() {
        return this.notifications.getList();
    }

    find(id) {
        return this.notifications.get(id);
    }

    create(data) {
        return this.notifications.post(data);
    }
}
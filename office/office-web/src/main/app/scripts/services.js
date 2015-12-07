// datatables
require('datatables');
require('drmonty-datatables-tabletools/js/dataTables.tableTools');
require('datatables-bootstrap3-plugin');
require('angular-datatables');
require('angular-datatables-bootstrap');
require('angular-datatables-tabletools');

import {CSV} from './Services/CSV'
import {Page} from './Services/Page.js'
import {User} from './Services/User.js'
import {Modal} from './Services/Modal.js'
import {Button} from './Services/Button.js'
import {DataTable} from './Services/DataTable.js'
import {Analytics} from './Services/Analytics.js'
import {Notifications} from './Services/Notifications.js'
import {Statistics} from './Services/Statistics.js'

import {PeopleService} from './Services/PeopleService.js'
import {OrganizationService} from './Services/OrganizationService.js'
import {BatchService} from './Services/BatchService.js'
import {DocumentService} from './Services/DocumentService.js'
import {TypeService} from './Services/TypeService.js'
import {SourceService} from './Services/SourceService.js'
import {ArticleService} from './Services/ArticleService.js'
import {MailNotificationService} from './Services/MailNotificationService.js'
import {GalleryService} from './Services/GalleryService.js'
import {DiscoveryService} from './Services/DiscoveryService.js'
import {SuggestService} from './Services/SuggestService.js'
import MuseumService from './Services/MuseumService'

angular.module('zdo.office.services',
    [
        'datatables',
        'datatables.bootstrap',
        'datatables.tabletools'
    ]).service('RestangularCachable', Restangular => Restangular.withConfig(RestangularConfigurer => {
        RestangularConfigurer.setDefaultHttpFields({cache: true});
    }))
    .service('CSV', CSV)
    .service('Page', Page)
    .service('User', User)
    .service('Modal', Modal)
    .service('DataTable', DataTable)
    .service('Analytics', Analytics)
    .service('Statistics', Statistics)
    .service('Notifications', Notifications)
    .service('PeopleService', PeopleService)
    .service('OrganizationService', OrganizationService)
    .service('BatchService', BatchService)
    .service('DocumentService', DocumentService)
    .service('TypeService', TypeService)
    .service('SourceService', SourceService)
    .service('ArticleService', ArticleService)
    .service('MailNotificationService', MailNotificationService)
    .service('GalleryService', GalleryService)
    .service('DiscoveryService', DiscoveryService)
    .service('SuggestService', SuggestService)
    .service('MuseumService', MuseumService)
    .service('Button', Button)
    ;

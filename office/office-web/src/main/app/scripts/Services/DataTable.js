import {Utils} from './Utils.js';

const _instances = {};

export class DataTable {
    /**
     * @ngInject
     * @param $compile
     * @param $filter
     * @param DTOptionsBuilder
     * @param DTColumnBuilder
     * @param {Button} Button
     * @param {DocumentService} DocumentService
     * @param {CSV} CSV
     */
    constructor($compile, $filter, DTOptionsBuilder, DTColumnBuilder, Button, DocumentService, CSV) {
        this.compile = $compile;
        this.filter = $filter;
        this.optionsBuilder = DTOptionsBuilder;
        this.columnBuilder = DTColumnBuilder;
        this.documentService = DocumentService;
        this.csv = CSV;
        this._button = Button;
    }

    /**
     * @returns {Button}
     */
    get Button() {
        return this._button;
    }

    createOptions(scope) {
        const options = this.optionsBuilder.newOptions();
        options.withTableToolsButtons([
            {
                'sExtends': 'select_all',
                'sButtonText': 'Vybrat vše',
                'aButtons': ['select_all']
            },
            {
                'sExtends': 'select_none',
                'sButtonText': 'Zrušit výběr',
                'aButtons': ['select_none']
            },
            {
                sExtends: 'text',
                sButtonText: 'Export do CSV',
                fnClick: () => {
                    let csv = "\uFEFF", columnDefs = scope.hasOwnProperty('vm') ? scope.vm.columns : scope.columns, columns = [];
                    angular.forEach(columnDefs, column => {
                        if (column.mData !== null) {
                            columns.push(Utils.doubleQuote(column.sTitle));
                        }
                    });

                    let table = scope.getTable().DataTable;
                    csv = csv.concat(columns.join(';') + '\n');

                    angular.forEach(table.rows()[0], row => {
                        let rowData = [];
                        angular.forEach(table.cells()[0], cell => {
                            if (cell.row === row) {
                                let columnDef = columnDefs[cell.column];
                                if (columnDef.mData) {
                                    let data = table.cell(cell).render('display');
                                    data = Utils.doubleQuote(data);
                                    rowData.push(data);
                                }
                            }
                        });

                        csv = csv.concat(rowData.join(';') + '\n');
                    });

                    this.csv.create('export.csv', csv);
                }
            }
        ])
            .withTableToolsOption('sRowSelect', 'multi')
            .withTableToolsOption('fnPreRowSelect', function (event, nodes) {
                if (event) {
                    let target = event.target || event.srcElement, name = target.nodeName;
                    if (name === 'INPUT') {
                        return true;
                    }

                    if (name === 'TD') {
                        angular.element(nodes).first().find('.btn.btn-default:first').trigger('click');
                        return false;
                    }

                    if (name === 'A' || name === 'I' || name === 'BUTTON') {
                        return false;
                    }
                }

                return true;
            })
            .withTableToolsOption('fnRowSelected', nodes => {
                angular.element(nodes).find('input:checkbox').prop('checked', true);
            })
            .withTableToolsOption('fnRowDeselected', nodes => {
                angular.element(nodes).find('input:checkbox').prop('checked', false);
            })
            .withOption('createdRow', row => {
                this.compile(angular.element(row).contents())(scope);
            })
            .withOption('dom', '<"pull-left"Tf>t<"dt-panelfooter clearfix"ip>')
            .withOption('lengthChange', false)
            .withOption('autoWidth', false)
            .withOption('pageLength', 25)
            .withOption('processing', true)
            .withOption('deferRender', true)
            .withOption('order', [1, 'asc']);

        options.withBootstrap();
        options.oClasses.sFilterInput = 'form-control';

        options.language = {
            'sEmptyTable': 'Tabulka neobsahuje žádná data',
            'sInfo': 'Zobrazuji _START_ až _END_ z celkem _TOTAL_ záznamů',
            'sInfoEmpty': 'Zobrazuji 0 až 0 z 0 záznamů',
            'sInfoFiltered': '(filtrováno z celkem _MAX_ záznamů)',
            'sInfoPostFix': '',
            'sInfoThousands': ' ',
            'sLengthMenu': 'Zobraz záznamů _MENU_',
            'sLoadingRecords': 'Načítám...',
            'sProcessing': 'Provádím...',
            'sSearch': 'Hledat:',
            'sZeroRecords': 'Žádné záznamy nebyly nalezeny',
            'oPaginate': {
                'sFirst': 'První',
                'sLast': 'Poslední',
                'sNext': 'Další',
                'sPrevious': 'Předchozí'
            },
            'oAria': {
                'sSortAscending': ': aktivujte pro řazení sloupce vzestupně',
                'sSortDescending': ': aktivujte pro řazení sloupce sestupně'
            }
        };

        return options;
    }

    fromPromise(scope, promise) {
        const options = this.createOptions(scope);
        return _.merge(options, this.optionsBuilder.fromFnPromise(promise));
    }

    createDocumentTypeColumn(property, title) {
        return this.createColumn(property, title).renderWith(type => this.documentService.Types[type]);
    }

    createDocumentStateColumn(property, title) {
        return this.createColumn(property, title).renderWith(state => this.documentService.getStates()[state]);
    }

    createColumn(property, title, searchable = false) {
        let column = this.columnBuilder.newColumn(property);
        if (title) {
            column.withTitle(title);
        }

        column.searchable = searchable;

        return column;
    }

    createCheckbox() {
        return this.createColumn(null).notSortable().withOption('width', '1px').renderWith(() => '<input type="checkbox">');
    }

    createDateColumn(property, title) {
        let column = this.createColumn(property, title).renderWith(render.bind(this));

        column.isDate = true;
        column.width = '150px';

        return column;

        function render(value, type) {
            if (type === 'display') {
                return this.filter('date')(value * 1000, 'medium');
            }

            return value;
        }
    }

    createBoolColumn(property, title) {
        return this.createColumn(property, title).renderWith(value => '<i class="fa ' + (value ? 'fa-check' : 'fa-remove') + '"></i>');
    }

    createActions(renderFn) {
        return this.createColumn(null, 'Akce').notSortable().renderWith(obj => {
            let result = renderFn(obj);
            if (_.isArray(result)) {
                result = result.join('');
            }

            return result;
        }).withOption('width', '75px');
    }

    disableTableTools(options) {
        _.remove(options.oTableTools.aButtons, { sExtends: 'select_all' });
        _.remove(options.oTableTools.aButtons, { sExtends: 'select_none' });

        return options;
    }

    addInstance(instance, name = 'table') {
        _instances[name] = instance;
    }

    getInstance(name = 'table') {
        if (_instances.hasOwnProperty(name) === false) {
            throw 'Table instance does not exist.';
        }

        return _instances[name];
    }

    getData(table) {
        if (angular.isUndefined(table)) {
            table = this.getInstance();
        }

        return table.DataTable.data();
    }

    reload(table) {
        if (angular.isUndefined(table)) { // reload default instance
            table = this.getInstance();
        }

        if (this.getSelectedRows(table).length > 0) {
            table.DataTable.tabletools().fnSelectNone();
        }

        table.reloadData();
    }

    getSelectedRows(table) {
        return table.DataTable.tabletools().fnGetSelectedData();
    }
}

const LANGUAGES = {
    'aar': 'afar',
    'abk': 'abchazština',
    'ace': 'aceh(ština)',
    'ach': 'ačoli',
    'ada': 'adangme',
    'ady': 'adygei',
    'afa': 'afroasijské',
    'afh': 'afrihili',
    'afr': 'afrikánština',
    'ajm': 'aljamia',
    'aka': 'akan',
    'akk': 'akkadština',
    'alb': 'albánština',
    'ale': 'aleutština',
    'alg': 'algonkinské',
    'amh': 'amharština',
    'ang': 'anglosaština,',
    'apa': 'apačské',
    'ara': 'arabština',
    'arc': 'aramejština',
    'arg': 'aragonská',
    'arm': 'arménština',
    'arn': 'mapuche',
    'arp': 'arapaho',
    'art': 'umělé',
    'arw': 'arawacké',
    'asm': 'asámština',
    'ast': 'bable',
    'ath': 'athapaskánské',
    'aus': 'australské',
    'ava': 'avarština',
    'ave': 'avestský',
    'awa': 'avadhština',
    'aym': 'ajmarština',
    'aze': 'ázerbájdžánština',
    'bad': 'banda',
    'bai': 'bamileke',
    'bak': 'baškirština',
    'bal': 'balúčština',
    'bam': 'bambarština',
    'ban': 'balijština',
    'baq': 'baskičtina',
    'bas': 'basa',
    'bat': 'baltské',
    'bej': 'bedža',
    'bel': 'běloruština',
    'bem': 'bembština',
    'ben': 'bengálština',
    'ber': 'berberské',
    'bho': 'bhódžpurí',
    'bih': 'bihárština',
    'bik': 'bikolština',
    'bin': 'bini',
    'bis': 'bislama',
    'bla': 'siksika',
    'bnt': 'bantuské',
    'bos': 'bosenština',
    'bra': 'bradžština',
    'bre': 'bretonština',
    'btk': 'batačtina',
    'bua': 'burjatština',
    'bug': 'bugiština',
    'bul': 'bulharština',
    'bur': 'barmština',
    'cad': 'caddo',
    'cai': 'indiánské',
    'cam': 'khmérština',
    'car': 'karibština',
    'cat': 'katalánština',
    'cau': 'kavkazské',
    'ceb': 'cebuánština',
    'cel': 'keltské',
    'cmc': 'čamské',
    'cop': 'koptština',
    'cor': 'kornština',
    'cos': 'korsičtina',
    'cpe': 'kreolština',
    'cpf': 'kreolština',
    'cpp': 'kreolština',
    'cre': 'cree',
    'crh': 'krymská',
    'crp': 'kreolština',
    'cus': 'kušitské',
    'cze': 'čeština',
    'dak': 'dakota',
    'dan': 'dánština',
    'dar': 'dargwa',
    'day': 'dajáčtina',
    'del': 'delaware',
    'den': 'slave',
    'dgr': 'dogrib',
    'din': 'dinkština',
    'div': 'divehi',
    'doi': 'dógrí',
    'dra': 'drávidské',
    'dua': 'dualština',
    'dum': 'nizozemština,',
    'dut': 'nizozemština',
    'dyu': 'djula',
    'dzo': 'dzongkä',
    'efi': 'efik',
    'egy': 'egyptština',
    'eka': 'ekajuk',
    'elx': 'elamština',
    'eng': 'angličtina',
    'enm': 'angličtina,',
    'epo': 'esperanto',
    'esk': 'eskymáčtina',
    'esp': 'esperanto',
    'est': 'estonština',
    'eth': 'etiopština',
    'ewe': 'eweština',
    'ewo': 'ewondo',
    'fan': 'fang',
    'fao': 'faerština',
    'far': 'faerština',
    'fat': 'fantiština',
    'fij': 'fidžijština',
    'fin': 'finština',
    'fiu': 'ugrofinské',
    'fon': 'fonština',
    'fre': 'francouzština',
    'fri': 'fríština',
    'frm': 'francouzština,',
    'fro': 'francouzština,',
    'fry': 'fríština',
    'ful': 'fulahština',
    'fur': 'friulština',
    'gaa': 'ga',
    'gae': 'skotská',
    'gag': 'galicijština',
    'gal': 'oromština',
    'gay': 'gayo',
    'gba': 'gbaja',
    'gem': 'germánské',
    'geo': 'gruzínština',
    'ger': 'němčina',
    'gez': 'etiopština',
    'gil': 'kiribatština',
    'gla': 'skotská',
    'gle': 'irština',
    'glg': 'galicijština',
    'glv': 'manština',
    'gmh': 'němčina,',
    'goh': 'němčina,',
    'gon': 'góndština',
    'gor': 'gorontalo',
    'got': 'gótština',
    'grb': 'grebo',
    'grc': 'řečtina,',
    'gre': 'řečtina,',
    'grn': 'guaraní',
    'gua': 'guaraní',
    'guj': 'gudžarátština',
    'gwi': 'gwich‘in',
    'hai': 'haida',
    'hat': 'haitsko-francouzská',
    'hau': 'hauština',
    'haw': 'havajština',
    'heb': 'hebrejština',
    'her': 'herero',
    'hil': 'hiligayonština',
    'him': 'himáčalí',
    'hin': 'hindština',
    'hit': 'chetitština',
    'hmn': 'hmongština',
    'hmo': 'hiri',
    'hun': 'maďarština',
    'hup': 'hupa',
    'cha': 'čamoro',
    'chb': 'čibča',
    'che': 'čečenština',
    'chg': 'čagatajština',
    'chi': 'čínština',
    'chk': 'čukčtina',
    'chm': 'marijština',
    'chn': 'Chinook',
    'cho': 'choctawština',
    'chp': 'chipewyan',
    'chr': 'cherokee',
    'chu': 'církevní',
    'chv': 'čuvaština',
    'chy': 'čejenština',
    'iba': 'iban',
    'ibo': 'igbo',
    'ice': 'islandština',
    'ido': 'ido',
    'iii': 's‘čchuanština',
    'ijo': 'idžo',
    'iku': 'inuktitut',
    'ile': 'interlingue',
    'ilo': 'ilokánština',
    'ina': 'interlingua',
    'inc': 'indo-árijské',
    'ind': 'indonéština',
    'ine': 'indo-evropské',
    'inh': 'inguština',
    'int': 'interlingua',
    'ipk': 'inupiaq',
    'ira': 'íránské',
    'iri': 'irština',
    'iro': 'irokézské',
    'ita': 'italština',
    'jav': 'javánština',
    'jpn': 'japonština',
    'jpr': 'judeo-perština',
    'jrb': 'judeo-arabština',
    'kaa': 'karakalpačtina',
    'kab': 'kabulí',
    'kac': 'kačjinština',
    'kal': 'kalmyčtina',
    'kam': 'kambština',
    'kan': 'kannadština',
    'kar': 'karenština',
    'kas': 'kašmírí',
    'kau': 'kanuri',
    'kaw': 'kawi',
    'kaz': 'kazaština',
    'kbd': 'kabardština',
    'kha': 'khásí',
    'khi': 'khoisanské',
    'khm': 'khmerština',
    'kho': 'chotánština',
    'kik': 'kukujština',
    'kin': 'rwandština',
    'kir': 'kyrgyzština',
    'kmb': 'kimbundština',
    'kok': 'kónkánština',
    'kom': 'komijština',
    'kon': 'konžština',
    'kor': 'korejština',
    'kos': 'kosrajština',
    'kpe': 'kpelle',
    'kro': 'kru',
    'kru': 'kurukh',
    'kua': 'kuaňamština',
    'kum': 'kumyčtina',
    'kur': 'kurdština',
    'kus': 'kusaie',
    'kut': 'kutenai',
    'lad': 'ladinština',
    'lah': 'lahndština',
    'lam': 'lambština',
    'lan': 'okcitánština',
    'lao': 'laoština',
    'lap': 'sami',
    'lat': 'latina',
    'lav': 'lotyština',
    'lez': 'lezgiština',
    'lim': 'limburština',
    'lin': 'lingalština',
    'lit': 'litevština',
    'lol': 'mongština',
    'loz': 'lozština',
    'ltz': 'lucemburština',
    'lua': 'luba-luluaština',
    'lub': 'lubu-katanžština',
    'lug': 'ganda',
    'lui': 'luiseňo',
    'lun': 'lundština',
    'luo': 'luoština',
    'lus': 'lušáí',
    'mac': 'makedonština',
    'mad': 'madurština',
    'mag': 'magahština',
    'mah': 'maršalština',
    'mai': 'maithilština',
    'mak': 'makasarština',
    'mal': 'malajálamština',
    'man': 'mandingština',
    'mao': 'maorština',
    'map': 'malajsko-polynéské',
    'mar': 'maráthština',
    'mas': 'masajština',
    'max': 'manština',
    'may': 'malajština',
    'mdr': 'mandar',
    'men': 'mende',
    'mga': 'irština,',
    'mic': 'micmac',
    'min': 'minangkabau',
    'mis': 'různé',
    'mkh': 'mon-khmerské',
    'mla': 'malgaština',
    'mlg': 'malgaština',
    'mlt': 'maltština',
    'mnc': 'manchu',
    'mni': 'manipurština',
    'mno': 'manobo',
    'moh': 'mohawk',
    'mol': 'moldavština',
    'mon': 'mongolština',
    'mos': 'mosi',
    'mul': 'více',
    'mun': 'mundské',
    'mus': 'muskogee',
    'mwr': 'márvárština',
    'myn': 'mayské',
    'nah': 'nahuatl',
    'nai': 'severoamerické',
    'nap': 'neapolská',
    'nau': 'nauruština',
    'nav': 'navahština',
    'nbl': 'ndebelština',
    'nde': 'ndebelština',
    'ndo': 'ndondština',
    'nds': 'němčina,',
    'nep': 'nepálština',
    'new': 'névárština',
    'nia': 'nias',
    'nic': 'nigersko-konžské',
    'niu': 'niue',
    'nno': 'nynorsk',
    'nob': 'bokmål',
    'nog': 'nogai',
    'non': 'norština,',
    'nor': 'norština',
    'nso': 'sothoština,',
    'nub': 'nubijské',
    'nya': 'ňandžština',
    'nym': 'ňamwežština',
    'nyn': 'nyankolština',
    'nyo': 'nyorština',
    'nzi': 'nzima',
    'oci': 'okcitánština',
    'oji': 'odžibwejština',
    'ori': 'urijština',
    'orm': 'oromština',
    'osa': 'osage',
    'oss': 'osetština',
    'ota': 'turečtina,',
    'oto': 'osmanské',
    'paa': 'papuánské',
    'pag': 'pangsinan',
    'pal': 'pahlaví',
    'pam': 'pampangau',
    'pan': 'paňdžábština',
    'pap': 'papiamento',
    'pau': 'palauština',
    'peo': 'perština,',
    'per': 'perština',
    'phi': 'filipínské',
    'phn': 'foiničtina',
    'pli': 'páli',
    'pol': 'polština',
    'pon': 'pohnpeiština',
    'por': 'portugalština',
    'pra': 'prákrty',
    'pro': 'provensálština,',
    'pus': 'pašto',
    'que': 'kečuánština',
    'raj': 'rádžasthánština',
    'rap': 'rapanuiština',
    'rar': 'rarotongština',
    'roa': 'románské',
    'roh': 'rétorománština',
    'rom': 'romština',
    'rum': 'rumunština',
    'run': 'rundština',
    'rus': 'ruština',
    'sad': 'sandawština',
    'sag': 'sangština',
    'sah': 'jakutština',
    'sai': 'jihoamerické',
    'sal': 'salishské',
    'sam': 'samarština',
    'san': 'sanskrt',
    'sao': 'samojština',
    'sas': 'sasakština',
    'sat': 'santálí',
    'scc': 'srbština',
    'sco': 'skotština',
    'scr': 'chorvatština',
    'sel': 'selkupština',
    'sem': 'semitské',
    'sga': 'irština,',
    'sgn': 'znakové',
    'shn': 'šanština',
    'sho': 'šonština',
    'sid': 'sidamo',
    'sin': 'sinhálština',
    'sio': 'siouxské',
    'sit': 'sinotibetské',
    'sla': 'slovanské',
    'slo': 'slovenština',
    'slv': 'slovinština',
    'sma': 'sami',
    'sme': 'sami',
    'smi': 'sami',
    'smj': 'lule',
    'smn': 'inari',
    'smo': 'samojština',
    'sms': 'skolt',
    'sna': 'šonština',
    'snd': 'sindhština',
    'snh': 'sinhálština',
    'snk': 'sonikština',
    'sog': 'soghdština',
    'som': 'somálština',
    'son': 'songhajština',
    'sot': 'sothoština,',
    'spa': 'španělština',
    'srd': 'sardština',
    'srr': 'serer',
    'ssa': 'nilsko-saharské',
    'sso': 'sothoština,',
    'ssw': 'svatština',
    'suk': 'sukuma',
    'sun': 'sundština',
    'sus': 'susu',
    'sux': 'sumerština',
    'swa': 'svahilština',
    'swe': 'švédština',
    'swz': 'svatština',
    'syr': 'syrština',
    'tag': 'tagalština',
    'tah': 'tahitština',
    'tai': 'thajské',
    'taj': 'tadžičtina',
    'tam': 'tamilština',
    'tar': 'tatarština',
    'tat': 'tatarština',
    'tel': 'telugu',
    'tem': 'temne',
    'ter': 'tereno',
    'tet': 'tetumština',
    'tgk': 'tadžičtina',
    'tgl': 'tagalština',
    'tha': 'thajština',
    'tib': 'tibetština',
    'tig': 'tigrejština',
    'tir': 'tigriňa',
    'tiv': 'tivština',
    'tkl': 'tokelauština',
    'tli': 'tlingit',
    'tmh': 'tamašek',
    'tog': 'tongština',
    'ton': 'tongština',
    'tpi': 'tok',
    'tru': 'truk',
    'tsi': 'tsimshijské',
    'tsn': 'tswanština',
    'tso': 'tsongština',
    'tsw': 'tswanština',
    'tuk': 'turkmenština',
    'tum': 'tumbukština',
    'tup': 'tupi',
    'tur': 'turečtina',
    'tut': 'altajské',
    'tvl': 'tuvalština',
    'twi': 'twi',
    'tyv': 'tuvština',
    'udm': 'udmurtština',
    'uga': 'ugaritština',
    'uig': 'ujgurština',
    'ukr': 'ukrajinština',
    'umb': 'umbundu',
    'und': 'neurčený,',
    'urd': 'urdština',
    'uzb': 'uzbečtina',
    'vai': 'vai',
    'ven': 'vendština',
    'vie': 'vietnamština',
    'vol': 'volapük',
    'vot': 'votiatština',
    'wak': 'wakashské',
    'wal': 'walamština',
    'war': 'waray',
    'was': 'washo',
    'wel': 'velština',
    'wen': 'lužická',
    'wln': 'valonština',
    'wol': 'wolofština',
    'xho': 'xhosština',
    'yao': 'jaoština',
    'yap': 'Yapese',
    'yid': 'jidiš',
    'yor': 'jorubština',
    'ypk': 'yupik',
    'zap': 'zapotéčtina',
    'zen': 'zenaga',
    'zha': 'čuangština',
    'znd': 'zandština',
    'zul': 'zuluština',
    'zun': 'zunijština'
};

const DC_TERMS = {
    'inventoryId': __('Inventární číslo'),
    'title': __('Název'),
    'description': __('Popis'),
    'creator': __('Autor'),
    'spatial': __('Místo'),
    'publisher': __('Vydavatel'),
    'format': __('Formát'),
    'temporal': __('Období'),
    'subject': __('Téma'),
    'language': __('Jazyk'),
    'type': __('Typ dokumentu')
};

const ATTRIBUTES = [
    {
        name: __('Typ dokumentu'),
        key: 'documentType'
    },
    {
        name: __('Podtyp dokumentu'),
        key: 'documentSubType'
    },
    {
        name: __('Popis'),
        key: 'description'
    },
    {
        name: __('Jazyk'),
        key: 'language'
    },
    {
        name: __('Autor'),
        key: 'creator'
    },
    {
        name: __('Vydavatel'),
        key: 'publisher'
    },
    {
        name: __('Rok vydání'),
        key: 'created'
    },
    {
        name: __('Formát'),
        key: 'format'
    },
    {
        name: __('Období'),
        key: 'temporal'
    },
    {
        name: __('Téma'),
        key: 'subject'
    },
    {
        name: __('Místo'),
        key: 'spatial'
    }
];

import {DocumentEntity} from '../Entities/DocumentEntity.js';

export class DocumentService {
    /**
     * @ngInject
     */
    constructor(RestangularCachable, $modal) {
        this.rest = RestangularCachable;
        this.modal = $modal;
    }

    get Attributes() {
        return ATTRIBUTES;
    }

    get DCTerms() {
        return DC_TERMS;
    }

    get Languages() {
        return LANGUAGES;
    }

    _createEntity(document) {
        return new DocumentEntity(document);
    }

    getById(id) {
        return this.rest.one('search/id').get({id: id}).then(this._createEntity);
    }

    getByInventoryId(id) {
        return this.rest.one('search/invId').get({id: id}).then(this._createEntity);
    }

    getMoreLikeThis(id) {
        return this.rest.one('search/mlt').get({id: id});
    }

    /**
     *
     * @param {Array} tiles
     * @param {Array} thumbnails
     * @param {DocumentEntity} document
     * @param {DocumentEntity} issue
     * @param {Object} settings
     */
    createViewer(tiles, thumbnails, document, issue, settings) {
        let $modal = this.modal;

        if (tiles) {
            $modal.open({
                templateUrl: 'views/controllers/Document/viewer.html',
                animation: false,
                size: 'lg',
                controller: controller,
                controllerAs: 'vm'
            });
        }

        /**
         * @ngInject
         * @param {DataService} DataService
         */
        function controller(DataService) {
            let vm = this;

            vm.tiles = tiles;
            vm.osd = null;
            vm.mode = 'image';
            vm.thumbnails = thumbnails;
            vm.watermarkId = document.watermark ? settings.watermarkId : null;

            vm.setPage = setPage;
            vm.isCurrent = isCurrent;
            vm.toggleTextLayer = toggleTextLayer;

            loadTextLayer();
            loadWatermark();

            function loadWatermark() {
                if (document.watermark) {
                    DataService.getPortalSettings().then(portalSettings => {
                        if (settings.watermarkId) {
                            vm.watermarkId = settings.watermarkId;
                        } else if (portalSettings.watermarkId) {
                            vm.watermarkId = portalSettings.watermarkId;
                        } else {
                            vm.watermarkId = null;
                        }
                    });
                }
            }

            function isCurrent(page) {
                if (vm.osd) {
                    return vm.osd.currentPage === page;
                }
            }

            function setPage(page) {
                vm.osd.setPage(page);
                loadTextLayer();
            }

            function loadTextLayer() {
                if (vm.mode === 'text') {
                    DataService.findOCR(issue ? issue.inventoryId : document.inventoryId, vm.osd.currentPage + 1).then(success);
                }

                function success(text) {
                    vm.text = text;
                }
            }

            function toggleTextLayer() {
                vm.mode = vm.mode === 'image' ? 'text' : 'image';
                loadTextLayer();
            }
        }
    }
}

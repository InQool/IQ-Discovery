const Configuration = {
    subtitle: __('Najděte poklady ve fondech paměťových institucí Středočeského kraje.'),
    document: {
        hideInventoryId: true,
        showFieldsForAnonymousUser: [
            'subject',
            'creator',
            'spatial',
            'temporal'
        ],
        showCustomFieldsForAnonymousUser: [
            'Název',
            'Předmět',
            'Rod',
            'Druh',
            'Lokalita',
            'Místo vydání',
            'Datace',
            'Autor'
        ]
    },
    footer: {
        logo: 'logo.stredoceskykraj.png',
        eu: {
            href: 'http://www.strukturalni-fondy.cz/cs/Microsites/Integrovany-OP/Uvodni-strana',
            text: 'Portál byl vytvořen v rámci projektu Krajské služby eGovernmentu Středočeského kraje, reg. č.: CZ.1.06/2.1.00/19.09274, na který Středočeský kraj získal finanční podporu z výzvy č. 19 v rámci Integrovaného operačního programu.'
        },
        portalName: __('Portál pro zpřístupnění digitálního obsahu'),
        address: {
            name: 'Středočeský kraj',
            street: 'Zborovská 11',
            zip: '150 21',
            city: 'Praha 5',
            url: 'http://www.kr-stredocesky.cz'
        },
        partners: [
            'České muzeum stříbra',
            'Galerie Středočeského kraje',
            'Muzeum Mladoboleslavska',
            'Muzeum Podblanicka',
            'Hornické muzeum Příbram',
            'Muzeum Českého krasu',
            'Muzeum T.G.M. Rakovník',
            'Oblastní muzeum Praha-východ',
            'Památník Antonína Dvořáka ve Vysoké u Příbrami',
            'Památník Karla Čapka ve Staré Huti u Dobříše',
            'Polabské muzeum',
            'Rabasova galerie Rakovník',
            'Regionální muzeum v Jílovém u Prahy',
            'Regionální muzeum v Kolíně',
            'Regionální muzeum Mělník',
            'Sládečkovo vlastivědné muzeum v Kladně',
            'Středočeská vědecká knihovna v Kladně',
            'Středočeská muzeum v Roztokách u Prahy',
            'Ústav archeologické památkové péče středních Čech'
        ]
    },
    disabledStates: [
        'secured.about'
    ]
};

module.exports = Configuration;

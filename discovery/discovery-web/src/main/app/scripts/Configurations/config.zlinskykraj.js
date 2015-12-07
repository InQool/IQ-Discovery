const Configuration = {
    subtitle: __('Najděte poklady v digitalizovaných fondech paměťových institucí Zlínského kraje.'),
    document: {
        hideSignature: true,
        allowDocumentSubType: true
    },
    clipoard: {
        reservationOfferVerification: true
    },
    footer: {
        logo: 'logo.zlinskykraj.gif',
        eu: {
            href: 'http://www.strukturalni-fondy.cz/iop',
            text: 'PROJEKT JE SPOLUFINANCOVÁN Z PROSTŘEDKŮ EVROPSKÉ UNIE, EVROPSKÉHO FONDU PRO REGIONÁLNÍ ROZVOJ'
        },
        portalName: __('Portál pro zpřístupnění digitálního obsahu'),
        address: {
            name: 'Zlínský kraj',
            street: 'třída Tomáše Bati 21',
            zip: '761 90',
            city: 'Zlín',
            url: 'http://www.kr-zlinsky.cz'
        },
        administrator: 'vitezslav.mach@kr-zlinsky.cz',
        partners: {
            'Slovácké muzeum v Uherském Hradišti': 'http://www.slovackemuzeum.cz',
            'Muzeum jihovýchodní Moravy ve Zlíně': 'http://www.muzeum-zlin.cz',
            'Muzeum regionu Valašsko': 'http://www.muzeumvalassko.cz',
            'Muzeum Kroměřížska': 'http://www.muzeum-km.cz',
            'Krajská galerie výtvarného umění ve Zlíně': 'http://www.galeriezlin.cz/cz',
            'Krajská knihovna Františka Bartoše ve Zlíně': 'http://kfbz.cz',
            'Hvězdárna Valašské Meziříčí': 'http://www.astrovm.cz'
        },
        about: `Projekt „Zpřístupnění digitálního obsahu“ (ZDO) byl pořízen v rámci realizace projektu „Rozvoj e-Governmentu ve Zlínském kraji II“, reg č. CZ. 1. 06/2.1.00/19.09273.<br>
			<br>
			Projekt je realizován v rámci Integrovaného operačního programu (IOP).`
    }
};

module.exports = Configuration;

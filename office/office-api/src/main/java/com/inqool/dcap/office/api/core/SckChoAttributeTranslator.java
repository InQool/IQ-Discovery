package com.inqool.dcap.office.api.core;

/**
 * @author Lukas Jane (inQool) 5. 11. 2015.
 */
public class SckChoAttributeTranslator {
    public static String translateAttribute(String name, String type) {
        String translation = translateCommon(name);
        if(translation != null) {
            return translation;
        }
        switch (type) {
            case "přírodovědní sbírky":
                return translatePrirodovedni(name);
            case "muzejni knihovna":
                return translateKnihovni(name);
            case "společenskovědní sbírky":
            default:
                return translateSpolecenskovedni(name);
        }
    }

    private static String translateCommon(String name) {
        switch(name) {
            /*case "nazevOrganizace":
                return "Instituce název";
                break;
            case "cisloOrganizace":
                return "Instituce číslo";
                break;
            case "typ":
                return "Typ dat";
                break;
            case:
                return "Podsbírka CES";
                break;
            case:
                return "Publikovat";
                break;
            case:
                return "Jednoznačný identifikátor";
                break; */
            default:
                return null;
        }
    }

    private static String translateSpolecenskovedni(String name) {
        switch (name) {
            case "nazevSbirky":
                return "Sbírka";
            case "inventarniCislo":
                return "Inventární číslo";
            case "prirustkoveCislo":
                return "Přírůstkové číslo";
            case "pomocneCislo":
                return "Pomocné číslo";
            case "predmet":
                return "Předmět";
            /*case "":
                return "Název/Titul";*/
            case "popis":
                return "Popis/Obsah";
            case "popisLaicky":
                return "Laický popis";
            case "avers":
                return "Avers";
            case "revers":
                return "Revers";
            case "hrana":
                return "Hrana";
            case "material":
                return "Materiál";
            case "technika":
                return "Technika";
            case "puvodOsoba":
                return "Autor/Autor snímku";
            case "puvodObdobiUdalosti":
                return "Datování";
            case "puvodCasUdalosti":
                return "Datum vydání/vzniku";
            case "puvodZeme":
                return "Země";
            case "puvodKatastralniUzemi":
                return "Katastr";
            case "nalezLokalita":
                return "Lokalita/Místo nálezu";
            case "puvodLokalita":
                return "Místo původu";
            case "puvodMincovna":
                return "Mincovna";
            case "format":
                return "Formát";
            case "rozmery":
                return "Rozměry";
            case "pocetKusu":
                return "Počet kusů";
            default:
                return null;
        }
    }

    private static String translatePrirodovedni(String name) {
        switch (name) {
            case "nazevSbirky":
                return "Sbírka";
            case "inventarniCislo":
                return "Inventární číslo";
            case "prirustkoveCislo":
                return "Přírůstkové číslo";
            case "pomocneCislo":
                return "Pomocné číslo";
            case "celed":
                return "Čeleď";
            case "rod":
                return "Rod";
            case "druh":
                return "Druh";
            case "predmet":
                return "Předmět";
            case "popis":
                return "Popis";
            case "popisLaicky":
                return "Laický popis pro zveřejnění";
            case "nalezObdobiUdalosti":
                return "Datum sběru";
            case "nalezCasUdalosti":
                return "Datum sběru";
            case "nalezZeme":
                return "Stát/Země";
            case "nalezKatastralniUzemi":
                return "Katastrální území";
            case "nalezMapovyCtverec":
                return "Mapový čtverec";
            case "pocetKusu":
                return "Počet kusů";
            default:
                return null;
        }
    }

    private static String translateKnihovni(String name) {
        switch (name) {
            case "signatura":
                return "Signatura";
            case "fond":
                return "Fond";
            case "druhDokumentu":
                return "Druh dokumentu";
            case "autor":
                return "Autoři";
            case "editor":
                return "Editor";
            case "nazev":
                return "Název";
            case "podnazev":
                return "Podnázev";
            case "prekladatel":
                return "Překladatel";
            case "ilustrator":
                return "Ilustrátor";
            case "oznaceniVydani":
                return "Označení vydání";
            case "mistoVydani":
                return "Místo vydání";
            case "rokVydani":
                return "Rok vydání";
            case "nakladatel":
                return "Nakladatel";
            case "edice":
                return "Edice";
            case "vytiskl":
                return "Vytiskl";
            case "jazykDokumentu":
                return "Jazyk dokumentu";
            case "rozsah":
                return "Rozsah";
            case "isbn":
                return "ISBN";
            case "issn":
                return "ISSN";
            case "predmetovaHesla":
                return "Předmětová hesla";
            case "jmenneOdkazy":
                return "Jmenné odkazy";
            case "knihopisnaPoznamka":
                return "Knihopisná poznámka";
            case "rozpisSvazku":
                return "Rozpis svazků";
            case "udajeOPeriodiku":
                return "Údaje o periodiku";
            case "pocetVytisku":
                return "Počet výtisků";
            default:
                return null;
        }
    }
}

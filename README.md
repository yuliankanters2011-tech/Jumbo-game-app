# Jumbo Speurclub
Onofficieel Nederlandstalig boodschappen-speurspel voor Android 7+. Geen advertenties of achtergrondtracking. Zoek in de ingebouwde productlijst, verdien 50 punten per uniek product en ontgrendel de marktbadge. De optionele camera leest barcodes lokaal; bij een onbekende code kies je zelf het product. Voortgang en koppelingen blijven lokaal.

## Installeren
**[Download Jumbo-Speurclub.apk](./Jumbo-Speurclub.apk?raw=true)** rechtstreeks op je Android-toestel. Open het bestand (geen zip) en sta zo nodig installatie vanuit je browser of bestandsbeheerder toe. Dit is een zelfstandig ondertekende releasebuild voor Android 7 of nieuwer. Staat er al een anders ondertekende testversie op je toestel, dan moet je die eerst verwijderen; je lokale collectie gaat daarbij verloren. Zelf een testbuild maken: Android SDK 35, Java 17 en Gradle 8.9, `gradle assembleDebug`.

## Samen spelen
Asynchroon clubklassement, geen live kaart. Start `node server/club.js` op een eigen server achter een HTTPS-proxy naar poort 8080. Bewaar `scores.json` op een persistent volume (`DATA_FILE` en `PORT` instelbaar). Alle spelers vullen dezelfde HTTPS-URL en clubnaam in en kiezen verschillende aliassen. Alleen bij drukken op delen worden clubnaam, alias en score verzonden. Geen publieke server meegeleverd. Bedoeld voor vertrouwde vrienden: geen authenticatie of anti-cheat.

## Browserpreview
`python3 -m http.server 8000 --directory docs`. Automatisch barcodelezen werkt in de APK; de browser leest automatisch waar BarcodeDetector beschikbaar is, anders kun je typen.

## Mijn uitrusting en GitHub Pages
GPS-afstand, geschatte stappen (versnellingsmeter), bewegingsstatus en camerascanner hebben afzonderlijke schakelaars, standaard uit. Alleen de voorkeuren worden bewaard, niet je route. Afstand en stappen gelden per sessie. Sensoren pauzeren op de achtergrond. Een browser zonder geschikte sensor kan geen metingen geven. De camera start alleen na een extra druk bij Scanner.

Publiceer via **Settings → Pages → Deploy from a branch → arena/d10381c5-jumbo-game-app → /docs → Save**. Dezelfde bestanden gaan mee in de APK. Na een eerste online bezoek kan de site offline laden als de browser de serviceworker-cache bewaart.

## Winkeltocht (1.2)
Kies een Oosterhouts filiaal op de speurradar. Noord is boven; de radar toont richting en hemelsbrede afstand, geen wandelroute. Claim het speurpunt binnen 150 m met een recente plaatsbepaling (±60 m of beter), of kies handmatig inchecken. Scan vervolgens de verpakking van Barilla Spaghetti Nº5 500 g. Alleen de ondersteunde EAN ontgrendelt de winkelbadge. Bij nieuwe pasta in je collectie krijg je ook 50 punten. Er is geen live voorraadinformatie. Een andere verpakking kan een andere EAN hebben.

Bronnen: [Nieuwe Bouwlingstraat, © OpenStreetMap-bijdragers](https://mapcarta.com/N2853302973), [winkelgebied Arkendonk (gebiedsmiddelpunt)](https://postcodebijadres.nl/arkendonk+oosterhout), [productcode, Open Food Facts](https://world.openfoodfacts.org/product/8076800195057/spaghetti-n-5-barilla).

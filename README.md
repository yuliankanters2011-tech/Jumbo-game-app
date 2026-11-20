# Jumbo Speurclub
Onofficieel Nederlandstalig boodschappen-speurspel voor Android 7+. Geen advertenties of achtergrondtracking. Zoek in de ingebouwde productlijst, verdien 50 punten per uniek product en ontgrendel de marktbadge. De optionele camera leest barcodes lokaal; bij een onbekende code kies je zelf het product. Voortgang en koppelingen blijven lokaal.

## Installeren
Open de groene workflow **Bouw Android APK** onder GitHub Actions en download het artifact **Jumbo-Speurclub-APK**. Pak de zip uit en open `app-debug.apk` op je Android-toestel. Sta indien nodig installeren vanuit deze bron toe. Dit is een test-APK, geen Play Store-release. Zelf bouwen: Android SDK 35, Java 17 en Gradle 8.9, `gradle assembleDebug`.

## Samen spelen
Asynchroon clubklassement, geen live kaart. Start `node server/club.js` op een eigen server achter een HTTPS-proxy naar poort 8080. Bewaar `scores.json` op een persistent volume (`DATA_FILE` en `PORT` instelbaar). Alle spelers vullen dezelfde HTTPS-URL en clubnaam in en kiezen verschillende aliassen. Alleen bij drukken op delen worden clubnaam, alias en score verzonden. Geen publieke server meegeleverd. Bedoeld voor vertrouwde vrienden: geen authenticatie of anti-cheat.

## Browserpreview
`python3 -m http.server 8000 --directory docs`. Automatisch barcodelezen werkt in de APK; de browser leest automatisch waar BarcodeDetector beschikbaar is, anders kun je typen.

## Mijn uitrusting en GitHub Pages
GPS-afstand, geschatte stappen (versnellingsmeter), bewegingsstatus en camerascanner hebben afzonderlijke schakelaars, standaard uit. Alleen de voorkeuren worden bewaard, niet je route. Afstand en stappen gelden per sessie. Sensoren pauzeren op de achtergrond. Een browser zonder geschikte sensor kan geen metingen geven. De camera start alleen na een extra druk bij Scanner.

Publiceer via **Settings → Pages → Deploy from a branch → arena/d10381c5-jumbo-game-app → /docs → Save**. Dezelfde bestanden gaan mee in de APK. Na een eerste online bezoek kan de site offline laden als de browser de serviceworker-cache bewaart.

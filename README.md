# Jumbo Speurclub
Onofficieel Nederlandstalig boodschappen-speurspel voor Android 7+. Geen GPS, advertenties of achtergrondtracking. Zoek in de ingebouwde productlijst, verdien 50 punten per uniek product en ontgrendel de marktbadge. De optionele camera leest barcodes lokaal; bij een onbekende code kies je zelf het product. Voortgang en koppelingen blijven lokaal.

## Installeren
Open de groene workflow **Bouw Android APK** onder GitHub Actions en download het artifact **Jumbo-Speurclub-APK**. Pak de zip uit en open `app-debug.apk` op je Android-toestel. Sta indien nodig installeren vanuit deze bron toe. Dit is een test-APK, geen Play Store-release. Zelf bouwen: Android SDK 35, Java 17 en Gradle 8.9, `gradle assembleDebug`.

## Samen spelen
Asynchroon clubklassement, geen live kaart. Start `node server/club.js` op een eigen server achter een HTTPS-proxy naar poort 8080. Bewaar `scores.json` op een persistent volume (`DATA_FILE` en `PORT` instelbaar). Alle spelers vullen dezelfde HTTPS-URL en clubnaam in en kiezen verschillende aliassen. Alleen bij drukken op delen worden clubnaam, alias en score verzonden. Geen publieke server meegeleverd. Bedoeld voor vertrouwde vrienden: geen authenticatie of anti-cheat.

## Browserpreview
`python3 -m http.server 8000 --directory app/src/main/assets`. Automatisch barcodelezen werkt in de APK; de browser biedt handmatige invoer en optioneel camerabeeld.

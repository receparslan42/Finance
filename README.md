# Finance 

A modern Android app for tracking cryptocurrency markets with Jetpack Compose. It fetches live market data, gainers/losers, and historical price series, lets you search coins, and save favourites locally with Room. Charts are rendered with Vico.

## Overview
- Package: `com.receparslan.finance`
- Min SDK: 26
- Target/Compile SDK: 36
- Kotlin: 2.2.21
- AGP: 8.13.1
- UI: Jetpack Compose + Material 3 + Navigation Compose
- Storage: Room (favourites)
- Networking: Retrofit + OkHttp + Gson Converter
- Images: Coil
- Charts: Vico
- Data sources: CoinGecko (markets/search), Binance (historical klines)

## Features
- Live cryptocurrency market list with price and 24h change
- Top Gainers and Losers tabs
- Favourites (persisted with Room)
- Search by coin
- Detail screen with price chart and metadata
- Bottom navigation with multiple screens

## Screenshots

| Home Page                                                | Gainers                                                  | Losers                                                 |
|----------------------------------------------------------|----------------------------------------------------------|--------------------------------------------------------|
| <img src="screenshoots/home_screen.jpg" alt="Home Page"> | <img src="screenshoots/gainer_screen.jpg" alt="Gainers"> | <img src="screenshoots/loser_screen.jpg" alt="Losers"> |

| Search                                                  | Favourites                                                      |
|---------------------------------------------------------|-----------------------------------------------------------------|
| <img src="screenshoots/search_screen.jpg" alt="Search"> | <img src="screenshoots/favourites_screen.jpg" alt="Favourites"> |

| Detail                                                    | Detail                                                      | Detail                                                      |
|-----------------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------|
| <img src="screenshoots/detail_screen.jpg" alt="Detail 1"> | <img src="screenshoots/detail_screen_2.jpg" alt="Detail 2"> | <img src="screenshoots/detail_screen_3.jpg" alt="Detail 3"> |

## Tech Stack
- Jetpack Compose, Material 3, Navigation Compose
- Room (`CryptocurrencyDatabase`, `CryptocurrencyDao`)
- Retrofit interfaces for CoinGecko and Binance in `service/CryptocurrencyServices.kt`
- ViewModels per screen in `viewmodel/`
- Models in `model/` (e.g., `Cryptocurrency`, `KlineData`)

## Architecture (MVVM)
This project follows a clear Model–View–ViewModel separation:

- Model: Data classes (`Cryptocurrency`, `CryptocurrencyList`, `KlineData`) and persistence layer (Room entities + `CryptocurrencyDao`, `CryptocurrencyDatabase`). They represent and store app data.
- View: Jetpack Compose UI in `ui/screens/` plus `MainActivity` which hosts the `NavHost`. Composables render immutable state and emit user intents.
- ViewModel: Classes in `viewmodel/` (e.g., `HomeViewModel`, `GainerAndLoserViewModel`, `FavouritesViewModel`, `SearchViewModel`, `DetailViewModel`). Each exposes StateFlows/LiveData (if used) and business logic: fetching market pages, filtering gainers/losers, managing favourites (insert/delete), performing searches, and preparing detail/chart data.
- Service / Data Source: Retrofit interfaces in `service/CryptocurrencyServices.kt` act as a lightweight repository layer. ViewModels call these suspend functions, transform responses, and update UI state.

Benefits: Decoupled UI and data logic, easier testing of ViewModels, and composables remain stateless aside from local UI state (e.g., selected bottom nav index). Room integration ensures favourites survive process death, while network results are refreshed on demand.

## API Details
Two public APIs are consumed—no API keys required:

1. CoinGecko (Base: `https://api.coingecko.com/api/v3/`)
   - `GET coins/markets?vs_currency=usd&per_page=250&page={n}`: paginated market data (id, symbol, name, image, current price, 24h change, last updated). Mapped directly onto `Cryptocurrency` fields.
   - `GET search?query={text}`: returns matched coins; wrapped in `CryptocurrencyList`.
   - Additional helper endpoints declared (by ids/names) can be leveraged for targeted fetches.
   - Rate Limits: Public free tier ~30–50 calls/min per IP (subject to change). Excess requests may yield 429 responses; ViewModels should consider simple retry/backoff if expanded.

2. Binance (Base: `https://api.binance.com/api/v3/`)
   - `GET klines?symbol={SYMBOL}&startTime={ms}&endTime={ms}&interval={code}&limit=1000`: historical OHLCV data used for charts. Returned list entries are raw arrays; mapped into `KlineData` (openTime, open, high, low, close, closeTime).
   - Typical `interval` values: `1m`, `5m`, `15m`, `1h`, `4h`, `1d` etc.
   - Weight-based rate limits; current usage is light (single-user app) and unlikely to hit restrictions.

Error Handling Notes:
- Retrofit `Response<List<Cryptocurrency>>` is checked for `isSuccessful`; failures should surface a fallback UI (e.g., loading/error state placeholder).
- Network permissions: `INTERNET` and `ACCESS_NETWORK_STATE` declared in `AndroidManifest.xml`.
- Potential enhancements: caching layer, repository abstraction, unified error model.

Security & Privacy:
- No secrets or user PII stored; favourites persist locally only.
- All requests are HTTPS.

## Requirements
- Android Studio Ladybug or newer
- Android Gradle Plugin 8.13+
- JDK 21 (project is configured for Java/Kotlin 21 toolchain)
- Internet connection (uses public CoinGecko and Binance endpoints; no API keys required)

## Getting Started
### Open in Android Studio
1. Clone the repo
2. File → Open → select the project root
3. Let Gradle sync finish
4. Run the `app` configuration on a device/emulator (Android 8.0+, API 26+)

### Build from terminal (Windows PowerShell)
```powershell
# From project root
./gradlew.bat clean
./gradlew.bat :app:assembleDebug
```
APK output: `app/build/outputs/apk/debug/`.

## Project Structure
- `app/src/main/java/com/receparslan/finance/`
  - `MainActivity.kt` – Compose scaffold + bottom navigation + NavHost
  - `ui/` – screens, charts and theme
  - `service/` – Retrofit services for CoinGecko/Binance
  - `database/` – Room DB + DAO for favourites
  - `model/` – data models
  - `viewmodel/` – screen ViewModels
- `app/src/main/AndroidManifest.xml` – app manifest & permissions

## Notes
- Public market data is subject to rate limits and availability.
- This project is for educational/demo purposes only; not financial advice.

## License
This project is licensed under the MIT License — see [`LICENSE`](LICENSE) for details.

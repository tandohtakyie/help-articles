# Help Articles Android App

A modern Android app demonstrating offline-first architecture with KMP caching and comprehensive error handling.

## What's Built

### Architecture
- **Offline-First Pattern**: Cache-first strategy with network fallback for instant loads and resilient operation
- **KMP Shared Module**: Business logic and caching shared across platforms (ready for iOS)
- **MVVM Architecture**: Clean separation between UI, ViewModels, and Repository
- **Hilt DI**: Dependency injection throughout app and WorkManager

### Core Features

**1. Article List Screen**
- Real-time search/filter
- Last updated timestamps
- Empty states and loading indicators
- Error banners with retry functionality

**2. Article Detail Screen**
- Full article content display
- Markdown-style formatting
- Error handling with cached fallback
- Back navigation

**3. Offline Support**
- SQLDelight cache with 24-hour TTL
- Works completely offline after first load
- Auto-refreshes when stale and network available

**4. Error Handling**
- Network errors (no connection, timeout)
- Server errors (5xx responses)
- Backend errors (with errorCode/Title/Message)
- Clear UI feedback for each error type

**5. Background Sync**
- Daily WorkManager job
- Runs only when: device online + battery not low
- Exponential backoff on failure

### Technology Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| UI | Jetpack Compose (Material 3) | Modern, declarative UI |
| State | StateFlow + ViewModels | Reactive, lifecycle-aware |
| DI | Hilt | Android standard, WorkManager support |
| Cache | SQLDelight (KMP) | Type-safe, multiplatform |
| Network | Ktor Client | KMP-compatible, lightweight |
| Async | Coroutines + Flow | Structured concurrency |
| Background | WorkManager | Reliable, constraint-based |

## Key Design Decisions

### 24-Hour Cache TTL
**Why?** Help articles don't change rapidly. 24h balances freshness with offline capability for day-long offline usage.

### KMP Shared Module
**Why?** Code reuse for future iOS app. Cache, Repository, and API client work on any platform.

### Offline-First Pattern
**Why?** Better UX - instant loads, no spinners, works without network.

### Mock Data
**Why?** No external dependencies, works offline, deterministic for testing.

### WorkManager Constraints
**Why?**
- **Network Required**: Prefetch needs API calls
- **Battery Not Low**: Respectful background behavior

## Build & Run
```bash
# Open in Android Studio
# File > Open > Select HelpArticles directory

# Or via command line
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## How It Works

### Cold Start (No Cache)
```
App Launch → Repository checks cache → Empty
→ Fetch from API → Success → Save to cache → Show UI
```

### Warm Start (Fresh Cache)
```
App Launch → Repository checks cache → Fresh (< 24h)
→ Return cached data → Show UI instantly
```

### Network Error (Stale Cache Available)
```
App Launch → Repository checks cache → Stale (> 24h)
→ Try fetch from API → Network Error
→ Fallback to stale cache → Show error banner + cached articles
```

### Background Sync
```
24h Timer → WorkManager triggered
→ Check constraints (online + battery OK)
→ Fetch latest articles → Update cache silently
```

## Testing

- **Unit Tests**: Cache staleness logic (documented, requires setup)
- **UI Tests**: Error handling + retry interaction
- **Manual Testing**: Offline mode via Airplane Mode

## Error Messages

| Error Type | User Sees |
|-----------|-----------|
| No Internet | "Network connection failed. Please check your internet connection." |
| Timeout | "Request timed out. Please try again." |
| Server 5xx | "Server error (500). Please try again later." |
| Backend Error | Custom errorTitle + errorMessage from API |

## What's Not Included

- Real backend API (using mocks)
- Pagination (loads all ~5 articles at once)
- User authentication
- Analytics/crash reporting
- iOS app (KMP foundation ready)

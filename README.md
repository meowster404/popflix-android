# PopFlix - Movie Discovery & Tracking App

![PopFlix Logo](app/src/main/res/mipmap-hdpi/ic_launcher.png)

PopFlix is a modern Android application for movie enthusiasts that allows users to discover, track, and manage their favorite movies and TV shows. Built with Kotlin and modern Android development practices, the app provides a seamless experience for browsing movies, managing watchlists, reading reviews, and watching trailers.

## 🚀 Features

### Core Features
- **Movie Discovery**: Browse popular, top-rated, upcoming, and now-playing movies
- **Advanced Search**: Search movies by title with real-time results
- **Detailed Movie Information**: View comprehensive movie details including cast, crew, ratings, and reviews
- **Watchlist Management**: Add movies to your personal watchlist for future viewing
- **Favorites**: Mark movies as favorites for quick access
- **Movie Trailers**: Watch official trailers directly in the app
- **User Reviews**: Read and browse user reviews for movies
- **Push Notifications**: Get notified about new releases and recommendations

### Categories & Filtering
- **Trending Movies**: Weekly trending movies
- **Latest Releases**: Current theatrical releases
- **Genre-based Discovery**: Action, Comedy, Kids movies, and more
- **Regional Content**: US-based latest releases

### User Experience
- **Dark/Light Theme Support**: Adaptive theming for better user experience
- **Intuitive Navigation**: Bottom navigation with sidebar for easy access
- **Responsive Design**: Optimized for various screen sizes
- **Offline Support**: Basic functionality works without internet connection

## 🛠️ Technical Stack

### Architecture & Development
- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle Kotlin DSL
- **UI Framework**: ViewBinding with Fragments

### Dependencies
- **Networking**: Retrofit 2.9.0 with OkHttp
- **Image Loading**: Glide 4.15.1
- **Authentication**: Firebase Auth with Google Sign-In
- **Database**: Firebase Firestore
- **JSON Processing**: Gson 2.10.1
- **Material Design**: Google Material Components 1.12.0

### API Integration
- **Movie Data**: The Movie Database (TMDB) API v3
- **Authentication**: Firebase Authentication
- **Cloud Storage**: Firebase Firestore

## 📱 App Structure

```
app/src/main/java/com/example/popflix/
├── activities/
│   ├── MainActivity.kt          # Main app launcher
│   ├── AuthActivity.kt          # Authentication container
│   ├── DetailsActivity.kt       # Movie details screen
│   ├── ProfileActivity.kt       # User profile management
│   ├── SettingsActivity.kt      # App settings
│   ├── FavoritesActivity.kt     # User favorites
│   ├── WatchlistActivity.kt     # Watchlist management
│   ├── SearchResultsActivity.kt # Search results
│   └── TrailerActivity.kt       # Video player
├── fragments/
│   ├── LoginFragment.kt         # Login screen
│   └── SignupFragment.kt        # Registration screen
├── adapters/
│   ├── MovieAdapter.kt          # Movie list adapter
│   ├── ReviewAdapter.kt         # Reviews adapter
│   └── WatchlistAdapter.kt      # Watchlist adapter
├── models/
│   ├── Movie.kt                 # Movie data model
│   ├── Review.kt                # Review data model
│   └── Video.kt                 # Video data model
├── services/
│   ├── ApiService.kt            # TMDB API interface
│   ├── ApiClient.kt             # API client configuration
│   └── NotificationService.kt   # Push notifications
└── utils/
    └── NotificationUtils.kt     # Notification utilities
```

## 🔧 Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Minimum SDK 24 (Android 7.0)
- Firebase project with Authentication and Firestore enabled

### Environment Setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd popflix2
   ```

2. **Configure API Keys**
   - Copy `.env.example` to `.env` in the `app/` directory
   - Add your TMDB API key:
     ```
     TMDB_API_KEY="your_tmdb_api_key_here"
     ```

3. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication with Email/Password and Google Sign-In
   - Enable Firestore Database
   - Download `google-services.json` and place in `app/` directory

4. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## 🔑 API Configuration

### TMDB API Setup
1. Get your API key from [TMDB](https://www.themoviedb.org/settings/api)
2. Add the key to `app/.env`:
   ```
   TMDB_API_KEY="your_api_key_here"
   ```

### Firebase Configuration
- Ensure `google-services.json` is properly configured
- Enable Google Sign-In in Firebase Authentication
- Configure Firestore security rules for user data

## 🎯 Key Features Deep Dive

### Authentication System
- **Email/Password Registration**: Secure user registration with Firebase Auth
- **Google Sign-In**: One-tap authentication with Google accounts
- **Profile Management**: User profile display and management
- **Session Persistence**: Automatic login state management

### Movie Data Management
- **Real-time Data**: Live movie data from TMDB API
- **Caching Strategy**: Efficient data caching for offline access
- **Image Optimization**: Optimized image loading with Glide
- **Error Handling**: Robust error handling for API failures

### User Experience Features
- **Smooth Animations**: Material Design animations and transitions
- **Accessibility**: Screen reader and accessibility support
- **Performance**: Optimized for smooth scrolling and fast loading
- **Battery Efficient**: Smart notification scheduling

## 🚨 Known Issues

### Current Issues (v1.0)
The following issues have been identified and are scheduled for resolution in upcoming releases:

1. **Google Sign-In Failures** ❌
   - **Issue**: Google Sign-In process fails intermittently
   - **Impact**: Users cannot authenticate with Google accounts
   - **Workaround**: Use email/password authentication as alternative
   - **Priority**: High

2. **Watchlist Crash** ❌
   - **Issue**: Application crashes when accessing the watchlist screen
   - **Impact**: Users cannot view or manage their watchlist
   - **Workaround**: Restart the application and avoid watchlist access
   - **Priority**: Critical

3. **Profile Data Persistence** ❌
   - **Issue**: User profile data is loaded locally and gets deleted on app restart
   - **Impact**: Users lose profile customizations and preferences
   - **Workaround**: Reconfigure profile settings after each app launch
   - **Priority**: High

4. **Profile Section Minor Bugs** ❌
   - **Issue**: Small UI/UX bugs in the user name/profile section
   - **Impact**: Minor visual inconsistencies in profile display
   - **Workaround**: None required - functionality is intact
   - **Priority**: Medium

### Reporting Issues
If you encounter any of these issues or discover new ones:
1. Check if it's already listed above
2. Create a detailed issue report in the repository
3. Include device information, Android version, and steps to reproduce
4. Attach screenshots if applicable

### Issue Status Legend
- ❌ **Open**: Issue identified but not resolved
- 🔄 **In Progress**: Actively being worked on
- ✅ **Resolved**: Issue has been fixed

## 📈 Performance Optimizations

- **Image Caching**: Glide automatic image caching
- **Network Optimization**: Retrofit with connection pooling
- **Memory Management**: Efficient RecyclerView adapters
- **Battery Optimization**: Smart notification scheduling

## 🔒 Security Features

- **API Key Protection**: Runtime API key loading from assets
- **Secure Authentication**: Firebase Authentication with secure token handling
- **Data Validation**: Input validation and sanitization
- **HTTPS Only**: All API calls use HTTPS

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [The Movie Database (TMDB)](https://www.themoviedb.org/) for movie data API
- [Firebase](https://firebase.google.com/) for authentication and database services
- [Glide](https://github.com/bumptech/glide) for image loading
- [Retrofit](https://github.com/square/retrofit) for API client

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team

---

**Built with ❤️ for movie lovers**

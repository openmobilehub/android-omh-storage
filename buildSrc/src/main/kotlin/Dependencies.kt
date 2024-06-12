object Libs {
    val reflection by lazy { "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}" }

    // KTX
    val coreKtx by lazy { "androidx.core:core-ktx:${Versions.coreKtx}" }
    val lifecycleKtx by lazy { "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleKtx}" }
    val viewModelKtx by lazy { "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleKtx}" }
    val activityKtx by lazy { "androidx.activity:activity-ktx:${Versions.activityKtx}" }
    val fragmentKtx by lazy { "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}" }

    // Retrofit
    val retrofit by lazy { "com.squareup.retrofit2:retrofit:${Versions.retrofit}" }
    val retrofitJacksonConverter by lazy { "com.squareup.retrofit2:converter-jackson:${Versions.retrofit}" }
    val okHttp by lazy { "com.squareup.okhttp3:okhttp:${Versions.okhttp}" }
    val okHttpLoggingInterceptor by lazy { "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}" }

    // Coroutines
    val coroutinesCore by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}" }
    val coroutinesAndroid by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}" }

    // GMS
    val googlePlayServicesAuth by lazy { "com.google.android.gms:play-services-auth:${Versions.googlePlayServicesAuth}" }
    val googleJacksonClient by lazy { "com.google.http-client:google-http-client-jackson:${Versions.googleJacksonClient}" }
    val googleAndroidApiClient by lazy { "com.google.api-client:google-api-client-android:${Versions.googleAndroidApiClient}" }
    val googleDrive by lazy { "com.google.apis:google-api-services-drive:${Versions.googleDriveServices}" }
    val avoidGuavaConflict by lazy { "com.google.guava:listenablefuture:${Versions.avoidGuavaConflict}" }
    val guava by lazy { "com.google.guava:guava:${Versions.guava}" }

    // Android
    val androidAppCompat by lazy { "androidx.appcompat:appcompat:${Versions.androidAppCompat}" }
    val material by lazy { "com.google.android.material:material:${Versions.material}" }
    val constraintlayout by lazy { "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}" }

    // Navigation
    val navigationFragmentKtx by lazy { "androidx.navigation:navigation-fragment-ktx:${Versions.navigationFragment}" }
    val navigationUIKtx by lazy { "androidx.navigation:navigation-ui-ktx:${Versions.navigationUIKtx}" }

    // Testing
    val junit by lazy { "junit:junit:${Versions.junit}" }
    val androidJunit by lazy { "androidx.test.ext:junit:${Versions.androidJunit}" }
    val mockk by lazy { "io.mockk:mockk:${Versions.mockk}" }
    val coroutineTesting by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}" }

    // Auth
    val omhCoreAuthLibrary by lazy { "com.openmobilehub.android.auth:core:${Versions.omhAuth}" }
    val omhGoogleNonGmsAuthLibrary by lazy { "com.openmobilehub.android.auth:plugin-google-non-gms:${Versions.omhAuth}" }
    val omhGoogleGmsAuthLibrary by lazy { "com.openmobilehub.android.auth:plugin-google-gms:${Versions.omhAuth}" }
    val omhDropboxAuthLibrary by lazy { "com.openmobilehub.android.auth:plugin-dropbox:${Versions.omhAuth}" }
    val omhMicrosoftAuthLibrary by lazy { "com.openmobilehub.android.auth:plugin-microsoft:${Versions.omhAuth}" }

    // Play services
    val googlePlayBase by lazy { "com.google.android.gms:play-services-base:${Versions.googlePlayBase}" }

    // MsGraph
    val msGraph by lazy { "com.microsoft.graph:microsoft-graph:${Versions.msGraph}" }

    // Splash
    val splash by lazy { "androidx.core:core-splashscreen:${Versions.splash}" }

    // Hilt
    val hiltAndroid by lazy { "com.google.dagger:hilt-android:${Versions.hilt}" }
    val hiltCompiler by lazy { "com.google.dagger:hilt-compiler:${Versions.hilt}" }

    // Glide
    val glide by lazy { "com.github.bumptech.glide:glide:${Versions.glide}" }
    val glideCompiler by lazy { "com.github.bumptech.glide:compiler:${Versions.glide}" }

    // Json
    val json by lazy { "org.json:json:${Versions.json}" }

    // Gson
    val httpClientGson by lazy { "com.google.http-client:google-http-client-gson:${Versions.httpClientGson}" }

    // Datastore
    val dataStore by lazy { "androidx.datastore:datastore-preferences:${Versions.dataStore}" }
}

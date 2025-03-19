@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOpenRouterApi(
        @ApplicationContext context: Context,
        apiKeyProvider: ApiKeyProvider
    ): OpenRouterApi {
        return OpenRouterApi(apiKeyProvider)
    }

    @Provides
    @Singleton
    fun provideApiKeyProvider(
        @ApplicationContext context: Context
    ): ApiKeyProvider {
        return ApiKeyProvider(context)
    }
}

@Singleton
class ApiKeyProvider @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore
    
    val apiKey: Flow<String?> = dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    suspend fun getApiKey(): String? {
        return apiKey.first()
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
}

package com.example.openrouterchat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000<\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001c\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u000e0\u0010H\u0007\u001a\u0018\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\u0014H\u0007\u001a\b\u0010\u0015\u001a\u00020\u000eH\u0007\u001a\u0010\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0017\u001a\u00020\u0018H\u0007\u001a\b\u0010\u0019\u001a\u00020\u000eH\u0007\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\"%\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006*\u00020\b8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006\u001a"}, d2 = {"API_KEY", "Landroidx/datastore/preferences/core/Preferences$Key;", "", "getAPI_KEY", "()Landroidx/datastore/preferences/core/Preferences$Key;", "dataStore", "Landroidx/datastore/core/DataStore;", "Landroidx/datastore/preferences/core/Preferences;", "Landroid/content/Context;", "getDataStore", "(Landroid/content/Context;)Landroidx/datastore/core/DataStore;", "dataStore$delegate", "Lkotlin/properties/ReadOnlyProperty;", "ApiKeyScreen", "", "onApiKeyEntered", "Lkotlin/Function1;", "ChatScreen", "apiKey", "db", "Lcom/example/openrouterchat/AppDatabase;", "DefaultPreview", "MessageBubble", "message", "Lcom/example/openrouterchat/Message;", "OpenRouterChatApp", "app_release"})
public final class MainActivityKt {
    @org.jetbrains.annotations.NotNull
    private static final kotlin.properties.ReadOnlyProperty dataStore$delegate = null;
    @org.jetbrains.annotations.NotNull
    private static final androidx.datastore.preferences.core.Preferences.Key<java.lang.String> API_KEY = null;
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> getDataStore(@org.jetbrains.annotations.NotNull
    android.content.Context $this$dataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.datastore.preferences.core.Preferences.Key<java.lang.String> getAPI_KEY() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void OpenRouterChatApp() {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ApiKeyScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onApiKeyEntered) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ChatScreen(@org.jetbrains.annotations.NotNull
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull
    com.example.openrouterchat.AppDatabase db) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void MessageBubble(@org.jetbrains.annotations.NotNull
    com.example.openrouterchat.Message message) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable
    public static final void DefaultPreview() {
    }
}
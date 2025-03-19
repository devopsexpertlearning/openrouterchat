package com.example.openrouterchat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\fJ!\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0003H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0012"}, d2 = {"Lcom/example/openrouterchat/OpenRouterApi;", "", "apiKey", "", "(Ljava/lang/String;)V", "client", "Lio/ktor/client/HttpClient;", "close", "", "getModels", "", "Lcom/example/openrouterchat/Model;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendMessage", "Lcom/example/openrouterchat/ChatCompletionResponse;", "model", "message", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class OpenRouterApi {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apiKey = null;
    @org.jetbrains.annotations.NotNull
    private final io.ktor.client.HttpClient client = null;
    
    public OpenRouterApi(@org.jetbrains.annotations.NotNull
    java.lang.String apiKey) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getModels(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.openrouterchat.Model>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendMessage(@org.jetbrains.annotations.NotNull
    java.lang.String model, @org.jetbrains.annotations.NotNull
    java.lang.String message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.openrouterchat.ChatCompletionResponse> $completion) {
        return null;
    }
    
    public final void close() {
    }
}
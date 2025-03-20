# Use Gitpod's full workspace image as base
FROM gitpod/workspace-full

# Install OpenJDK 17
RUN sudo apt update && sudo apt install -y openjdk-17-jdk

# Set Java 17 as the default
RUN sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-17-openjdk-amd64/bin/java 1 && \
    sudo update-alternatives --config java

# Install dependencies
RUN sudo apt update && sudo apt install -y unzip curl

# Install Android Command Line Tools
RUN mkdir -p /opt/android-sdk/cmdline-tools && cd /opt/android-sdk/cmdline-tools && \
    curl -o commandlinetools-linux.zip https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip && \
    unzip commandlinetools-linux.zip && rm commandlinetools-linux.zip && \
    mv cmdline-tools latest

# Set environment variables for Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Accept licenses and install platform tools & Android 33 SDK
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"

# Ensure correct permissions
RUN sudo chown -R gitpod:gitpod /opt/android-sdk

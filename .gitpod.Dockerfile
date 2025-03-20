# Use Gitpod's full workspace image as base
FROM gitpod/workspace-full

# Remove Java 11 if installed
RUN sudo apt remove -y openjdk-11-* && sudo apt autoremove -y

# Install OpenJDK 17
RUN sudo apt update && sudo apt install -y openjdk-17-jdk

# Ensure Java 17 is the default
RUN sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-17-openjdk-amd64/bin/java 1 && \
    sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java

# Verify Java version
RUN java -version

# Install dependencies
RUN sudo apt install -y unzip curl

# Create Android SDK directory with correct permissions
RUN sudo mkdir -p /opt/android-sdk && sudo chown -R gitpod:gitpod /opt/android-sdk

# Switch to non-root user (gitpod) and install Android Command Line Tools
USER gitpod
RUN mkdir -p /opt/android-sdk/cmdline-tools && cd /opt/android-sdk/cmdline-tools && \
    curl -o commandlinetools-linux.zip https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip && \
    unzip commandlinetools-linux.zip && rm commandlinetools-linux.zip && \
    mv cmdline-tools latest

# Set environment variables for Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Install Android SDK API 33 components
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"
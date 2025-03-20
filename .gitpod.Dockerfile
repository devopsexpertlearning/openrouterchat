# Use Gitpod's full workspace image as base
FROM gitpod/workspace-full

# Remove all existing Java versions (including SDKMAN! Java)
RUN sudo apt remove --purge -y openjdk-* && sudo apt autoremove -y && rm -rf /home/gitpod/.sdkman/candidates/java /home/gitpod/.sdkman

# Update package list
RUN sudo apt update

# Install OpenJDK 17
RUN sudo apt install -y openjdk-17-jdk

# Set Java 17 as default globally in all shell profiles
RUN echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" | sudo tee -a /etc/profile /etc/bash.bashrc /home/gitpod/.bashrc /home/gitpod/.zshrc /home/gitpod/.profile /home/gitpod/.bash_profile /home/gitpod/.bashrc.d/custom.sh /home/gitpod/.zlogin
RUN echo "export PATH=\$JAVA_HOME/bin:\$PATH" | sudo tee -a /etc/profile /etc/bash.bashrc /home/gitpod/.bashrc /home/gitpod/.zshrc /home/gitpod/.profile /home/gitpod/.bash_profile /home/gitpod/.bashrc.d/custom.sh /home/gitpod/.zlogin

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
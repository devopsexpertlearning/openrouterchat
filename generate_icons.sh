#!/bin/bash

# Create directories if they don't exist
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi

# Generate colored rectangles for different resolutions
convert -size 48x48 xc:#6650a4 app/src/main/res/mipmap-mdpi/ic_launcher.png
convert -size 48x48 xc:#6650a4 app/src/main/res/mipmap-mdpi/ic_launcher_round.png

convert -size 72x72 xc:#6650a4 app/src/main/res/mipmap-hdpi/ic_launcher.png
convert -size 72x72 xc:#6650a4 app/src/main/res/mipmap-hdpi/ic_launcher_round.png

convert -size 96x96 xc:#6650a4 app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert -size 96x96 xc:#6650a4 app/src/main/res/mipmap-xhdpi/ic_launcher_round.png

convert -size 144x144 xc:#6650a4 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert -size 144x144 xc:#6650a4 app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png

convert -size 192x192 xc:#6650a4 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
convert -size 192x192 xc:#6650a4 app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png

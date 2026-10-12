#!/bin/bash

APPIMAGE_PATH=$1
ARCH=$(uname -m)

case $ARCH in
	x86_64|amd64)
		curl -L https://github.com/ingStudiosOfficial/turtlebrowse/releases/latest/download/turtlebrowse_linux_amd64.AppImage -o $APPIMAGE_PATH
		;;
	aarch64)
		curl -L https://github.com/ingStudiosOfficial/turtlebrowse/releases/latest/download/turtlebrowse_linux_arm64.AppImage -o $APPIMAGE_PATH
		;;
	*)
		echo "Unsupported architecture: $ARCH"
		exit 1
		;;
esac

chmod +x $APPIMAGE_PATH
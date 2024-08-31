#!/bin/zsh

echo "Before cleanup"
df -h
echo ""
echo ""

rm -rf /tmp/* || true


curl -O https://raw.githubusercontent.com/dotnet/sdk/main/scripts/obtain/uninstall/dotnet-uninstall-pkgs.sh || true
chmod u+x dotnet-uninstall-pkgs.sh || true
sudo ./dotnet-uninstall-pkgs.sh || true

sudo rm -rf /usr/local/share/chromedriver-mac-x64 || true
sudo rm -rf "$CHROMEWEBDRIVER" || true
sudo rm -rf /usr/local/share/edge_driver || true
sudo rm -rf /usr/local/opt/geckodriver || true

sudo rm -rf /Applications/Xcode_15.1.app || true
# macos-13 (x86_64) default is Xcode 15.2 so keep it
sudo rm -rf /Applications/Xcode_15.3.app || true
# macos-14 (aarch64) default is Xcode 15.4 so keep it
sudo rm -rf /Applications/Xcode_15.0.1.app || true
sudo rm -rf /Applications/Xcode_14.3.1.app || true
sudo rm -rf /Applications/Xcode_16_beta_6.app || true
sudo rm -rf /Applications/Xcode_16.1_beta.app || true

# https://github.com/actions/runner-images/blob/main/images/macos/macos-14-arm64-Readme.md

sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/AppleTVOS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/AppleTVSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/WatchOS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/WatchSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/XROS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/XRSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/VisionOS.platform/Developer/SDKs/ || true

# Building Anitorrent requires Xcode, and it requires a macOS SDK 
ls /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/13.3 || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/14.0 || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/14.2 || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/14.4 || true

ls /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS16.4.sdk || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS17.0.sdk || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS17.2.sdk || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS17.4.sdk || true

rm -rf "$JAVA_HOME_11_arm64" || true
rm -rf "$JAVA_HOME_17_arm64" || true
rm -rf "$JAVA_HOME_21_arm64" || true

echo ""
echo ""

echo "After cleanup"
df -h

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
sudo rm -rf /Applications/Xcode_15.2.app || true
sudo rm -rf /Applications/Xcode_15.3.app || true
sudo rm -rf /Applications/Xcode_15.0.1.app || true
sudo rm -rf /Applications/Xcode_14.3.1.app || true
sudo rm -rf /Applications/Xcode_16_beta_6.app || true
sudo rm -rf /Applications/Xcode_16.1_beta.app || true


sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/AppleTVOS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/AppleTVSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/WatchOS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/WatchSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/XROS.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/XRSimulator.platform/Developer/SDKs/ || true
sudo rm -rf /Applications/Xcode.app/Contents/Developer/Platforms/VisionOS.platform/Developer/SDKs/ || true

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

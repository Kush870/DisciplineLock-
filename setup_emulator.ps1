$ErrorActionPreference = "Stop"

$sdkPath = Join-Path (Get-Location) "android-sdk"
$sdkManager = Join-Path $sdkPath "cmdline-tools\latest\bin\sdkmanager.bat"
$avdManager = Join-Path $sdkPath "cmdline-tools\latest\bin\avdmanager.bat"

Write-Host "Accepting licenses..."
$yesStr = "y`n" * 20
$yesStr | & $sdkManager --licenses | Out-Null

Write-Host "Installing Emulator and System Image (This may take a while, ~1.5GB download)..."
& $sdkManager "emulator" "system-images;android-34;google_apis;x86_64" | Out-Null

Write-Host "Creating Android Virtual Device (AVD)..."
# The 'echo no' is to answer 'no' to creating a custom hardware profile
"no`n" | & $avdManager create avd -n "DiscLockEmu" -k "system-images;android-34;google_apis;x86_64" --device "pixel" --force

Write-Host "Emulator setup complete! To start the emulator, run:"
Write-Host ".\android-sdk\emulator\emulator.exe -avd DiscLockEmu"

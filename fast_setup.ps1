$ErrorActionPreference = "Stop"
$ProgressPreference = 'SilentlyContinue'

$sdkPath = Join-Path (Get-Location) "android-sdk"
if (-Not (Test-Path $sdkPath)) { New-Item -ItemType Directory -Path $sdkPath | Out-Null }

$cmdlineToolsPath = Join-Path $sdkPath "cmdline-tools"
if (-Not (Test-Path $cmdlineToolsPath)) { New-Item -ItemType Directory -Path $cmdlineToolsPath | Out-Null }

$zipPath = Join-Path (Get-Location) "cmdline-tools.zip"

if (-Not (Test-Path (Join-Path $cmdlineToolsPath "latest"))) {
    Write-Host "Downloading Android Command Line Tools..."
    Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" -OutFile $zipPath

    Write-Host "Extracting Command Line Tools using tar..."
    # using tar.exe which is built into modern windows and much faster
    tar -xf $zipPath -C $cmdlineToolsPath

    Rename-Item -Path (Join-Path $cmdlineToolsPath "cmdline-tools") -NewName "latest"
    Remove-Item -Path $zipPath -Force
}

Write-Host "Accepting licenses..."
$sdkManager = Join-Path $cmdlineToolsPath "latest\bin\sdkmanager.bat"

$yesStr = "y`n" * 20
$yesStr | & $sdkManager --licenses | Out-Null

Write-Host "Installing SDK..."
& $sdkManager "platforms;android-34" "build-tools;34.0.0" "platform-tools" | Out-Null

$localPropertiesPath = Join-Path (Get-Location) "local.properties"
$escapedSdkPath = $sdkPath -replace '\\', '\\'
Set-Content -Path $localPropertiesPath -Value "sdk.dir=$escapedSdkPath"

$gradlePath = Join-Path (Get-Location) "gradle-8.7"
if (-Not (Test-Path $gradlePath)) {
    $gradleZip = Join-Path (Get-Location) "gradle.zip"
    Write-Host "Downloading Gradle..."
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.7-bin.zip" -OutFile $gradleZip
    Write-Host "Extracting Gradle using tar..."
    tar -xf $gradleZip -C (Get-Location)
    Remove-Item -Path $gradleZip -Force
}

Write-Host "Android SDK and Gradle setup complete!"

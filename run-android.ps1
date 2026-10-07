[CmdletBinding()]
param(
    [switch]$Setup,
    [switch]$PrepareOnly,
    [switch]$EmulatorOnly,
    [string]$AvdName,
    [ValidateRange(60, 1200)][int]$BootTimeoutSeconds = 480
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$runtimeRoot = Join-Path $projectRoot '.local\android'
$managedSdk = Join-Path $runtimeRoot 'sdk'
$managedAvds = Join-Path $runtimeRoot 'avd'
$packageName = 'com.example.kunlikvazifalar'

function Invoke-Checked {
    param([string]$Program, [string[]]$Arguments)
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Program failed (exit $LASTEXITCODE)." }
}

function Get-AvdNames {
    param([string]$Emulator)
    if (Test-Path -LiteralPath $Emulator) {
        $names = @(& $Emulator -list-avds)
        if ($LASTEXITCODE -ne 0) { throw 'Could not list virtual devices.' }
        $names | Where-Object { $_ -match '^[A-Za-z0-9_.-]+$' }
    }
}

try {
    if ($PrepareOnly -and !$Setup) { throw 'Use -Setup -PrepareOnly together.' }
    $buildSdk = $env:ANDROID_HOME
    $propertiesPath = Join-Path $projectRoot 'local.properties'
    if (Test-Path -LiteralPath $propertiesPath) {
        $sdkLine = Get-Content -LiteralPath $propertiesPath | Where-Object { $_ -match '^sdk\.dir\s*=' } | Select-Object -First 1
        if ($sdkLine) {
            $buildSdk = ($sdkLine -replace '^sdk\.dir\s*=\s*', '').Replace('\:', ':').Replace('\\', '\')
        }
    }
    if (!$buildSdk) { $buildSdk = $env:ANDROID_SDK_ROOT }
    if (!$buildSdk) { $buildSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $adb = Join-Path $buildSdk 'platform-tools\adb.exe'
    if (!(Test-Path -LiteralPath $adb)) { throw "Android SDK platform-tools missing: $adb" }

    $emulatorSdk = $buildSdk
    if (Test-Path -LiteralPath (Join-Path $managedSdk 'emulator\emulator.exe')) { $emulatorSdk = $managedSdk }
    $emulator = Join-Path $emulatorSdk 'emulator\emulator.exe'
    if (Test-Path -LiteralPath $managedAvds) { $env:ANDROID_AVD_HOME = $managedAvds }
    $avds = @(Get-AvdNames $emulator)

    if ($Setup -and $avds.Count -eq 0) {
        # Use D: workspace storage; keep the existing build SDK and debug signing key.
        $androidCli = Join-Path $env:USERPROFILE '.android\bin\android-cli.exe'
        if (!(Test-Path -LiteralPath $androidCli)) {
            throw 'Android CLI missing. Install SDK emulator, command-line tools and one API 36 x86_64 image using SDK Manager; see ANDROID-DEVELOPMENT.md.'
        }
        $emulatorSdk = $managedSdk
        New-Item -ItemType Directory -Path $managedSdk, $managedAvds -Force | Out-Null
        $env:ANDROID_AVD_HOME = $managedAvds
        $licensePath = Join-Path $buildSdk 'licenses'
        if (Test-Path -LiteralPath $licensePath) {
            $targetLicenses = Join-Path $managedSdk 'licenses'
            New-Item -ItemType Directory -Path $targetLicenses -Force | Out-Null
            Get-ChildItem -LiteralPath $licensePath -File | Copy-Item -Destination $targetLicenses
        }
        foreach ($sdkPackage in @('platform-tools', 'emulator', 'cmdline-tools/latest', 'system-images/android-36/default/x86_64')) {
            Invoke-Checked $androidCli @("--sdk=$managedSdk", 'sdk', 'install', $sdkPackage)
        }
        $env:ANDROID_HOME = $emulatorSdk
        $env:ANDROID_SDK_ROOT = $emulatorSdk
        $avdManager = Join-Path $managedSdk 'cmdline-tools\latest\bin\avdmanager.bat'
        if (!$AvdName) { $AvdName = 'Kenzo_API_36' }
        if ($AvdName -notmatch '^[A-Za-z0-9_.-]+$') { throw 'Invalid AVD name.' }
        $avdPath = Join-Path $managedAvds "$AvdName.avd"
        'no' | & $avdManager create avd --name $AvdName --package 'system-images;android-36;default;x86_64' --device 'pixel_5' --path $avdPath
        if ($LASTEXITCODE -ne 0) { throw 'AVD creation failed. Existing AVDs are never overwritten.' }
        $configPath = Join-Path $avdPath 'config.ini'
        $configLines = @(Get-Content -LiteralPath $configPath)
        $deviceSettings = [ordered]@{
            'hw.ramSize' = '4096'; 'hw.cpu.ncore' = '4'
            'hw.lcd.width' = '1080'; 'hw.lcd.height' = '1920'; 'hw.lcd.density' = '420'
            'hw.gpu.enabled' = 'yes'; 'hw.gpu.mode' = 'auto'
            'disk.dataPartition.size' = '4G'; 'showDeviceFrame' = 'no'
        }
        foreach ($setting in $deviceSettings.Keys) {
            $pattern = '^' + [regex]::Escape($setting) + '\s*='
            $configLines = @($configLines | Where-Object { $_ -notmatch $pattern })
            $configLines += "$setting=$($deviceSettings[$setting])"
        }
        Set-Content -LiteralPath $configPath -Value $configLines -Encoding ASCII
        $emulator = Join-Path $emulatorSdk 'emulator\emulator.exe'
        $avds = @(Get-AvdNames $emulator)
    }

    if (!(Test-Path -LiteralPath $emulator)) { throw 'Android Emulator missing. Run .\run-android.ps1 -Setup after enabling Windows Hypervisor Platform.' }
    if ($avds.Count -eq 0) { throw 'No AVD found. Run .\run-android.ps1 -Setup to create one device.' }
    if (!$AvdName) {
        if ($avds -contains 'Kenzo_API_36') { $AvdName = 'Kenzo_API_36' }
        elseif ($avds.Count -eq 1) { $AvdName = $avds[0] }
        else { throw "Choose one existing device with -AvdName. Available: $($avds -join ', ')" }
    }
    if ($avds -notcontains $AvdName) { throw "AVD '$AvdName' not found. Available: $($avds -join ', ')" }
    if ($PrepareOnly) {
        Write-Host "Prepared one AVD: $AvdName. Run .\run-android.ps1 after Windows Hypervisor Platform is ready."
        exit 0
    }

    $env:ANDROID_HOME = $emulatorSdk
    $env:ANDROID_SDK_ROOT = $emulatorSdk
    Invoke-Checked $adb @('start-server')
    function Find-TargetSerial {
        $deviceLines = @(& $adb devices)
        foreach ($deviceLine in $deviceLines) {
            if ($deviceLine -match '^(emulator-\d+)\s+device\s*$') {
                $candidateSerial = $Matches[1]
                $runningName = @(& $adb -s $candidateSerial emu avd name 2>$null)
                if ($runningName -contains $AvdName) { return $candidateSerial }
            }
        }
    }
    $serial = Find-TargetSerial
    $emulatorProcess = $null
    New-Item -ItemType Directory -Path $runtimeRoot -Force | Out-Null
    if (!$serial) {
        Invoke-Checked $emulator @('-accel-check')
        # Avoid a duplicate process if this AVD is still booting or temporarily offline.
        $alreadyStarting = Get-CimInstance Win32_Process -Filter "Name = 'emulator.exe' OR Name = 'qemu-system-x86_64.exe'" |
            Where-Object { $_.CommandLine -match ('(?:-avd\s+|@)' + [regex]::Escape($AvdName) + '(?:\s|$)') }
        if (!$alreadyStarting) {
            # The user requested a visible interactive emulator window.
            $emulatorProcess = Start-Process -FilePath $emulator -ArgumentList @('-avd', $AvdName, '-memory', '4096', '-cores', '4', '-gpu', 'auto', '-no-boot-anim') -WindowStyle Normal -PassThru `
                -RedirectStandardOutput (Join-Path $runtimeRoot 'emulator.stdout.log') `
                -RedirectStandardError (Join-Path $runtimeRoot 'emulator.stderr.log')
        }
        Write-Host "Waiting for $AvdName (up to $BootTimeoutSeconds seconds)..."
    }
    $deadline = [DateTime]::UtcNow.AddSeconds($BootTimeoutSeconds)
    $booted = $false
    do {
        $serial = Find-TargetSerial
        if ($serial) {
            $bootComplete = @(& $adb -s $serial shell getprop sys.boot_completed 2>$null)
            if ($bootComplete -contains '1') { $booted = $true; break }
        }
        if ($emulatorProcess -and $emulatorProcess.HasExited) {
            throw "Emulator exited. Check $runtimeRoot\emulator.stderr.log and Windows Hypervisor Platform; a Windows restart may be required."
        }
        Start-Sleep -Seconds 2
    } while ([DateTime]::UtcNow -lt $deadline)
    if (!$booted) { throw "Emulator boot timed out. Check $runtimeRoot\emulator.stderr.log. No app data was cleared." }
    Write-Host "Ready: $AvdName ($serial)"
    if ($EmulatorOnly) { exit 0 }

    Push-Location $projectRoot
    try {
        Invoke-Checked (Join-Path $projectRoot 'gradlew.bat') @(':app:assembleDebug', '--console=plain')
    } finally { Pop-Location }
    $apk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
    if (!(Test-Path -LiteralPath $apk)) { throw "Debug APK missing: $apk" }
    # Stop on signature/version conflicts; never uninstall or clear data to work around them.
    Invoke-Checked $adb @('-s', $serial, 'install', '-r', $apk)
    $launchResult = @(& $adb -s $serial shell am start -W -S -n "$packageName/.MainActivity" 2>&1)
    $launchExit = $LASTEXITCODE
    $launchResult | ForEach-Object { Write-Host $_ }
    if ($launchExit -ne 0 -or ($launchResult -join "`n") -match '(?m)^Error') { throw 'App launch failed.' }
    Write-Host 'Debug app updated using install -r and opened. App data was not cleared.'
} catch {
    Write-Error $_ -ErrorAction Continue
    exit 1
}

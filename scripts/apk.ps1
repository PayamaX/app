<#
.SYNOPSIS
    Install an APK onto a connected Android device.

.DESCRIPTION
    Pass an APK path as an argument to install that file.
    Pass a directory, or omit the path, to browse: each screen lists folders and APKs in the current directory.
    Lists devices from adb and asks which one to install to, unless -Serial is set.
    Reinstalls over an existing copy of the same app.

.PARAMETER ApkPath
    Full path to an .apk file, or a directory to start browsing from.
    When omitted, browsing starts in the current directory.

.PARAMETER Serial
    adb device serial. When omitted, you pick from the connected devices.

.EXAMPLE
    .\scripts\install-apk.ps1 -ApkPath "D:\Workspace\PayamaX\app-pax\out\release\app.apk"

.EXAMPLE
    .\scripts\install-apk.ps1 -ApkPath "D:\Workspace\PayamaX\app-pax\out"

.EXAMPLE
    .\scripts\install-apk.ps1
#>
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$ApkPath,

    [string]$Serial
)

$ErrorActionPreference = "Stop"

function Get-AdbDevices {
    $lines = & adb devices -l 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb devices failed: $($lines -join [Environment]::NewLine)"
    }

    $devices = @()
    foreach ($line in $lines) {
        $text = "$line".Trim()
        if (-not $text -or $text.StartsWith("List of devices")) {
            continue
        }

        $parts = $text -split '\s+'
        if ($parts.Count -lt 2) {
            continue
        }

        $model = $null
        $product = $null
        foreach ($part in $parts) {
            if ($part -like "model:*") {
                $model = $part.Substring(6)
            }
            elseif ($part -like "product:*") {
                $product = $part.Substring(8)
            }
        }

        $devices += [pscustomobject]@{
            Serial  = $parts[0]
            State   = $parts[1]
            Model   = $model
            Product = $product
        }
    }

    return $devices
}

function Select-ApkByBrowsing([string]$StartDir) {
    $current = (Resolve-Path -LiteralPath $StartDir).Path

    while ($true) {
        $parent = Split-Path -Parent $current
        $dirs = @(Get-ChildItem -LiteralPath $current -Directory | Sort-Object Name)
        $apks = @(Get-ChildItem -LiteralPath $current -File -Filter *.apk | Sort-Object Name)

        $entries = @()
        if ($parent -and $parent -ne $current) {
            $entries += [pscustomobject]@{ Kind = "up"; Name = ".."; Path = $parent }
        }
        foreach ($dir in $dirs) {
            $entries += [pscustomobject]@{ Kind = "dir"; Name = $dir.Name; Path = $dir.FullName }
        }
        foreach ($apk in $apks) {
            $entries += [pscustomobject]@{ Kind = "apk"; Name = $apk.Name; Path = $apk.FullName }
        }

        Write-Host ""
        Write-Host $current
        Write-Host ""

        if ($entries.Count -eq 0) {
            Write-Host "  (no folders or APKs)"
            throw "Nothing to open in $current"
        }

        for ($i = 0; $i -lt $entries.Count; $i++) {
            $entry = $entries[$i]
            $suffix = if ($entry.Kind -eq "apk") { "" } else { "/" }
            Write-Host ("  [{0}] {1}{2}" -f ($i + 1), $entry.Name, $suffix)
        }

        while ($true) {
            $answer = Read-Host "Folder or APK number"
            $index = 0
            if ([int]::TryParse($answer, [ref]$index) -and $index -ge 1 -and $index -le $entries.Count) {
                $chosen = $entries[$index - 1]
                if ($chosen.Kind -eq "apk") {
                    return $chosen.Path
                }

                $current = $chosen.Path
                break
            }

            Write-Host "Enter a number from 1 to $($entries.Count)."
        }
    }
}

function Resolve-ApkFile([string]$Path) {
    $clean = $Path.Trim().Trim('"').Trim("'")
    if (-not (Test-Path -LiteralPath $clean -PathType Leaf)) {
        throw "APK not found: $clean"
    }

    $resolved = (Resolve-Path -LiteralPath $clean).Path
    if ([System.IO.Path]::GetExtension($resolved) -ne ".apk") {
        throw "Not an APK file: $resolved"
    }

    return $resolved
}

function Select-Device($Devices) {
    $ready = @($Devices | Where-Object { $_.State -eq "device" })
    $blocked = @($Devices | Where-Object { $_.State -ne "device" })

    if ($blocked.Count -gt 0) {
        Write-Host "Not ready:"
        foreach ($device in $blocked) {
            $label = if ($device.Model) { $device.Model } else { $device.Serial }
            Write-Host ("  {0}  {1}  ({2})" -f $label, $device.State, $device.Serial)
        }
        Write-Host ""
    }

    if ($ready.Count -eq 0) {
        throw "No authorized device is connected. Accept the USB debugging prompt on the phone, then run this again."
    }

    Write-Host "Devices:"
    for ($i = 0; $i -lt $ready.Count; $i++) {
        $device = $ready[$i]
        $label = if ($device.Model) { $device.Model } else { $device.Serial }
        Write-Host ("  [{0}] {1}" -f ($i + 1), $label)
        Write-Host ("      {0}" -f $device.Serial)
    }

    while ($true) {
        $defaultHint = if ($ready.Count -eq 1) { " [1]" } else { "" }
        $answer = Read-Host "Install to device$defaultHint"
        if (-not $answer -and $ready.Count -eq 1) {
            return $ready[0]
        }

        $index = 0
        if ([int]::TryParse($answer, [ref]$index) -and $index -ge 1 -and $index -le $ready.Count) {
            return $ready[$index - 1]
        }

        Write-Host "Enter a number from 1 to $($ready.Count)."
    }
}

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw "adb was not found. Install Android platform-tools and add them to PATH."
}

if (-not $ApkPath) {
    $apk = Select-ApkByBrowsing (Get-Location).Path
}
else {
    $clean = $ApkPath.Trim().Trim('"').Trim("'")
    if (Test-Path -LiteralPath $clean -PathType Container) {
        $apk = Select-ApkByBrowsing $clean
    }
    else {
        $apk = Resolve-ApkFile $clean
    }
}
$devices = @(Get-AdbDevices)

if ($Serial) {
    $target = $devices | Where-Object { $_.Serial -eq $Serial -and $_.State -eq "device" } | Select-Object -First 1
    if (-not $target) {
        throw "Device '$Serial' is not connected and authorized. Run 'adb devices -l' to see serials."
    }
}
else {
    $target = Select-Device $devices
}

$label = if ($target.Model) { $target.Model } else { $target.Serial }
Write-Host ""
Write-Host "Installing"
Write-Host "  APK:    $apk"
Write-Host "  Device: $label ($($target.Serial))"
Write-Host ""

& adb -s $target.Serial install -r $apk
if ($LASTEXITCODE -ne 0) {
    throw "adb install failed with exit code $LASTEXITCODE"
}

Write-Host "Installed on $label."

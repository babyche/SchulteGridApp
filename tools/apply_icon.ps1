param(
    [string]$Source = "C:\Users\Administrator\Downloads\素材\舒尔特方格.png"
)
Add-Type -AssemblyName System.Drawing

$proj = Split-Path $PSScriptRoot -Parent
$res = Join-Path $proj "app\src\main\res"
$srcCopy = Join-Path $PSScriptRoot "icon_source.png"

$densities = @(
    @{ name = "mdpi"; size = 48 },
    @{ name = "hdpi"; size = 72 },
    @{ name = "xhdpi"; size = 96 },
    @{ name = "xxhdpi"; size = 144 },
    @{ name = "xxxhdpi"; size = 192 }
)

if (-not (Test-Path $Source)) { throw "source not found: $Source" }

Copy-Item -LiteralPath $Source -Destination $srcCopy -Force
$srcImg = New-Object System.Drawing.Bitmap($srcCopy)

foreach ($d in $densities) {
    $dir = Join-Path $res ("mipmap-" + $d.name)
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

    # square
    $bmp = New-Object System.Drawing.Bitmap($d.size, $d.size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.Clear([System.Drawing.Color]::Transparent)
    $g.DrawImage($srcImg, 0, 0, $d.size, $d.size)
    $g.Dispose()
    $bmp.Save((Join-Path $dir "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()

    # round
    $bmp = New-Object System.Drawing.Bitmap($d.size, $d.size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.Clear([System.Drawing.Color]::Transparent)
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $p.AddEllipse((New-Object System.Drawing.RectangleF(0, 0, $d.size, $d.size)))
    $g.SetClip($p)
    $g.DrawImage($srcImg, 0, 0, $d.size, $d.size)
    $g.Dispose()
    $bmp.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()

    Write-Output ("done " + $d.name)
}
$srcImg.Dispose()

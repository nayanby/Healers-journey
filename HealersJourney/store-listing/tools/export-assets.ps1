Add-Type -AssemblyName System.Drawing

$project = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$listing = Join-Path $project 'store-listing'
$source = Join-Path $listing 'source-art'
$assets = Join-Path $listing 'assets'
$res = Join-Path $project 'app/src/main/res'
New-Item -ItemType Directory -Force $assets, $source | Out-Null

$icon = [System.Drawing.Image]::FromFile((Join-Path $source 'pixel-crest.png'))
$hero = [System.Drawing.Image]::FromFile((Join-Path $source 'feature-art.png'))

function Save-Png($path, $width, $height, $paint, $pixelFormat = [System.Drawing.Imaging.PixelFormat]::Format32bppArgb) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height, $pixelFormat)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    & $paint $graphics $width $height
    $parent = Split-Path $path -Parent
    New-Item -ItemType Directory -Force $parent | Out-Null
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

$navy = [System.Drawing.Color]::FromArgb(255, 13, 36, 47)
$gold = [System.Drawing.Color]::FromArgb(255, 250, 212, 113)
$cream = [System.Drawing.Color]::FromArgb(255, 255, 244, 212)

$square = { param($g, $w, $h)
    $g.Clear($navy)
    $inset = [int]($w * 0.095)
    $size = $w - 2 * $inset
    $g.DrawImage($icon, $inset, $inset, $size, $size)
}
Save-Png (Join-Path $assets 'app-icon-512.png') 512 512 $square

$foreground = { param($g, $w, $h)
    $g.Clear([System.Drawing.Color]::Transparent)
    $size = [int]($w * 0.61)
    $inset = [int](($w - $size) / 2)
    $g.DrawImage($icon, $inset, $inset, $size, $size)
}
Save-Png (Join-Path $res 'drawable-nodpi/life_rpg_launcher_foreground.png') 432 432 $foreground

@{ mdpi = 48; hdpi = 72; xhdpi = 96; xxhdpi = 144; xxxhdpi = 192 }.GetEnumerator() | ForEach-Object {
    Save-Png (Join-Path $res "mipmap-$($_.Key)/ic_launcher.png") $_.Value $_.Value $square
}

$feature = { param($g, $w, $h)
    $g.Clear($navy)
    $sourceRatio = $hero.Width / $hero.Height
    $targetRatio = $w / $h
    if ($sourceRatio -gt $targetRatio) {
        $cropWidth = [int]($hero.Height * $targetRatio)
        $src = [System.Drawing.Rectangle]::new([int](($hero.Width - $cropWidth) / 2), 0, $cropWidth, $hero.Height)
    } else {
        $cropHeight = [int]($hero.Width / $targetRatio)
        $src = [System.Drawing.Rectangle]::new(0, [int](($hero.Height - $cropHeight) / 2), $hero.Width, $cropHeight)
    }
    $g.DrawImage($hero, [System.Drawing.Rectangle]::new(0,0,$w,$h), $src, [System.Drawing.GraphicsUnit]::Pixel)
    $font = [System.Drawing.Font]::new('Segoe UI', 80, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $subfont = [System.Drawing.Font]::new('Segoe UI', 26, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $goldBrush = [System.Drawing.SolidBrush]::new($gold)
    $creamBrush = [System.Drawing.SolidBrush]::new($cream)
    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(130,0,0,0))
    $g.DrawString('Life RPG', $font, $shadow, 68, 169)
    $g.DrawString('Life RPG', $font, $goldBrush, 64, 165)
    $g.DrawString('Turn your day into an adventure', $subfont, $creamBrush, 68, 278)
    $font.Dispose(); $subfont.Dispose(); $goldBrush.Dispose(); $creamBrush.Dispose(); $shadow.Dispose()
}
Save-Png (Join-Path $assets 'feature-graphic-1024x500.png') 1024 500 $feature ([System.Drawing.Imaging.PixelFormat]::Format24bppRgb)

$icon.Dispose(); $hero.Dispose()

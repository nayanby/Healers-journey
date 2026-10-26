$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$root = Join-Path $project 'design\badges'
$names = @('initiate','seeker','builder','keeper','routine','iron','achiever','legend')
$levels = @(1,5,10,20,30,50,75,100)
$materials = @('Wood','Copper','Silver','Gold sunburst','Emerald wreath','Silver wings','Royal crown','Celestial crest')
$canvas = [Drawing.Bitmap]::new(1280,760,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$graphics = [Drawing.Graphics]::FromImage($canvas)
$graphics.Clear([Drawing.Color]::FromArgb(17,24,39))
$graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.TextRenderingHint = [Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$font = [Drawing.Font]::new('Segoe UI',14,[Drawing.FontStyle]::Bold)
$caption = [Drawing.Font]::new('Segoe UI',11)
$format = [Drawing.StringFormat]::new()
$format.Alignment = [Drawing.StringAlignment]::Center
$dim = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(183,196,217))
for ($i=0; $i -lt $names.Count; $i++) {
    $x = ($i % 4) * 320
    $y = [Math]::Floor($i / 4) * 380
    $image = [Drawing.Image]::FromFile((Join-Path $project "app\src\main\res\drawable-nodpi\level_badge_$($names[$i]).png"))
    $graphics.DrawImage($image,[int]($x+24),[int]($y+8),272,272)
    $graphics.DrawString("Tier $($i+1) - Level $($levels[$i])",$font,[Drawing.Brushes]::White,[Drawing.RectangleF]::new($x,$y+288,320,30),$format)
    $graphics.DrawString($materials[$i],$caption,$dim,[Drawing.RectangleF]::new($x,$y+323,320,28),$format)
    $image.Dispose()
}
$canvas.Save((Join-Path $root 'progression.png'),[Drawing.Imaging.ImageFormat]::Png)
$dim.Dispose(); $format.Dispose(); $caption.Dispose(); $font.Dispose(); $graphics.Dispose(); $canvas.Dispose()
Write-Output 'Created eight-tier progression preview.'

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$source = Join-Path $project 'design\badges\masters'
$target = Join-Path $project 'app\src\main\res\drawable-nodpi'
$names = @('initiate','seeker','builder','keeper','routine','iron','achiever','legend')
$footprints = @(0.50,0.60,0.68,0.76,0.82,0.88,0.93,0.96)
$tier = 0
foreach ($name in $names) {
 $image = [Drawing.Image]::FromFile((Join-Path $source "level_$name.png"))
 # Measure alpha at export size, then crop only transparent padding. Keep the
 # original high-resolution pixels for the final resample, never recolor them.
 $probe = [Drawing.Bitmap]::new(192,192,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
 $pg = [Drawing.Graphics]::FromImage($probe)
 $pg.Clear([Drawing.Color]::Transparent)
 $pg.DrawImage($image,0,0,192,192)
 $left=192; $top=192; $right=-1; $bottom=-1
 for ($y=0; $y -lt 192; $y++) {
  for ($x=0; $x -lt 192; $x++) {
   if ($probe.GetPixel($x,$y).A -gt 24) {
    $left=[Math]::Min($left,$x); $top=[Math]::Min($top,$y)
    $right=[Math]::Max($right,$x); $bottom=[Math]::Max($bottom,$y)
   }
  }
 }
 if ($right -lt $left) { throw "Badge $name has no visible pixels." }
 $sx=[single]($left*$image.Width/192.0)
 $sy=[single]($top*$image.Height/192.0)
 $sw=[single](($right-$left+1)*$image.Width/192.0)
 $sh=[single](($bottom-$top+1)*$image.Height/192.0)
 $scale=192.0*$footprints[$tier]/[Math]::Max($sw,$sh)
 $dw=[single]($sw*$scale); $dh=[single]($sh*$scale)
 $bitmap = [Drawing.Bitmap]::new(192,192,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
 $g = [Drawing.Graphics]::FromImage($bitmap)
 $g.Clear([Drawing.Color]::Transparent)
 $g.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
 $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
 $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
 $dest = [Drawing.RectangleF]::new([single]((192-$dw)/2),[single]((192-$dh)/2),$dw,$dh)
 $src = [Drawing.RectangleF]::new($sx,$sy,$sw,$sh)
 $g.DrawImage($image,$dest,$src,[Drawing.GraphicsUnit]::Pixel)
 $bitmap.Save((Join-Path $target "level_badge_$name.png"),[Drawing.Imaging.ImageFormat]::Png)
 $g.Dispose(); $bitmap.Dispose(); $image.Dispose(); $pg.Dispose(); $probe.Dispose()
 $tier++
}
Write-Output 'Exported eight progressive transparent PNG badges.'

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$source = Join-Path $project 'design\icons\colorful'
$resources = Join-Path $project 'app\src\main\res\drawable-nodpi'
$prompts = Get-Content (Join-Path $project 'design\icons\color-prompts.json') -Raw | ConvertFrom-Json
foreach ($entry in $prompts.icons) {
 $name = $entry[0]
 $path = Join-Path $source "color_$name.png"
 if (!(Test-Path -LiteralPath $path)) { throw "Missing generated image: $name" }
}
# Keep full-resolution generated masters; export small transparent copies for Android.
foreach ($entry in $prompts.icons) {
 $name = $entry[0]
 $image = [Drawing.Image]::FromFile((Join-Path $source "color_$name.png"))
 $bitmap = [Drawing.Bitmap]::new(192,192,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
 $g = [Drawing.Graphics]::FromImage($bitmap)
 $g.Clear([Drawing.Color]::Transparent)
 $g.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
 $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
 $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
 $g.DrawImage($image,0,0,192,192)
 $bitmap.Save((Join-Path $resources "color_$name.png"),[Drawing.Imaging.ImageFormat]::Png)
 $g.Dispose(); $bitmap.Dispose(); $image.Dispose()
}
$base = Join-Path $project 'app\src\main\java\com\healersjourney\app\ui\components'
foreach ($file in @('Art.kt','AppIcon.kt')) {
 $path = Join-Path $base $file
 $text = [IO.File]::ReadAllText($path).Replace('R.drawable.rpg_', 'R.drawable.color_')
 if ($file -eq 'AppIcon.kt') {
  $text = $text.Replace('theme tint preserves contrast', 'original artwork colors are preserved')
  $text = $text.Replace('tint: Color = MaterialTheme.colorScheme.onSurface', 'tint: Color = androidx.compose.material3.LocalContentColor.current')
  $text = $text.Replace('        }, tint = tint)', '        }.then(Modifier.graphicsLayer { alpha = tint.alpha }), tint = Color.Unspecified)')
  $text = $text.Replace('"EmojiEvents" -> R.drawable.color_achievement', '"Flag" -> R.drawable.color_milestone'+"`n"+'        "EmojiEvents" -> R.drawable.color_achievement')
 }
 [IO.File]::WriteAllText($path,$text)
}
$path = Join-Path $base 'Common.kt'
$text = [IO.File]::ReadAllText($path).Replace('modifier = Modifier.size(AppLayout.icon), tint = MaterialTheme.colorScheme.primary)', 'modifier = Modifier.size(AppLayout.icon), tint = Color.Unspecified)')
[IO.File]::WriteAllText($path,$text)
$path = Join-Path $project 'app\src\main\AndroidManifest.xml'
$text = [IO.File]::ReadAllText($path).Replace('@drawable/rpg_launcher','@drawable/color_xp')
[IO.File]::WriteAllText($path,$text)
$path = Join-Path $project 'app\src\main\java\com\healersjourney\app\MainActivity.kt'
$text = [IO.File]::ReadAllText($path).Replace('import androidx.compose.material.icons.filled.Home', 'import androidx.compose.material.icons.filled.Flag'+"`n"+'import androidx.compose.material.icons.filled.Home')
$text = $text.Replace('Milestones("Milestones", Icons.Filled.EmojiEvents)', 'Milestones("Milestones", Icons.Filled.Flag)')
[IO.File]::WriteAllText($path,$text)
# Preview every master before shipping; never bake the preview background into assets.
$files = @(Get-ChildItem -LiteralPath $source -Filter 'color_*.png' | Sort-Object Name)
$sheet = [Drawing.Bitmap]::new(1280,([int][Math]::Ceiling($files.Count/8)*160))
$g = [Drawing.Graphics]::FromImage($sheet)
$g.Clear([Drawing.ColorTranslator]::FromHtml('#202123'))
$font = [Drawing.Font]::new('Segoe UI',10)
for ($i=0; $i -lt $files.Count; $i++) {
 $x=($i%8)*160; $y=[int][Math]::Floor($i/8)*160
 $im=[Drawing.Image]::FromFile($files[$i].FullName)
 $g.DrawImage($im,($x+24),($y+4),112,112)
 $g.DrawString($files[$i].BaseName.Replace('color_',''),$font,[Drawing.Brushes]::White,($x+6),($y+124))
 $im.Dispose()
}
$sheet.Save((Join-Path $project 'design\icons\colorful-catalog.png'),[Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $sheet.Dispose(); $font.Dispose()
Write-Output "Integrated $($files.Count) full-color PNG icons."

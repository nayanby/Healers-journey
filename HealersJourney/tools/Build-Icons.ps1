$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$output = Join-Path $project 'app\src\main\res\drawable-nodpi'
New-Item -ItemType Directory -Force $output | Out-Null
# Original 24-unit rounded-line artwork. L = polyline, C = circle, R = rounded rectangle.
$icons = [ordered]@{
 home = 'L 3,10 12,3 21,10;L 5,9 5,21 10,21 10,15 14,15 14,21 19,21 19,9'
 today = 'L 14,3 5,14 11,14 10,21 19,10 13,10 14,3'
 study = 'L 12,6 12,21;L 12,6 7,4 3,4 3,18 7,18 12,21 17,18 21,18 21,4 17,4 12,6;L 6,8 9,9;L 15,9 18,8'
 medicine = 'R 3,7,18,14;R 8,3,8,4;L 12,11 12,17;L 9,14 15,14'
 fitness = 'L 6,12 18,12;R 3,7,3,10;R 18,7,3,10;L 9,9 9,15;L 15,9 15,15'
 social = 'C 9,7,3;C 18,9,2;L 3,21 3,18 5,14 13,14 15,18 15,21;L 17,15 20,15 22,18 22,21'
 sleep = 'L 15,3 11,4 7,7 6,11 7,16 11,20 16,21 20,19 22,16 17,16 13,13 12,9 13,5 15,3;C 19,6,1'
 hobbies = 'C 12,12,9;C 8,8,1;C 14,6,1;C 18,11,1;L 5,16 9,16 11,20'
 finance = 'R 3,6,18,15;L 4,6 17,3 17,6;R 14,11,7,6;C 17,14,0.7'
 todo = 'R 4,4,16,17;L 8,3 16,3 16,6 8,6 8,3;L 7,12 9,14 13,10;L 7,18 16,18'
 care = 'R 7,8,11,13;L 12,8 12,4 18,4;L 9,3 15,3;L 10,14 15,14;L 12.5,11.5 12.5,16.5;C 21,8,1'
 milestone = 'L 12,3 12,21;L 12,4 21,4 18,8 21,12 12,12;L 6,21 18,21'
 achievement = 'R 7,3,10,11;L 7,6 3,6 3,10 7,13;L 17,6 21,6 21,10 17,13;L 12,14 12,20;L 8,21 16,21'
 logs = 'R 5,3,14,18;L 9,8 15,8;L 9,12 15,12;L 9,16 13,16'
 sync = 'L 4,8 7,4 13,3 18,6 20,10;L 20,4 20,10 14,10;L 20,16 17,20 11,21 6,18 4,14;L 4,20 4,14 10,14'
 menu = 'L 4,6 20,6;L 4,12 20,12;L 4,18 20,18'
 edit = 'L 4,16 4,21 9,21 21,9 16,4 4,16;L 13,7 18,12'
 add = 'L 12,4 12,20;L 4,12 20,12'
 delete = 'L 5,7 6,21 18,21 19,7;L 3,6 21,6;L 9,6 9,3 15,3 15,6;L 10,10 10,17;L 14,10 14,17'
 check = 'L 4,12 9,17 20,6'
 lock = 'R 5,10,14,11;L 8,10 8,6 10,3 14,3 16,6 16,10;L 12,14 12,17'
 left = 'L 15,5 8,12 15,19'
 right = 'L 9,5 16,12 9,19'
 up = 'L 5,15 12,8 19,15'
 down = 'L 5,9 12,16 19,9'
 back = 'L 11,5 4,12 11,19;L 4,12 21,12'
 forward = 'L 13,5 20,12 13,19;L 3,12 20,12'
 upload = 'L 5,16 3,14 3,10 6,7 9,7 12,3 16,4 18,8 21,10 21,14 19,16;L 12,21 12,10;L 8,14 12,10 16,14'
 download = 'L 5,16 3,14 3,10 6,7 9,7 12,3 16,4 18,8 21,10 21,14 19,16;L 12,10 12,21;L 8,17 12,21 16,17'
 logout = 'L 10,3 4,3 4,21 10,21;L 10,12 21,12;L 17,8 21,12 17,16'
 shower = 'L 5,21 5,7 7,3 11,3 14,6;L 10,9 16,5 19,9 13,13 10,9;L 13,16 12,18;L 17,14 16,16;L 20,12 19,14'
 haircut = 'C 6,17,3;C 18,17,3;L 8,15 18,3;L 16,15 6,3;C 12,11,0.7'
 shaving = 'L 6,5 19,5 19,10 6,10 6,5;L 10,10 10,13 14,13 14,21 10,21 10,13;L 8,7.5 17,7.5'
 manscaping = 'R 7,6,10,15;L 10,6 10,3 14,3 14,6;L 10,11 14,11;L 10,15 14,15'
 nails = 'L 8,10 8,6 10,3 13,3 15,6 15,11;L 5,10 5,16 8,21 16,21 19,16 19,11;R 9,5,5,6'
 xp = 'L 12,3 15,9 22,10 17,15 18,22 12,18 6,22 7,15 2,10 9,9 12,3'
 streak = 'L 12,2 13,9 17,6 20,12 20,16 17,21 8,21 4,17 4,12 8,7 8,13 12,2;L 11,14 9,18 12,20 15,18 13,15'
 week = 'R 3,5,18,16;L 3,10 21,10;L 7,3 7,7;L 17,3 17,7;L 7,15 10,18 17,12'
 badge_initiate = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;C 12,11,3'
 badge_seeker = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 8,13 12,7 16,13 8,13'
 badge_builder = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 8,14 8,9 16,9 16,14;L 12,9 12,14'
 badge_keeper = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 14,7 9,12 13,12 10,17'
 badge_routine = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;C 12,11,4;L 12,8 12,11 15,11'
 badge_iron = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 7,11 17,11;L 8,8 8,14;L 16,8 16,14'
 badge_achiever = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 8,11 11,14 17,8'
 badge_legend = 'L 12,3 21,7 19,16 12,22 5,16 3,7 12,3;L 7,9 9,14 15,14 17,9 14,11 12,7 10,11 7,9'
}
foreach ($name in $icons.Keys) {
 $bitmap = [Drawing.Bitmap]::new(192,192)
 $g = [Drawing.Graphics]::FromImage($bitmap)
 $g.Clear([Drawing.Color]::Transparent)
 $g.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
 $g.ScaleTransform(8,8)
 $pen = [Drawing.Pen]::new([Drawing.ColorTranslator]::FromHtml('#8FAEE0'),1.7)
 $pen.StartCap = $pen.EndCap = [Drawing.Drawing2D.LineCap]::Round
 $pen.LineJoin = [Drawing.Drawing2D.LineJoin]::Round
 foreach ($command in $icons[$name].Split(';')) {
  $type = $command.Substring(0,1)
  $data = $command.Substring(2)
  if ($type -eq 'L') {
   $points = [Drawing.PointF[]]@($data.Split(' ') | ForEach-Object { $xy=$_.Split(','); [Drawing.PointF]::new([single]$xy[0],[single]$xy[1]) })
   $g.DrawLines($pen,$points)
  } elseif ($type -eq 'C') {
   $v = @($data.Split(',') | ForEach-Object {[single]$_})
   $g.DrawEllipse($pen,($v[0]-$v[2]),($v[1]-$v[2]),(2*$v[2]),(2*$v[2]))
  } else {
   $v = @($data.Split(',') | ForEach-Object {[single]$_})
   $p = [Drawing.Drawing2D.GraphicsPath]::new()
   $r = [single][Math]::Min(2,[Math]::Min($v[2],$v[3])/2)
   $d = 2*$r
   $p.AddArc($v[0],$v[1],$d,$d,180,90)
   $p.AddArc(($v[0]+$v[2]-$d),$v[1],$d,$d,270,90)
   $p.AddArc(($v[0]+$v[2]-$d),($v[1]+$v[3]-$d),$d,$d,0,90)
   $p.AddArc($v[0],($v[1]+$v[3]-$d),$d,$d,90,90)
   $p.CloseFigure(); $g.DrawPath($pen,$p); $p.Dispose()
  }
 }
 $bitmap.Save((Join-Path $output "rpg_$name.png"),[Drawing.Imaging.ImageFormat]::Png)
 $pen.Dispose(); $g.Dispose(); $bitmap.Dispose()
}
$icons.Keys | Set-Content (Join-Path $project 'design\icons\manifest.txt')
$launcher = [Drawing.Bitmap]::new(512,512)
$g = [Drawing.Graphics]::FromImage($launcher)
$g.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([Drawing.ColorTranslator]::FromHtml('#202123'))
$pen = [Drawing.Pen]::new([Drawing.ColorTranslator]::FromHtml('#8FAEE0'),12)
$g.DrawEllipse($pen,64,64,384,384)
$star = [Drawing.Image]::FromFile((Join-Path $output 'rpg_xp.png'))
$g.DrawImage($star,128,112,256,256)
$star.Dispose(); $pen.Dispose(); $g.Dispose()
$launcher.Save((Join-Path $output 'rpg_launcher.png'),[Drawing.Imaging.ImageFormat]::Png)
$launcher.Dispose()
Write-Output "Rendered $($icons.Count) transparent 192px PNG icons and a 512px launcher."

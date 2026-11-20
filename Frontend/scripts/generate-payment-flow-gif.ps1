Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName WindowsBase

$ErrorActionPreference = "Stop"

$width = 1120
$height = 640
$outDir = Join-Path $PSScriptRoot "..\docs\assets"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$gifPath = Join-Path $outDir "streamguard-payment-flow.gif"

$paper = [System.Drawing.Color]::FromArgb(244, 240, 230)
$card = [System.Drawing.Color]::FromArgb(255, 253, 248)
$line = [System.Drawing.Color]::FromArgb(214, 204, 188)
$ink = [System.Drawing.Color]::FromArgb(28, 25, 21)
$muted = [System.Drawing.Color]::FromArgb(107, 100, 91)
$moss = [System.Drawing.Color]::FromArgb(31, 107, 69)
$mossDeep = [System.Drawing.Color]::FromArgb(20, 74, 48)
$soft = [System.Drawing.Color]::FromArgb(231, 243, 234)
$arrowIdle = [System.Drawing.Color]::FromArgb(196, 184, 168)

$titleFont = New-Object System.Drawing.Font "Georgia", 28, ([System.Drawing.FontStyle]::Regular)
$labelFont = New-Object System.Drawing.Font "Segoe UI", 13, ([System.Drawing.FontStyle]::Bold)
$subFont = New-Object System.Drawing.Font "Segoe UI", 10
$captionFont = New-Object System.Drawing.Font "Segoe UI", 15
$stepFont = New-Object System.Drawing.Font "Segoe UI", 11, ([System.Drawing.FontStyle]::Bold)

$nodes = @(
  @{ Id = 0; X = 36;  Y = 118; W = 196; H = 84; Title = "Customer UI"; Sub = "React" },
  @{ Id = 1; X = 268; Y = 118; W = 196; H = 84; Title = "API Gateway"; Sub = "port 8083" },
  @{ Id = 2; X = 500; Y = 118; W = 196; H = 84; Title = "Order Service"; Sub = "order created" },
  @{ Id = 3; X = 732; Y = 118; W = 196; H = 84; Title = "Payment Service"; Sub = "payment initiated" },
  @{ Id = 4; X = 888; Y = 248; W = 196; H = 84; Title = "Razorpay"; Sub = "Checkout" },
  @{ Id = 5; X = 888; Y = 392; W = 196; H = 84; Title = "Webhook"; Sub = "server to server" },
  @{ Id = 6; X = 656; Y = 392; W = 196; H = 84; Title = "Signature check"; Sub = "payment service" },
  @{ Id = 7; X = 424; Y = 392; W = 196; H = 84; Title = "DB + Outbox"; Sub = "one transaction" },
  @{ Id = 8; X = 232; Y = 392; W = 168; H = 84; Title = "Publisher"; Sub = "every 5 seconds" },
  @{ Id = 9; X = 36;  Y = 392; W = 168; H = 84; Title = "Kafka"; Sub = "payment-events" },
  @{ Id = 10; X = 36; Y = 248; W = 196; H = 84; Title = "Order consumer"; Sub = "duplicate safe" },
  @{ Id = 11; X = 268; Y = 248; W = 196; H = 84; Title = "Order confirmed"; Sub = "PAID or CANCELLED" }
)

$captions = @(
  "The customer books a product in the React UI.",
  "The UI calls the API gateway. It does not call a service port directly.",
  "Order service stores the order as PENDING.",
  "Payment service loads that order and creates the Razorpay order.",
  "The browser opens Checkout with the server-created order id and amount.",
  "Razorpay sends the webhook to the payment service. The browser does not.",
  "The payment service accepts the webhook only after the signature matches.",
  "Payment status and the outbox row commit in the same database transaction.",
  "The scheduled publisher sends pending outbox rows and waits for Kafka.",
  "PUBLISHED means Kafka acknowledged the record, not that the order changed.",
  "The order consumer applies the event once, then ignores the same payment id.",
  "The order becomes PAID or CANCELLED. The UI confirms only from that status."
)

function New-RoundPath([single]$x, [single]$y, [single]$w, [single]$h, [single]$r) {
  $path = New-Object System.Drawing.Drawing2D.GraphicsPath
  $d = $r * 2
  $path.AddArc($x, $y, $d, $d, 180, 90)
  $path.AddArc(($x + $w - $d), $y, $d, $d, 270, 90)
  $path.AddArc(($x + $w - $d), ($y + $h - $d), $d, $d, 0, 90)
  $path.AddArc($x, ($y + $h - $d), $d, $d, 90, 90)
  $path.CloseFigure()
  return $path
}

function Draw-Arrow($g, $x1, $y1, $x2, $y2, $color, $active) {
  $penWidth = 2
  if ($active) { $penWidth = 4 }
  $pen = New-Object System.Drawing.Pen $color, $penWidth
  $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
  $g.DrawLine($pen, $x1, $y1, $x2, $y2)
  $pen.Dispose()

  $angle = [Math]::Atan2(($y2 - $y1), ($x2 - $x1))
  $size = 9
  if ($active) { $size = 12 }
  $p1 = New-Object System.Drawing.PointF $x2, $y2
  $p2 = New-Object System.Drawing.PointF ([single]($x2 - $size * [Math]::Cos($angle - 0.45))), ([single]($y2 - $size * [Math]::Sin($angle - 0.45)))
  $p3 = New-Object System.Drawing.PointF ([single]($x2 - $size * [Math]::Cos($angle + 0.45))), ([single]($y2 - $size * [Math]::Sin($angle + 0.45)))
  $brush = New-Object System.Drawing.SolidBrush $color
  $g.FillPolygon($brush, @($p1, $p2, $p3))
  $brush.Dispose()
}

function Get-Center($node, [string]$edge) {
  $cx = $node.X + ($node.W / 2)
  $cy = $node.Y + ($node.H / 2)
  switch ($edge) {
    "right" { return @{ X = ($node.X + $node.W); Y = $cy } }
    "left" { return @{ X = $node.X; Y = $cy } }
    "bottom" { return @{ X = $cx; Y = ($node.Y + $node.H) } }
    "top" { return @{ X = $cx; Y = $node.Y } }
  }
}

function Draw-Frame([int]$active) {
  $bmp = New-Object System.Drawing.Bitmap $width, $height
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
  $g.Clear($paper)

  $header = New-Object System.Drawing.SolidBrush $ink
  $g.DrawString("StreamGuard", $titleFont, $header, 36, 28)
  $header.Dispose()
  $subBrush = New-Object System.Drawing.SolidBrush $muted
  $g.DrawString("Payment flow  ·  one step at a time", $subFont, $subBrush, 40, 72)
  $stepText = "Step $($active + 1) of 12"
  $stepSize = $g.MeasureString($stepText, $stepFont)
  $stepBrush = New-Object System.Drawing.SolidBrush $mossDeep
  $g.DrawString($stepText, $stepFont, $stepBrush, ($width - $stepSize.Width - 36), 40)
  $stepBrush.Dispose()

  $links = @(
    @{ From = 0; To = 1; A = "right"; B = "left" },
    @{ From = 1; To = 2; A = "right"; B = "left" },
    @{ From = 2; To = 3; A = "right"; B = "left" },
    @{ From = 3; To = 4; A = "bottom"; B = "top" },
    @{ From = 4; To = 5; A = "bottom"; B = "top" },
    @{ From = 5; To = 6; A = "left"; B = "right" },
    @{ From = 6; To = 7; A = "left"; B = "right" },
    @{ From = 7; To = 8; A = "left"; B = "right" },
    @{ From = 8; To = 9; A = "left"; B = "right" },
    @{ From = 9; To = 10; A = "top"; B = "bottom" },
    @{ From = 10; To = 11; A = "right"; B = "left" }
  )

  foreach ($link in $links) {
    $from = $nodes[$link.From]
    $to = $nodes[$link.To]
    $start = Get-Center $from $link.A
    $end = Get-Center $to $link.B
    $hot = ($link.To -eq $active)
    $color = $arrowIdle
    if ($hot) { $color = $moss }
    $pad = 8
    $dx = $end.X - $start.X
    $dy = $end.Y - $start.Y
    $len = [Math]::Sqrt($dx * $dx + $dy * $dy)
    if ($len -gt 0) {
      $start.X = $start.X + ($dx / $len) * $pad
      $start.Y = $start.Y + ($dy / $len) * $pad
      $end.X = $end.X - ($dx / $len) * ($pad + 4)
      $end.Y = $end.Y - ($dy / $len) * ($pad + 4)
    }
    Draw-Arrow $g $start.X $start.Y $end.X $end.Y $color $hot
  }

  foreach ($node in $nodes) {
    $done = $node.Id -lt $active
    $hot = $node.Id -eq $active
    $fill = $card
    $border = $line
    $borderWidth = 1.5
    if ($done) { $fill = $soft; $border = $moss }
    if ($hot) { $fill = $soft; $border = $moss; $borderWidth = 3.5 }
    $path = New-RoundPath $node.X $node.Y $node.W $node.H 16
    $brush = New-Object System.Drawing.SolidBrush $fill
    $pen = New-Object System.Drawing.Pen $border, $borderWidth
    $g.FillPath($brush, $path)
    $g.DrawPath($pen, $path)
    $brush.Dispose()
    $pen.Dispose()
    $path.Dispose()

    $titleBrush = New-Object System.Drawing.SolidBrush $ink
    $titleSize = $g.MeasureString($node.Title, $labelFont)
    $tx = $node.X + (($node.W - $titleSize.Width) / 2)
    $g.DrawString($node.Title, $labelFont, $titleBrush, $tx, ($node.Y + 22))
    $titleBrush.Dispose()

    $small = New-Object System.Drawing.SolidBrush $(if ($hot) { $mossDeep } else { $muted })
    $subSize = $g.MeasureString($node.Sub, $subFont)
    $sx = $node.X + (($node.W - $subSize.Width) / 2)
    $g.DrawString($node.Sub, $subFont, $small, $sx, ($node.Y + 48))
    $small.Dispose()
  }

  $bar = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 253, 248))
  $g.FillRectangle($bar, 36, 524, 1048, 78)
  $bar.Dispose()
  $barPen = New-Object System.Drawing.Pen $line, 1.5
  $g.DrawRectangle($barPen, 36, 524, 1048, 78)
  $barPen.Dispose()

  $capBrush = New-Object System.Drawing.SolidBrush $ink
  $g.DrawString($captions[$active], $captionFont, $capBrush, 56, 548)
  $capBrush.Dispose()
  $subBrush.Dispose()

  $g.Dispose()
  return $bmp
}

function Convert-ToBitmapSource($bmp) {
  $stream = New-Object System.IO.MemoryStream
  $bmp.Save($stream, [System.Drawing.Imaging.ImageFormat]::Png)
  $stream.Position = 0
  $decoder = New-Object System.Windows.Media.Imaging.PngBitmapDecoder $stream, ([System.Windows.Media.Imaging.BitmapCreateOptions]::PreservePixelFormat), ([System.Windows.Media.Imaging.BitmapCacheOption]::OnLoad)
  $frame = $decoder.Frames[0]
  $converted = New-Object System.Windows.Media.Imaging.FormatConvertedBitmap $frame, ([System.Windows.Media.PixelFormats]::Bgra32), $null, 0
  $converted.Freeze()
  $stream.Dispose()
  return $converted
}

$encoder = New-Object System.Windows.Media.Imaging.GifBitmapEncoder
for ($i = 0; $i -lt 12; $i++) {
  $bmp = Draw-Frame $i
  $targetW = 900
  $targetH = [int]($height * $targetW / $width)
  $small = New-Object System.Drawing.Bitmap $targetW, $targetH
  $scaled = [System.Drawing.Graphics]::FromImage($small)
  $scaled.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $scaled.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
  $scaled.DrawImage($bmp, 0, 0, $targetW, $targetH)
  $scaled.Dispose()
  $bmp.Dispose()
  $source = Convert-ToBitmapSource $small
  $small.Dispose()
  $meta = New-Object System.Windows.Media.Imaging.BitmapMetadata "gif"
  $delay = 120
  if ($i -eq 11) { $delay = 220 }
  $meta.SetQuery("/grctlext/Delay", [uint16]$delay)
  $meta.SetQuery("/grctlext/Disposal", [byte]2)
  if ($i -eq 0) {
    $app = [System.Text.Encoding]::ASCII.GetBytes("NETSCAPE2.0")
    $meta.SetQuery("/appext/Application", $app)
    $meta.SetQuery("/appext/Data", ([byte[]](0x01, 0x00, 0x00)))
  }
  $frame = [System.Windows.Media.Imaging.BitmapFrame]::Create($source, $null, $meta, $null)
  $encoder.Frames.Add($frame)
}

$fs = [System.IO.File]::Open($gifPath, [System.IO.FileMode]::Create)
$encoder.Save($fs)
$fs.Dispose()

$titleFont.Dispose()
$labelFont.Dispose()
$subFont.Dispose()
$captionFont.Dispose()
$stepFont.Dispose()

Write-Output $gifPath
Write-Output ((Get-Item $gifPath).Length)

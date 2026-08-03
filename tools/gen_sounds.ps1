# Generates the tap sound resources (raw/correct.wav, raw/wrong.wav).
# Run:  powershell -ExecutionPolicy Bypass -File tools\gen_sounds.ps1
param(
    [string]$OutDir = (Join-Path (Split-Path $PSScriptRoot -Parent) "app\src\main\res\raw")
)

$sr = 44100

function New-SineWav([string]$path, [double]$freq, [double]$dur, [double]$vol, [double]$fade) {
    $n = [int]($sr * $dur)
    $data = [byte[]]::new($n * 2)
    for ($i = 0; $i -lt $n; $i++) {
        $t = $i / $sr
        $attack = [Math]::Min(1.0, $t / 0.008)
        $release = [Math]::Min(1.0, (($dur - $t) / $fade))
        $env = $attack * $release
        $s = [Math]::Sin(2.0 * [Math]::PI * $freq * $t) * $vol * $env
        $s = [Math]::Max(-1.0, [Math]::Min(1.0, $s))
        $v = [int]($s * 32767.0)
        $data[$i * 2] = [byte]($v -band 0xFF)
        $data[$i * 2 + 1] = [byte](($v -shr 8) -band 0xFF)
    }
    $size = 36 + $n * 2
    $fs = [System.IO.File]::Create($path)
    $bw = [System.IO.BinaryWriter]::new($fs)
    try {
        $bw.Write([byte[]]("RIFF" -as [char[]]))
        $bw.Write([int]$size)
        $bw.Write([byte[]]("WAVE" -as [char[]]))
        $bw.Write([byte[]]("fmt " -as [char[]]))
        $bw.Write([int]16)
        $bw.Write([int16]1)
        $bw.Write([int16]1)
        $bw.Write([int]$sr)
        $bw.Write([int]($sr * 2))
        $bw.Write([int16]2)
        $bw.Write([int16]16)
        $bw.Write([byte[]]("data" -as [char[]]))
        $bw.Write([int]($n * 2))
        $bw.Write($data)
    }
    finally {
        $bw.Dispose()
        $fs.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
# Correct: pleasant 880Hz beep, short
New-SineWav (Join-Path $OutDir "correct.wav") 880 0.16 0.5 0.06
# Wrong: low 196Hz dull buzz, slightly longer
New-SineWav (Join-Path $OutDir "wrong.wav") 196 0.28 0.55 0.09
Write-Output "correct.wav: $((Get-Item (Join-Path $OutDir 'correct.wav')).Length) bytes"
Write-Output "wrong.wav:   $((Get-Item (Join-Path $OutDir 'wrong.wav')).Length) bytes"
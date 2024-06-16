# Obtener el directorio actual
$currentDir = Get-Location

# Crear un directorio de respaldo
$backupDir = Join-Path -Path $currentDir -ChildPath "backup_$(Get-Date -Format 'yyyyMMddHHmmss')"
New-Item -ItemType Directory -Path $backupDir

# Función para copiar archivos excluyendo el directorio de respaldo
function Copy-ItemExcludeBackup {
    param (
        [string]$sourcePath,
        [string]$destPath,
        [string]$backupDir
    )

    Get-ChildItem -Path $sourcePath -Recurse -File | Where-Object {
        $_.FullName -notmatch [regex]::Escape($backupDir)
    } | ForEach-Object {
        $dest = Join-Path -Path $destPath -ChildPath $_.FullName.Substring($sourcePath.Length)
        New-Item -ItemType Directory -Path (Split-Path -Path $dest) -Force
        Copy-Item -Path $_.FullName -Destination $dest
    }
}

# Copiar archivos .js y .kt al directorio de respaldo
Copy-ItemExcludeBackup -sourcePath $currentDir -destPath $backupDir -backupDir $backupDir

# Función para agregar comentario con nombre del archivo
function Add-Comment {
    param (
        [string]$filePath,
        [string]$fileName
    )

    $content = Get-Content $filePath -Raw

    # Verifica si el archivo ya tiene el comentario con el nombre del archivo
    if ($content -notmatch "^// $fileName") {
        $newContent = "// $fileName`n`n$content"
        Set-Content -Path $filePath -Value $newContent
        Write-Host "Comment added to $fileName"
    } else {
        Write-Host "Comment already exists in $fileName"
    }
}

# Procesar archivos .js y .kt excluyendo el directorio de respaldo
Get-ChildItem -Path $currentDir -Filter *.js -Recurse | Where-Object {
    $_.FullName -notmatch [regex]::Escape($backupDir)
} | ForEach-Object {
    $file = $_.FullName
    $fileName = $_.Name
    Add-Comment -filePath $file -fileName $fileName
}

Get-ChildItem -Path $currentDir -Filter *.kt -Recurse | Where-Object {
    $_.FullName -notmatch [regex]::Escape($backupDir)
} | ForEach-Object {
    $file = $_.FullName
    $fileName = $_.Name
    Add-Comment -filePath $file -fileName $fileName
}

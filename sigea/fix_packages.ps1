$root = "C:\Users\felip\Desktop\sigea\Plataforma-de-Ensino-On-line\sigea\dominio-principal\src\main\java"
$files = Get-ChildItem -Path $root -Recurse -Filter "*.java"

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    
    # 1. Fix Package Declaration
    # Calculate expected package from path relative to src/main/java
    $parts = $file.FullName -split "src\\main\\java\\"
    if ($parts.Length -lt 2) { continue }
    $relPath = $parts[1]
    $dirPath = [System.IO.Path]::GetDirectoryName($relPath)
    $expectedPackage = $dirPath -replace "\\", "."
    
    # Replace package declaration
    # Regex to find package statement. Case insensitive.
    $content = $content -replace "package\s+[\w\.]+;", "package $expectedPackage;"
    
    # 2. Fix Imports
    # Remove the intermediate domain packages
    $domains = "ambientecolaborativo", "compartilhado", "comunicacaogeral", "gestaoacademica", "identidadeacesso"
    foreach ($domain in $domains) {
        # Replace 'dev.com.sigea.dominio.DOMAIN.' with 'dev.com.sigea.dominio.'
        # Escape dots in regex
        $pattern = "dev\.com\.sigea\.dominio\.$domain\."
        $replacement = "dev.com.sigea.dominio."
        $content = $content -replace $pattern, $replacement
    }
    
    [System.IO.File]::WriteAllText($file.FullName, $content)
    Write-Host "Processed $($file.Name) -> Package: $expectedPackage"
}

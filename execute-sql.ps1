# Script PowerShell pour exécuter le SQL de test
# Utilisation: .\execute-sql.ps1

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "FibreOps - Script d'insertion de donnees de test" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Demander le mot de passe MySQL
$password = Read-Host "Entrez le mot de passe MySQL root" -AsSecureString
$plain_password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($password))

Write-Host ""
Write-Host "Connexion a MySQL..." -ForegroundColor Yellow

# Verifier que le fichier SQL existe
if (-not (Test-Path ".\insert-test-data.sql")) {
    Write-Host "[ERROR] Le fichier 'insert-test-data.sql' n'a pas ete trouve!" -ForegroundColor Red
    exit 1
}

# Executer le script SQL
Write-Host "Execution du script d'insertion..." -ForegroundColor Yellow

$sql_file = Get-Item ".\insert-test-data.sql"
$sql_content = Get-Content $sql_file -Raw

try {
    # Utiliser mysql.exe pour executer le script
    $mysql_cmd = "mysql -u root -p$plain_password -h localhost fibre_optique_db"
    
    # Ecrire le contenu dans un pipe
    $sql_content | & mysql -u root -p$plain_password -h localhost fibre_optique_db 2>&1 | Out-Host
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "[SUCCESS] Donnees de test inserees!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Credentials de test disponibles:" -ForegroundColor Cyan
        Write-Host "  Admin: admin@example.com" -ForegroundColor Cyan
        Write-Host "  Tech 1: tech1@example.com" -ForegroundColor Cyan
        Write-Host "  Tech 2: tech2@example.com" -ForegroundColor Cyan
        Write-Host "  Commercial 1: commercial1@example.com" -ForegroundColor Cyan
        Write-Host "  Client 1: client1@example.com" -ForegroundColor Cyan
        Write-Host "  Client 2: client2@example.com" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Mot de passe pour tous: test123" -ForegroundColor Yellow
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "[ERROR] Erreur lors de l'execution du script SQL" -ForegroundColor Red
        Write-Host "Code d'erreur: $LASTEXITCODE" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "[ERROR] Une erreur s'est produite:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""

@echo off
REM Script pour exécuter le SQL de test sur fibre_optique_db
REM Assurez-vous que MySQL est installé et accessible via le PATH

echo Execution du script d'insertion de donnees de test...
echo.

REM Remplacez "password" par votre mot de passe MySQL root
mysql -u root -ppassword -h localhost < "insert-test-data.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Donnees de test inserees avec succes!
    echo.
    echo Credentials de test:
    echo - admin@example.com / test123
    echo - tech1@example.com / test123
    echo - client1@example.com / test123
) else (
    echo.
    echo [ERROR] Erreur lors de l'execution du script SQL
    echo Verifiez:
    echo - MySQL est installe et running
    echo - Les credentials sont corrects
    echo - Le fichier insert-test-data.sql est dans le meme dossier
)

pause

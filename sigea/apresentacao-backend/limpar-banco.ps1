# Script para limpar o banco de dados H2
# Usar este script quando houver problemas com migrações do Flyway ou bloqueio do banco

Write-Host "Parando processos Java..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "*java*" } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "Limpando banco de dados H2..." -ForegroundColor Yellow

Remove-Item -Path "sigea.mv.db" -ErrorAction SilentlyContinue
Remove-Item -Path "sigea.trace.db" -ErrorAction SilentlyContinue
Remove-Item -Path "sigea.lock.db" -ErrorAction SilentlyContinue

Write-Host "Banco de dados limpo com sucesso!" -ForegroundColor Green
Write-Host "Agora você pode executar: mvn spring-boot:run" -ForegroundColor Cyan

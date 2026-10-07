# Konsol kodlamasini UTF-8 (Emoji ve Turkce karakter destegi) olarak ayarlar
[console]::InputEncoding = [console]::OutputEncoding = New-Object System.Text.UTF8Encoding
chcp 65001 | Out-Null

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " ENOCTA CASE - Test Otomasyonu Baslatiliyor..." -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# Maven komutunu calistir
mvn clean verify

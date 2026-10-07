@echo off
:: Konsol kodlamasini UTF-8 (Emoji ve Turkce karakter destegi) olarak ayarlar
chcp 65001 > nul

echo ========================================================
echo ENOCTA CASE - Test Otomasyonu Baslatiliyor...
echo ========================================================
echo.

:: Maven komutunu calistir
call mvn clean verify

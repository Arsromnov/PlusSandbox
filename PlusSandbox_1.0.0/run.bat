@echo off
title PlusSandbox - Физическая Песочница+
echo Запуск PlusSandbox...

if exist "PlusSandbox.jar" (
    java -jar PlusSandbox.jar
) else (
    echo JAR файл не найден! Запускаю компиляцию...
    call compile.bat
    if exist "PlusSandbox.jar" (
        java -jar PlusSandbox.jar
    )
)

pause
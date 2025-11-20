@echo off
title Компиляция PlusSandbox
echo Компиляция игры...

if not exist "src" (
    echo Папка src не найдена!
    pause
    exit
)

if not exist "saves" mkdir saves

javac -d . src/Main.java

if %errorlevel% == 0 (
    echo Компиляция успешна!
    echo Создание JAR файла...
    
    jar cfe PlusSandbox.jar Main *.class
    
    if exist "*.class" del /Q *.class
    echo JAR файл создан: PlusSandbox.jar
) else (
    echo Ошибка компиляции!
)

pause

@echo off
title EasyTickets
:Set variables
set "CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 9.0"
set "EMBER_APP_PATH=D:\EasyTickets\MovieTicketBooking\app"
set "CLASS_PATH=C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\EasyTickets\WEB-INF\classes"
set "URL=http://localhost:4200/home"

:Stop Tomcat
taskkill /F /IM tomcat.exe >nul 2>&1

:Stop Ember.js
taskkill /F /IM node.exe >nul 2>&1

:Start Tomcat
start /B cmd /c "%CATALINA_HOME%\bin\catalina.bat" jpda run

:Initiaze Data using Initializer class
cd /D "%CLASS_PATH%"
java initialize.Initializer

:Navigate to Ember.js application directory and start Ember server in the background
cd /D "%EMBER_APP_PATH%"
start /B cmd /c "ember serve"

:Wait for Ember server to start
timeout /t 15>nul

:Open Google Chrome
start chrome "%URL%"

set PATH=%PLEO_HOME%\bin;%PATH%
%PLEO_HOME%\bin\ugobe_project_tool needs_behavior.upf rebuild
copy /Y build\needs_behavior.urf F:\needs_behavior.urf
copy /Y automon.txt F:\automon.txt
pause
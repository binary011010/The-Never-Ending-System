del /Q classes\* 
del /Q bin\*
javac -d classes\ src\com\tnes\*.java
jar cmf Manifest.mf bin\tnes.jar -C classes com
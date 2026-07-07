$javaPath = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot\bin"
& "$javaPath\javac.exe" Test.java
& "$javaPath\java.exe" -cp ".;C:\Users\Alisha\.m2\repository\org\xerial\sqlite-jdbc\3.44.0.0\sqlite-jdbc-3.44.0.0.jar;C:\Users\Alisha\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar" Test

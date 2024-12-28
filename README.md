# SD-projeto

Compilação: 
Usando [maven](https://maven.apache.org/)

Programa servidor -> `mvn package -Pserver-build`

Programa cliente -> `mvn package -Pclient-build`

Execução: Na pasta SD-projeto 

Programa servidor -> `java -jar target/server-build.jar <NMaxConexões>`

Programa cliente -> `java -jar target/client-build.jar`
# Trabalho de Sistemas Distribuidos : (UFU 2019.1)

Grupo:

+ Guilherme Fagotti
+ João Marcos Gomes
+ Marilia Leal
+ Rafael Morais de Assis

Links:

+ Repositório do Clone: [<https://github.com/rafanthx13/facomsd>](https://github.com/rafanthx13/facomsd)

+ BitBucket: [<https://bitbucket.org/facom_ufu/gbc074gsi028/src/master/>](https://bitbucket.org/facom_ufu/gbc074gsi028/src/master/)



## Descrição do Trabalho (Desc do Slide)

**Objetivo**

​	O objetivo deste projeto é desenvolver um sistema de bancos de dados genérico/multiuso, a ser usado como bloco de construção em outros projetos.
​	Com este objetivo, replicaremos abordagens bem conhecidas e funcionais, aplicando diversas técnicas de desenvolvimento de sistemas distribuídos.



## Como executar



**Executar no Linux (bash) **
No linux (e no windows se você descobrir como) você pode executar script que executa vários comandos encadeados.

Há os arquivos `serv.sh` e `cli.sh` que foram configurados para compilar e executar os arquivos de Servidor e Cliente.

Então execute (em ordem e em terminais diferentes):
+ `bash serv.sh`
+ `bash cli.sh`



**No Windows**

Caso os srcipt `.sh` não funcionarem, você pode executar tudo manualmente.

Descrição dos comandos JAVA:
+ `javac File_java.java` : Compila o arquivo `File_java.java`
+ `java File_java `: Executa o arquivo `FIle_java.class` (o ByteCode)

Assim para executar o projeto será:
````
javac Servidor.java
java Servidor

javac Cliente.java
java Cliente
````

Para remover arquivos .class (compilados):
`find . -type f -name '*.class' -delete`
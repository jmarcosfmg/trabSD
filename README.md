# Trabalho de Sistemas Distribuidos : (UFU 2019.1) - Útima Entrega

Grupo:

+ Guilherme Fagotti
+ João Marcos Gomes
+ Marilia Leal
+ Rafael Morais de Assis

Links:

+ Repositório do Clone: [<https://github.com/rafanthx13/facomsd>](https://github.com/rafanthx13/facomsd)
+ BitBucket: [<https://bitbucket.org/facom_ufu/gbc074gsi028/src/master/>](https://bitbucket.org/facom_ufu/gbc074gsi028/src/master/)



## Entrega 3 - 08/07/2019 - Segunda

Nesta terceira entrega, vocês usarão o Atomix para replicar seus servidores, usando a abordagem e máquinas de estados replicadas.

O trabalho deverá ser executado sobre a etapa 1. Para aqueles que executarem sobre a etapa 2, haverá um bônus (de satisfação, de trabalho, e, possivelmente, de nota).

**Requisitos**

+ um cliente pode se conectar a qualquer um dos servidores.
+ múltiplos clientes existem em concorrentemente
+ IMPLEMENTEM O CLIENTE DE TESTES

#### Critérios de Avaliação da Planilha

- Dados foram replicados em pelo menos 3 replicas	
- Numero de nós é configurável sem recompilação?	
- Roteamento permite acessar qualquer das réplicas?	
- Sistema continua online no caso de falha qualquer processo?	
- Mantém funcionalidades

**Observações**

+ No Slide há um exemplo do atomix: da página 683 à 700

+ [Link do BitBucket - atomix](https://bitbucket.org/facom_ufu/gbc074gsi028/src/master/lab/atomix_lab2/)

   + > Comentário do lasaro no Piazza: o bitbucket, na pasta  latex,  tem  o fonte. também no mesmo repo, há um demo do novo atomix-raft. é  bem simples, e compila e roda no terminal.



## Como executar (o que temos da entrega 1)

**Executar no Linux (bash)**

No linux (e no windows se você descobrir como) você pode executar script que executa vários comandos encadeados.

Há os arquivos `serv.sh` e `cli.sh` que foram configurados para compilar e executar os arquivos de Servidor e Cliente.

Então execute (em ordem e em terminais diferentes):

- `bash serv.sh`
- `bash cli.sh`

**No Windows**

Caso os srcipt `.sh` não funcionarem, você pode executar tudo manualmente.

Descrição dos comandos JAVA:

- `javac File_java.java` : Compila o arquivo `File_java.java`
- `java File_java `: Executa o arquivo `File_java.class` (o ByteCode)

Assim para executar o projeto será:

```
javac Servidor.java
java Servidor

javac Cliente.java
java Cliente
```

Para remover arquivos .class (os arquivos compilados (opcional)):

`find . -type f -name '*.class' -delete`
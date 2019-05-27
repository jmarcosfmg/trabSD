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

**Executar no Linux (bash)**

No linux (e no windows se você descobrir como) você pode executar script que executa vários comandos encadeados.

Há os arquivos `serv.sh` e `cli.sh` que foram configurados para compilar e executar os arquivos de Servidor e Cliente.

Então execute (em ordem e em terminais diferentes):
+ `bash serv.sh`
+ `bash cli.sh`



**No Windows**

Caso os srcipt `.sh` não funcionarem, você pode executar tudo manualmente.

Descrição dos comandos JAVA:
+ `javac File_java.java` : Compila o arquivo `File_java.java`
+ `java File_java `: Executa o arquivo `File_java.class` (o ByteCode)

Assim para executar o projeto será:
````
javac Servidor.java
java Servidor

javac Cliente.java
java Cliente
````

Para remover arquivos .class (os arquivos compilados (opcional)):

`find . -type f -name '*.class' -delete`



## Entrega 2 - 03/06/20129 - Segunda

- Em época de Big Data, um banco de dados com apenas um servidor é uma nulidade.
- Como armazenar meus milhões e milhões de registros? É necessário espalhar os dados por múltiplos servidores, e para tanto usaremos como modelo o funcionamento do Chord.

### Separação de atividades da ENTREGA 2

**SUJEITO A MODIFICAÇÕES E SUJESTÕES, É APENAS UMA FORMA DE DEMARCA OBJETIVAMENTE O QUE TEREMOS QUE FAZER E UMA ORDEM.**

- [ ] 1. Modificação do Servidor:
     1. - [ ] Modificar a classe servidor para aceitar vários servidores rodando simultanemante em portas diferentes.
        1. - [ ] Definir o número *n* de servidores.
        2. - [ ] Definir como fazer os identificadores dos Servidores
     2. - [ ] Definir método para particionamento na criação do servidor . Sugestão: deverá ser informado o intervalo de valores que ele cobre.
     3. - [ ] Criar a definir bem a **tabela de roteamento** além da referencia que um servidor terá de outro.
        1. - [ ] Criar solução para que essa tabela seja atualizada a cada 30s
        2. - [ ] *"A tabela de roteamento só aponta para nos nós seguintes, não os anteriores, e contem m entradas, segundo a regra estabelecida."*
        
        3. - [ ] OBS: A tabela de rotemanteo é uma para cada servidor
     4. - [ ] Por uma fila F4 para distribuição de comandos entre os servidores. 
     5. - [ ] Criar um quinto método de `REPASS` para passar um comando para F4 quando o servidor não é válido para atender a requisição.
     6. - [ ] Fazer a verificaçâo se um servidor pode ou não atender a um comando e `REPASSAR` se não puder
- [ ] 2. Modificações na Comunicação
     1. - [ ] Usar gRPC
     2. - [ ] Alterar `CREATE, UPDATE, READ, DELETE, REPASS` para usar o gRPC

- [ ] 3. Modificações no Banco de Dados:
     1. - [ ] É feito um update de log/snap a cada `u` segundos. Escolher `u`.
     2. - [ ] Configurar Snapshot
        1. - [ ] Um snapshot é uma foto dos dados que estão na memória
        2. - [ ] Sugestão de estrutura: `json`
     3. - [ ] Configurar Log
        1. - [ ] O `log.x` guarda as operaões feitas para se obter o `snap.x+1` 
     4. - [ ] Maniupla arquivos de log/snap para que fique somente os 3 ultimos arquivos de log/snap. Ou seja, apartir do tempo `4*u` o primeiro log/snap é deletado, para se ter somente 3 arquivos de log/snap


#### Roteamento

De acordo com a especificação da entrega anterior, cada requisição é colocada em uma fila F1, de onde é re-enfileirada nas filas F2 e F3.

- Para esta entrega, antes de re-enfileirar a mensagem, o servidor deverá analisar se é realmente responsabilidade deste servidor.
- Caso o seja, a requisição é re-enfileirada em F2 e F3, como antes. Caso contrário, será colocada em uma fila F4.
- Um thread retira de F4 e invoca, consultando uma tabela de roteamento, o nó responsável pela requisição para que a processe ou que pelo menos esteja mais próximo que o mesmo.
- O servidor primeiro contactado pelo cliente é o responsável por enviar a resposta para o cliente.

#### Entrada e saída de nós

Segue o esquema de anel lógico definido pelo Chord.

- Seja n o número de nós a serem colocados no sistema na execução de testes.
- Cada servidor é identificado por um número de m bits.
- O primeiro nó a entrar no sistema recebe um identificador aleatório e a informação de que é o primeiro nó.
- Os nós seguintes entram no sistema um por vez, recebem identificadores aleatórios e o endereço de um nó já no sistema.
- Há um intervalo de tempo de 30 segundos no mínimo para que informação sobre a tabela de roteamento seja atualizada.
- Ao sair, um nó informa os nós seguinte e anterior.

#### Particionamento

O particionamento da responsabilidade sobre os dados seguirá o esquema de anel lógico definido pelo Chord.

- Seja n o número de nós a serem colocados no sistema na execução de testes.
- Cada servidor é identificado por um número de m bits.
- Seja uma sequência de nós com identificadores X < Y < Z: o nó Y é responsável pelos dados com chaves internas de X + 1 a Y, o nó Y por chaves de Y + 1 a Z, e assim por diante.

#### Comunicação

- Toda comunicação deve ser agora feira usando gRPC.
- Cada operação é realizada via uma função diferente (i.e., há uma função para C, outra para R, ...).
- Servidores redirecionam requisições também usando gRPC, usando a mesma interface usada por clientes.
- Toda requisição é executada assincronamente do ponto de vista de quem invoca a requisição.
- A tabela de roteamento só aponta para nos nós seguintes, não os anteriores, e contem m entradas, segundo a regra estabelecida.
- Múltiplos saltos podem ser necessários até que a requisição seja respondida.

#### Tratamento de Falhas

- Assuma que não haverão falhas, apenas saídas controladas.
- Assuma que o envio de requisições para até que o sistema seja estabilizado.
- Nós podem ser reiniciados e, como na primeira entrega, devem ter seu estado recuperado pelo uso do log de operações e de snapshot do banco de dados.

#### Log + Snapshot

- Para evitar que o log se torne grande demais, frequentemente serão feitos snapshots do estado atual do banco de dados.
- Um snapshot do banco captura o estado atual do mesmo, em arquivo, e portanto torna desnecessário o arquivo de logs contendo as operações anteriores ao snapshot.

#### Snapshoting

- A cada U segundos, o estado atual do banco será gravado em um arquivo nomeado `snap.X,` onde X é um contador de logs. Isto é, o primeiro snapshot será gravado como snap.1, o segundo como snap.2 e assim por diante.
- As operações executadas antes de um snapshot X serão gravadas e um arquivo de log.(X-1). O primeiro arquivo de logs será então o log.0.
- Uma vez iniciado o snapshot que cria snap.X, nenhuma nova operação será escrita em log.(X-1). Novas operações são escritas em log.X.
- Serão mantidos pelo sistema os últimos 3 arquivos de log e de snapshot. Isto é, se o último snapshot executado foi o décimo, então há no sistema os logs log.8, log.9. log.10 (sendo escrito), e os snapshots snap.8, snap.9, e snap.10.
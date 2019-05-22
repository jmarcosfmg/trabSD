## Entrega 1

​	Para a primeira entrega vocês desenvolverão a “cara” do banco de dados, permitindo que clientes se conectem e realizem operações de acordo com a especificação da API. Desenvolverão também um cliente em linha de comando para que se possa manipular o banco de dados, bem como um cliente de testes, que estressará o banco para verificar sua corretude e funcionalidades.

**Cliente Interativo**

Leitura de comandos

+ 1 thread em loop infinito apresentando menu de comandos e lendo comandos do teclado
+ uma vez digitado um comando, o mesmo é validado localmente
+ se válido, comando é enviado ao servidor
+ se inválido, mensagem de erro é apresentada
+ o comando sair termina a execução deste thread

Apresentação de respostas

+ 1 thread em loop infinito recebendo mensagens do servidor
+ uma vez recebida uma mensagem, a mesma é apresentada na tela
+ uma vez terminado o thread de leitura de comandos, o thread de apresentação espera pelo menos 5 segundos por novas mensagens do servidor e então se termina, terminando o processo.

**O Servidor**

A base de dados

+ é um mapa de BigInteger (inteiro de precisão infinita) para um vetor de bytes (ou algo que o valha)
+ mantido em memória apenas (por enquanto) e manipulado por 4 operações (CRUD)
+ observando a semântica de cada operação (create 6 = update)
+ todos os comandos retornam um código de erro/sucesso mais resposta, se for adequado.

Apesar do banco ser em memória, toda operação será logada emdisco.

Terá arquitetura em estágios, tendo

+ 1 ou mais threads recebendo comandos e colocando em uma fila F1
+ 1 thread consumindo comandos de F1 e colocando cópias do comando em uma fila F2 e em outra fila F3
+ 1 thread consumindo comandos de F2 e gravando-os em
  disco.
+ 1 thread consumindo de F3 aplicando o comando no banco
  de dados (mapa)

**O thread de log**

grava comandos em um arquivo de log

+ mantendo o arquivo aberto durante a execução do programa
+ adicionando comandos sempre ao fim do arquivo
+ somente se o comando altera a base de dados (Reads são descartados)

**O thread de Processamento**

executa os comandos

+ contra o mapa
+ emitindo mensages de sucesso (create/update/delete)
+ respondendo com informação solicitada (read)
+ emitindo erros quando adequado (create/update/delete/read)
+ na ordem em que os comandos foram enfileirados em F3

**Tolerância a Falhas**

Como o mapa é mantido em memória, no caso de falhas, todo o banco apagado. Para recuperá-lo

+ Na reinicialização do processo
+ abra o arquivo de log
+ e processe-o na sequência em que foi escrito
+ reexecutando todas as operações gravadas
+ antes de aceitar novas requisições de clientes.

**Acesso Concorrente**

Diversos clientes podem ser iniciados em paralelo e contactando o mesmo servidor.

**Comunicação**

+ Toda comunicação é feita via TCP.
+ E o canal de comuniação com o cliente é mantido aberto
  enquanto o mesmo estiver executando.
+ Todas as portas usadas na comunicação são especificadas
  via arquivos de configuração.
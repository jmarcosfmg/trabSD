## Git

`git status`: Verifica os arquivos do seu repositorio, se houve alguma modificaçao; se o repositorio foi atualizado e você precisa fazer um `git pull` dentre outras cosias

**Como Subir modificações**

1. Dê `git status` pra ve se tem aluma modificação para subir
2. Dê `git add --all` que sobe todas as modificações que foram listadas no `git status`
   + As modificações estarão no *Staging Area local*
3. Dê `git commit -m "msg"`, em `msg` troque pela mensagem que explica o seu commit (entre aspas duplas)
   + Exemplo: `git commit -m "First Commit"`
   + Com isso, você salvou suas modificações, como se tive-se tirado uma foto do estado atual do seu repositório
4. Dê `git push` para subir suas modificações para o repositório, ou seja, para o site do git (assim todos poderão baixar e atualizar as suas branch)

**Como atualizar sua branch**

+ Dê `git pull`, se houver alguma moficação na branch no remote (no site) entâo será baixada localmente

  **Caso der problema no git pull**

+ **CASO DER ERRO E APAREÇA `CONFLIT`** **ENTAO QUER DIZER QUE DEU CONFLITO ENTRE SUA BRANCH E A REMOTE (do site), PROCURE ENTÃO RESOLVER ISSO. UM EXEMPLO É O `kdiff3`** **UMA FERRAMENTA PARA RESOLVER CONFLITO DE GIT**
+ Se quiser evitar tentar resolver, entao, baixe denovo o repositorio remoto em outro lugar e nele faça as modificações que você estava fazendo

+ OBS: Alguns editores de código e IDE mais modernos possuem mecanismos para tentar resolver, ou, pelo menos mostrar onde deu conflito

**Branch**

+ Caso queira uma branch (ramo) de desenvolvimento só para você, utilize `git checkout -b nome_da_branch` , ela será local e até você subila, assim você pode fazer seus push sem se preocupar em dar conflito com outras pessoas
+ `git merge branch_name` : Junta sua branch, a que você está com a `branch_name`. SUa branch receberá de  `branch_name`
+ `git branch`: lista as branchs locias e em qual você está
+ `git checkout branch_name`: troca sua branch para `branch_name`,
  + **ISSO MUDA TODOS OS SEUS ARQUIVOS LOCAL, MAS AS COISAS DE SUA BRANCH FICARÃO GUARDADAS, BASTA VOLTAR A BRANCH**
+ Se você nao criar uma branch e ficar na master, entao, antes de dar um `git push` de um `git pull` antes ee resolva os conflitos localmente

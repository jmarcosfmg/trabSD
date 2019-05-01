import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.Scanner;

public class Servidor extends Thread {

	// Parte que controla as conexões por meio de threads.
	// Note que a instanciação está no main.
	private static Vector <PrintStream> Fila_F1;
	private Mapa mapa;
	private Socket conexao;
	private int opcao;

	// Objeto que manipula o Log
	private LogFileManager logfile = new LogFileManager();

	public static void main(String args[]) {
		// instancia o vetor de clientes conectados
		Fila_F1 = new Vector <PrintStream> ();
		try {
			// criando um socket que fica escutando a porta 2222.
			ServerSocket s = new ServerSocket(5082);
			// Loop principal.
			while (true) {
				// aguarda algum cliente se conectar. A execução do
				// servidor fica bloqueada na chamada do método accept da
				// classe ServerSocket. Quando algum cliente se conectar
				// ao servidor, o método desbloqueia e retorna com um
				// objeto da classe Socket, que é porta da comunicação.
				System.out.println("Esperando alguma requisição ao BD");
				Socket conexao = s.accept();
				// cria uma nova thread para tratar essa conexão
				System.out.println("Recebida conexao na porta " + conexao.getPort());
				Thread t = new Servidor(conexao);

				t.start();
				// voltando ao loop, esperando mais alguém se conectar.
			}
		}catch (IOException e) {
			// caso ocorra alguma excessão de E/S, mostre qual foi.
			System.out.println("IOException: " + e);
		}
	}

	public Servidor(Socket s) {
		conexao = s;
		mapa = new Mapa();
		logfile.openFile(); // prepara para escrever no arquivo
		loadRecordsFromFile(); // carrega registros do arquivo
	}
	
	// execução da thread
	public void run() {
		try {
			// objetos que permitem controlar fluxo de comunicação
			BufferedReader entrada = new BufferedReader(new	InputStreamReader(conexao.getInputStream()));
			Scanner ler = new Scanner(System.in);
			PrintStream saida = new PrintStream(conexao.getOutputStream());
			// primeiramente, espera-se a opcao do servidor 
			BigInteger chave;
			byte[] valor;
			while(true){
				try{
				opcao = Integer.parseInt(entrada.readLine());
				// agora, verifica se string recebida é valida, pois
				// sem a conexão foi interrompida, a string é null.
				// Se isso ocorrer, deve-se terminar a execução.
				System.out.println("Opcao escolhida: "+opcao);
				if (opcao == 5){
					break;
				}
				// CREATE
				else if (opcao == 1){
					saida.println("Opcao selecionada = "+opcao);
					chave = new BigInteger(entrada.readLine());
					//System.out.println(chave);
					//saida.println("Entre com o valor:");
					valor = entrada.readLine().getBytes();	
					if(mapa.create(chave, valor) == 0 && logfile.writeRecord(new Record(chave, "C", valor))) { 
						saida.println("Inserido com sucesso");
					}
					else {
						saida.println("Erro na insercao");
					}
				}
				// GET
				else if (opcao == 2){
					saida.println("Opcao selecionada = " + opcao);
					chave = new BigInteger(entrada.readLine());
					if(mapa.existe(chave)){
						valor = mapa.read(chave);
						String msgDecode = new String(valor); // convertendo byte para string
						saida.println("Valor Procurado: " + msgDecode); // Exibir
					} else {
						saida.println("Valor nao encontrado"); // Exibir
					}
					
				}
				// UPDATE
				else if (opcao == 3){
					saida.println("Opcao selecionada = " + opcao);
					chave = new BigInteger(entrada.readLine());
					saida.println("Entre com o valor:");
					valor = entrada.readLine().getBytes();	
					if(mapa.update(chave,valor) == 0 && logfile.writeRecord(new Record(chave, "U", valor)))
						saida.println("Atualizacao feita com sucesso");
					else 
						saida.println("Erro");
				}
				// DELETE
				else if (opcao == 4){
					//saida.println("Opcao selecionada = "+opcao+"Entre com a chave:");
					chave = new BigInteger(entrada.readLine());
					if(mapa.delete(chave) == 0 && logfile.writeRecord(new Record(chave, "D")) )
						saida.println("Exclusao feita com sucesso");
					else 
						saida.println("Erro");
				}
				// Uma vez que se tem um cliente conectado
				// coloca-se fluxo de saída para esse cliente no vetor de
				// clientes conectados.
				Fila_F1.add(saida);
				// Fila_F1 é objeto compartilhado por várias threads!
				// De acordo com o manual da API, os métodos são
				// sincronizados. Portanto, não há problemas de acessos
				// simultâneos.
				// Verificar se linha é null (conexão interrompida)
				// Se não for nula, pode-se compará-la com métodos string
				
				}catch(NullPointerException a) {
				}
				catch(Exception a) {
					saida.println("Erro, operacao não foi efetuada. "+a);
				}
			}
			// Uma vez que o cliente enviou linha em branco, retira-se
			// fluxo de saída do vetor de clientes e fecha-se conexão.
			Fila_F1.remove(saida);
			logfile.closeFile();
			System.out.println(conexao.getPort()+" se desconectou"); 
			conexao.close();
	}catch (IOException e) {
		// Caso ocorra alguma excessão de E/S, mostre qual foi.
		System.out.println("IOException: " + e);
	}
}
	// Carrega Records do log para a memoria
  public void loadRecordsFromFile(){
    List<Record> listOfRecords = logfile.readRecords();
    for (Record record : listOfRecords){
      executeRecord(record);
    }
	}
	
	// Decide qual operaçâo executar de acordo com a label/rotulo do Record
  public void executeRecord(Record record){
    switch (record.getLabel()){
      case "C":
         createRecord(record);
         break;
      case "U":
         updateRecord(record);
         break;
      case "D":
         deleteRecord(record);
         break;
     }
	}
	
	public void createRecord(Record record){
    mapa.create(record.getKey(), record.getData());
  }

  public void updateRecord(Record record){
    mapa.update(record.getKey(), record.getData());
  }

  public void deleteRecord(Record record){
    if(this.mapa.existe(record.getKey())){
      mapa.delete(record.getKey());
    }
	}
	
	// Imprime o HashMap como key --> value | caso quiser vaer tudo
  public void printHashMap(){
    for (BigInteger aKey: this.mapa.getMapa().keySet()){
      System.out.println("key [bytes]: " + aKey + " | Value: " + new String(mapa.getMapa().get(aKey)) );
    }
    if(this.mapa.getMapa().isEmpty()){
      System.out.println("O HashMap na memoria esta vazio!");
    }
  }

}

class Mapa{
   
	private Map<BigInteger, byte[]> mapa;

	public Mapa(){
	    	this.mapa = new HashMap<>();
	}
	 
	public boolean existe(BigInteger o1) {
	    	if (mapa.get(o1) == null) return false;
			else return true;
	}

	public int create(BigInteger o1, byte[] o2){
		if (!existe(o1)){
			mapa.put(o1,o2);
			return 0;
		}else return -1;
	}
	
  	public int update(BigInteger o1, byte[] o2){
		if(existe(o1)){
			mapa.remove(o1);
			mapa.put(o1,o2);
			return 0;		
		}
		else return 1;
	}

	public int delete(BigInteger o1){
		if(existe(o1)){
			mapa.remove(o1);
			return 0;		
		}
		else return 1;
	}
	
	public byte[] read(BigInteger o1){
		return mapa.get(o1);
	}

	public Map<BigInteger, byte[]> getMapa() {
		return mapa;
	}

}

class Record {

  private BigInteger key;
  private String label;
  private byte[] data;

  // Estrutura para agrupar todos os dados
  Record(BigInteger key, String label){
    this.key = key;
    this.label = label;
    this.data = null;
  }

  Record(BigInteger key, String label, byte[] data){
    this.key = key;
    this.label = label;
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }
  public BigInteger getKey() {
    return key;
  }
  public String getLabel() {
    return label;
  }

}

class LogFileManager {

  private final String fileName = "log"; // Valor constantedo arquivo

  public FileOutputStream writer;

  // Retorna uma Lista do Tipo Record que leu do Arquivo 'log.txt'
  public List<Record> readRecords(){
    List<Record> listrecord = new ArrayList<Record>();

    Record aData;
    String labelOption;
    BigInteger key;
    byte[] dataLine;
    String line;
    String[] splitted;
    existFile();

    try(BufferedReader bufferedReader = new BufferedReader( new FileReader(this.fileName))){
      while( (line = bufferedReader.readLine()) != null){
        if(line.equals("")){
          continue; // linha vazia sera pulada;
        }
        splitted = line.split(" ");
        labelOption = splitted[0];
        key = new BigInteger(splitted[1]);
        if(labelOption.equals("D")){ // DELETE
          aData = new Record(key, labelOption);
        } else { // CREATE OR UPDATE
          line = bufferedReader.readLine();
          dataLine = line.getBytes(); // vai ler a lionha e agora vai guardar
          aData = new Record(key, labelOption, dataLine);
        }
        listrecord.add(aData);
      }
      bufferedReader.close();
    } catch (Exception e){
       printException(e, "readRecords");
    }
    return listrecord;
  }

   // Adiciona Registros Record no arquivo
   public boolean writeRecord(Record record){
		try {
			System.out.println("Label : "  + record.getLabel());
			System.out.println("Key : " + record.getKey().toString());
			writer.write( (record.getLabel() + " " + record.getKey().toString() + "\n").getBytes() );
			if(!record.getLabel().equals("D")){
				writer.write( (new String(record.getData()) + "\n").getBytes() );
			}
			writer.flush();
			return true;
		} catch (Exception e) {
			printException(e, "writeRecord");
			return false;
		}
	}

  // Fecha o arquivo
  public void closeFile(){
    try {
      writer.close();
    } catch (Exception e) {
      printException(e, "closeFile");
    }
  }

  // Abre o arquivo ou o cria se nao existir
  public void openFile(){
      try {
        File file = new File(this.fileName);
        if(!file.exists()){
          file.createNewFile(); // cria o arquivo o mesmo se nao existir
        }
        this.writer = new FileOutputStream(file, true); // true é para adicionar no final, o modo 'append'
        this.writer.write(System.lineSeparator().getBytes());
      } catch (Exception e) {
        printException(e, "openFile");
      }
   }

   // Verifica se o arquivo existe ou nao, se nao, o cria
   public void existFile() {
    try {
      File file = new File(this.fileName);
      if(!file.exists()){
        file.createNewFile(); // cria o arquivo o mesmo se nao existir
      }
    } catch (Exception e) {
      printException(e, "existFile");
    }
   }

   // Imprime Execeções que podem aparecer de forma melhor para Debugar
   public void printException(Exception e, String func){
     System.out.println("ERROR in function: " + func);
     System.out.println(e.toString());
     e.printStackTrace();
     System.exit(1);
   }

}

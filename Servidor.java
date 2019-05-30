import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.Scanner;

import util.datahandler.*;
import util.memory.*;

public class Servidor extends Thread {

	// Parte que controla as conexões por meio de threads.
	// Note que a instanciação está no main.
	private static BlockingQueue < Comando > Fila_F1 = new LinkedBlockingDeque < >();
	private static BlockingQueue < Comando > Fila_F2 = new LinkedBlockingDeque < >();
	private static BlockingQueue < Comando > Fila_F3 = new LinkedBlockingDeque < >();

	private static final int TIME_UPDATE_BD = 5 * 1000; // mili segundos

	public static void main(String args[]) {
		try {

			// criando um socket que fica escutando a porta 5082.
			ServerSocket s = new ServerSocket(5082);

			Thread t1 = new Thread(new Fila1Manager(Fila_F1, Fila_F2, Fila_F3)); //pega o que estiver na fila f1 e manda para fila f2 e fila f3
			t1.start();

			MapManager mapManager = new MapManager(Fila_F2);
			
			Timer timer = new Timer();
			// schedules the task to be run in an interval 
			// timer.scheduleAtFixedRate(task, TIME_UPDATE_BD, TIME_UPDATE_BD);
			Thread t2 = new Thread(mapManager); //pega o que estiver na fila f2 e faz as operacoes na memoria
			t2.start();

			// modificado para timeTask ter referencia para ele
			LogFileManager logFileManager = new LogFileManager(Fila_F3, Fila_F2);

			TimerTask task = new TimerManagerBD(mapManager.getMapa(), logFileManager); // Thread que executa a cada u segundos. Tem a referencia ao Mapa da maemoria, a classe 'Mapa'

			Thread t3 = new Thread(logFileManager); //pega o que estiver na fila f3 e faz as operacoes do log
			t3.start();

			timer.scheduleAtFixedRate(task, TIME_UPDATE_BD, TIME_UPDATE_BD);
			System.out.println("Servidor inicializado");
			// Loop principal.
			while (true) {
				Socket conexao = new Socket();
				conexao = s.accept();
				Thread t0 = new Thread(new Fila1Adder(Fila_F1, conexao)); //cada cliente tem uma thread responsavel por receber seus comandos e
				//adiciona-los a fila
				System.out.println("Recebida conexao na porta " + conexao.getPort());
				t0.start();
			}
		} catch(IOException e) {
			// caso ocorra alguma excessão de E/S, mostre qual foi.
			System.out.println("IOException: " + e);
		}
	}
}

/*

=> Perguntar sobre o array de BYtes. Isso é chato e sem isso poderia usar JSON

===> Estrutura da pasta bd
bd/
	SnapShot.1/
		log.0
		snap.1.txt
	Snashot.2/
		log.1
		snap.2.txt

PARTE DE RAFAEL: LOG E SNAPSHOT E BD:

+ Quando Inicializar o Servidor:
	+ Se não houver nada:
		+ não carrega nada
	+ Se houver algum Snapshot:
		+ Carrega no mapa o ultimo snapshot

+ Quando é feito um Snapshot:
	+ Verifica qual será o seu contatdor:
		+ Se nâo houver nenhum. Seŕa Zero
		+ Se houver será o último mais um
	+ Salva SnapSHot e Log
	+ Verifica se há mais de 3 pastas
		+ identificar a ultima pasta
	
	* ================ ATENÇAO ==================
	* Se atente com a diferneça entre a inicializaçao e uma execuçâo nromal
	* Se atente ao fato de pastsa nâo existirem no inicio. Fazer essa verificaçâo e crialas se nao existir
	* ==> Ele quer que tenha um arquivo de configuraçâo: usar JSON
	* ==> PErguntar se pode ser array de Char para usar JSON
	* ==> Ele quer que exista arquivos de teste para executar automaticamente as cosias da parte 1

*/

class TimerManagerBD extends TimerTask{

		private Mapa mapa;
		private LogFileManager logFileManager;

		public static final String MY_DIRECTORY = System.getProperty("user.dir");

		public static String SNAP_SHOT_DIR = "SnapShot";
		private final String LOG_FILE = "log";
		private final String SNAP_FILE = "snap";
		private final String POINT = ".";
		public static String REGEX_POINT = "\\.";
		public static String DEFAULT_FILEPATH_DB = MY_DIRECTORY + "/" + "db";
		
		public TimerManagerBD(Mapa mapa, LogFileManager logFileManager){
			this.mapa = mapa;
			this.logFileManager = logFileManager;
		}

		// TODO: So salvar se haver alguma mudaça (por uma flag) ou aumentar o tempo u
		@Override
		public void run() {
			
			int nexCounter = 0;
			List<Integer> listCounters = null;

			// task to run goes here
			System.out.println("\tTIME TASKER DB MANAGER !!!");

			// Verifica se a basta BD existe
			Boolean existDB = checkDbDirectory();
			if(!existDB){
				System.out.println("ERRO NA CRIACAO DA PASTA DB OU NAO EXISTE!");
				System.exit(1);
			} 

			// Verifica se essa eh a primeira execucao
			Boolean firstExecution = firstExecution();
			
			// Primeira Execucao: O log começa com o contador 1
			if(firstExecution){
				nexCounter = 1; 
			}

			// Não eh a primeira execucao : uscara o proximo contado apartir dos numeros que ja existem
			if(!firstExecution){
				listCounters = getListCounters( listDirectory(DEFAULT_FILEPATH_DB) );
				nexCounter = listCounters.get(0) + 1;
			}
			
			// Define os nomes dos arquivos: diretorio, log e snap
			String nextDirFilePath = DEFAULT_FILEPATH_DB + "/" + SNAP_SHOT_DIR + POINT + Integer.toString(nexCounter);
			String nextSnapFilePath = nextDirFilePath + "/" + SNAP_FILE + POINT + Integer.toString(nexCounter);
			String nextLogFilePath = nextDirFilePath + "/" + LOG_FILE + POINT + Integer.toString(nexCounter - 1);

			// Criar arquivos
			new File(nextDirFilePath).mkdir(); 

			// Salva SnapShot e Log
			saveSnapshot(mapa.getMapa(), nextSnapFilePath);
			saveLog(nextLogFilePath);

			// Verifica se vai precisar deletar algum arquivo, so ocrorre quando nao eh a primeira execucao
			if(!firstExecution){
				if(listCounters.size() >= 3){
					int fouthNumber = listCounters.get(2);
					String dirDeleted = DEFAULT_FILEPATH_DB + "/" + SNAP_SHOT_DIR + POINT + Integer.toString(fouthNumber);
					deleteDirectory(dirDeleted);
				}
			}

		}

		/**
		 * Na Realidadee, nao eh TImeManagerBD que salva o Log, ele so vai setar ao LogFileManager
		 * Para salvar em outro arquivo
		 * @param logFile novo arquivo de log
		 */
		private void saveLog(String logFile){
			this.logFileManager.setLogFile(logFile);
		}

		/**
		 * Salva SnaShot, percorrendo pelo mapeamento
		 * @param snapMap HashMap do Banco na memoria
		 * @param snapFile nome do arquivo
		 */
		private void saveSnapshot(Map<BigInteger, byte[]>  snapMap, String snapFile){
			FileOutputStream writer;
			// Abre o Arquivo ou o cria
			File file = new File(snapFile);
			try {
				if (!file.exists()) {
					file.createNewFile(); 
				}
				writer = new FileOutputStream(file, true);
				// Itera sobre o mapa e grava no arquivo
				for (Map.Entry<BigInteger, byte[]> entry : snapMap.entrySet()) {
					writer.write( ( entry.getKey().toString() + "\n").getBytes() );
					writer.write( (new String(entry.getValue()) + "\n").getBytes() );
					writer.flush();
					System.out.println(entry.getKey() + "/" + new String(entry.getValue()));
				}
				// Fecha arquivo
				writer.close();
			} catch (Exception e) {
				System.err.println("Erro ao salvar SnapShot");
				e.printStackTrace();
			}
		}

		/**
		 * Verifica se o direotiro existe, se nao o cria.
		 * @return true se 'db/' existir ou se for criado corretamente. 'false' se der algum erro ao crialo
		 */
		private Boolean checkDbDirectory(){
			File dir = new File(DEFAULT_FILEPATH_DB);
			try {
				if(!dir.exists()){
					Boolean resultMkdir = dir.mkdirs();
					return resultMkdir;
				} else {
					return true;
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * Verifica se ja existe alguma pasta 'SnapShot' 'db/'
		 * @return true: se existir nao exixtir a pasta, entao eh a primeira execucao
		 * false: se existir alguma pasta 'SnapSHot'
		 * */
		public static Boolean firstExecution(){
			List<String> listDir = listDirectory(DEFAULT_FILEPATH_DB);
			if(listDir.isEmpty()){
				return true;
			} else {
				for (String dir : listDir) {
					if(isTypeFile(dir, TimerManagerBD.SNAP_SHOT_DIR)){
						return false;
					}
				}
			}
			return true;
		}
		
		/**
		 * Retorna a lista de arquivos do filepath passado
		 * @param directory
		 * 		Direotiro a ser analisado
		 * @return Lista de String dos arquivos desse diretorio
		 */
		public static List<String> listDirectory(String directory){
			List<String> listDirectories = new ArrayList<String>();
			File folder = new File(directory);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isDirectory()) {
					listDirectories.add(listOfFiles[i].getName());
				}
			}
			return listDirectories;
		}

		/**
		 * Verifica para o arquivo "file.ext" se "file" == "type". Identifica se eh um dos 3 tipos de arquivos do BD
		 * @param file
		 * @param type
		 * @return Bool
		 */
		public static Boolean isTypeFile(String file, String type){
			String[] splitedString = file.split(TimerManagerBD.REGEX_POINT); // usa regex e '.' eh um char especial em regex
			if(splitedString[0].equals(type)){
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Conveter uma uma de arq/dir "file.number" para uma lista de inteiros com SOMENTE "numbers". Ex: ["arq.1", "arq.2"] ==> [1,2]
		 * @param listDirectories
		 * @return
		 */
		public static List<Integer> convertArrayStringToInt(List<String> listDirectories){
			List<Integer> intList = new ArrayList<Integer>();
			String aux[];
			String dirNumber;
			for (String dir : listDirectories) {
				aux = dir.split(TimerManagerBD.REGEX_POINT);
				dirNumber = aux[1];
				intList.add(Integer.parseInt(dirNumber));
			}
			return intList;
		}

		/**
		 * Pega os identificadores do arquivo/pasta e retorna uma lista ordenada para usalo depois para pegar
		 * o proximo valor do SNapachot e qual deleter se precisar
		 * @param listDirectories
		 * 		Lista dos nomes de arquivos/pastas 
		 * @return
		 * 		Lista em ordem descendente [10, 9, 8, 7...] dos identificadores encontrados
		 */
		public static List<Integer> getListCounters(List<String> listDirectories){
			List<Integer> listNumbers = convertArrayStringToInt(listDirectories);
			Collections.sort(listNumbers);
			Collections.reverse(listNumbers);
			return listNumbers;
		}

		/**
		 * Deleta diretorio. para isso, delete todos os arquivos que tiver dentro tambem
		 * @param directory
		 */
		private void deleteDirectory(String directory){
			File folder = new File(directory);
    	File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				File currentFile = new File(directory + "/" + listOfFiles[i].getName());
    		currentFile.delete();
			}
			folder.delete();
		}

}


class Fila1Adder implements Runnable {

	private BlockingQueue < Comando > Fila_F1;
	private Socket conexao;

	public Fila1Adder(BlockingQueue < Comando > a, Socket s) {
		Fila_F1 = a;
		conexao = s;
	}

	public void run() {
		BigInteger chave = new BigInteger("0");
		int opcao = 0;
		byte[] valor = null;
		try {
			BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			Scanner ler = new Scanner(System. in );
			PrintStream saida = null;
			saida = new PrintStream(conexao.getOutputStream());
			if (saida == null) System.out.println("Erro, saida nula");
			// primeiramente, espera-se a opcao do servidor 
			while (true) {
				opcao = Integer.parseInt(entrada.readLine());
				// agora, verifica se string recebida é valida, pois
				// sem a conexão foi interrompida, a string é null.
				// Se isso ocorrer, deve-se terminar a execução.
				System.out.println("Cliente: " + conexao.getPort() + ". Opcao escolhida: " + opcao);
				if (opcao == 5) {
					throw new IOException();
				}
				// CREATE
				else if (opcao == 1) {
					chave = new BigInteger(entrada.readLine());
					valor = entrada.readLine().getBytes();
				}
				// GET
				else if (opcao == 2) {
					chave = new BigInteger(entrada.readLine());
					valor = null;
				}
				// UPDATE
				else if (opcao == 3) {
					chave = new BigInteger(entrada.readLine());
					valor = entrada.readLine().getBytes();
				}
				// DELETE
				else if (opcao == 4) {
					chave = new BigInteger(entrada.readLine());
					valor = null;
				}
				// Entrada de comandos em F1 pelo cliente
				Fila_F1.put(new Comando(opcao, chave, valor, new PrintStream(conexao.getOutputStream())));
			}
		} catch(Exception e) {
			try {
				System.out.println(conexao.getPort() + " se desconectou");
				conexao.close();
			} catch(IOException err) {}
		}
	}

}


class Fila1Manager implements Runnable {

	private BlockingQueue < Comando > Fila_F1;
	private BlockingQueue < Comando > Fila_F2;
	private BlockingQueue < Comando > Fila_F3;

	public Fila1Manager(BlockingQueue < Comando > f1, BlockingQueue < Comando > f2, BlockingQueue < Comando > f3) {
		Fila_F1 = f1;
		Fila_F2 = f2;
		Fila_F3 = f3;
	}

	// Distribuicao de comandos de F1 para F2 e F3
	public void run() {
		Comando comando;
		System.out.println("Fila1Manager rodando");
		while (true) {
			try {
				comando = (Comando) Fila_F1.take();
				System.out.println(comando);
				Fila_F2.put(comando);
				Fila_F3.put(comando);
			} catch(InterruptedException e) {

			}
		}
	}

}


class MapManager implements Runnable {

	private Mapa mapa;
	private BlockingQueue < Comando > Fila_F2;

	public MapManager(BlockingQueue < Comando > f2) {
		Fila_F2 = f2;
		mapa = new Mapa();
	}

	// Recebe Comandos de  F2 para inserir na memoria
	public void run() {
		Comando comando;
		int opcao;
		int flag = -1;
		System.out.println("MapManager rodando");
		while (true) {
			try {
				comando = (Comando) Fila_F2.take();
				opcao = comando.getOperacao();
				// CREATE
				if (opcao == 1) {
					flag = mapa.create(comando.getChave(), comando.getValor());
				}
				// GET
				else if (opcao == 2) {
					comando.getCliente().println(new String(mapa.read(comando.getChave())));
					flag = 0;
				}
				// UPDATE
				else if (opcao == 3) {
					flag = mapa.update(comando.getChave(), comando.getValor());
				}
				// DELETE
				else if (opcao == 4) {
					flag = mapa.delete(comando.getChave());
				}
				if (flag == 1) comando.getCliente().println("Falha na operacao!");
			} catch(InterruptedException e) {} catch(NullPointerException a) {}
		}
	}

	public Mapa getMapa(){
		return this.mapa;
	}

}


// class Mapa {

// 	private Map < BigInteger, byte[] > mapa;

// 	public Mapa() {
// 		this.mapa = new HashMap < >();
// 	}

// 	public boolean existe(BigInteger o1) {
// 		if (mapa.get(o1) == null) return false;
// 		else return true;
// 	}

// 	public int create(BigInteger o1, byte[] o2) {
// 		if (!existe(o1)) {
// 			mapa.put(o1, o2);
// 			return 0;
// 		} else return - 1;
// 	}

// 	public int update(BigInteger o1, byte[] o2) {
// 		if (existe(o1)) {
// 			mapa.remove(o1);
// 			mapa.put(o1, o2);
// 			return 0;
// 		} else return 1;
// 	}

// 	public int delete(BigInteger o1) {
// 		if (existe(o1)) {
// 			mapa.remove(o1);
// 			return 0;
// 		} else return 1;
// 	}

// 	public byte[] read(BigInteger o1) {
// 		return mapa.get(o1);
// 	}

// 	public Map < BigInteger,
// 	byte[] > getMapa() {
// 		return mapa;
// 	}

// }


class LogFileManager implements Runnable {

	private BlockingQueue < Comando > Fila_F3;
	private String logFile; // Valor que nao sera mais constante
	private String snapStartFile; 
	public FileOutputStream writer;

	public LogFileManager(BlockingQueue < Comando > f3, BlockingQueue < Comando > f2) {
		Fila_F3 = f3;
		/* Entrada Inicial de comandos em F2 a partir do arquivo (feito uma unica vez, ao start do server)
		   Nao passa por F1 pois ai ele distribuiria para F3, que seria tirada na thread de LogFIleManger (essa mesma)
			 entao haveria uma duplicação no arquivo. Para evitar isso, mandamos direto para F2 que trata comandos para a memoria

			// F1 : Fila que distribui para F2 e F3
			// F2 : Fila de comandos retiradas pelo MapManager para salavar na memoria
			// F3 : FIla de comandos retirods pelo LogFileManager para salvar no disco
		*/

		if(!TimerManagerBD.firstExecution()){
			// Nao eh primeira execucao: vai saber e carrega o ultimo SnapShot
			int lastSnapDir = TimerManagerBD.getListCounters( TimerManagerBD.listDirectory( TimerManagerBD.DEFAULT_FILEPATH_DB)).get(0);
			this.snapStartFile = TimerManagerBD.DEFAULT_FILEPATH_DB + "/SnapShot." + Integer.toString(lastSnapDir) + "/" + "snap." + Integer.toString(lastSnapDir);
			loadRecordsFromFile(f2); // inicializa agora pelo snapShot
		} else {
			this.logFile = startLogFile(); // busca o ultimo arquivo de log
		}
	}

	// Recebe comandos de F3 para inserir no arquivo
	public void run() {
		BigInteger chave;
		byte[] valor;
		int opcao;
		Comando comando;
		System.out.println("LogFileManager rodando");
		while (true) {
			try {
				comando = (Comando) Fila_F3.take();
				openFile(this.logFile);
				System.out.println("LOG FILE MANAGER ENTROU AKI APRA GRAVAR EM : " + this.logFile);
				opcao = comando.getOperacao();
				// CREATE
				if (opcao == 1) {
					writeRecord(new Record(comando.getChave(), "C", comando.getValor()));
				}
				// GET
				else if (opcao == 2) {}
				// UPDATE
				else if (opcao == 3) {
					writeRecord(new Record(comando.getChave(), "U", comando.getValor()));
				}
				// DELETE
				else if (opcao == 4) {
					writeRecord(new Record(comando.getChave(), "D"));
				}
				closeFile();
			} catch(InterruptedException e) {

			} catch(NullPointerException a) {

			}
		}
	}

	/**
	 * Descobre qual vai ser o filePath do arquivo de log ATUAL. É necessario pois o TImeBDMnager so vai rodar depois de um tempo, 
	 * Entao LogFileManager precisa ele buscar ele mesmo no incio. Depois fica a crgo de TImeBDManager mudar isso
	 * de log
	 * @return log File Path
	 */
	public String startLogFile(){
		Boolean firstExec = TimerManagerBD.firstExecution();
		int lastSnapDir;
		String logFilePath;
		// Cria a pasta inicia
		// cria o primiero arquivo de log seta o primeiro arquivo de log
		if(firstExec){
			lastSnapDir = 1;
			new File(TimerManagerBD.DEFAULT_FILEPATH_DB + "/SnapShot." + Integer.toString(lastSnapDir)).mkdir();
			return TimerManagerBD.DEFAULT_FILEPATH_DB + "/SnapShot." + Integer.toString(lastSnapDir) + "/" + "log." + Integer.toString(lastSnapDir - 1);
		} else {
			// Vai esperar haver um ultimo arquivo de log e pegar esse ultimo
			lastSnapDir = TimerManagerBD.getListCounters( TimerManagerBD.listDirectory( TimerManagerBD.DEFAULT_FILEPATH_DB)).get(0);
			logFilePath = TimerManagerBD.DEFAULT_FILEPATH_DB + "/SnapShot." + Integer.toString(lastSnapDir) + "/" + "log." + Integer.toString(lastSnapDir -1);
			System.out.println("Start Server : logFilePath : "  + logFilePath);
			// testa se esse arquivo existe, ele eh o que vai ser inicializado, entao, eh importante
			try {
				File file = new File(logFilePath);
				if (!file.exists()) {
					System.out.println("ERRO : DEVERIA HAVER O ARQUIVO DE LOG : " + logFilePath + " MAS PARECE QUE NAO EXISTE!");
					System.exit(1);
				}
			} catch(Exception e) {
				System.out.println("logFile : " + logFile);
				printException(e, "existFile");
			}
			return logFilePath;
		}
	}

	/**
	 * Usado pelo TimeMangerBD para seta o novo filepath do log a cada execução
	 * @param logFile
	 */
	public void setLogFile(String logFile){
		this.logFile = logFile;
	}

	// Carrega Records do log para a memoria
	public void loadRecordsFromFile(BlockingQueue < Comando > f2) {
		List < Record > listOfRecords = readRecords();
		for (Record record: listOfRecords) {
			executeRecord(record, f2);
		}
	}

	// Decide qual operaçao executar de acordo com a label/rotulo do Record
	public void executeRecord(Record record, BlockingQueue < Comando > f2) {
		switch (record.getLabel()) {
		case "C":
			createRecord(record, f2);
			break;
		case "U":
			updateRecord(record, f2);
			break;
		case "D":
			deleteRecord(record, f2);
			break;
		}
	}

	public void createRecord(Record record, BlockingQueue < Comando > f2) {
		try {
			f2.put(new Comando(1, record.getKey(), record.getData(), null));
		} catch(InterruptedException e) {}
	}

	public void updateRecord(Record record, BlockingQueue < Comando > f2) {
		try {
			f2.put(new Comando(3, record.getKey(), record.getData(), null));
		} catch(InterruptedException e) {}
	}

	public void deleteRecord(Record record, BlockingQueue < Comando > f2) {
		try {
			f2.put(new Comando(4, record.getKey(), null, null));
		} catch(InterruptedException e) {}
	}

	/**
	 * Retorna uma lista de REcords. DIferetne da Primeira entrega, vai pegar de um arquivo de SnapSHot
	 * e por iso o processo foi umpouco modidifcado.
	 * @return
	 */
	public List < Record > readRecords() {
		List < Record > listrecord = new ArrayList < Record > ();

		Record aData;
		String labelOption;
		BigInteger key;
		byte[] dataLine;
		String line;
		String[] splitted;
		// modificado para pegar de snap
		existFile(this.snapStartFile);

		// TODO: AKI SERA O MAIS NOVO ARQUIVO DE SNAPSHOT
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.snapStartFile))) {
			while ((line = bufferedReader.readLine()) != null) {
				
				/////// Carregar um LOG FILE

				// if (line.equals("")) {
				// 	continue; // linha vazia sera pulada;
				// }
				// splitted = line.split(" ");
				// labelOption = splitted[0];
				// key = new BigInteger(splitted[1]);
				// if (labelOption.equals("D")) { // DELETE
				// 	aData = new Record(key, labelOption);
				// } else { // CREATE OR UPDATE
				// 	line = bufferedReader.readLine();
				// 	dataLine = line.getBytes(); // vai ler a linha e agora vai guardar
				// 	aData = new Record(key, labelOption, dataLine);
				// }

				////// CARREGAR UM SNAPSHOT

				// line tem agora so o index
				if (line.equals("")) {
					continue; // linha vazia sera pulada;
				}
				key = new BigInteger(line);
				line = bufferedReader.readLine();
				dataLine = line.getBytes(); // vai ler a linha e agora vai guardar
				aData = new Record(key, "C", dataLine); // sempre um create do SnapSHot
				listrecord.add(aData);
			}
			bufferedReader.close();
		} catch(Exception e) {
			printException(e, "readRecords");
		}
		return listrecord;
	}

	// Adiciona Registros Record no arquivo
	public boolean writeRecord(Record record) {
		try {

			System.out.println("Label : " + record.getLabel());
			System.out.println("Key : " + record.getKey().toString());

			// Insercao de chaves no arquivo
			writer.write((record.getLabel() + " " + record.getKey().toString() + "\n").getBytes());
			
			// Se for CREATE ou UPDATE insere o dado concreto
			if (!record.getLabel().equals("D")) {
				writer.write((new String(record.getData()) + "\n").getBytes());
			}

			writer.flush();
			return true;
		} catch(Exception e) {
			printException(e, "writeRecord");
			return false;
		}
	}

	// Fecha o arquivo
	public void closeFile() {
		try {
			writer.close();
		} catch(Exception e) {
			printException(e, "closeFile");
		}
	}

	// Abre o arquivo ou o cria se nao existir
	public void openFile(String logFile) {
		try {
			File file = new File(logFile);
			if (!file.exists()) {
				file.createNewFile(); // cria o arquivo o mesmo se nao existir
			}
			this.writer = new FileOutputStream(file, true); // true é para adicionar no final, o modo 'append'
			this.writer.write(System.lineSeparator().getBytes());
		} catch(Exception e) {
			printException(e, "openFile");
		}
	}

	// Verifica se o arquivo existe ou nao. Se nao, o cria
	public void existFile(String logFile) {
		try {
			File file = new File(logFile);
			if (!file.exists()) {
				file.createNewFile(); // cria o arquivo o mesmo se nao existir
			}
		} catch(Exception e) {
			System.out.println("\t => logFile : " + logFile);
			printException(e, "existFile");
		}
	}

	// Imprime Execeções que podem aparecer num formato otimizado para debugar erros
	public void printException(Exception e, String func) {
		System.out.println("ERROR in function: " + func);
		System.out.println(e.toString());
		e.printStackTrace();
		System.exit(1);
	}

}


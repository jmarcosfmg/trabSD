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
			TimerTask task = new TimerManagerBD(mapManager.getMapa()); // Thread que executa a cada u segundos. Tem a referencia ao Mapa da maemoria, a classe 'Mapa'
			Timer timer = new Timer();
			// schedules the task to be run in an interval 
			timer.scheduleAtFixedRate(task, 0, TIME_UPDATE_BD);
			Thread t2 = new Thread(mapManager); //pega o que estiver na fila f2 e faz as operacoes na memoria
			t2.start();

			Thread t3 = new Thread(new LogFileManager(Fila_F3, Fila_F2)); //pega o que estiver na fila f3 e faz as operacoes do log
			t3.start();
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


*/
class TimerManagerBD extends TimerTask{

		private Mapa mapa;

		public static final String MY_DIRECTORY = System.getProperty("user.dir");

		private final String SNAP_SHOT_DIR = "SnapShot";
		private final String LOG_FILE = "log";
		private final String SNAP_FILE = "snap";
		private final String POINT = ".";
		private final String DEFAULT_FILEPATH = MY_DIRECTORY + "/" + "bd";
		
		public TimerManagerBD(Mapa mapa){
			this.mapa = mapa;
		}

		@Override
		public void run() {
			/**
			 * ================ ATENÇAO ==================
			 * Se atente com a diferneça entre a inicializaçao e uma execuçâo nromal
			 * Se atente ao fato de pastsa nâo existirem no inicio. Fazer essa verificaçâo e crialas se nao existir
			 * ==> Ele quer que tenha um arquivo de configuraçâo: usar JSON
			 * ==> PErguntar se pode ser array de Char para usar JSON
			 * ==> Ele quer que exista arquivos de teste para executar automaticamente as cosias da parte 1
			*/
			int nexCounter = 88;

			// task to run goes here
			System.out.println("Hello !!!");

			Boolean exist = existBdDir();
			Boolean firstExecution = !exist;
			if(!exist){
				System.out.println("nao havia o direotiro, entao foi criado bd/ ");
			}
			
			// log começa com zero e o primeiro numero sera 1
			if(firstExecution){
				nexCounter = 1; 

			}

			// Busca descobrir qual sera o proxmio counter a partir das pastas que ja existem
			if(!firstExecution){
				List<String> listDirectories = splitListOfFilesByType( listDirectory(TimerManagerBD.MY_DIRECTORY), SNAP_SHOT_DIR );
			  nexCounter = getNextCounter(listDirectories);

			}
			
			System.out.println(nexCounter);

			String nextDirFilePath = DEFAULT_FILEPATH + "/" + SNAP_SHOT_DIR + POINT + Integer.toString(nexCounter);
			String nextSnapFilePath = nextDirFilePath + "/" + SNAP_FILE + POINT + Integer.toString(nexCounter);
			String nextLogFilePath = nextDirFilePath + "/" + LOG_FILE + POINT + Integer.toString(nexCounter - 1);

			new File(nextDirFilePath).mkdir(); // cria diretorio

			/** SALVA ARQUIVO DE LOG */
			/** SALVA SNAP_SHOT */

			// Verifica se vai precisar deletar algum arquivo
			if(!firstExecution){

			}


			// Salav arquivos

			// List<String> files;
			// List<String> direc;
			
			// List<String>[] returnList = listFilesOfDirectory(MY_DIRECTORY);
			// files = returnList[0];
			// direc = returnList[1];

			// List<String> listDirectories = splitListOfFilesByType( listDirectory(TimerManagerBD.MY_DIRECTORY), SNAP_SHOT_DIR );
			// int nexCounter = getNextCounter(listDirectories);

			


		}

		// Retorna um array de duas posiçoes com array dinmaicos que tem
		// respectivamente a lista de arquivos e de direotiros do diretorio passado em parametro
		/************** Esta comentado posi talvez nâo vou precisar usar */
		// private List<String>[] listFilesOfDirectory(String directory){

		// 	List<String>[] returnList = new ArrayList<String>[2];
		// 	returnList[0]= new ArrayList<String>(); // arquivos
		// 	returnList[1]= new ArrayList<String>(); // diretorio

		// 	File folder = new File(directory);
    // 	File[] listOfFiles = folder.listFiles();
			
		// 	for (int i = 0; i < listOfFiles.length; i++) {
		// 		if (listOfFiles[i].isFile()) {
		// 			returnList[0].add(listOfFiles[i].getName());
		// 		} else if (listOfFiles[i].isDirectory()) {
		// 			returnList[1].add(listOfFiles[i].getName());
		// 		}
		// 	}
   
		// 	return returnList;
		// }

		// Retorna uma lista de diretorios de um diretorio
		private List<String> listDirectory(String directory){
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

		// Verifica se ha pelo menos uma pasta com "SnapShot" valido
		private Boolean existSnapShot(List<String> listDirectories){
			if(listDirectories.isEmpty()){
				return false;
			} else{
				// verificar se tem as pastas
				return true;
			}
		}

		/*
// Abre o arquivo ou o cria se nao existir
	public void openFile() {
		try {
			File file = new File(this.fileName);
			if (!file.exists()) {
				file.createNewFile(); // cria o arquivo o mesmo se nao existir
			}
			this.writer = new FileOutputStream(file, true); // true é para adicionar no final, o modo 'append'
			this.writer.write(System.lineSeparator().getBytes());
		} catch(Exception e) {
			printException(e, "openFile");
		}
	}
		*/

		// verifica se existe a paasta 'bd/'. Se nao existir, a cria
		private Boolean existBdDir(){
			File file = new File(DEFAULT_FILEPATH);
			if(!file.exists()){
				file.createNewFile();
				return true;
			} else {
				return false;
			}

		}

		// Verifica se ha alguma pasta que tenha o nome SnapHot no diretorio
		private Boolean existSnapShotDirectory(List<String> listDirectories){
			for (String dir : listDirectories) {
					if(isTypeFile(dir, SNAP_SHOT_DIR)){
						return true;
					}
			}
			return false;
		}

		// Verifica se um arquivo/pasta é de um certo tipo
		private Boolean isTypeFile(String file, String type){
			String[] splitedString = file.split(POINT);
			if(splitedString[0].equals(type)){
				return true;
			} else {
				return false;
			}
		}

		// retorna a lista de string de somente aquilo que eh de um certo tipo
		// tipo, nas patas bd, passando uma lista e "Snapshit" so vai voltar pastas que comecem com SnapShot
		private List<String> splitListOfFilesByType(List<String> files, String type){
			List<String> filesOfType = new ArrayList<String>();
			for (String arq : files) {
				if(isTypeFile(arq, type)){
					filesOfType.add(arq);
				}
			}
			return filesOfType;
		}
		
		// Retorna um array list de inteiros 
		private List<Integer> convertArrayStringToInt(List<String> listDirectories){
			List<Integer> intList = new ArrayList<Integer>();
			String aux[];
			String dirNumber;
			for (String dir : listDirectories) {
				aux = dir.split(POINT);
				dirNumber = aux[1];
				intList.add(Integer.parseInt(dirNumber));
			}
			return intList;
		}

		// Retorno o proximo contador a ser usado.
		private int getNextCounter(List<String> listDirectories){
			List<Integer> listNumbers = convertArrayStringToInt(listDirectories);
			int max = Collections.max(listNumbers);
			return max + 1;
		}

		// Retorna o numero da pasta a ser deletada ou -1 se nao achar
		private int getCounterToDelete(List<String> listDirectories){
			List<Integer> listCountsDir = convertArrayStringToInt(listDirectories);
			if(listCountsDir.size() <= 3){
				return -1;
			} else{
				return Collections.min(listCountsDir);
			}
		}

		// Deleta um direotiro: e tudo que tiver dentro dele. Passa todo o seu FilePath
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


// class Comando {

// 	private int operacao;
// 	private BigInteger chave;
// 	private byte[] valor;
// 	private PrintStream cliente;

// 	public Comando(int op, BigInteger ch, byte[] val, PrintStream cl) {
// 		this.operacao = op;
// 		this.chave = ch;
// 		this.valor = val;
// 		this.cliente = cl;
// 	}

// 	public int getOperacao() {
// 		return operacao;
// 	}
// 	public BigInteger getChave() {
// 		return chave;
// 	}
// 	public byte[] getValor() {
// 		return valor;
// 	}
// 	public PrintStream getCliente() {
// 		return cliente;
// 	}

// 	// Metodo para imprimir um objeto diretamente no 'print'
// 	public String toString() {
// 		String value = "";
// 		if(this.operacao == 1 || this.operacao == 3){
// 			value = " | " + new String(this.valor);
// 		}
// 		return "Comando = " + Integer.toString(this.operacao) + value;
// 	}

// }


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
	private final String fileName = "log"; // Valor constante do nome do arquivo
	public FileOutputStream writer;

	public LogFileManager(BlockingQueue < Comando > f3, BlockingQueue < Comando > f2) {
		Fila_F3 = f3;
		// Entrada Inicial de comandos em F2 a partir do arquivo (feito uma unica vez, ao start do server)
		/* Nao passa por F1 pois ai ele distribuiria para F3, que seria tirada na thread de LogFIleManger (essa mesma)
			 entao haveria uma duplicação no arquivo. Para evitar isso, mandamos direto para F2 que trata comandos para a memoria
		*/
		loadRecordsFromFile(f2);
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
				openFile();
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

	// Retorna uma Lista de 'Record's, que ssao as instancais ldias do arquivo log.txt
	public List < Record > readRecords() {
		List < Record > listrecord = new ArrayList < Record > ();

		Record aData;
		String labelOption;
		BigInteger key;
		byte[] dataLine;
		String line;
		String[] splitted;
		existFile();

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.fileName))) {
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals("")) {
					continue; // linha vazia sera pulada;
				}
				splitted = line.split(" ");
				labelOption = splitted[0];
				key = new BigInteger(splitted[1]);
				if (labelOption.equals("D")) { // DELETE
					aData = new Record(key, labelOption);
				} else { // CREATE OR UPDATE
					line = bufferedReader.readLine();
					dataLine = line.getBytes(); // vai ler a lionha e agora vai guardar
					aData = new Record(key, labelOption, dataLine);
				}
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
	public void openFile() {
		try {
			File file = new File(this.fileName);
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
	public void existFile() {
		try {
			File file = new File(this.fileName);
			if (!file.exists()) {
				file.createNewFile(); // cria o arquivo o mesmo se nao existir
			}
		} catch(Exception e) {
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


// class Record {

// 	private BigInteger key;
// 	private String label;
// 	private byte[] data;

// 	// Estrutura para agrupar todos os dados
// 	Record(BigInteger key, String label) {
// 		this.key = key;
// 		this.label = label;
// 		this.data = null;
// 	}

// 	Record(BigInteger key, String label, byte[] data) {
// 		this.key = key;
// 		this.label = label;
// 		this.data = data;
// 	}

// 	public byte[] getData() {
// 		return data;
// 	}
// 	public BigInteger getKey() {
// 		return key;
// 	}
// 	public String getLabel() {
// 		return label;
// 	}
// }
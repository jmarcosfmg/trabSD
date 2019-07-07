package atomixraft.server;

import atomixraft.handler.Comando;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Servidor extends Thread {

	// Parte que controla as conexões por meio de threads.
	// Note que a instanciação está no main.
	private static BlockingQueue <Comando> Fila_F1 = new LinkedBlockingDeque <>();
	private static BlockingQueue < Comando > Fila_F2 = new LinkedBlockingDeque < >();
	private static BlockingQueue < Comando > Fila_F3 = new LinkedBlockingDeque < >();

	public static void main(String args[]) {
		try {
			// criando um socket que fica escutando a porta 5082.
			ServerSocket s = new ServerSocket(5082);

			Thread t1 = new Thread(new Fila1Manager(Fila_F1, Fila_F2, Fila_F3)); //pega o que estiver na fila f1 e manda para fila f2 e fila f3
			t1.start();
			Thread t2 = new Thread(new MapManager(Fila_F2)); //pega o que estiver na fila f2 e faz as operacoes na memoria
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


// class Fila1Adder implements Runnable {

// 	private BlockingQueue < Comando > Fila_F1;
// 	private Socket conexao;

// 	public Fila1Adder(BlockingQueue < Comando > a, Socket s) {
// 		Fila_F1 = a;
// 		conexao = s;
// 	}

// 	public void run() {
// 		BigInteger chave = new BigInteger("0");
// 		int opcao = 0;
// 		byte[] valor = null;
// 		try {
// 			BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
// 			Scanner ler = new Scanner(System. in );
// 			PrintStream saida = null;
// 			saida = new PrintStream(conexao.getOutputStream());
// 			if (saida == null) System.out.println("Erro, saida nula");
// 			// primeiramente, espera-se a opcao do servidor
// 			while (true) {
// 				opcao = Integer.parseInt(entrada.readLine());
// 				// agora, verifica se string recebida é valida, pois
// 				// sem a conexão foi interrompida, a string é null.
// 				// Se isso ocorrer, deve-se terminar a execução.
// 				System.out.println("Cliente: " + conexao.getPort() + ". Opcao escolhida: " + opcao);
// 				if (opcao == 5) {
// 					throw new IOException();
// 				}
// 				// CREATE
// 				else if (opcao == 1) {
// 					chave = new BigInteger(entrada.readLine());
// 					valor = entrada.readLine().getBytes();
// 				}
// 				// GET
// 				else if (opcao == 2) {
// 					chave = new BigInteger(entrada.readLine());
// 					valor = null;
// 				}
// 				// UPDATE
// 				else if (opcao == 3) {
// 					chave = new BigInteger(entrada.readLine());
// 					valor = entrada.readLine().getBytes();
// 				}
// 				// DELETE
// 				else if (opcao == 4) {
// 					chave = new BigInteger(entrada.readLine());
// 					valor = null;
// 				}
// 				// Entrada de comandos em F1 pelo cliente
// 				Fila_F1.put(new Comando(opcao, chave, valor, new PrintStream(conexao.getOutputStream())));
// 			}
// 		} catch(Exception e) {
// 			try {
// 				System.out.println(conexao.getPort() + " se desconectou");
// 				conexao.close();
// 			} catch(IOException err) {}
// 		}
// 	}

// }


// class Fila1Manager implements Runnable {

// 	private BlockingQueue < Comando > Fila_F1;
// 	private BlockingQueue < Comando > Fila_F2;
// 	private BlockingQueue < Comando > Fila_F3;

// 	public Fila1Manager(BlockingQueue < Comando > f1, BlockingQueue < Comando > f2, BlockingQueue < Comando > f3) {
// 		Fila_F1 = f1;
// 		Fila_F2 = f2;
// 		Fila_F3 = f3;
// 	}

// 	// Distribuicao de comandos de F1 para F2 e F3
// 	public void run() {
// 		Comando comando;
// 		System.out.println("Fila1Manager rodando");
// 		while (true) {
// 			try {
// 				comando = (Comando) Fila_F1.take();
// 				System.out.println(comando);
// 				Fila_F2.put(comando);
// 				Fila_F3.put(comando);
// 			} catch(InterruptedException e) {

// 			}
// 		}
// 	}

// }


// class MapManager implements Runnable {

// 	private Mapa mapa;
// 	private BlockingQueue < Comando > Fila_F2;

// 	public MapManager(BlockingQueue < Comando > f2) {
// 		Fila_F2 = f2;
// 		mapa = new Mapa();
// 	}

// 	// Recebe Comandos de  F2 para inserir na memoria
// 	public void run() {
// 		Comando comando;
// 		int opcao;
// 		int flag = -1;
// 		System.out.println("MapManager rodando");
// 		while (true) {
// 			try {
// 				comando = (Comando) Fila_F2.take();
// 				opcao = comando.getOperacao();
// 				// CREATE
// 				if (opcao == 1) {
// 					flag = mapa.create(comando.getChave(), comando.getValor());
// 				}
// 				// GET
// 				else if (opcao == 2) {
// 					comando.getCliente().println(new String(mapa.read(comando.getChave())));
// 					flag = 0;
// 				}
// 				// UPDATE
// 				else if (opcao == 3) {
// 					flag = mapa.update(comando.getChave(), comando.getValor());
// 				}
// 				// DELETE
// 				else if (opcao == 4) {
// 					flag = mapa.delete(comando.getChave());
// 				}
// 				if (flag == 1) comando.getCliente().println("Falha na operacao!");
// 			} catch(InterruptedException e) {} catch(NullPointerException a) {}
// 		}
// 	}

// }


// class Mapa {

// 	private Map < BigInteger,
// 	byte[] > mapa;

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


// class LogFileManager implements Runnable {

// 	private BlockingQueue < Comando > Fila_F3;
// 	private final String fileName = "log"; // Valor constante do nome do arquivo
// 	public FileOutputStream writer;

// 	public LogFileManager(BlockingQueue < Comando > f3, BlockingQueue < Comando > f2) {
// 		Fila_F3 = f3;
// 		// Entrada Inicial de comandos em F2 a partir do arquivo (feito uma unica vez, ao start do server)
// 		/* Nao passa por F1 pois ai ele distribuiria para F3, que seria tirada na thread de LogFIleManger (essa mesma)
// 			 entao haveria uma duplicação no arquivo. Para evitar isso, mandamos direto para F2 que trata comandos para a memoria
// 		*/
// 		loadRecordsFromFile(f2);
// 	}

// 	// Recebe comandos de F3 para inserir no arquivo
// 	public void run() {
// 		BigInteger chave;
// 		byte[] valor;
// 		int opcao;
// 		Comando comando;
// 		System.out.println("LogFileManager rodando");
// 		while (true) {
// 			try {
// 				comando = (Comando) Fila_F3.take();
// 				openFile();
// 				opcao = comando.getOperacao();
// 				// CREATE
// 				if (opcao == 1) {
// 					writeRecord(new Record(comando.getChave(), "C", comando.getValor()));
// 				}
// 				// GET
// 				else if (opcao == 2) {}
// 				// UPDATE
// 				else if (opcao == 3) {
// 					writeRecord(new Record(comando.getChave(), "U", comando.getValor()));
// 				}
// 				// DELETE
// 				else if (opcao == 4) {
// 					writeRecord(new Record(comando.getChave(), "D"));
// 				}
// 				closeFile();
// 			} catch(InterruptedException e) {

// 			} catch(NullPointerException a) {

// 			}
// 		}
// 	}

// 	// Carrega Records do log para a memoria
// 	public void loadRecordsFromFile(BlockingQueue < Comando > f2) {
// 		List < Record > listOfRecords = readRecords();
// 		for (Record record: listOfRecords) {
// 			executeRecord(record, f2);
// 		}
// 	}

// 	// Decide qual operaçao executar de acordo com a label/rotulo do Record
// 	public void executeRecord(Record record, BlockingQueue < Comando > f2) {
// 		switch (record.getLabel()) {
// 		case "C":
// 			createRecord(record, f2);
// 			break;
// 		case "U":
// 			updateRecord(record, f2);
// 			break;
// 		case "D":
// 			deleteRecord(record, f2);
// 			break;
// 		}
// 	}

// 	public void createRecord(Record record, BlockingQueue < Comando > f2) {
// 		try {
// 			f2.put(new Comando(1, record.getKey(), record.getData(), null));
// 		} catch(InterruptedException e) {}
// 	}

// 	public void updateRecord(Record record, BlockingQueue < Comando > f2) {
// 		try {
// 			f2.put(new Comando(3, record.getKey(), record.getData(), null));
// 		} catch(InterruptedException e) {}
// 	}

// 	public void deleteRecord(Record record, BlockingQueue < Comando > f2) {
// 		try {
// 			f2.put(new Comando(4, record.getKey(), null, null));
// 		} catch(InterruptedException e) {}
// 	}

// 	// Retorna uma Lista de 'Record's, que ssao as instancais ldias do arquivo log.txt
// 	public List < Record > readRecords() {
// 		List < Record > listrecord = new ArrayList < Record > ();

// 		Record aData;
// 		String labelOption;
// 		BigInteger key;
// 		byte[] dataLine;
// 		String line;
// 		String[] splitted;
// 		existFile();

// 		try {
// 			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.fileName));
// 			while ((line = bufferedReader.readLine()) != null) {
// 				if (line.equals("")) {
// 					continue; // linha vazia sera pulada;
// 				}
// 				splitted = line.split(" ");
// 				labelOption = splitted[0];
// 				key = new BigInteger(splitted[1]);
// 				if (labelOption.equals("D")) { // DELETE
// 					aData = new Record(key, labelOption);
// 				} else { // CREATE OR UPDATE
// 					line = bufferedReader.readLine();
// 					dataLine = line.getBytes(); // vai ler a lionha e agora vai guardar
// 					aData = new Record(key, labelOption, dataLine);
// 				}
// 				listrecord.add(aData);
// 			}
// 			bufferedReader.close();
// 		} catch(Exception e) {
// 			printException(e, "readRecords");
// 		}
// 		return listrecord;
// 	}

// 	// Adiciona Registros Record no arquivo
// 	public boolean writeRecord(Record record) {
// 		try {

// 			System.out.println("Label : " + record.getLabel());
// 			System.out.println("Key : " + record.getKey().toString());

// 			// Insercao de chaves no arquivo
// 			writer.write((record.getLabel() + " " + record.getKey().toString() + "\n").getBytes());

// 			// Se for CREATE ou UPDATE insere o dado concreto
// 			if (!record.getLabel().equals("D")) {
// 				writer.write((new String(record.getData()) + "\n").getBytes());
// 			}

// 			writer.flush();
// 			return true;
// 		} catch(Exception e) {
// 			printException(e, "writeRecord");
// 			return false;
// 		}
// 	}

// 	// Fecha o arquivo
// 	public void closeFile() {
// 		try {
// 			writer.close();
// 		} catch(Exception e) {
// 			printException(e, "closeFile");
// 		}
// 	}

// 	// Abre o arquivo ou o cria se nao existir
// 	public void openFile() {
// 		try {
// 			File file = new File(this.fileName);
// 			if (!file.exists()) {
// 				file.createNewFile(); // cria o arquivo o mesmo se nao existir
// 			}
// 			this.writer = new FileOutputStream(file, true); // true é para adicionar no final, o modo 'append'
// 			this.writer.write(System.lineSeparator().getBytes());
// 		} catch(Exception e) {
// 			printException(e, "openFile");
// 		}
// 	}

// 	// Verifica se o arquivo existe ou nao. Se nao, o cria
// 	public void existFile() {
// 		try {
// 			File file = new File(this.fileName);
// 			if (!file.exists()) {
// 				file.createNewFile(); // cria o arquivo o mesmo se nao existir
// 			}
// 		} catch(Exception e) {
// 			printException(e, "existFile");
// 		}
// 	}

// 	// Imprime Execeções que podem aparecer num formato otimizado para debugar erros
// 	public void printException(Exception e, String func) {
// 		System.out.println("ERROR in function: " + func);
// 		System.out.println(e.toString());
// 		e.printStackTrace();
// 		System.exit(1);
// 	}

// }


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

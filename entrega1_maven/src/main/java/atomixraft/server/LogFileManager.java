package atomixraft.server;

import atomixraft.handler.Comando;
import atomixraft.handler.Record;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;

public class LogFileManager implements Runnable {

	private BlockingQueue <Comando> Fila_F3;
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

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.fileName));
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
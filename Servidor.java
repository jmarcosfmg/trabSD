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
				System.out.println("Recebida conexao "+conexao);
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
				System.out.println("opcao escolhida: "+opcao);
				if (opcao == 5){
					break;
				}
				else if (opcao == 1){
					saida.println("Opcao selecionada = "+opcao);
					chave= new BigInteger(entrada.readLine());
					//System.out.println(chave);
					//saida.println("Entre com o valor:");
					valor= entrada.readLine().getBytes();	
					if(mapa.create(chave, valor) == 0) saida.println("Inserido com sucesso");
					else saida.println("Erro na insercao");
				}
				else if (opcao == 2){
					saida.println("Opcao selecionada = "+opcao);
					chave = new BigInteger(entrada.readLine());
					valor= mapa.read(chave);
					String msgDecode = new String(valor); // convertendo byte para string
					saida.println("Valorprocurado: "+ msgDecode); // Exibir
					
				}
				else if (opcao == 3){
					saida.println("Opcao selecionada = "+opcao);
					chave= new BigInteger(entrada.readLine());
					saida.println("Entre com o valor:");
					valor= entrada.readLine().getBytes();	
					if(mapa.update(chave,valor) == 0) saida.println("Atualizacao feita com sucesso");
					else saida.println("Erro");
				}
				else if (opcao == 4){
					//saida.println("Opcao selecionada = "+opcao+"Entre com a chave:");
					chave= new BigInteger(entrada.readLine());
					if(mapa.delete(chave) == 0) saida.println("Exclusao feita com sucesso");
					else saida.println("Erro");
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
				}catch(NumberFormatException e){}
			}
			// Uma vez que o cliente enviou linha em branco, retira-se
			// fluxo de saída do vetor de clientes e fecha-se conexão.
			Fila_F1.remove(saida);
			conexao.close();
		}
		catch (IOException e) {
			// Caso ocorra alguma excessão de E/S, mostre qual foi.
			System.out.println("IOException: " + e);
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
}

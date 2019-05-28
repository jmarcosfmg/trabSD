package util.datahandler;

import java.io.PrintStream;
import java.math.BigInteger; 

public class Comando {

	private int operacao;
	private BigInteger chave;
	private byte[] valor;
	private PrintStream cliente;

	public Comando(int op, BigInteger ch, byte[] val, PrintStream cl) {
		this.operacao = op;
		this.chave = ch;
		this.valor = val;
		this.cliente = cl;
	}

	public int getOperacao() {
		return operacao;
	}
	public BigInteger getChave() {
		return chave;
	}
	public byte[] getValor() {
		return valor;
	}
	public PrintStream getCliente() {
		return cliente;
	}

	// Metodo para imprimir um objeto diretamente no 'print'
	public String toString() {
		String value = "";
		if(this.operacao == 1 || this.operacao == 3){
			value = " | " + new String(this.valor);
		}
		return "Comando = " + Integer.toString(this.operacao) + value;
	}

}
package util.datahandler;
/*
Para dar certo tem que:
1. o pacote as pastas tem que ter no minimo 3 letras
2. deve-se por public em quase tudo, se nao por, nao da pra acessear de outro pacote
3. Importar as coisas
*/
import java.math.BigInteger; 

public class Record {

	private BigInteger key;
	private String label;
	private byte[] data;

	// Estrutura para agrupar todos os dados
	public Record(BigInteger key, String label) {
		this.key = key;
		this.label = label;
		this.data = null;
	}

	public Record(BigInteger key, String label, byte[] data) {
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
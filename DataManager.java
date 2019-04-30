import java.util.Map;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.lang.SecurityException;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.io.*;

public class DataManager {

  private Map<BigInteger, String> hashMap = new HashMap<>();
  private LogFileManager logfile = new LogFileManager();

  // Carrega Records do log.txt para a memoria
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
    hashMap.put(record.getKey(), record.getData());
  }

  public void updateRecord(Record record){
    hashMap.put(record.getKey(), record.getData());
  }

  public void deleteRecord(Record record){
    if(this.hashMap.containsKey(record.getKey())){
      hashMap.remove(record.getKey());
    }
  }

  // Executa o record na memoria e no disco
  public void executeCommand(Record record){
    executeRecord(record);
    this.logfile.writeRecord(record);
  }

  // Imprime o HasmMap como key --> value
  public void printHashMap(){
    for (BigInteger aKey: this.hashMap.keySet()){
      System.out.println("key: " + aKey + " | Value: " + hashMap.get(aKey).toString() );
    }
    if(this.hashMap.isEmpty()){
      System.out.println("O HashMap na memoria esta vazio!");
    }
  }

  public static void main(String[] args) {
     DataManager manager = new DataManager();
     // READ : lendo do a rquivo
     manager.loadRecordsFromFile();
     manager.printHashMap();
     // WRITE : Escrevendo no arquivo em HasrCode (Esse valor constante, deverá ser mudado para ser o que o usuário passar)
     manager.logfile.openFile();
     // Exemplo de como deve ser passado, perceba que vai sair
     // Mude o valor toda hora para poder comprar o primeiro printHashMap com o Segundo
     manager.executeCommand( new Record(new BigInteger("109"), "C", "ESCRITA-re"));
     manager.logfile.closeFile();
     // Result final
     System.out.println();
     manager.printHashMap();
  }

}

class Record {

  private BigInteger key;
  private String label;
  private String data;

  /*
    Record é o 'struct/Json' que caracterisa um comando, serve para juntar os 3 valores: operaçao, chave do map e valor
  */
  Record(BigInteger key, String label){
    this.key = key;
    this.label = label;
    this.data = null;
  }

  Record(BigInteger key, String label, String data){
    this.key = key;
    this.label = label;
    this.data = data;
  }

  public String getData() {
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

  private final String fileName = "log.txt"; // Valor constantedo arquivo

  public FileWriter writer;

  // Retorna uma Lista do Tipo Record que leu do Arquivo 'log.txt'
  public List<Record> readRecords(){
    List<Record> listrecord = new ArrayList<Record>();
    Record aData;
    String labelOption ;
    BigInteger key;
    String dataLine;
    try(Scanner input = new Scanner(Paths.get(this.fileName))){
      while (input.hasNext()){
        labelOption = input.next();
        key = input.nextBigInteger();
        if(labelOption.equals("D")){
          // System.out.println("DELETE");
          // System.out.println("key: " + key);
          aData = new Record(key, labelOption);
        } else {
          // System.out.println("CREATE OR UPDATE");
          input.nextLine(); // pular a linha
          dataLine = input.nextLine(); // vai ler a lionha e agora vai guardar
          // System.out.println("dataLine : " + dataLine);
          aData = new Record(key, labelOption, dataLine);
          // System.out.println(key);
        }
        listrecord.add(aData);
      }
      input.close();
    } catch (Exception e){
       printException(e, "readRecords");
    }
    return listrecord;
  }

   // Adiciona Registros Record no arquivo
   public void writeRecord(Record record){
     try {
       writer.write(record.getLabel() + " " + record.getKey().toString());
       if(!record.getLabel().equals("D")){
         writer.write("\n" + record.getData());
       }
       writer.flush();
     } catch (Exception e) {
       printException(e, "writeRecord");
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
        this.writer = new FileWriter(file, true); // true é para adicionar no final, o modo 'append'
        this.writer.write(System.lineSeparator());
      } catch (Exception e) {
        printException(e, "openFile");
      }
   }

   // Imprime Execeções que podem aparecer de forma melhor para Debugar
   public void printException(Exception e, String func){
     System.out.println("ERROR : EXIT : funcao: " + func);
     System.out.println(e.getMessage());
     System.out.println(e.toString());
     e.printStackTrace();
     System.exit(1);
   }

}

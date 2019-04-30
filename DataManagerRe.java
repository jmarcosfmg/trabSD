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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DataManagerRe {

  private Map<BigInteger, byte[]> hashMap = new HashMap<>();
  private LogFileManager logfile = new LogFileManager();

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
      System.out.println("key [bytes]: " + aKey + " | Value: " + new String(hashMap.get(aKey)) );
    }
    if(this.hashMap.isEmpty()){
      System.out.println("O HashMap na memoria esta vazio!");
    }
  }

  public static void main(String[] args) {
     DataManagerRe manager = new DataManagerRe();
     // READ : lendo do a rquivo
     manager.loadRecordsFromFile();
     manager.printHashMap();
     // WRITE : Escrevendo no arquivo em HasrCode (Esse valor constante, deverá ser mudado para ser o que o usuário passar)
     manager.logfile.openFile();
     // Exemplo de como deve ser passado, perceba que vai sair
     // Mude o valor toda hora para poder comprar o primeiro printHashMap com o Segundo
     manager.executeCommand( new Record(new BigInteger("107"), "C", "RAFA".getBytes()));
     manager.logfile.closeFile();
     // Result final
     System.out.println();
     manager.printHashMap();
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
   public void writeRecord(Record record){
     try {
       writer.write( (record.getLabel() + " " + record.getKey().toString() + "\n").getBytes() );
       if(!record.getLabel().equals("D")){
         writer.write( (new String(record.getData()) + "\n").getBytes() );
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
     System.out.println("ERROR : EXIT : funcao: " + func);
     System.out.println(e.getMessage());
     System.out.println(e.toString());
     e.printStackTrace();
     System.exit(1);
   }

}

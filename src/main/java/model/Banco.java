package model;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Banco implements Serializable {

    List<Conta> contas = new ArrayList<Conta>();
    private static final long serialVersionUID = 1L;

    public Banco(){

    }


    public Conta CriarConta(String nome, String cpf, String senha){

        boolean emUso = false;

        for (Conta conta: contas) {
            if(conta.cpf.equals(cpf)){
                emUso = true;
                break;
            }
        }
        if(!emUso){
            Conta conta = new Conta(nome, cpf, senha);
            contas.add(conta);
            this.saveSerializable();
            return conta;
        } else {
            return null;
        }

    }

    public List<Conta> retornaContasNome(String nome){
        List<Conta> contas = new ArrayList<Conta>();
        String re = ""+nome+".*";
        for ( Conta conta: this.contas ) {
            if(conta.nome.matches(re)){
                contas.add(conta);
            }
        }
        return contas;
    }

    public Conta getConta(String cpf){

        for (Conta conta: contas) {
            if(conta.cpf.equals(cpf)){
                return conta;
            }
        }
        return null;
    }

    public boolean transferencia(Conta origem, String cpf, Double valor){
        if(origem.saldo - valor < 0){
            return false;
        }

        Conta destino = this.getConta(cpf);
        Operacao operacao = new Operacao(origem, destino, valor);
        origem.transferencia(operacao);
        destino.transferencia(operacao);
        this.saveSerializable();

        return true;
    }

    public boolean autenticacao(String cpf, String senha){
        Conta conta = getConta(cpf);
        if(conta != null){
            return conta.senha.equals(senha);
        }
        return false;
    }

    public Banco loadSerializable(){
        try
        {
            //Carrega o arquivo
            FileInputStream arquivoLeitura = new FileInputStream("./saida.dat");
            // Classe responsavel por recuperar os objetos do arquivo
            ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura);
            Banco bancoRetorno = (Banco) objLeitura.readObject();
            objLeitura.close();
            arquivoLeitura.close();
            return bancoRetorno;
        }
        catch(Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    public void saveSerializable(){
        try
        {
            //Gera o arquivo para armazenar o objeto
            FileOutputStream arquivoGrav =
                    new FileOutputStream("./saida.dat");
            //Classe responsavel por inserir os objetos
            ObjectOutputStream objGravar = new ObjectOutputStream(arquivoGrav);
            //Grava o objeto cliente no arquivo
            objGravar.writeObject(this);
            objGravar.flush();
            objGravar.close();
            arquivoGrav.flush();
            arquivoGrav.close();
            System.out.println("Objeto gravado com sucesso!");
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

}

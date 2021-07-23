package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Banco implements Serializable {

    List<Conta> contas = new ArrayList<Conta>();
    private static final long serialVersionUID = 1L;

    public Banco(){

    }

    public boolean verificaCpf(String cpf){
        // percorre as contas

        boolean emUso = false;

        for (Conta conta: contas) {
            if(conta.cpf.equals(cpf)){
                emUso = true;
                break;
            }
        }

        return emUso;
    }

    public Conta getConta(String cpf){

        for (Conta conta: contas) {
            if(conta.cpf.equals(cpf)){
                return conta;
            }
        }
        return null;
    }

    public Double getSaldo(String cpf){
        return this.getConta(cpf).getSaldo();
    }

    public List<Operacao> getHistorico(String cpf){
        return this.getConta(cpf).getHistorico();
    }

    public Conta CriarConta(String nome, String cpf, String senha){

        Conta conta = new Conta(nome, cpf, senha);
        contas.add(conta);
        return conta;

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

    public Operacao transferencia(String origem, String destino, Double valor){

        Conta out = this.getConta(origem);
        Conta in = this.getConta(destino);

        Operacao operacao = new Operacao(out, in, valor);
        out.transferencia(operacao);
        in.transferencia(operacao);

        return operacao;
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

package model;

import java.util.ArrayList;
import java.util.List;

public class Conta {

    String cpf = "";
    String nome = "";
    String senha = "";
    Double saldo = 1000.0;

    List<Operacao> historico = new ArrayList<Operacao>();

    public Conta(String nome, String cpf, String senha){
        this.nome = nome;
        this.cpf = cpf;
        this.senha = senha;
    }

    public boolean autenticacao(String nome, String senha){

        //Verifica com o servidor se esta batendo
        // Se tiver batendo, seta this.cpf = cpf e this.senha = senha
        return true;

    }

    public Double getSaldo(){

        return this.saldo;

    }

    public List<Operacao> getHistorico(){

        return this.historico;

    }

    public void transferencia(Operacao operacao){

        if(operacao.origem.cpf.equals(this.cpf)){
            this.saldo = this.saldo - operacao.valor;
        } else {
            this.saldo = this.saldo + operacao.valor;
        }

        historico.add(operacao);

    }

    @Override
    public String toString() {
        return "cpf=" + this.cpf +", nome=" + this.nome;
    }

    public String getCpf() {
        return cpf;
    }
}

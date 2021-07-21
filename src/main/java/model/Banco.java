package model;

import java.util.ArrayList;
import java.util.List;

public class Banco {

    List<Conta> contas = new ArrayList<Conta>();

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

        return true;
    }

    public boolean autenticacao(String cpf, String senha){
        Conta conta = getConta(cpf);
        if(conta != null){
            return conta.senha.equals(senha);
        }
        return false;
    }

}

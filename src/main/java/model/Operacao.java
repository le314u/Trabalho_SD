package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Operacao implements Serializable{

    Conta origem;
    Conta destino;
    Double valor;
    String data;

    private static final long serialVersionUID = 1L;

    public Operacao(Conta origem, Conta destino, Double valor){
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.data = getDateTime();
    }

    public Operacao(Conta origem, Conta destino, Double valor, String data){
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.data = data;
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


    @Override
    public String toString() {
        return "origem=" + this.origem.cpf +
                "\ndestino=" + this.destino.cpf +
                "\nvalor=R$" + this.valor +
                "\ndata=" + this.data + "\n------";

    }
}

package com.mycompany.mavemproject1;
import model.Banco;
import org.json.JSONObject;
import view.CLI;

public class Main {

    static private void debug(Banco banco) {

        banco.CriarConta("lucas","1", "123");
        banco.CriarConta("outro","2", "123");

    }

    public static void main(String[] args) {

        Banco banco = new Banco();
        debug(banco);
        CLI view = new CLI(banco);

    }


}

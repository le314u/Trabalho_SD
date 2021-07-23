package com.mycompany.mavemproject1;
import model.Banco;
import view.CLI;
import org.jgroups.*;
import org.jgroups.util.*;

public class Main {

    static private void debug(Banco banco) {

        banco.CriarConta("lucas","1", "123");
        banco.CriarConta("outro","2", "123");

    }

    public static void main(String[] args) {

        Banco banco = new Banco();
        //debug(banco);
        banco = banco.loadSerializable();



        CLI view = new CLI(banco);

    }


}

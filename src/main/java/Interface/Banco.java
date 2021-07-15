/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Banco {

    Scanner in = new Scanner(System.in);
    String contaLogada = "";

    public Banco() {

        this.showMenuInicial();

    }

    public void runTime() {
        boolean running = true;
        while (running) {
            int option = showMenuInicial();
            switch (option) {
                case 1:
                    JSONObject cadastro = showMenuCadastro();
                    //cadastrar no banco

                    break;
                case 2:
                    JSONObject usuario = showMenuLogin();
                    if (verificaLogin(usuario)) {
                        //setar variavel global do usuario logado
                        showMenuFuncionalidades();
                        this.contaLogada = "";
                    }
                    ;
                    break;
                default:
                    running = false;
                    break;
            }
        }

    }

    public void showMenuFuncionalidades() {

        int option = -1;
        while (option != 0) {
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1- Transferência:");
            System.out.println("Digite 2- Extrato:");
            System.out.println("Digite 3- Consulta:");
            System.out.println("Digite 0 - sair:");
            // Pega a opções do menu
            try {
                option = getOption();
            } catch (Exception e) {
                option = -1;
            }

            switch (option) {
                case 1:
                    showMenuTransferencia();
                    break;
                case 2:
                    showMenuExtrato();
                    break;
                case 3:
                    showMenuConsulta();
                    break;

            }
        }

    }

    private JSONObject showMenuTransferencia() {
        //Consulta nas contas
        JSONObject receptor = new JSONObject();
        String cpf = showMenuPesquisa();
        System.out.println("digite o valor: ");
        String input = getInput();
        float valor = Float.parseFloat(input);
        receptor.put("cpf", cpf);
        receptor.put("valor", valor);
        return receptor;
    }

    private String showMenuPesquisa() {
        System.out.println("digite o nome da pessoa: ");
        String nome = getInput();
        //fazer a pesquisa do banco
        //lista nome e CPF possivel
        System.out.println("digite o numero da pessoa");
        int option = getOption();
        //retorna o cpf da pessoa de indice option
        return "";
    }

    public JSONObject showMenuLogin() {
        System.out.println("Digite sua nome:");
        String nome = getInput();
        System.out.println("Digite sua senha:");
        String senha = getInput();
        JSONObject usuario = new JSONObject();
        usuario.put("nome", nome);
        usuario.put("senha", senha);
        return usuario;
    }


    public boolean verificaLogin(JSONObject usuario) {

        return true;
    }

    public JSONObject showMenuCadastro() {
        System.out.println("Digite um nome:");
        String nome = getInput();
        System.out.println("Digite um cpf:");
        String cpf = getInput();
        System.out.println("Digite uma senha:");
        String senha = getInput();
        //Prepara o Json para o retorno
        JSONObject retorno = new JSONObject();
        retorno.put("nome", nome);
        retorno.put("cpf", cpf);
        retorno.put("senha", senha);
        return retorno;
    }

    public Integer getOption() {
        String option = in.nextLine();
        return Integer.parseInt(option);
    }

    public String getInput() {
        return in.nextLine();
    }

    public int showMenuInicial() {
        int option = 0;
        while (option < 1 || option > 2) {
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1 - Para criar uma conta");
            System.out.println("Digite 2 - Para entrar em uma conta");
            // Pega a opçõ do menu
            try {
                option = getOption();
            } catch (Exception e) {
                option = 0;
            }
        }
        return option;
    }

    public void clear() {
        //Implementar para limpar a tela
    }
}

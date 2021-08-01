package view;

import java.util.Scanner;

import org.json.JSONObject;

public class MenuIO {


    Scanner in = new Scanner(System.in);

    public MenuIO() {
    }

    public int optInicial() {
        int option = 0;
        while (option < 1 || option > 2) {
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1 - Para criar uma conta");
            System.out.println("Digite 2 - Para entrar em uma conta");
            option = getOption();
        }
        return option;
    }

    public int optFuncionalidades() {
        int option = 0;
        while (option == 0) {
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1 - Transferência:");
            System.out.println("Digite 2 - Extrato:");
            System.out.println("Digite 3 - Consulta:");
            //System.out.println("Digite 0 - sair:");
            // Pega a opções do menu
            try {
                option = getOption();
            } catch (Exception e) {
                option = -1;
            }
        }
        return option;
    }

    public JSONObject getLogin() {
        System.out.println("Digite seu cpf:");
        String cpf = getInput();
        System.out.println("Digite sua senha:");
        String senha = getInput();
        JSONObject usuario = new JSONObject();
        usuario.put("cpf", cpf);
        usuario.put("senha", senha);
        return usuario;
    }

    public JSONObject getCadastro() {
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

    // Depende da pesquisa
    public JSONObject getTransferencia(JSONObject data) {
        //Consulta nas contas
        String cpf = data.getString("cpf");
        System.out.println("digite o valor: ");
        Double valor = Double.parseDouble(getInput());
        JSONObject receptor = new JSONObject();
        receptor.put("cpf", cpf);
        receptor.put("valor", valor);
        return receptor;
    }

    public JSONObject getPesquisa() {
        System.out.println("digite o nome da pessoa: ");
        String nome = getInput();
        JSONObject result = new JSONObject();
        result.put("nome", nome);
        return result;
    }

    public void getExtrato(JSONObject data) {
        // Percorre todo o extrado
        System.out.println("Implementar: ");
    }

    public void showTransferencia(Boolean resp){
        if(resp){
            System.out.println("Transferência realizada com sucesso!");
        } else {
            System.out.println("Falha na Transferência.");
        }
    }

    public void showCadastro(Boolean resp){
        if(resp){
            System.out.println("Cadastro realizado com sucesso!");
        } else {
            System.out.println("Falha no Cadastro.");
        }
    }

    public void showLogin(Boolean resp){
        if(resp){
            System.out.println("Logado com sucesso!");
        } else {
            System.out.println("Falha na Autentificação.");
        }
    }

    public void showConsulta(JSONObject extrato){

    }

    //Depois trocar tipo para ficar no padrão
    public String getCpf(JSONObject list) {
            // Mostra todas as pessoas da lista
            System.out.println("digite o numero da pessoa");
            int option = getOption();
            // De acordo com o numero retorna o CPF
            return "";

    }

    private Integer getOption() {
        try {
            return Integer.parseInt(in.nextLine());
        } catch (Exception e) {
            return 0;
        }
    }

    private String getInput() {
        return in.nextLine();
    }

    public void pause(){
        System.out.println("Digite enter para continuar:");
        in.nextLine();
    }

    private void clear () {
        //Limpa a tela no windows, no linux e no MacOS
    }
}


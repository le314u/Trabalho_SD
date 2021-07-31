package view;

import java.util.List;
import java.util.Scanner;

import model.Banco;
import model.Conta;
import model.Operacao;
import org.json.JSONObject;

public class CLI {

    Scanner in = new Scanner(System.in);
    Banco banco = null;
    Conta contaLogada = null;


    public CLI(Banco banco) {

        this.banco = banco;
        this.runTime();
        //puxa banco
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
                        this.contaLogada = this.banco.getConta(usuario.getString("cpf"));
                        showMenuFuncionalidades();
                        this.contaLogada = null;
                    }

                    break;
                default:
                    running = false;
                    break;
            }
        }

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

    public void showMenuFuncionalidades() {

        int option = -1;
        while (option != 0) {
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1 - Transferência:");
            System.out.println("Digite 2 - Extrato:");
            System.out.println("Digite 3 - Consulta:");
            System.out.println("Digite 0 - sair:");
            // Pega a opções do menu
            try {
                option = getOption();
            } catch (Exception e) {
                option = -1;
            }

            switch (option) {
                case 1:
                    JSONObject data = showMenuTransferencia();
                    boolean result = this.banco.transferencia(contaLogada, data.getString("cpf"), data.getDouble("valor"));
                    if(result){
                        System.out.println("Transferência realizada com sucesso!");
                    } else System.out.println("Falha na Transferência.");
                    break;
                case 2:
                    showMenuExtrato();
                    break;
                case 3:
                    showMenuConsulta();
                    break;

            }
            pause();
        }

    }

    public JSONObject showMenuLogin() {
        System.out.println("Digite seu cpf:");
        String cpf = getInput();
        System.out.println("Digite sua senha:");
        String senha = getInput();
        JSONObject usuario = new JSONObject();
        usuario.put("cpf", cpf);
        usuario.put("senha", senha);
        return usuario;
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

    private JSONObject showMenuTransferencia() {
        //Consulta nas contas
        JSONObject receptor = new JSONObject();
        String cpf = showMenuPesquisa();
        System.out.println("digite o valor: ");
        String input = getInput();
        Double valor = Double.parseDouble(input);
        receptor.put("cpf", cpf);
        receptor.put("valor", valor);
        return receptor;
    }

    private void showMenuExtrato(){

        List<Operacao> historico = this.contaLogada.getHistorico();
        int tamanho = historico.size();

        if(tamanho == 0){
            System.out.println("A conta não possui movimentação.");
        } else {
            for(int i=tamanho - 1; i >= 0; i--) {
                System.out.println(historico.get(i));
            }
        }
    }

    private void showMenuConsulta(){
        System.out.println("R$"+contaLogada.getSaldo());
    }

    private String showMenuPesquisa() {
        System.out.println("digite o nome da pessoa: ");
        String nome = getInput();
        List<Conta> contas = this.banco.retornaContasNome(nome);
        if(contas != null){
            int index = 0;
            for (Conta conta : contas ) {
                System.out.println("["+index+"]\t"+conta.toString());
            }
            System.out.println("digite o numero da pessoa");
            int option = getOption();
            return contas.get(option).getCpf();
        }
        return "";
    }

    public boolean verificaLogin(JSONObject usuario) {
        return this.banco.autenticacao(usuario.getString("cpf"), usuario.getString("senha"));
    }

    public Integer getOption() {
        String option = in.nextLine();
        return Integer.parseInt(option);
    }

    public String getInput() {
        return in.nextLine();
    }

    public void pause(){
        System.out.println("Digite enter para continuar:");
        in.nextLine();
    }

    public void clear() {
        //Implementar para limpar a tela
    }

}

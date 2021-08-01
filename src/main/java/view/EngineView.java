package view;

import common.Payload;
import model.Operacao;
import org.json.JSONObject;

import java.util.List;

public class EngineView {

    MenuIO menuIO = new MenuIO();
    String token = "";
    ConnectView connect;

    public EngineView() {
        //this.connect = new ConnectView();
        this.runTime();
    }

    public void runTime() {
        while (true) {
            this.Inicial(menuIO.optInicial());
        }
    }

    public void Inicial(Integer option){
        switch (option) {
            case 1://criar uma conta
                JSONObject cadastro = menuIO.getCadastro();
                Payload pCadastro = new Payload(cadastro, "cadastro","view",true);
                //Envia dados para criação de conta
                // JGROUPS
                // menu menu.showCadastro(RESPOSTA)
                break;
            case 2://entrar em uma conta
                JSONObject usuario = menuIO.getLogin();
                Payload pLogin = new Payload(usuario, "cadastro","view",true);
                //Envia dados para logar em uma conta
                // JGROUPS
                //Recebe a id + hash que é um token informando que foi logado
                Boolean logado = true; // logar(JSONObject resp);
                menuIO.showLogin(logado);
                if ( logado ) {
                    Integer opt = -1;
                    while(opt != 0){
                        opt = menuIO.optFuncionalidades();
                        this.Funcionalidades(opt);
                    }
                    deslogar();
                }
                break;
        }
    }

    public void Funcionalidades(Integer option) {
        switch (option) {
            case 1://Transferência
                JSONObject nome = menuIO.getPesquisa();
                //Jgroups -- Procura a Pessoa
                JSONObject dest = null;
                // JSONObject dest = menuIO.getCpf(JSONObject resp);
                JSONObject dataTransf = menuIO.getTransferencia(dest);
                Payload pCadastro = new Payload(dataTransf, "transferencia","view",true);
                //envia os dados para fazer a transferência
                boolean result = true;// Recebe a informação se a  transferência foi ou não realizada
                menuIO.showTransferencia(result);
                break;
            case 2://Extrato
                // Pesquisa
                // Pega Dados
                // Faz a pesquisa
                JSONObject aux = null;
                menuIO.getExtrato(aux);
                Payload pExtrato = new Payload(null, "extrato","view",true);
                //envia mensagem do extrato
                //tenta mostrar o extrato
                //showMenuExtrato();
                break;
            case 3://Consulta
                JSONObject consulta = menuIO.getPesquisa();
                Payload pConsulta = new Payload(consulta, "extrato","view",true);
                //envia mensagem de consulta
                //menuIO.menuConsulta();
                break;
            default:
                //Sai
                break;

        }
        menuIO.pause();
    }

    private Boolean deslogar(){
        this.token = "";
        return true;
    }

    private Boolean logar(JSONObject data){
        // de acordo com os dados verifica se logou ou não
        return !this.token.equals("");
    }

    public static void main(String[] args) throws Exception {
        new EngineView();
    }

}

package model;

import common.Payload;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class ConnectModel extends ReceiverAdapter implements RequestHandler {

    // Cria os canais
    JChannel channelModel;
    JChannel channelController;

    // Cria os despachantes para comunicar com cada canal
    MessageDispatcher despachanteModel;
    MessageDispatcher despachanteController;

    // Variavel auxiliar para ter controle se o coordenador foi mudado ou nao
    Address lastCoordenador;

    // Banco de dados
    Banco banco;

    // Variavel usada para escolher um membro do cluster
    Integer balanceador;

    public ConnectModel() {

    }

    private void start() throws Exception {

        balanceador = 0;

        // Carrega o arquivo salvo no hd
        carregar();

        // Instancia o canal de comunicação e os integrantes do grupo
        channelModel = new JChannel("sequencer.xml");
        channelModel.setReceiver(this);
        despachanteModel = new MessageDispatcher(channelModel, this, this, this);
        channelModel.connect("model");


        eventLoop();
        channelModel.close();
    }

    public void newCoordenador() {

        try{
            // Instanciando o coordenador
            if (souCoordenador(channelModel) && channelController == null) {

                channelController = new JChannel("sequencer.xml");
                channelController.setReceiver(this);
                despachanteController = new MessageDispatcher(channelController, this, this, this);
                channelController.connect("control");
                setCoordenador();
                //channelController.close();
            }
        } catch (Exception e) {
            System.out.println("Erro ao definir um novo coordenador");
            e.printStackTrace();
        }


    }

    public void setCoordenador() {

        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        lastCoordenador = getCoordenador(channelModel);
        JSONObject json = new JSONObject();
        json.put("coordenador", channelController.getAddress());
        Payload conteudo = new Payload(json, "newCoordenador", "model", true);

        try {
            Address cluster = null;
            Message mensagem = new Message(null, null, conteudo.toString());
            channelController.send(mensagem);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    // Verifica se sou o coordenador
    public boolean souCoordenador(Channel canal) {

        // Pega o meu endereço
        Address meuEndereco = canal.getAddress();

        // verifica se sou coordenador
        if (getCoordenador(canal).equals(meuEndereco)) {
            return true;
        }

        return false;

    }

    public Address getCoordenador(Channel canal) {
        Vector<Address> cluster = new Vector<Address>(canal.getView().getMembers()); // CUIDADO: o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)
        Address primeiroMembro = cluster.elementAt(0);  //OBS.: 0 a N-1

        return primeiroMembro;
    }

    private void eventLoop() {

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                System.out.println ("Thread awaked");
            }
        }

    }

    // Salva o banco de dados na memória
    public void salvar() {
        try {
            //Gera o arquivo para armazenar o objeto
            FileOutputStream arquivoGrav =
                    new FileOutputStream("./saida.dat");
            //Classe responsavel por inserir os objetos
            ObjectOutputStream objGravar = new ObjectOutputStream(arquivoGrav);
            //Grava o objeto cliente no arquivo
            objGravar.writeObject(this.banco);
            objGravar.flush();
            objGravar.close();
            arquivoGrav.flush();
            arquivoGrav.close();
            System.out.println("Objeto gravado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Carrega o banco de dados para a memória e retorna se foi possível
    public boolean carregar() {
        try {
            //Carrega o arquivo
            FileInputStream arquivoLeitura = new FileInputStream("./saida.dat");
            // Classe responsavel por recuperar os objetos do arquivo
            ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura);
            Banco bancoRetorno = (Banco) objLeitura.readObject();
            objLeitura.close();
            arquivoLeitura.close();
            this.banco = bancoRetorno;
            return true;
        } catch (Exception e) {
            System.out.println("Arquivo não carregado.");
            this.banco = new Banco();
            return false;
        }
    }

    // A cada iteração escolhe um membro que não seja o coordenador
    public Address pickMember() {
        int qtdMembros = channelModel.getView().size() - 1;
        balanceador++;
        int escolhido = balanceador % qtdMembros;
        return channelModel.getView().getMembers().get(escolhido + 1);
    }

    public String typeFunc(String func) {
        String leitura[] = {"verificaCpf", "buscaConta", "saldo", "extrato", "pesquisa"};
        String escrita[] = {"cadastro", "transferencia"};

        if (Arrays.asList(leitura).contains(func)) {
            return "leitura";
        } else if (Arrays.asList(escrita).contains(func)) {
            return "escrita";
        } else return "erro";

    }

    // responde requisições recebidas
    public Object handle(Message msg) throws Exception {

        // Extrai a mensagem e a converte para o tipo payload
        Payload pergunta = (Payload) msg.getObject();

        if (pergunta.isCoordenador()) {

            switch (pergunta.getFunc()) {
                case "verificaCpf":
                    // Retorna se existe uma conta com o cpf ou nao
                    boolean isUsed = this.banco.verificaCpf(pergunta.getJson().getString("cpf"));
                    JSONObject retornoCpf = new JSONObject().put("cpf", isUsed);
                    return new Payload(retornoCpf, "verificaCpf", "model", souCoordenador(channelModel));

                case "buscaConta":
                    // Retorna a conta com o cpf tal
                    Conta conta = this.banco.getConta(pergunta.getJson().getString("cpf"));
                    JSONObject retornoConta = new JSONObject().put("cpf", conta.toString());
                    return new Payload(retornoConta, "buscaConta", "model", souCoordenador(channelModel));

                case "saldo":
                    // Retorna o saldo da conta X
                    Double saldo = this.banco.getSaldo(pergunta.getJson().getString("cpf"));
                    JSONObject retornoSaldo = new JSONObject().put("saldo", saldo);
                    return new Payload(retornoSaldo, "saldo", "model", souCoordenador(channelModel));

                case "extrato":
                    // Retorna o historico da conta X
                    String extrato = "";
                    List<Operacao> historico = this.banco.getHistorico(pergunta.getJson().getString("cpf"));
                    for (Operacao operacao : historico) {
                        extrato = extrato.concat(operacao.toString());
                    }
                    JSONObject retornoExtrato = new JSONObject().put("extrato", extrato);
                    return new Payload(retornoExtrato, "extrato", "model", souCoordenador(channelModel));

                case "cadastro":

                    JSONObject json = pergunta.getJson();
                    Conta newConta = this.banco.CriarConta(
                            json.getString("nome"),
                            json.getString("cpf"),
                            json.getString("senha")
                    );

                    JSONObject retornoNewConta = new JSONObject().put("cadastro", newConta.toString());
                    salvar();
                    return new Payload(retornoNewConta, "cadastro", "model", souCoordenador(channelModel));

                case "transferencia":
                    JSONObject json2 = pergunta.getJson();

                    Operacao op = this.banco.transferencia(
                            json2.getString("origemCpf"),
                            json2.getString("destinoCpf"),
                            json2.getDouble("valor"));

                    JSONObject retornoOperacao = new JSONObject().put("transferencia", op.toString());
                    salvar();
                    return new Payload(retornoOperacao, "transferencia", "model", souCoordenador(channelModel));

                case "pesquisa":
                    break;
                default:
                    System.out.println("DEFAULT");
                    return new Payload(null, "invalido", "model", souCoordenador(channelModel));
            }

            ///

        } else {

            if (typeFunc(pergunta.getFunc()).equals("leitura")) {
                // Envia para um
                Address membro = pickMember();
                RspList rspList = this.enviaUnicast(despachanteModel, membro, pergunta);
                return rspList.getFirst();
                // Retorna a resposta

            } else if (typeFunc(pergunta.getFunc()).equals("escrita")) {
                // Envia para todos
                RspList rspList = this.enviaMulticast(despachanteModel, pergunta);
                return rspList.getResults();
                // Retorna a resposta

            } else {
                // Exibe msg de erro
                System.out.println("Erro");
            }
            return new Payload(null, "invalido", "control", souCoordenador(channelModel));

        }
        return new Payload(null, "invalido", "model", souCoordenador(channelModel));
    }

    public void receive(Message msg) {



    }

    public void viewAccepted(View new_view) {
        System.out.println(new_view);
        try{
            if(souCoordenador(channelController)){
                System.out.println("Tenho que alterar o coordenador");
                if(channelController.getView().getMembers().size() > 1){
                    System.out.println("Saio");
                    channelController.close();
                    channelController = null;
                    newCoordenador();
                    System.out.println(" Entro denovo");
                }
            }
        } catch(Exception e){
            System.out.println("Exception");
        }

        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        if (souCoordenador(channelModel) && (!getCoordenador(channelModel).equals(lastCoordenador)) && channelController == null) {

            newCoordenador();

        }
        lastCoordenador = getCoordenador(channelModel);
    }

    public void aux(){

    }

    private RspList enviaMulticast(MessageDispatcher despachante, Payload conteudo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + conteudo);
        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem = new Message(cluster, conteudo.toString());
        //mensagem.getSrc()

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //envia o MULTICAST
        System.out.println("==> Respostas do cluster ao MULTICAST:\n" + respList + "\n");
        return respList;
    }

    private RspList enviaAnycast(MessageDispatcher despachante, Collection<Address> subgrupo, Object conteudo) throws Exception {


        Message mensagem = new Message(null, conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // só ESPERA receber a primeira resposta do subgrupo (FIRST) // Outras opções: ALL, MAJORITY, NONE
        opcoes.setAnycasting(true);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o ANYCAST


        return respList;
    }

    private RspList enviaUnicast(MessageDispatcher despachante, Address destino, Object conteudo) throws Exception {

        Message mensagem = new Message(destino, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        Vector<Address> subgrupo = new Vector<Address>();
        subgrupo.add(destino);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o UNICAST

        return respList;
    }

    public static void main(String[] args) throws Exception {
        new ConnectModel().start();
    }

}

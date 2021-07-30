package control;

import org.json.JSONObject;
import common.Payload;
import model.Banco;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;

import java.util.Collection;
import java.util.Vector;

public class ConnectControl extends ReceiverAdapter implements RequestHandler {

    // Cria os canais
    JChannel channelController;
    JChannel channelView;

    // Cria os despachantes para comunicar com cada canal
    MessageDispatcher despachanteController;
    MessageDispatcher despachanteView;

    // Variavel auxiliar para ter controle se o coordenador foi mudado ou nao
    Address lastCoordenador;
    Address acessPoint;

    // Banco de dados
    Banco banco;

    // Variavel usada para escolher um membro do cluster
    Integer balanceador;

    public ConnectControl() {

    }

    private void start() throws Exception {

        balanceador = 0;

        // Instancia o canal de comunicação e os integrantes do grupo
        channelController = new JChannel("sequencer.xml");
        channelController.setReceiver(this);
        despachanteController = new MessageDispatcher(channelController, this, this, this);
        channelController.connect("control");

        eventLoop();
        channelController.close();
    }

    public void newCoordenador() throws Exception {

        // Instanciando o coordenador
        if (souCoordenador() && channelView == null) {

            channelView = new JChannel("sequencer.xml");
            channelView.setReceiver(this);
            despachanteView = new MessageDispatcher(channelView, this, this, this);
            channelView.connect("view");
            setCoordenador();
            //channelController.close();
        }

    }

    public void setCoordenador() {

        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        lastCoordenador = getCoordenador();
        JSONObject json = new JSONObject();
        json.put("coordenador", channelView.getAddress());
        Payload conteudo = new Payload(json, "newCoordenador", "control", false);

        try {
            System.out.println("Enviando mensagem");
            RspList resposta = enviaMulticast(despachanteView, conteudo);
            Object aux = resposta.getResults();
            Object aux2 = resposta.getFirst();
            System.out.println(aux);
            System.out.println(aux2);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao fazer multicast ao informor novo coordenador");
        }

    }

    // Verifica se sou o coordenador
    public boolean souCoordenador() {

        // Pega o meu endereço
        Address meuEndereco = channelController.getAddress();

        // verifica se sou coordenador
        if (getCoordenador().equals(meuEndereco)) {
            return true;
        }

        return false;

    }

    public Address getCoordenador() {
        Vector<Address> cluster = new Vector<Address>(channelController.getView().getMembers()); // CUIDADO: o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)
        Address primeiroMembro = cluster.elementAt(0);  //OBS.: 0 a N-1

        return primeiroMembro;
    }


    private void eventLoop() {
        // Verifica com os demais se eu ja estou atualizado TODO
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                System.out.println ("Thread awaked");
            }
        }

    }

    // A cada iteração escolhe um membro que não seja o coordenador
    public Address pickMember() {
        int qtdMembros = channelController.getView().size() - 1;
        balanceador++;
        int escolhido = balanceador % qtdMembros;
        return channelController.getView().getMembers().get(escolhido + 1);
    }

//    public String typeFunc(String func) {
//
//        String leitura[] = {"verificaCpf", "buscaConta", "saldo", "extrato", "pesquisa"};
//        String escrita[] = {"cadastro", "transferencia"};
//
//        if (Arrays.asList(leitura).contains(func)) {
//            return "leitura";
//        } else if (Arrays.asList(escrita).contains(func)) {
//            return "escrita";
//        } else return "erro";
//
//    }

    // responde requisições recebidas
    public Object handle(Message msg) throws Exception {

        // Extrai a mensagem e a converte para o tipo payload
        //String a = msg.toString();
        //ObjectInputStream b = (ObjectInputStream) msg.getObject();
        Payload pergunta = (Payload) msg.getObject();
        //Payload pergunta = (Payload) b.readObject();
        if (pergunta.isCoordenador()) {


        } else {
            //
            switch (pergunta.getFunc()) {

                case "newCoordenador":
                    // Salva o endereço do coordenador do model
                    //acessPoint = pergunta.getJson().getString("coordenador");
                    return new Payload(null, "ok", "model", true);

                default:
                    System.out.println("DEFAULT");
                    return new Payload(null, "invalido", "model", true);
            }

        }
        return new Payload(null, "invalido", "model", false);
    }

    public void receive(Message msg) {

    }

    public void viewAccepted(View new_view) {
        System.out.println(new_view);
        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        if (souCoordenador() && (!getCoordenador().equals(lastCoordenador)) && channelView == null) {

            try {
                newCoordenador();
            } catch (Exception e) {
                System.out.println("Erro ao definir um novo coordenador");
                e.printStackTrace();
            }

        }
        lastCoordenador = getCoordenador();
    }


    private RspList enviaMulticast(MessageDispatcher despachante, Payload conteudo) throws Exception {
        System.out.println("\nENVIEI a pergunta: " + conteudo);
        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem = new Message(cluster, conteudo.toString());

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
        new ConnectControl().start();
    }

}

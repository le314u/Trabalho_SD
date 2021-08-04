package view;

import common.Payload;
import control.ConnectControl;
import model.Banco;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class ConnectView extends ReceiverAdapter implements RequestHandler {

    // Cria os canais
    JChannel channelView;

    // Cria os despachantes para comunicar com cada canal
    MessageDispatcher despachanteView;

    // Variavel auxiliar para ter controle se o coordenador foi mudado ou nao

    Address accessPoint;

    public ConnectView() {
        try {
            this.start();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void start() throws Exception {

        // Instancia o canal de comunicação e os integrantes do grupo
        channelView = new JChannel("C:/Users/Cliente/IdeaProjects/projeto/src/main/java/common/cast.xml");
        channelView.setReceiver(this);
        despachanteView = new MessageDispatcher(channelView, this, this, this);
        channelView.connect("view");


        eventLoop();
        channelView.close();
    }

    // Informa a todos os membros do cluster que há um novo ponto de acesso
    public void newAccessPoint() {


        JSONObject json = new JSONObject();
        json.put("accessPoint", this.accessPoint);
        Payload conteudo = new Payload(json, "newAccessPoint", "view", souCoordenador(channelView));

        try {

            enviaMulticast(conteudo);

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
        // Verifica com os demais se eu ja estou atualizado TODO
        EngineView engine = new EngineView(this);
        engine.runTime();

    }

    // Converte payload para string (usado no retorno das mensagens)
    public String p2str(Payload p){
        return p.toString();
    }

    // responde requisições recebidas
    public Object handle(Message msg) throws Exception {

        Payload pergunta = new Payload(msg.getObject().toString());
        // Caso o novo membro queira saber qual o ponto de acesso ao canal controler
        if (souCoordenador(channelView) && pergunta.getFunc().equals("accessPoint")){
            if(accessPoint != null){
                JSONObject json = new JSONObject();
                json.put("accessPoint", accessPoint);
                return p2str(new Payload(json, "accessPoint", "view", souCoordenador(channelView)));
            }

        }

        return p2str(new Payload(null, "invalido", "view", souCoordenador(channelView)));
    }

    public void receive(Message msg) {

        // fixa o ponto de accesso para o outro canal
        Payload pergunta = new Payload(msg.getObject().toString());
        if (pergunta.getFunc().equals("newCoordenador")) {
            if(pergunta.getChannel().equals("control")){
                accessPoint = msg.getSrc();
                newAccessPoint();
            }

        } else if (pergunta.getFunc().equals("newAccessPoint")) {
            String endereco = pergunta.getJson().getString("accessPoint");
            saveAp(endereco);
        }

    }

    // Recurso tecnico necessario
    public void saveAp(String endereco){

        for (Address membro : channelView.getView().getMembers()) {
            if(membro.toString().equals(endereco)){
                accessPoint = membro;
            }
        }
    }

    // Pergunta ao coordenador qual o ponto de acesso
    public void getAccessPoint(){

        if (!souCoordenador(channelView) && accessPoint != null){
            try{

                Payload p = new Payload(null, "accessPoint", "view", souCoordenador(channelView));

                Payload resultado = new Payload(sendCoordenador(p));

                String endereco = resultado.getJson().getString("accessPoint");
                saveAp(endereco);

            } catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    //// CONTINUAR ALTERANDO A VIEW ?
    public void viewAccepted(View new_view) {

        if (!souCoordenador(channelView) && channelView.getView().getMembers().size() > 1){
            this.getAccessPoint();
        }

        System.out.println(new_view);
    }

    private RspList enviaMulticast(Payload conteudo) throws Exception {
        MessageDispatcher despachante = despachanteView;
        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem = new Message(cluster, conteudo.toString());

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //envia o MULTICAST

        return respList;
    }


//    private RspList enviaAnycast(MessageDispatcher despachante, Collection<Address> subgrupo, Object conteudo) throws Exception {
//
//
//        Message mensagem = new Message(null, conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados
//
//        RequestOptions opcoes = new RequestOptions();
//        opcoes.setMode(ResponseMode.GET_FIRST); // só ESPERA receber a primeira resposta do subgrupo (FIRST) // Outras opções: ALL, MAJORITY, NONE
//        opcoes.setAnycasting(true);
//
//        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o ANYCAST
//
//
//        return respList;
//    }

    private String sendCoordenador(Payload conteudo) throws Exception {

        MessageDispatcher despachante = despachanteView;
        Address destino = getCoordenador(channelView);
        Message mensagem = new Message(destino, conteudo.toString());

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        return despachante.sendMessage(mensagem, opcoes); //envia o UNICAST


    }

    public String sendControl(Payload conteudo) throws Exception {

        MessageDispatcher despachante = despachanteView;
        getAccessPoint();

        Address destino = accessPoint;
        Message mensagem = new Message(destino, conteudo.toString());

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        return despachante.sendMessage(mensagem, opcoes); //envia o UNICAST


    }


    public static void main(String[] args) {
        try{
            new ConnectView().start();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

}

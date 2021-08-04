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
    Address accessPoint;

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
        if (souCoordenador(channelController) && channelView == null) {

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
        lastCoordenador = getCoordenador(channelController);
        JSONObject json = new JSONObject();
        json.put("coordenador", channelView.getAddress());
        Payload conteudo = new Payload(json, "newCoordenador", "control", false);

        try {
            Address cluster = null;
            Message mensagem = new Message(null, null, conteudo.toString());
            channelView.send(mensagem);

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

    public String handleControl(Payload pergunta){

        Address destino = pickMember();
        switch (pergunta.getFunc()){
            case("cadastro"):
                try{
                    pergunta.setFunc("buscaConta");
                    String resultado = sendModel(pergunta);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
        try{
            return sendModel(pergunta);
        } catch (Exception e){
            return p2str(new Payload(null, "invalido", "control", souCoordenador(channelController)));
        }
    }

    public String handleView(Payload pergunta){

        getAccessPoint();
        try{
            pergunta.setChannel("control");
            return this.sendCoordenador(pergunta);
        } catch (Exception e){
            return p2str(new Payload(null, "invalido", "control", souCoordenador(channelController)));
        }

    }

    // responde requisições recebidas
    public Object handle(Message msg) throws Exception {

        // Extrai a mensagem e a converte para o tipo payload
        //String a = msg.toString();
        //ObjectInputStream b = (ObjectInputStream) msg.getObject();
        Payload pergunta = new Payload(msg.getObject().toString());
        //Payload pergunta = (Payload) b.readObject();

        switch (pergunta.getChannel()){
            case "control":
                return handleControl(pergunta);
            case "view":
                return handleView(pergunta);
        }

        return p2str(new Payload(null, "invalido", "model", false));
    }

    // Converte payload para string (usado no retorno das mensagens)
    public String p2str(Payload p){
        return p.toString();
    }

    // Recurso tecnico necessario
    public void saveAp(String endereco){

        for (Address membro : channelController.getView().getMembers()) {
            if(membro.toString().equals(endereco)){
                accessPoint = membro;
            }
        }
    }

    // Pergunta ao coordenador qual o ponto de acesso
    public void getAccessPoint(){

        if (!souCoordenador(channelController)){
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

    public void receive(Message msg) {

        // fixa o ponto de accesso para o outro canal
        Payload pergunta = new Payload(msg.getObject().toString());
        if (pergunta.getFunc().equals("newCoordenador")) {
            accessPoint = msg.getSrc();

        }
    }
    public String debug(View new_view){
        if (channelController.getView().equals(new_view)){
            Address meuEndereco = channelController.getAddress();
            return "control -"+meuEndereco.toString();
        } else if (channelView.getView().equals(new_view)){
            Address meuEndereco = channelView.getAddress();
            return "view -"+meuEndereco.toString();
        }
        return "Não sei";
    }

    public void viewAccepted(View new_view) {
        System.out.println(debug(new_view) + new_view.toString());

        try{
            if(souCoordenador(channelView)){
                if(channelView.getView().getMembers().size() > 1){
                    System.out.println("Resete conexão");
                    channelView.close();
                    channelView = null;
                    newCoordenador();
                }
            }
        } catch(Exception e){
            System.out.println("Exception");
        }
        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        if (souCoordenador(channelController) && (!getCoordenador(channelController).equals(lastCoordenador)) && channelView == null) {

            try {
                newCoordenador();
            } catch (Exception e) {
                System.out.println("Erro ao definir um novo coordenador");
                e.printStackTrace();
            }

        }
        lastCoordenador = getCoordenador(channelController);
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

    private String sendCoordenador(Payload conteudo) throws Exception {

        MessageDispatcher despachante = despachanteController;
        Address destino = getCoordenador(channelController);
        Message mensagem = new Message(destino, conteudo.toString());

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        return despachante.sendMessage(mensagem, opcoes); //envia o UNICAST


    }

    public String sendModel(Payload conteudo) throws Exception {

        MessageDispatcher despachante = despachanteController;
        if (accessPoint == null){
            getAccessPoint();
        }
        Address destino = accessPoint;
        Message mensagem = new Message(destino, conteudo.toString());

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        return despachante.sendMessage(mensagem, opcoes); //envia o UNICAST


    }


    public static void main(String[] args) throws Exception {
        new ConnectControl().start();
    }

}

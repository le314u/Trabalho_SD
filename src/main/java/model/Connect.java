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

public class Connect extends ReceiverAdapter implements RequestHandler {

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

    public Connect(){

    }

    private void start() throws Exception{

        balanceador = 0;

        // Carrega o arquivo salvo no hd
        carregar();

        // Instancia os canais de comunicação
        channelModel = new JChannel("sequencer.xml");
        channelController = new JChannel("sequencer.xml");

        // Instanciando o coordenador
        if(souCoordenador()){
            channelController.setReceiver(this);
            despachanteController=new MessageDispatcher(channelController, this, this, this);
            channelController.connect("control");
            //channelController.close();
        }

        // Instanciando integrante do grupo
        channelModel.setReceiver(this);
        despachanteModel=new MessageDispatcher(channelModel, this, this, this);
        channelModel.connect("model");
        eventLoop();
        channelModel.close();
    }

    public void setCoordenador(){

    }

    // Verifica se sou o coordenador
    public boolean souCoordenador(){

        // Pega o meu endereço
        Address meuEndereco = channelModel.getAddress();

        // verifica se sou coordenador
        if (getCoordenador().equals(meuEndereco))
            return true;
        return false;

    }

    public Address getCoordenador(){
        Vector<Address> cluster = new Vector<Address>(channelModel.getView().getMembers()); // CUIDADO: o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)
        Address primeiroMembro = cluster.elementAt(0);  //OBS.: 0 a N-1

        return primeiroMembro;
    }


    private void eventLoop(){
        // Verifica com os demais se eu ja estou atualizado TODO
        while(true){

        }

    }

    // Salva o banco de dados na memória
    public void salvar(){
        try
        {
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
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Carrega o banco de dados para a memória e retorna se foi possível
    public boolean carregar(){
        try
        {
            //Carrega o arquivo
            FileInputStream arquivoLeitura = new FileInputStream("./saida.dat");
            // Classe responsavel por recuperar os objetos do arquivo
            ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura);
            Banco bancoRetorno = (Banco) objLeitura.readObject();
            objLeitura.close();
            arquivoLeitura.close();
            this.banco = bancoRetorno;
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            this.banco = new Banco();
            return false;
        }
    }

    // A cada iteração escolhe um membro que não seja o coordenador
    public Address pickMember(){
        int qtdMembros = channelModel.getView().size() - 1;
        balanceador++;
        int escolhido = balanceador % qtdMembros;
        return channelModel.getView().getMembers().get(escolhido+1);
    }

    public String typeFunc(String func){

        String leitura[] = {"verificaCpf", "buscaConta", "saldo", "extrato", "pesquisa"};
        String escrita[] = {"cadastro", "transferencia"};

        if(Arrays.asList(leitura).contains(func)){
            return "leitura";
        } else if (Arrays.asList(escrita).contains(func)){
            return "escrita";
        } else return "erro";

    }

    public Object handle(Message msg) throws Exception{ // responde requisições recebidas

        // Extrai a mensagem e a converte para o tipo payload
        Payload pergunta = (Payload) msg.getObject();

        if(pergunta.isCoordenador()){

            if(typeFunc(pergunta.getFunc()).equals("leitura")){
                // Envia para um
                Address membro = pickMember();
                RspList rspList = this.enviaUnicast(despachanteModel, membro, pergunta);
                // Retorna a resposta

            } else if(typeFunc(pergunta.getFunc()).equals("escrita")){
                // Envia para todos
                RspList rspList = this.enviaMulticast(despachanteModel, pergunta);

                // Retorna a resposta

            } else {
                // Exibe msg de erro
                System.out.println("Erro");
            }
            return new Payload(null, "invalido", "control",false);
        }
        else{
            //
            switch (pergunta.getFunc()){
                case "verificaCpf":
                    // Retorna se existe uma conta com o cpf ou nao
                    boolean isUsed = this.banco.verificaCpf(pergunta.getJson().getString("cpf"));
                    JSONObject retornoCpf = new JSONObject().put("cpf",isUsed);
                    return new Payload(retornoCpf, "verificaCpf", "model",true);

                case "buscaConta":
                    // Retorna a conta com o cpf tal
                    Conta conta = this.banco.getConta(pergunta.getJson().getString("cpf"));
                    JSONObject retornoConta = new JSONObject().put("cpf",conta.toString());
                    return new Payload(retornoConta, "buscaConta", "model",true);

                case "saldo":
                    // Retorna o saldo da conta X
                    Double saldo = this.banco.getSaldo(pergunta.getJson().getString("cpf"));
                    JSONObject retornoSaldo = new JSONObject().put("saldo",saldo);
                    return new Payload(retornoSaldo, "saldo", "model",true);

                case "extrato":
                    // Retorna o historico da conta X
                    String extrato = "";
                    List<Operacao> historico = this.banco.getHistorico(pergunta.getJson().getString("cpf"));
                    for (Operacao operacao: historico) {
                        extrato = extrato.concat(operacao.toString());
                    }
                    JSONObject retornoExtrato = new JSONObject().put("extrato",extrato);
                    return new Payload(retornoExtrato, "extrato", "model",true);

                case "cadastro":

                    JSONObject json = pergunta.getJson();
                    Conta newConta = this.banco.CriarConta(
                            json.getString("nome"),
                            json.getString("cpf"),
                            json.getString("senha")
                    );

                    JSONObject retornoNewConta = new JSONObject().put("cadastro",newConta.toString());
                    salvar();
                    return new Payload(retornoNewConta, "cadastro", "model",true);

                case "transferencia":
                    JSONObject json2 = pergunta.getJson();

                    Operacao op = this.banco.transferencia(
                            json2.getString("origemCpf"),
                            json2.getString("destinoCpf"),
                            json2.getDouble("valor"));

                    JSONObject retornoOperacao = new JSONObject().put("transferencia",op.toString());
                    salvar();
                    return new Payload(retornoOperacao, "transferencia", "model",true);

                case "pesquisa":
                    break;
                default:
                    return new Payload(null, "invalido", "model",true);
            }

        }
        return new Payload(null, "invalido", "model",false);
    }

    public void receive(Message msg){

    }

    public void viewAccepted(View new_view){
        // Apenas o coordenador se apresenta para o controler
        // E somente quando houver uma mudança de coordenador
        if(souCoordenador() && (!lastCoordenador.equals(getCoordenador()))){
            lastCoordenador = getCoordenador();
            JSONObject json = new JSONObject();
            json.put("coordenador", lastCoordenador);
            Payload conteudo = new Payload(json,"newCoordenador","model",true);

            try{
                enviaMulticast(despachanteController, conteudo);
            } catch(Exception e){
                System.out.println("Erro ao fazer multicast ao informor novo coordenador");
            }

        }
        lastCoordenador = getCoordenador();
    }


    private RspList enviaMulticast(MessageDispatcher despachante, Payload conteudo) throws Exception{

        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem=new Message(cluster, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //envia o MULTICAST

        return respList;
    }


    private RspList enviaAnycast(MessageDispatcher despachante, Collection<Address> subgrupo, Object conteudo) throws Exception{


        Message mensagem=new Message(null, conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // só ESPERA receber a primeira resposta do subgrupo (FIRST) // Outras opções: ALL, MAJORITY, NONE
        opcoes.setAnycasting(true);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o ANYCAST


        return respList;
    }


    private RspList enviaUnicast(MessageDispatcher despachante, Address destino, Object conteudo) throws Exception{

        Message mensagem=new Message(destino, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        Vector<Address> subgrupo = new Vector<Address>();
        subgrupo.add(destino);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o UNICAST

        return respList;
    }



    public static void main(String[] args) throws Exception{
        new Connect().start();
    }

}

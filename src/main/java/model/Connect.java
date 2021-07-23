package model;

import common.Payload;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

public class Connect extends ReceiverAdapter implements RequestHandler {

    JChannel channelModel;
    JChannel channelController;
    MessageDispatcher despachanteModel;
    MessageDispatcher despachanteController;

    Banco banco;

    public Connect(){

    }

    private void start() throws Exception{



        channelModel = new JChannel("sequencer.xml");
        channelController = new JChannel("sequencer.xml");

        // Só o coordenador é instanciado
        // Verifico se sou coordenador
        // Caso seja, instancio comunicacao com outro grupo
        channelController.setReceiver(this);
        despachanteController=new MessageDispatcher(channelController, this, this, this);
        channelController.connect("control");
        eventCoordenadorLoop();
        channelController.close();

        channelModel.setReceiver(this);
        despachanteModel=new MessageDispatcher(channelModel, this, this, this);
        channelModel.connect("model");
        eventLoop();
        channelModel.close();



    }

    private void eventLoop(){

        while(true){
            // Verifica se existe um coordenador
            // Caso nao exista, ve quem pode ser o proximo coordenador

            // De tempos em tempos persiste os dados no db
        }

    }

    private void eventCoordenadorLoop(){

    }

    public void salvar(){
        try
        {
            //Gera o arquivo para armazenar o objeto
            FileOutputStream arquivoGrav =
                    new FileOutputStream("./saida.dat");
            //Classe responsavel por inserir os objetos
            ObjectOutputStream objGravar = new ObjectOutputStream(arquivoGrav);
            //Grava o objeto cliente no arquivo
            objGravar.writeObject(this);
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

    public Object handle(Message msg) throws Exception{ // responde requisições recebidas

        Payload pergunta = (Payload) msg.getObject();
        System.out.println("RECEBI uma mensagem: " + pergunta+"\n");  // DEBUG exibe o conteúdo da solicitação

        if(pergunta.isCoordenador()){
            // Retransmite para o grupo
            return " SIM "; // resposta padrão desse helloworld à requisição contida na mensagem
        }
        else{
            boolean modificado = false;
            // Trata a paradinha (switch grandao - pergunta o get funcao)
            switch (pergunta.getFunc()){
                case "verificaCpf":
                    // Retorna se existe uma conta com o cpf ou nao
                    boolean isUsed = this.banco.verificaCpf(pergunta.getJson().getString("cpf"));
                    JSONObject retornoCpf = new JSONObject().put("cpf",isUsed);
                    return new Payload(retornoCpf, "verificaCpf", "model",true);
                    break;
                case "buscaConta":
                    // Retorna a conta com o cpf tal
                    Conta conta = this.banco.getConta(pergunta.getJson().getString("cpf"));
                    JSONObject retornoConta = new JSONObject().put("cpf",conta.toString());
                    return new Payload(retornoConta, "buscaConta", "model",true);
                    break;
                case "saldo":
                    // Retorna o saldo da conta X
                    Double saldo = this.banco.getSaldo(pergunta.getJson().getString("cpf"));
                    JSONObject retornoSaldo = new JSONObject().put("saldo",saldo);
                    return new Payload(retornoSaldo, "saldo", "model",true);
                    break;
                case "extrato":
                    // Retorna o historico da conta X
                    String extrato = "";
                    List<Operacao> historico = this.banco.getHistorico(pergunta.getJson().getString("cpf"));
                    for (Operacao operacao: historico) {
                        extrato = extrato.concat(operacao.toString());
                    }
                    JSONObject retornoExtrato = new JSONObject().put("extrato",extrato);
                    return new Payload(retornoExtrato, "extrato", "model",true);
                    break;
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
                    break;
                case "transferencia":
                    JSONObject json2 = pergunta.getJson();

                    Operacao op = this.banco.transferencia(
                            json2.getString("origemCpf"),
                            json2.getString("destinoCpf"),
                            json2.getDouble("valor"));

                    JSONObject retornoOperacao = new JSONObject().put("transferencia",op.toString());
                    salvar();
                    return new Payload(retornoOperacao, "transferencia", "model",true);
                    break;
                case "pesquisa":
                    break;
                default:
                    return new Payload(null, "invalido", "model",true);
            }

        }


        Payload requisicao = (Payload) msg.getObject();
        String funcao = requisicao.getFunc();
        switch (funcao){
            case "criacao":
                try {

                    JSONObject json = requisicao.getJson();
                    Conta conta = this.banco.CriarConta(json.getString("nome"), json.getString("cpf"), json.getString("senha"));

                    JSONObject jsonRetorno = new JSONObject();
                    if (conta == null) {
                        jsonRetorno.put("confirmacao", false);
                    } else {
                        jsonRetorno.put("confirmacao", true);
                    }
                    Payload confirmacao = new Payload(jsonRetorno, "retCriacao");
                    Message s = new Message(null, null, confirmacao);

                    channel.send(s);
                }
                catch (Exception e){
                    System.out.println(e);
                }
                break;
        }
    }

    public void receive(Message msg){
        Payload requisicao = (Payload) msg.getObject();
        String funcao = requisicao.getFunc();
        switch (funcao){
            case "criacao":
                try {

                    JSONObject json = requisicao.getJson();
                    Conta conta = this.banco.CriarConta(json.getString("nome"), json.getString("cpf"), json.getString("senha"));

                    JSONObject jsonRetorno = new JSONObject();
                    if (conta == null) {
                        jsonRetorno.put("confirmacao", false);
                    } else {
                        jsonRetorno.put("confirmacao", true);
                    }
                    Payload confirmacao = new Payload(jsonRetorno, "retCriacao");
                    Message s = new Message(null, null, confirmacao);

                    channel.send(s);
                }
                catch (Exception e){
                    System.out.println(e);
                }
                break;
        }
    }

    public void viewAccepted(View new_view){

    }

    private RspList enviaMulticast(Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem=new Message(cluster, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_MAJORITY); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);


        RspList respList = despachante.castMessage(null, mensagem, opcoes); //envia o MULTICAST
        System.out.println("==> Respostas do cluster ao MULTICAST:\n" +respList+"\n"); //DEBUG: exibe as respostas

        return respList;
    }


    private RspList enviaAnycast(Collection<Address> subgrupo, Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(null, conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // só ESPERA receber a primeira resposta do subgrupo (FIRST) // Outras opções: ALL, MAJORITY, NONE
        opcoes.setAnycasting(true);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o ANYCAST
        System.out.println("==> Respostas do subgrupo ao ANYCAST:\n" +respList+"\n"); //DEBUG: exibe as respostas

        return respList;
    }


    private String enviaUnicast(Address destino, Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(destino, conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        String resp = despachante.sendMessage(mensagem, opcoes); //envia o UNICAST
        System.out.println("==> Resposta do membro ao UNICAST:\n" +resp+"\n"); //DEBUG: exibe as respostas

        return resp;
    }



    public static void main(String[] args) throws Exception{
        new Connect().start();
    }

}

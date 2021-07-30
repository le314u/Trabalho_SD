/*
 SAIBA MAIS: http://www.jgroups.org/manual/html/user-building-blocks.html#MessageDispatcher
/**/

import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;

import java.util.*;

public class TiposDeCast extends ReceiverAdapter implements RequestHandler {

    JChannel canalDeComunicacao;
    MessageDispatcher  despachante;

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Código-fonte referente ao JGroups

    private void start() throws Exception {

        //Cria o canal de comunicação com uma configuração XML do JGroups
        //canalDeComunicacao=new JChannel("udp.xml");
        //canalDeComunicacao=new JChannel("sequencer.xml");
        canalDeComunicacao=new JChannel("cast.xml");

        // MessageDispatcher(Channel channel, MessageListener l, MembershipListener l2, RequestHandler req_handler)
        despachante=new MessageDispatcher(canalDeComunicacao, this, this, this); // canal, quem tem receive(), quem tem viewAccecpted(), quem tem handle()

        canalDeComunicacao.setReceiver(this);	//quem irá lidar com as mensagens recebidas

        canalDeComunicacao.connect("TiposDeCast");
        eventLoop();
        canalDeComunicacao.close();

    }

    // extends ReceiverAdapter
    public void receive(Message msg) { //exibe mensagens recebidas
        System.out.println("" + msg.getSrc() + ": " + msg.getObject()); // DEBUG
    }

    // extends ReceiverAdapter
    public void viewAccepted(View new_view) { //exibe alterações na composição do cluster
        System.out.println("\t** nova View do cluster: " + new_view);   // DEBUG
    }


    // implements RequestHandler
    public Object handle(Message msg) throws Exception{ // responde requisições recebidas

        String pergunta = (String) msg.getObject();
        System.out.println("RECEBI uma mensagem: " + pergunta+"\n");  // DEBUG exibe o conteúdo da solicitação

        if(pergunta.contains("concorda"))
            return " SIM "; // resposta padrão desse helloworld à requisição contida na mensagem
        else
            return " NÃO "; // resposta padrão desse helloworld à requisição contida na mensagem
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Código-fonte da minha aplicação de exemplo

    final int TAMANHO_MINIMO_CLUSTER = 4;

    private void eventLoop() {

        Address meuEndereco = canalDeComunicacao.getAddress();

        while( canalDeComunicacao.getView().size() < TAMANHO_MINIMO_CLUSTER )
            Util.sleep(100); // aguarda os membros se juntarem ao cluster

        Vector<Address> cluster = new Vector<Address>(canalDeComunicacao.getView().getMembers()); // CUIDADO: o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)
        Address primeiroMembro = cluster.elementAt(0);  //OBS.: 0 a N-1
        Address segundoMembro  = cluster.elementAt(1);  //OBS.: 0 a N-1
        // Address ultimoMembro   = cluster.lastElement();


        // Definiremos um subgrupo do cluster, contendo apenas o 3º e 4º membros (OBS.: 0 a N-1)
        Vector<Address> subgrupo = new Vector<Address>();
        subgrupo.add(cluster.elementAt(2));
        subgrupo.add(cluster.elementAt(3));
        // CUIDADO: o conteúdo do Vector poderá ficar desatualizado (ex.: se algum membro sair ou entrar na View)

        if( meuEndereco.equals(primeiroMembro) ) {  // somente o primeiro membro envia o teste abaixo

            try {
                enviaUnicast( segundoMembro, "O segundo membro do cluster concorda?" ); //envia unicast para o primeiro membro do cluster

                enviaAnycast( subgrupo, "Algum membro do subgrupo concorda?" ); //envia anycast para um subgrupo do cluster

                enviaMulticast( "A maioria dos membros do cluster concorda?" ); //envia multicast para todos do cluster
            }
            catch(Exception e) {
                System.err.println( "ERRO: " + e.toString() );
            }

        } // if primeiro
        else{
            while( canalDeComunicacao.getView().getMembers().contains(primeiroMembro) )
                Util.sleep(100); // aguarda o primeiro membro sair do cluster

            System.out.println("\nBye bye...");
        }

    }//eventLoop  


    private RspList enviaMulticast(Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Address cluster = null; //OBS.: não definir um destinatário significa enviar a TODOS os membros do cluster
        Message mensagem=new Message(cluster, "{MULTICAST} "+conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_MAJORITY); // ESPERA receber a resposta da MAIORIA dos membros (MAJORITY) // Outras opções: ALL, FIRST, NONE
        opcoes.setAnycasting(false);

        RspList respList = despachante.castMessage(null, mensagem, opcoes); //envia o MULTICAST
        System.out.println("==> Respostas do cluster ao MULTICAST:\n" +respList+"\n"); //DEBUG: exibe as respostas

        return respList;
    }


    private RspList enviaAnycast(Collection<Address> subgrupo, Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(null, "{ ANYCAST } " + conteudo); //apesar do endereço ser null, se as opcoes contiverem anycasting==true enviará somente aos destinos listados

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_FIRST); // só ESPERA receber a primeira resposta do subgrupo (FIRST) // Outras opções: ALL, MAJORITY, NONE
        opcoes.setAnycasting(true);

        RspList respList = despachante.castMessage(subgrupo, mensagem, opcoes); //envia o ANYCAST
        System.out.println("==> Respostas do subgrupo ao ANYCAST:\n" +respList+"\n"); //DEBUG: exibe as respostas

        return respList;
    }


    private Object enviaUnicast(Address destino, Object conteudo) throws Exception{
        System.out.println("\nENVIEI a pergunta: " + conteudo);

        Message mensagem=new Message(destino, "{ UNICAST } " + conteudo);

        RequestOptions opcoes = new RequestOptions();
        opcoes.setMode(ResponseMode.GET_ALL); // ESPERA receber a resposta do destino // Outras opções: MAJORITY, FIRST, NONE
        // opcoes.setMode(ResponseMode.GET_NONE); // não ESPERA receber a resposta do destino // Outras opções: ALL, MAJORITY, FIRST

        Object resp = despachante.sendMessage(mensagem, opcoes); //envia o UNICAST
        System.out.println("==> Resposta do membro ao UNICAST:\n" +resp+"\n"); //DEBUG: exibe as respostas

        return resp;
    }


    public static void main(String[] args) throws Exception {
        new TiposDeCast().start();
    }

}//class
package common;

import org.json.JSONObject;

import java.io.Serializable;

public class Payload implements Serializable {

    JSONObject json;
    String func;
    String channel;
    boolean coordenador; // Quem esta enviando o payload

    public Payload(){

    }

    public Payload(String payloadString){
        try{
            JSONObject json = new JSONObject(payloadString);
            this.func = json.getString("func");
            this.channel = json.getString("channel");
            this.coordenador = json.getBoolean("coordenador");
            this.json = json.getJSONObject("json");
        } catch (Exception e){
            System.out.println("Entrou no catch do payload (ERRO A CORRIGIR)");
        }

    }

    public Payload(JSONObject json, String func, String channel, boolean coordenador){
        if (json == null){
            this.json = new JSONObject();
        } else {
            this.json = json;
        }
        this.func = func;
        this.channel = channel;
        this.coordenador = coordenador;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isCoordenador() {
        return coordenador;
    }

    public void setCoordenador(boolean coordenador) {
        this.coordenador = coordenador;
    }



//    @Override
//    public String toString() {
//        return new StringBuffer(" Street : ")
//                .append(this.street)
//                .append(" Country : ")
//                .append(this.country).toString();
//    }


    @Override
    public String toString() {
        return "{" +
                "json:" + json.toString() +
                ", func:'" + func + '\'' +
                ", channel:'" + channel + '\'' +
                ", coordenador:" + coordenador +
                '}';
    }



}

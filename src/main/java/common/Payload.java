package common;

import org.json.JSONObject;

import java.io.Serializable;

public class Payload implements Serializable {

    JSONObject json;
    String func;
    String channel;
    boolean coordenador;

    public Payload(){

    }

    public Payload(JSONObject json, String func, String channel, boolean coordenador){
        this.json = json;
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

    @Override
    public String toString() {
        return new StringBuffer("json : ").append(this.json)
                .append("func : ").append(this.func)
                .append("channel : ").append(this.channel)
                .append("coordenador : ").append(this.coordenador).toString();
    }

//    @Override
//    public String toString() {
//        return new StringBuffer(" Street : ")
//                .append(this.street)
//                .append(" Country : ")
//                .append(this.country).toString();
//    }


//    @Override
//    public String toString() {
//        return "Payload{" +
//                "json=" + json.toString() +
//                ", func='" + func + '\'' +
//                ", channel='" + channel + '\'' +
//                ", coordenador=" + coordenador +
//                '}';
//    }



}

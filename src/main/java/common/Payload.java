package common;

import org.json.JSONObject;

public class Payload {

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
}

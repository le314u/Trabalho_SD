/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.io.IOException;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

public class Banco {
    
    Scanner in = new Scanner(System.in);
    
    public Banco(){
        this.showMenuInicial();
    };
    
    
    public JSONObject showMenuCadastro() throws IOException{
        System.out.println("Digite um nome:");
        String nome = getInput();
        System.out.println("Digite um cpf:");
        String cpf = getInput();
        System.out.println("Digite uma senha:");
        String senha = getInput();
        //Prepara o Json para o retorno
        JSONObject retorno = new JSONObject();
        retorno.put("nome", nome);
	retorno.put("cpf", cpf);
	retorno.put("senha", senha);
        return retorno;
    }
    
    public Integer getOption(){
        String option = in.nextLine();
        return Integer.parseInt(option);
    }
    
    public String getInput(){
        return in.nextLine();
    }
    
    public int showMenuInicial(){
        int option = 0;
        while (option < 1 || option > 2) {   
            //Limpa o menu
            this.clear();
            // Mostra o menu
            System.out.println("Digite 1 - Para criar uma conta");
            System.out.println("Digite 2 - Para entrar em uma conta");
            // Pega a opçõ do menu
            try {
                option = getOption();
            } catch (Exception e) {
                option = 0;
            }
        }
        return option;
    }
    
    public void clear(){
        //Implementar para limpar a tela
    }
}

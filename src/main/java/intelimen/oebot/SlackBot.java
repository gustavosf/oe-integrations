package intelimen.oebot;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Abstração para comandos do intelimen.oebot.SlackBot
 *
 * Created by gustavo on 22/01/15.
 */
public class SlackBot {

    private final String host;
    private final String token;

    String postResource= "https://%s/services/hooks/slackbot";

    /**
     * Constructor
     * @param host  Host da empresa no slack
     * @param token Token gerado pelo slack para integração com o bot
     */
    public SlackBot(String host, String token) {
        this.host = host;
        this.token = token;
    }

    /**
     * Gerador estático
     * @return SlackBot
     */
    public static SlackBot getInstance() {
        return new SlackBot(System.getenv("SLACK_HOST"),
                System.getenv("SLACK_TOKEN"));
    }

    //*************************************************************************//
    //***  COMANDOS   *********************************************************//
    //*************************************************************************//

    /**
     * Envia uma mensagem para um determinado canal
     * @param channel Canal em que o bot deverá enviar a mensagem
     * @param message Mensagem a ser enviada
     * @return        true se a requisição foi enviada com sucesso
     */
    public Boolean message(String channel, String message) {

        String url = String.format(postResource, this.host);

        try {
            Unirest.post(url)
                   .queryString("token", this.token)
                   .queryString("channel", channel)
                   .body(message)
                   .asBinary();
        } catch (UnirestException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

}

# Integração entre Github e Slackbot

Service para ligar WebHooks do Github com o Slackbot do Slack.

Até o momento, trata apenas o evento de *abertura de pull request*, onde o bot deverá enviar notificação para um canal predeterminado informando que um pull foi aberto.

## Configuração

### Heroku

Você pode fazer o deploy desta aplicação diretamente no Heroku. Para tal, siga o passo-a-passo abaixo:

1. Crie uma conta no Heroku
2. Instale o [toolbelt deles](https://toolbelt.heroku.com/)
3. Siga os comandos abaixo para preparar
   ```bash
   git clone https://github.com/gustavosf/slackbot-for-pull-request.git
   cd slackbot-for-pull-request
   heroku create
   git push heroku master
   ```
4. Ao usar `heroku create`, ele deverá ter retornado uma url. Salve ela para depois

O serviço já está instalado no heroku, vamos configurar o resto

### Slack

Você primeiro deve incluir uma integração no Slack. Para isso, siga os passos abaixo:

1. Acesse o slack
2. Clique no nome da empresa no canto superior esquerdo
3. Clique em `Configure Integrations`
4. No final da página você encontrará `intelimen.oebot.SlackBot`. Clique em `Add` para incluiur uma integração
5. Clique em `Add Slackbot Integration`
6. Sua integração está configurada. Da URL que ele retorna, extraia o host e o token

Você deverá ter neste momento, dois parâmetros como estes:

```
host = objectedge.slack.com
token = AlX2w0YcaBrzi2iy6gUZAV7Y
```

Você precisa indicar estes parâmetros para a sua instância no Heroku, pois este app busca eles das variáveis de sistema. Para isso, digite no terminal, dentro da pasta do repo:


```bash
heroku config:set SLACK_HOST=objectedge.slack.com
heroku config:set SLACK_TOKEN=AlX2w0YcaBrzi2iy6gUZAV7Y
heroku config:set SLACK_REPO_CHANNEL="{\"gustavosf/oebot\":\"#oebot-updates\"}"
```
**Altere os valores acima para os que foram gerados por você**

Repare que o parâmetro `SLACK_REPO_CHANNEL` é um json com uma lista de <repo>:<channel>. Este é o mapeamento que este app fará para saber para qual canal deve mandar atualizações de um determinado repositório.

### GitHub

1. Acesse o repositório que você deseja instalar esta integração
2. Clique em `Settings`
3. Clique em `Webhooks & Services`
4. Clique em `Add webhook`
5. Em *Payload URL*, coloque a url do heroku
6. Deixe *Content Type* como `application/json`
7. Coloque um secret qualquer (todo: aceitar reqs apenas com o secret correto)
8. Selecione `Let me select individual events`
9. Selecione apenas o evento `Pull Request`
10. Marque como `Active`
11. Clique em `Add webhook`

O GitHub estará configurado também.

Você já deve ter todo ambiente configurado, e o seu bot já deverá conseguir enviar atualizações de Pull Requests do Github para o Slack!
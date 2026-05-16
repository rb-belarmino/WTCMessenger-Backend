# WTC Messenger - Sistema de Mensageria e Orquestração 🚀

O **WTC Messenger** é uma arquitetura de microsserviços baseada em Java/Spring Boot projetada para envio de notificações (Push), gerenciamento de campanhas e autenticação segura de usuários. A infraestrutura utiliza conteinerização via Docker e mensageria assíncrona com Apache Kafka.

---

## 🏗 Arquitetura do Projeto

A solução é composta pelos seguintes serviços e infraestrutura:

* **Auth Service** (Porta `8080`): Microsserviço responsável pelo registro, autenticação de usuários, e emissão de tokens JWT (Access & Refresh tokens).
* **Messaging Service** (Porta `8082`): Microsserviço central responsável pelo disparo de campanhas, gerenciamento da timeline dos clientes, leitura dos tópicos Kafka, e integração via SDK com o Google Firebase Cloud Messaging (FCM).
* **Apache Kafka & Zookeeper**: Gerenciam as filas e mensageria distribuída assíncrona (ex: tópicos `wtc.message.send` e `wtc.campaign.dispatch`).
* **MongoDB Atlas**: Banco de dados NoSQL em nuvem usado por ambos os serviços para armazenar usuários, logs de campanhas e histórico de mensagens.

---

## ⚙️ Pré-requisitos

* [Docker](https://www.docker.com/products/docker-desktop) e [Docker Compose](https://docs.docker.com/compose/install/) instalados na sua máquina.
* Java 21+ e Maven (Opcional, apenas se quiser rodar os serviços fora do contêiner).

---

## 🚀 Como Rodar o Projeto

### 1. Configure as Variáveis de Ambiente
O projeto precisa de um arquivo `.env` para carregar as credenciais sensíveis (isso garante que as senhas não fiquem expostas no código).

Na raiz do projeto, crie um arquivo chamado `.env` e adicione o seguinte conteúdo:
```env
JWT_SECRET=d3RjLXNlY3JldC1rZXktY2hhbmdlLWluLXByb2R1Y3Rpb24tMTIzNDU2Nzg=
MONGODB_URI=mongodb+srv://[usuario]:[senha]@[seu-cluster].mongodb.net/wtc_messenger?appName=Cluster0
```
> **Nota:** Peça a URI correta do MongoDB Atlas para o administrador do projeto, caso não a tenha.

### 2. Inicie os Contêineres
Na raiz do projeto, abra o seu terminal e execute:
```bash
docker compose up --build -d
```
Esse comando fará o download das imagens do Kafka, Zookeeper e fará o build dos dois microsserviços localmente, executando-os em segundo plano.

Para acompanhar os logs de inicialização, você pode usar:
```bash
docker compose logs -f
```

---

## 🧪 Como Testar a Aplicação

O projeto conta com o **Swagger UI** implementado em todos os serviços. O Swagger oferece uma interface gráfica super amigável para você visualizar e testar todos os endpoints sem precisar configurar o Postman.

### Passo 1: Acesse o Swagger do Auth Service
Acesse no seu navegador: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Neste portal, você poderá gerenciar as rotas de autenticação.
1. Localize a rota `POST /auth/login`.
2. Clique no botão **"Try it out"**.
3. Insira as suas credenciais no formato JSON e clique em **"Execute"**.
4. Você receberá um `accessToken` na resposta. **Copie esse token**, pois ele será usado para acessar o próximo microsserviço.

### Passo 2: Acesse o Swagger do Messaging Service
Acesse no seu navegador: **[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)**

Este é o coração das mensagens e envio de notificações. Como esse serviço exige segurança, você precisará usar o token gerado anteriormente.
1. Nos endpoints listados no Swagger (ex: `GET /customers/{id}/timeline`), clique em **"Try it out"**.
2. O Swagger exibirá os campos necessários da requisição. Caso precise testar manualmente via headers (se a opção Global Authorize não estiver visível), certifique-se de adicionar um Header HTTP padrão chamado `Authorization` com o valor `Bearer SEU_TOKEN_COPIADO`.
3. Dispare os testes de criação de campanhas, busca de histórico ou disparo de Push Notifications.

---

## 📝 Avisos Importantes

* **Firebase FCM**: O serviço de mensageria utiliza um arquivo `firebase-service-account.json` (dentro de `src/main/resources`) para autenticar o envio real de Push Notifications para celulares via Firebase. Se esse arquivo não existir, o sistema atuará em modo de "simulação", gerando os logs corretos, mas sem efetivar o disparo final para a nuvem do Google.
* **Ambientes de Teste do Messaging:** Existe um arquivo `compose.yaml` dentro da pasta do `messaging-service`. Ele **não** deve ser usado para rodar a aplicação no dia a dia. Ele serve apenas como laboratório isolado caso queira construir um banco MongoDB zerado apenas para brincar localmente com o código daquele serviço, sem impactar o `auth-service`.

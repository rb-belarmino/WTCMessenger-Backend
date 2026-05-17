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
JWT_SECRET=seu_secret_base64_aqui
MONGODB_URI=sua_uri_do_mongodb_atlas_aqui
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

## 🧪 Estratégia e Execução de Testes (TDD & Cobertura)

O projeto segue padrões de excelência de engenharia através de **TDD (Test-Driven Development)**, contando com testes unitários, testes de integração simulados e testes de fluxo End-to-End (E2E).

Temos três maneiras de rodar e validar a saúde do sistema:

### 1. Testes Unitários via Docker (Recomendado - Isolamento Total)
Para rodar os testes sem a necessidade de ter o Java ou o Maven instalados fisicamente na sua máquina, você pode usar os próprios contêineres do Docker. Isso evita problemas de compatibilidade de versões do JDK local.

* **Rodar testes do Auth Service:**
  ```bash
  docker compose run --rm auth-service mvn test
  ```

* **Rodar testes do Messaging Service:**
  ```bash
  docker compose run --rm messaging-service mvn test
  ```

### 2. Testes Unitários via Script / Maven Local (Rápido para Desenvolvimento)
Se você possui o JDK 21 configurado no seu terminal, você pode rodar os testes de forma rápida e direta.

* **No Auth Service:**
  ```bash
  cd auth-service
  ../mvnw clean test
  ```

* **No Messaging Service:**
  ```bash
  cd messaging-service
  ./mvnw clean test
  ```

### 3. Teste Automatizado End-to-End (E2E)
Para validar se a orquestração entre os contêineres está funcionando 100% (Auth Service gerando JWT -> passando segurança do Gateway -> Messaging Service jogando no Kafka -> Consumidor gravando no Mongo e entregando via Firebase):

1. Certifique-se de que os contêineres estão rodando (`docker compose up -d`).
2. Na raiz do projeto, execute o script E2E:
   ```bash
   ./test-e2e.sh
   ```
   *(Caso ocorra erro de permissão, execute `chmod +x test-e2e.sh` antes).*

---

## 🏗️ Governança de Testes no Pipeline / Containers

### Processo nos Containers (Multi-stage Build)
Atualmente, as imagens Docker localizadas nos `Dockerfile`s utilizam a flag `-DskipTests` no comando `mvn package` para permitir que o desenvolvedor suba o ambiente de desenvolvimento local de forma ultra rápida. 

No entanto, para **governança de ambiente de Produção/CI-CD**, a melhor prática da arquitetura é remover a flag `-DskipTests` no build corporativo. Isso garante que:
1. Nenhuma imagem Docker seja construída se houver um teste unitário falhando.
2. A integridade do ambiente produtivo permaneça inalterada.

### Cobertura de Testes Atual
* **`AuthServiceTest`**: Valida geração de JWT, refresh tokens stateless e segurança contra credenciais inválidas.
* **`CampaignServiceTest`**: Valida a validação de regras de negócio de engajamento antes de persistir uma campanha e disparar para o Kafka.
* **`MessageServiceTest`**: Valida validações 1:1, status de mensagens e conciliação de eventos de envio.
* **`CampaignWorkerTest`**: Valida o processamento do consumidor assíncrono Kafka, integração de eventos simulados com Firebase e commit resiliente de offsets.

---

---

## 📝 Avisos Importantes

* **Firebase FCM**: O serviço de mensageria utiliza um arquivo `firebase-service-account.json` (dentro de `src/main/resources`) para autenticar o envio real de Push Notifications para celulares via Firebase. Se esse arquivo não existir, o sistema atuará em modo de "simulação", gerando os logs corretos, mas sem efetivar o disparo final para a nuvem do Google.
* **Ambientes de Teste do Messaging:** Existe um arquivo `compose.yaml` dentro da pasta do `messaging-service`. Ele **não** deve ser usado para rodar a aplicação no dia a dia. Ele serve apenas como laboratório isolado caso queira construir um banco MongoDB zerado apenas para brincar localmente com o código daquele serviço, sem impactar o `auth-service`.

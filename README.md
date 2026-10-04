# 🛠️ SwissToolbox API

Microsserviço utilitário de alta performance desenvolvido com **Java 21** e **Spring Boot 4**, projetado para transformações sob demanda de arquivos e mídias, como conversão de imagens para PDF e geração dinâmica de QR Codes. Todas as operações são auditadas e persistidas de forma transparente em um banco de dados **PostgreSQL**.

---

## 🚀 Funcionalidades

- **Conversão de Imagem para PDF**: Converte arquivos de imagem únicos (`PNG`, `JPEG`) em documentos PDF padronizados utilizando a biblioteca Apache PDFBox.
- **Geração de QR Code**: Gera imagens de QR Code (`PNG`) em tempo real com ZXing, suportando personalização de conteúdo (texto/URL) e dimensões configuráveis.
- **Auditoria Automatizada de Processamento**: Rastreamento detalhado de cada arquivo e operação processada, registrando metadados (`operation_type`, `file_name`, `input_size_bytes`, `duration_ms`, `status`) no banco de dados.
- **Suíte de Testes Automatizados**: Cobertura de testes unitários isolados com Mockito e testes de integração da camada web utilizando `MockMvc`.
- **Interface Web Integrada**: Cliente leve em página única (`index.html`) para validação rápida e download direto dos arquivos via navegador.

---

## 🧰 Tecnologias Utilizadas

- **Linguagem:** Java 21 (OpenJDK)
- **Framework:** Spring Boot 4.1.1
- **Banco de Dados:** PostgreSQL 16 (via Docker Compose)
- **Persistência:** Spring Data JPA / Hibernate
- **Bibliotecas:** Apache PDFBox 3.x, ZXing 3.5.x
- **Testes:** JUnit 5, Mockito, MockMvc
- **Gerenciador de Dependências:** Apache Maven

---

## 📋 Arquitetura e Endpoints

### 1. Conversão de Documentos
* **Rota:** `POST /api/v1/conversions/image-to-pdf`
* **Content-Type:** `multipart/form-data`
* **Parâmetro de Formulário:** `file` (`.png`, `.jpg`, `.jpeg`)
* **Retorno:** Fluxo binário do PDF (`application/pdf`) com cabeçalho `Content-Disposition: attachment`.

### 2. Gerador de QR Code
* **Rota:** `GET /api/v1/qrcodes`
* **Parâmetros de Consulta (Query Params):**
  - `text` *(obrigatório)*: Texto ou URL a ser codificada.
  - `width` *(opcional, padrão: 300)*: Largura da imagem em pixels.
  - `height` *(opcional, padrão: 300)*: Altura da imagem em pixels.
* **Retorno:** Fluxo binário da imagem PNG (`image/png`).

### 3. Modelo de Auditoria (`processing_audit`)
Cada requisição realizada grava o diagnóstico de execução no banco de dados:

| Coluna | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Chave primária autoincrementável |
| `operation_type` | `VARCHAR(50)` | Identificador da operação (`IMAGE_TO_PDF`, `QRCODE_GENERATION`) |
| `file_name` | `VARCHAR(255)` | Nome do arquivo original ou recurso gerado |
| `input_size_bytes` | `BIGINT` | Tamanho do payload processado em bytes |
| `duration_ms` | `BIGINT` | Tempo total de processamento em milissegundos |
| `status` | `VARCHAR(20)` | Resultado da operação (`SUCCESS` ou `FAILED`) |
| `created_at` | `TIMESTAMP` | Data e hora do registro |

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos
- **JDK 21** instalado e configurado nas variáveis de ambiente.
- **Docker** e **Docker Compose** instalados e em execução.
- **Maven** instalado (ou uso do wrapper `./mvnw`).

**1.Subindo o Banco de Dados: Abra o terminal na pasta raiz do projeto e execute o comando "docker compose up -d" para iniciar o container do PostgreSQL em segundo plano na porta 5433 da sua máquina.

**2.Executando a API: Inicie a aplicação executando a classe principal pelo IntelliJ IDEA ou rode o comando "mvn clean spring-boot:run" no terminal. A API estará pronta para receber chamadas no endereço http://localhost:8080.

**3.Usando pela Interface Web: Com o backend ligado, abra o arquivo index.html no navegador. Na aba Imagem para PDF, escolha um arquivo PNG ou JPEG e clique em Converter e Baixar PDF para receber o documento pronto. Na aba Gerador QR Code, digite o texto ou link desejado, clique em Gerar QR Code e baixe a imagem PNG criada na tela.

**4.Usando por Clientes HTTP (Apidog, Postman ou cURL): Para converter arquivos, faça uma requisição POST para http://localhost:8080/api/v1/conversions/image-to-pdf usando multipart/form-data com o arquivo anexado no campo "file". Para criar um QR Code, faça uma requisição GET para http://localhost:8080/api/v1/qrcodes passando o texto no parâmetro "text" (ex: ?text=https://github.com) para receber os bytes da imagem.

**5.Verificando a Auditoria: Para conferir os registros de processamento no banco de dados, rode no terminal: docker exec -it toolbox-postgres psql -U postgres -d toolbox_db -c "SELECT id, operation_type, file_name, duration_ms, status FROM processing_audit ORDER BY id DESC LIMIT 5;"Pré-requisitos: Tenha instalado em sua máquina o Java 21 (JDK), o Docker e o Docker Compose.

**6.Subindo o Banco de Dados: Abra o terminal na pasta raiz do projeto e execute o comando "docker compose up -d" para iniciar o container do PostgreSQL em segundo plano na porta 5433 da sua máquina.

**7.Executando a API: Inicie a aplicação executando a classe principal pelo IntelliJ IDEA ou rode o comando "mvn clean spring-boot:run" no terminal. A API estará pronta para receber chamadas no endereço http://localhost:8080.

**8.Usando pela Interface Web: Com o backend ligado, abra o arquivo index.html no navegador. Na aba Imagem para PDF, escolha um arquivo PNG ou JPEG e clique em Converter e Baixar PDF para receber o documento pronto. Na aba Gerador QR Code, digite o texto ou link desejado, clique em Gerar QR Code e baixe a imagem PNG criada na tela.

**9.Usando por Clientes HTTP (Apidog, Postman ou cURL): Para converter arquivos, faça uma requisição POST para http://localhost:8080/api/v1/conversions/image-to-pdf usando multipart/form-data com o arquivo anexado no campo "file". Para criar um QR Code, faça uma requisição GET para http://localhost:8080/api/v1/qrcodes passando o texto no parâmetro "text" (ex: ?text=https://github.com) para receber os bytes da imagem.

**10.Verificando a Auditoria: Para conferir os registros de processamento no banco de dados, rode no terminal: docker exec -it toolbox-postgres psql -U postgres -d toolbox_db -c "SELECT id, operation_type, file_name, duration_ms, status FROM processing_audit ORDER BY id DESC LIMIT 5;"


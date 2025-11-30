# Getting Started

Olá, tudo bem?

Esta é a API para o site Mirage Project. Atualmente, ela já tem a capacidade de fazer login com tokens e fazer o controle de listas.
Todas as rotas são protegidas pelo Spring Security, sendo abertas somente as rotas `/auth`.

## Autenticação

As rotas de autenticação (`/auth`) são constituídas de:

### Login (`/auth/login`)
É uma rota **POST** que espera receber no corpo da requisição um JSON com informações de email e password.

**Request Body:**
```json
{
  "email": "ex@example.com",
  "password": "12345678"
}
```

**Response (200 OK):**
```json
{
    "userId": 1,
    "name": "Exemple user",
    "email": "ex@example.com",
    "avatarUrl": null,
    "token": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "expirationDateUTC": "2025-11-23T22:14:07.491Z"
    }
}
```

### Registro (`/auth/register`)
É uma rota **POST** para criar um novo usuário.

**Request Body:**
```json
{
  "name": "Novo Usuario",
  "email": "novo@example.com",
  "password": "senha123"
}
```

**Response (201 Created):**
Retorna os dados do usuário criado e o token de acesso (similar ao login).

### Logout (`/auth/logout`)
É uma rota **POST** para deslogar o usuário. Requer o token no header.

**Response (200 OK):**
```json
{
    "status": "OK",
    "message": "Logout successful"
}
```

### Dados do Usuário (`/auth/me`)
É uma rota **GET** que retorna as informações do usuário logado. Requer o token no header.

**Response (200 OK):**
```json
{
    "id": 1,
    "name": "Exemple user",
    "email": "ex@example.com",
    "Created_at": "2025-11-23T10:00:00.000Z"
}
```

## Listas de Filmes

As rotas de filmes (`/movies`) necessitam que o token seja enviado no header `Authorization` (Bearer Token).

### Obter Filmes de uma Lista (`GET /movies/{listName}`)
Retorna a lista de IDs dos filmes salvos em uma lista específica (ex: `favorites`, `watched`, `top-10`).

**Response (200 OK):**
```json
[101, 102, 550]
```

### Adicionar Filme à Lista (`POST /movies/{listName}`)
Adiciona um filme a uma lista específica.

**Request Body:**
```json
{
  "movieId": 550
}
```

**Response (200 OK):**
```json
{
    "status": "OK",
    "message": "Movie added to list 'favorites'"
}
```

### Remover Filme da Lista (`DELETE /movies/{listName}/{movieId}`)
Remove um filme específico de uma lista.

**Response (200 OK):**
```json
{
    "status": "OK",
    "message": "Movie removed from list 'favorites'"
}
```

### Deletar Lista (`DELETE /movies/{listName}`)
Remove todos os filmes de uma lista.

**Response (200 OK):**
```json
{
    "status": "OK",
    "message": "List removed 'favorites'"
}
```

### Atualizar Posição no Top 10 (`PUT /movies/top-10/position`)
Atualiza a posição de um filme na lista de Top 10.

**Request Body:**
```json
{
  "movieId": 550,
  "position": 2
}
```

**Response (200 OK):**
```json
{
    "status": "OK",
    "message": "Top-10 updated"
}
```


## Executando o Projeto Localmente
Para rodar o projeto em ambiente local, recomenda-se o uso da IDE IntelliJ. É necessário configurar corretamente o arquivo .env, pois ele não é detectado automaticamente.

Após isso, você pode executar o projeto utilizando o comando de execução configurado ou simplesmente clicando no botão Run/Play dentro da própria IDE.

### Links

Frontend (Vercel): https://mirage-project.vercel.app/

Backend (Render): https://mirageserver.onrender.com/

### Participantes
- Rodrigo Garcia Alves
- Lavinia Rocha Brandino Silva

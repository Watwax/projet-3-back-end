# ChâTop — API backend

API REST de gestion des locations, des utilisateurs et des messages. Le backend utilise Java 17, Spring Boot, Spring Data JPA, MySQL et des JWT.

## Prérequis

- Java 17
- MySQL 8+
- Git

## Installation

```bash
git clone https://github.com/Watwax/projet-3-back-end.git
cd projet-3-back-end
```

Créer la base et les tables :

```sql
CREATE DATABASE chatop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Depuis la racine du projet, importer le schéma fourni :

```bash
mysql -u root -p chatop_db < database/schema.sql
```

Créer le fichier `.env` à la racine en copiant `.env.example`, puis renseigner les paramètres MySQL et générer une clé JWT :

```bash
cp .env.example .env
openssl rand -base64 32
```

Placer la clé générée dans `JWT_SECRET`. Le fichier `.env` n'est pas versionné. Les tables doivent être créées avant le démarrage ; Hibernate vérifie le schéma (`ddl-auto=validate`) et ne le modifie pas.

## Configuration

| Variable | Description | Exemple |
| --- | --- | --- |
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/chatop_db` |
| `DB_USERNAME` | Utilisateur MySQL | `chatop_user` |
| `DB_PASSWORD` | Mot de passe MySQL | — |
| `JWT_SECRET` | Clé de signature en Base64 (32 octets minimum) | `openssl rand -base64 32` |
| `JWT_EXPIRATION_MS` | Durée de validité du JWT en millisecondes | `3600000` |
| `UPLOAD_DIR` | Dossier de stockage des images (facultatif) | `uploads` |

Les locations utilisent une surface et un prix entiers, conformément au contrat API.

## Lancement et tests

À la racine du dépôt :

```bash
./mvnw spring-boot:run
```

Exécuter les tests :

```bash
./mvnw test
```

L'API écoute par défaut sur `http://localhost:8080`.

## Documentation Swagger / OpenAPI

- Interface interactive : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Spécification OpenAPI JSON : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Les routes protégées proposent le schéma Bearer JWT dans Swagger : utiliser **Authorize** et fournir le token. Les routes d'inscription et de connexion sont publiques.

## Routes

| Méthode | Route | Accès |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | JWT |
| `GET` | `/api/rentals` | JWT |
| `GET` | `/api/rentals/{id}` | JWT |
| `POST` | `/api/rentals` | JWT |
| `PUT` | `/api/rentals/{id}` | JWT |
| `POST` | `/api/messages` | JWT |
| `GET` | `/api/user/{id}` | JWT |

La création d'une location utilise `multipart/form-data` avec `name`, `surface`, `price`, `description` et le fichier `picture`. Les images JPEG, PNG et GIF (10 Mo maximum) sont enregistrées dans `UPLOAD_DIR`; leur chemin `/uploads/<nom>` est stocké dans la base. Pour le développement avec Angular, le proxy doit relayer `/api/**` et `/uploads/**` vers le backend.

Les mots de passe ne sont jamais renvoyés par l'API. Le propriétaire d'une location est déduit du JWT lors de la création et seul ce propriétaire peut la modifier. L'identité de l'auteur d'un message est vérifiée par rapport au JWT.

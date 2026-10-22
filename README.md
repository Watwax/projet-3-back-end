# projet-3-back-end

## Connexion à la base de données locale

L'application utilise MySQL. Créer un fichier `.env` à la racine du projet à partir de `.env.example`, puis renseigner l'URL de la base et les identifiants. Générer une clé JWT avec `openssl rand -base64 32` et coller la sortie comme valeur de `JWT_SECRET`. Le fichier `.env` est ignoré par Git.

Lance ensuite l'application avec :

```bash
./mvnw spring-boot:run
```

Les tables doivent déjà exister dans la base : Hibernate vérifie leur structure au démarrage sans la modifier (`ddl-auto=validate`).

## Authentification

Les routes `POST /api/auth/register` et `POST /api/auth/login` sont publiques et renvoient un JWT dans un objet `{ "token": "..." }`. Toutes les autres routes nécessitent `Authorization: Bearer <token>`. La route `GET /api/auth/me` renvoie les informations publiques de l'utilisateur authentifié.

## Routes REST

Toutes les routes `/api/**` nécessitent un JWT valide, à l'exception de `POST /api/auth/register` et `POST /api/auth/login`. `GET /api/auth/me` renvoie les informations publiques de l'utilisateur authentifié.

| Méthode | Route | Résultat |
| --- | --- | --- |
| `GET` | `/api/rentals` | Liste des locations sous la forme `{ "rentals": [...] }` |
| `GET` | `/api/rentals/{id}` | Détail d'une location, avec l'URL de l'image dans `picture` |
| `POST` | `/api/rentals` | Création d'une location (`multipart/form-data`), réponse HTTP 200 |
| `PUT` | `/api/rentals/{id}` | Mise à jour d'une location (`multipart/form-data`) |
| `POST` | `/api/messages` | Envoi d'un message à propos d'une location |
| `GET` | `/api/user/{id}` | Informations publiques d'un utilisateur |

La création d'une location attend les champs `name`, `surface`, `price`, `description` et le fichier `picture`. La mise à jour accepte les mêmes champs ; l'image est conservée si `picture` est omis. Les fichiers JPEG, PNG et GIF de 10 Mo maximum sont stockés dans `uploads/` par défaut ; la variable `UPLOAD_DIR` permet de choisir un autre répertoire. Le chemin `/uploads/<nom>` est enregistré dans la colonne `picture` et renvoyé par l'API comme URL de l'image. En développement, le proxy Angular relaie également `/uploads/**` au backend.

Les réponses de location et d'utilisateur utilisent des DTO et n'exposent pas directement les entités JPA ni les mots de passe. Le propriétaire d'une location et l'auteur d'un message sont contrôlés par rapport au JWT ; les identifiants fournis par le client ne permettent pas d'agir au nom d'un autre utilisateur.

Les dates des réponses suivent le format Mockoon `yyyy/MM/dd`. Les réponses d'authentification refusée sont vides sur les routes privées, sauf `GET /api/auth/me` qui renvoie `{}` ; une erreur de connexion renvoie `{"message":"error"}` et les corps invalides renvoient `{}` avec le statut 400.

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

## Locations

La route `GET /api/rentals` renvoie les locations sous la forme `{ "rentals": [...] }`. Elle nécessite un JWT valide. Les locations sont sérialisées en DTO afin de ne pas exposer directement les entités JPA.

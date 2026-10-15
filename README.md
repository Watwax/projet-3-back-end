# projet-3-back-end

## Connexion à la base de données locale

L'application utilise MySQL. Créer un fichier `.env` à la racine du projet à partir de `.env.example`, puis renseigner l'URL de la base et les identifiants. Ce fichier est ignoré par Git.

Lance ensuite l'application avec :

```bash
./mvnw spring-boot:run
```

Les tables doivent déjà exister dans la base : Hibernate vérifie leur structure au démarrage sans la modifier (`ddl-auto=validate`).

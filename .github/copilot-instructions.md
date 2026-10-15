# Instructions pour GitHub Copilot - Projet Java Spring Boot

## Stack Technique
- **Langage :** Java 17
- **Framework :** Spring Boot
- **Gestionnaire de dépendances :** Maven
- **Accès aux données :** Spring Data JPA

## Architecture et Design Patterns
- **Architecture en couches :** Respecter strictement la séparation Controller -> Service -> Repository.
- **DTOs :** Ne jamais exposer les entités JPA directement dans les contrôleurs REST. Utiliser des DTOs dédiés pour les requêtes et les réponses.
- **Injection de dépendances :** Utiliser exclusivement l'injection par constructeur (déclarer les champs en `private final`). Éviter `@Autowired` sur les champs.
- **Gestion des transactions :** Annoter explicitement les méthodes modifiant l'état de la base de données dans la couche service avec `@Transactional`.

## Bonnes Pratiques API REST
- Suivre les conventions RESTful (noms de ressources au pluriel, verbes HTTP corrects : GET, POST, PUT, DELETE).
- Utiliser les codes de statut HTTP appropriés (200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found, 500 Internal Server Error).
- Valider systématiquement les payloads d'entrée dans les contrôleurs à l'aide de l'annotation `@Valid`.

## Gestion des Erreurs
- Centraliser la gestion des exceptions à l'aide d'un composant `@RestControllerAdvice`.
- Mapper les exceptions métier vers des codes de statut HTTP précis et retourner un format d'erreur uniforme.
- Utiliser SLF4J (via Lombok `@Slf4j`) pour tracer les erreurs critiques, en évitant de logger des données sensibles.

## Tests
- Rédiger des tests unitaires pour la logique métier avec JUnit 5 et Mockito.
- Rédiger des tests d'intégration pour les contrôleurs web en utilisant `MockMvc` ou `@WebMvcTest`.
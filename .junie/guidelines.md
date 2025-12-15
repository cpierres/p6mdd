# Development Guidelines for P6MDD Project

This document provides guidelines and instructions for developers working on the P6MDD project.
Ecrire les commentaires en français.

## Build/Configuration Instructions

### Prerequisites
- Java 22
- Node.js and npm (for Angular frontend)
- Docker and Docker Compose
- PostgreSQL (or use the provided Docker container)

### Backend (Spring Boot)

#### Configuration
1. The backend uses environment variables for configuration, defined in the `.env` file:
   ```
   P6_DB_USER=mdduser
   P6_DB_PASSWORD=mddpw
   ```

2. The application uses Spring Boot's configuration properties for security and JWT settings:
   - `jwt.secret-key`: Clé secrète encodée en Base64 pour la signature des JWT (HMAC)
   - `jwt.expiration-time`: Durée de vie du token d'accès (en secondes)
   - `jwt.refresh-token-expiration`: Durée de vie du refresh token (en secondes, par défaut 604800 = 7 jours)
   - `frontend.url`: Liste CSV des origines Front autorisées pour CORS (ex: `http://localhost:4200,https://app.example.com`)

#### Building and Running
1. Navigate to the backend module:
   ```
   cd modules/back
   ```

2. Build the application:
   ```
   ./mvnw clean install
   ```

3. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

4. Alternatively, use Docker Compose to start the database:
   ```
   docker-compose up -d
   ```

### Frontend (Angular)

#### Configuration
The frontend configuration is managed through Angular environment files.

#### Architecture
The frontend uses Angular's standalone component architecture exclusively:
- All components, directives, and pipes must be created using the standalone: true option
- No NgModules should be used in the application
- Feature organization is done through folders and imports, not modules
- Components should import their dependencies directly using the imports array in the @Component decorator
- privilégier les Signal par rapport à RxJS lorsque c'est possible

#### Building and Running
1. Navigate to the frontend module:
   ```
   cd modules/front
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Run the development server:
   ```
   npm start
   ```

4. Build for production:
   ```
   npm run build
   ```

## Testing Information

### Backend Testing

#### Test Framework
- JUnit 5 (Jupiter) for unit tests
- Spring Boot Test for integration tests
- Reactor Test for testing reactive components
- Spring Security Test for testing security components

#### Running Tests
1. Run all tests:
   ```
   cd modules/back
   ./mvnw test
   ```

2. Run a specific test class:
   ```
   ./mvnw test -Dtest=JwtServiceTest
   ```

#### Writing Tests
1. Place test classes in the `src/test/java` directory, mirroring the package structure of the class being tested.
2. Use the `@SpringBootTest` annotation for integration tests that require the Spring context.
3. Use `@Autowired` to inject dependencies.
4. Follow the Arrange-Act-Assert (AAA) pattern for test methods.

Example test for JwtService:
```java
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UUID userId;
    private String username;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "test@example.com";
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Given a user ID and username

        // When generating a token
        String token = jwtService.generateToken(userId, username);

        // Then the token should not be null or empty
        assertNotNull(token);
        //assertFalse(token.isEmpty());

        // And the token should be parseable and contain the correct claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtService.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Verify the subject (username)
        assertEquals(username, claims.getSubject());

        // Verify the custom claim (user ID)
        assertEquals(userId.toString(), claims.get("id").toString());

        // Verify that the token has an expiration date
        assertNotNull(claims.getExpiration());
    }
}
```

### Frontend Testing

#### Test Framework
- Jest for test specification
- Jest for test running
- Angular Testing Utilities for component testing (TestBed)

#### Running Tests
1. Run all tests:
   ```
   cd modules/front
   npm test
   ```

2. Run tests with code coverage:
   ```
   npm test -- --code-coverage
   ```

#### Writing Tests
1. Place test files alongside the files they test, with a `.spec.ts` suffix.
2. Use Angular's TestBed and Jest for component testing.
3. Follow the Arrange-Act-Assert (AAA) pattern for test methods.
4. Use cypress for e2e testing

Example component test:
```typescript
describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'front' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('front');
  });
});
```

## Additional Development Information

### Project Structure
- `modules/back`: Spring Boot backend (Spring Boot 3.4.4)
  - Reactive application using Spring WebFlux and R2DBC
  - JWT-based authentication
  - PostgreSQL database with Flyway migrations
- `modules/front`: Angular frontend
  - Angular 19.2.x
  - Angular Material for UI components
  - RxJS and Angular Signal for reactive programming 

### Code Style
- Backend:
  - Java 22 features
  - Lombok for reducing boilerplate
  - MapStruct for object mapping
  - Reactive programming with Project Reactor
- Frontend:
  - SCSS for styling
  - Angular standalone components only (no NgModules)
  - Angular Material design system
  - privilégier Signal par rapport à RxJS lorsque c'est possible puisque c'est la nouvelle norme d'Angular

### Database

#### Database Configuration
- PostgreSQL with R2DBC for reactive database access
- Flyway for database migrations
- Database name: dbmdd
- Schema name: mddsocial

#### Database Schema
The database uses a dedicated schema `mddsocial` for all tables. The schema is created in the migration file `V000.002__init_schemas.sql`.

#### Tables and Relationships

1. **users** - Stores user information
   - `id`: UUID, primary key, auto-generated using pgcrypto's gen_random_uuid()
   - `email`: VARCHAR(100), not null, unique
   - `password`: VARCHAR(100)
   - `username`: VARCHAR(80), not null, unique
   - `created_at`: TIMESTAMP, default NOW()
   - `updated_at`: TIMESTAMP, default NOW()

2. **topics** - Stores topic information
   - `id`: UUID, primary key, auto-generated using pgcrypto's gen_random_uuid()
   - `title`: VARCHAR(255), not null
   - `description`: TEXT, not null
   - The database is pre-populated with 5 example topics

3. **user_topic_subscription** - Junction table for many-to-many relationship between users and topics
   - `id`: UUID, primary key, auto-generated using pgcrypto's gen_random_uuid()
   - `user_id`: UUID, not null, foreign key to users.id with cascade delete
   - `topic_id`: UUID, not null, foreign key to topics.id with cascade delete

#### Entity Relationships
- A user can subscribe to multiple topics (many-to-many relationship)
- A topic can have multiple subscribers (many-to-many relationship)
- The relationship is managed through the user_topic_subscription junction table

#### Database Interaction
- The application uses Spring Data R2DBC for reactive database access
- Entity classes:
  - `User`: Maps to the users table
  - `Topic`: Maps to the topics table
  - `UserTopicSubscription`: Maps to the user_topic_subscription table
- Repository interfaces:
  - `UserRepository`: Provides CRUD operations for User entities
  - `TopicRepository`: Provides CRUD operations for Topic entities
  - `UserTopicSubscriptionRepository`: Provides CRUD operations for UserTopicSubscription entities

#### Migration Strategy
- Flyway is used for database migrations
- Migration files are located in `src/main/resources/db/migration/V000/`
- Migration files are executed in order based on their version number
- Current migrations:
  - `V000.001__init.sql`: Creates the pgcrypto extension
  - `V000.002__init_schemas.sql`: Creates the mddsocial schema
  - `V000.003__add_users_table.sql`: Creates the users table
  - `V000.004__add_topics_table.sql`: Creates the topics table and inserts example data
  - `V000.005__add_user_topic_subscription_table.sql`: Creates the user_topic_subscription table

### Docker Configuration

The project uses Docker and Docker Compose for containerization and orchestration:

#### Docker Compose
- A `docker-compose.yml` file is located at the root of the project
- Docker Compose is automatically executed by Spring Boot through the `spring-boot-docker-compose` dependency
- No manual Docker Compose commands are needed when running the application with Spring Boot

#### Docker Compose File Structure
```yaml
services:
  postgres-r2dbc:
    image: postgres:17
    restart: always
    container_name: postgres-r2dbc-mdd
    environment:
      POSTGRES_USER: ${P6_DB_USER}
      POSTGRES_PASSWORD: ${P6_DB_PASSWORD}
      POSTGRES_DB: dbmdd
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "mdduser"]
      interval: 10s
      timeout: 5s
      retries: 5
volumes:
  postgres_data:
```

#### Key Features
- Uses PostgreSQL 17 as the database container
- Environment variables are loaded from the `.env` file
- Persistent volume for database data
- Health check to ensure database availability
- Exposed on port 5432

### Authentication
- Authentification basée sur JWT (accès) + refresh token stocké côté serveur
- Spring Security (WebFlux) avec serveur de ressources OAuth2 (validation JWT)
- Intercepteurs et guards Angular pour gérer l’injection du token d’accès, le rafraîchissement automatique et la redirection si non authentifié

### API Documentation
- SpringDoc OpenAPI for API documentation
- Available at `/swagger-ui.html` when the backend is running

### Debugging
- Add debug logs in tests with @Sl4j for backend
- Use browser developer tools for frontend debugging

### Structure Générale du Projet Front

```
modules/front/
├── src/
│   ├── app/                  # Code source principal de l'application
│   ├── assets/               # Ressources statiques (images, fonts, etc.)
│   ├── environments/         # Configurations d'environnement (dev, prod)
│   ├── index.html            # Page HTML principale
│   ├── main.ts               # Point d'entrée de l'application
│   └── styles.css            # Styles globaux
```

#### Organisation du Répertoire `app`

```
app/
├── features/                 # Fonctionnalités principales organisées par domaine
├── home/                     # Composant de la page d'accueil
├── shared/                   # Éléments partagés à travers l'application
├── app.component.*           # Composant racine de l'application
├── app.config.ts             # Configuration de l'application
└── app.routes.ts             # Configuration des routes
```

#### Structure des Fonctionnalités (Features)

Chaque fonctionnalité dans le dossier `features` est organisée selon la structure suivante :

```
features/
├── auth/                     # Fonctionnalité d'authentification
│   ├── components/           # Composants réutilisables spécifiques à l'authentification
│   ├── interfaces/           # Interfaces TypeScript pour l'authentification
│   ├── pages/                # Pages complètes liées à l'authentification
│   └── services/             # Services pour la gestion de l'authentification
├── post/                     # Fonctionnalité de gestion des posts
├── topic/                    # Fonctionnalité de gestion des topics
└── user/                     # Fonctionnalité de gestion des utilisateurs
```

#### Distinction entre Components et Pages

- **Components** : Éléments réutilisables qui peuvent être intégrés dans différentes pages
  ```
  components/
  ├── topic-list/             # Exemple de composant pour afficher une liste de topics
  │   ├── topic-list.component.html
  │   ├── topic-list.component.scss
  │   ├── topic-list.component.spec.ts
  │   └── topic-list.component.ts
  ```

- **Pages** : Composants de niveau supérieur qui représentent des pages complètes de l'application
  ```
  pages/
  ├── topic-list-all/         # Page qui affiche tous les topics
  │   ├── topic-list-all.component.html
  │   ├── topic-list-all.component.scss
  │   ├── topic-list-all.component.spec.ts
  │   └── topic-list-all.component.ts
  ```

#### Éléments Partagés (Shared)

```
shared/
├── components/               # Composants réutilisables dans toute l'application
├── interceptors/             # Intercepteurs HTTP pour la gestion des requêtes
├── interfaces/               # Interfaces TypeScript partagées
├── models/                   # Modèles de données partagés
└── services/                 # Services utilisables dans toute l'application
```

#### Routing

Le fichier `app.routes.ts` définit toutes les routes de l'application :
- Routes principales pointant vers les composants de page
- Redirections pour les routes par défaut et non valides
- Organisation des routes par fonctionnalité (ex: `/auth/login`, `/topics`)

#### Conventions de Nommage

- **Composants** : `[nom]-component.ts`
- **Pages** : Généralement dans un dossier dédié avec le même modèle de nommage que les composants
- **Services** : `[nom].service.ts`
- **Tests** : `[nom-du-fichier].spec.ts`

#### Bonnes Pratiques

1. **Organisation par Fonctionnalité** : Chaque domaine fonctionnel est isolé dans son propre dossier
2. **Séparation des Préoccupations** : Distinction claire entre composants, services, interfaces, etc.
3. **Architecture Standalone** : Tous les composants sont autonomes (standalone: true)
4. **Réutilisation** : Les composants communs sont placés dans le dossier `shared`
5. **Tests Unitaires** : Chaque composant et service a son fichier de test associé

### Structure Générale du Projet Back
 
```
modules/back/
├── src/
│   ├── main/
│   │   ├── java/com/mdd/back/  # Code source principal de l'application
│   │   └── resources/          # Ressources et configurations
│   └── test/                   # Tests unitaires et d'intégration
├── .mvn/                       # Configuration Maven Wrapper
├── mvnw                        # Script Maven Wrapper pour Linux/Mac
└── mvnw.cmd                    # Script Maven Wrapper pour Windows
```

#### Organisation du Répertoire `java/com/mdd/back`

```
java/com/mdd/back/
├── config/                     # Configuration de l'application
├── controller/                 # Contrôleurs REST
├── entities/                   # Entités JPA/R2DBC
├── exception/                  # Exceptions personnalisées
├── mappers/                    # Mappers pour la conversion d'objets
├── models/                     # Modèles de données (DTO)
├── repositories/               # Repositories pour l'accès aux données
├── services/                   # Services métier
├── utils/                      # Utilitaires
└── BackApplication.java        # Point d'entrée de l'application
```

#### Structure des Ressources

```
resources/
├── db/migration/               # Scripts de migration Flyway
│   └── V000/                   # Migrations initiales
├── application.yml             # Configuration principale de l'application
└── application-test.yml        # Configuration pour les tests
```

#### Architecture des Contrôleurs

Les contrôleurs sont organisés par domaine fonctionnel :

```
controller/
├── AuthController.java         # Gestion de l'authentification
└── TopicController.java        # Gestion des topics
```

Chaque contrôleur :
- Est annoté avec `@RestController` et `@RequestMapping`
- Utilise des annotations Swagger pour la documentation
- Suit les principes REST pour les endpoints
- Retourne des réponses réactives avec `Mono` ou `Flux`

#### Modèle de Données

L'application utilise une architecture en couches avec :

1. **Entités** : Représentent les tables de la base de données
   ```
   entities/
   ├── BaseEntity.java          # Classe de base avec champs communs
   ├── User.java                # Entité utilisateur
   ├── Topic.java               # Entité topic
   └── UserTopicSubscription.java # Entité de relation many-to-many
   ```

2. **Repositories** : Interfaces pour l'accès aux données
   ```
   repositories/
   ├── UserRepository.java
   ├── TopicRepository.java
   └── UserTopicSubscriptionRepository.java
   ```

3. **Services** : Logique métier
   ```
   services/
   ├── AuthService.java         # Service d'authentification
   ├── JwtService.java          # Service de gestion des tokens JWT
   └── TopicService.java        # Service de gestion des topics
   ```

4. **DTOs** : Objets de transfert de données
   ```
   models/
   ├── UserDto.java             # DTO pour les utilisateurs
   ├── TopicDto.java            # DTO pour les topics
   └── AuthSuccess.java         # DTO pour les réponses d'authentification
   ```

#### Sécurité

La sécurité est gérée par Spring Security (WebFlux) avec JWT, en mode « serveur de ressources OAuth2 autonome » :

```
config/
└── SecurityConfig.java         # Configuration de la sécurité (WebFlux + OAuth2 Resource Server)
```

Principes et composants clés :
- Serveur de ressources OAuth2 avec validation JWT: `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`
- Émission des tokens côté application (rôle d’autorisation) via:
  - `JwtEncoder` (NimbusJwtEncoder + ImmutableSecret) pour signer les JWT (HMAC avec clé secrète Base64)
  - `JwtService` pour générer les tokens (claims: sub=email, id=UUID)
- Validation des tokens (rôle de ressource) via:
  - `ReactiveJwtDecoder` (NimbusReactiveJwtDecoder) initialisé avec la clé fournie par `JwtService#getSecretKey()`
- Hachage des mots de passe avec `PasswordEncoder` (BCrypt)
- Authentification stateless (pas de session côté serveur), HTTP Basic désactivé, CSRF désactivé pour API REST

Endpoints publics (sans authentification):
- `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`
- `/api/topics` (liste), flux SSE publics: `/api/topics/stats/stream`, `/api/posts/stream`, `/api/comments/stream`
- Swagger/OpenAPI: `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`
Toutes les autres routes requièrent une authentification Bearer JWT.

CORS:
- Les origines autorisées doivent être définies via la propriété `frontend.url` (liste CSV). Exemple: `http://localhost:4200,https://app.example.com`
- Dans le code actuel, `http://localhost:4200` est temporairement autorisé en dur (TODO rétablir la liste `frontend.url`).
- Méthodes autorisées: GET, POST, PUT, DELETE, OPTIONS. En-têtes autorisés: Authorization, Content-Type, Cookie. Expose: Set-Cookie, Access-Control-Allow-Credentials. Credentials activés.

Gestion des tokens:
- Access token (JWT): court terme, envoyé par le front dans l’en-tête Authorization: `Bearer <token>`.
- Refresh token: stocké côté serveur (table) et envoyé au client dans un cookie `refresh_token` HttpOnly.
  - Cookie: HttpOnly, `SameSite=Strict` en HTTPS sinon `Lax`, `Secure` si HTTPS, `Path=/`, `Max-Age=7 jours`.
  - Propriété de config: `jwt.refresh-token-expiration` (secondes, défaut 604800=7 jours).
- Flux:
  - Login/Register: génèrent un access token + créent un refresh token côté serveur et définissent le cookie HttpOnly.
  - Refresh: `/api/auth/refresh` lit le cookie `refresh_token`, valide en base, puis renvoie un nouvel access token et prolonge le cookie.
  - Logout: `/api/auth/logout` supprime en base les refresh tokens de l’utilisateur et expire le cookie côté client.

Front Angular (rappel):
- Intercepteur HTTP pour attacher l’access token aux requêtes sortantes et déclencher un refresh automatique si nécessaire.
- Guard de route pour rediriger les utilisateurs non authentifiés.
- Stocker l’access token en mémoire (ou storage sécurisé) et ne jamais exposer le refresh token (HttpOnly).

Propriétés de configuration pertinentes:
- `jwt.secret-key` (clé secrète Base64 pour HMAC)
- `jwt.expiration-time` (durée de l’access token)
- `jwt.refresh-token-expiration` (durée du refresh token)
- `frontend.url` (origines CORS autorisées)

#### Conventions de Nommage

- **Contrôleurs** : `[Nom]Controller.java`
- **Services** : `[Nom]Service.java`
- **Entités** : Noms au singulier, ex: `User.java`
- **Repositories** : `[Nom]Repository.java`
- **DTOs** : `[Nom]Dto.java` ou descriptif spécifique

#### Bonnes Pratiques

1. **Programmation Réactive** : Utilisation de Mono/Flux pour les opérations asynchrones
2. **Séparation des Préoccupations** : Architecture en couches (contrôleur, service, repository)
3. **Documentation API** : Annotations Swagger pour tous les endpoints
4. **Validation** : Validation des entrées avec les annotations Jakarta Validation
5. **Gestion des Exceptions** : Exceptions personnalisées et gestionnaire global

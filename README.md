# Projet P6 - MDD Full Stack


- En préalable à ce README, je recommande de lire le **Dossier des choix techniques et d'architecture** : 
[https://veille.cpierres.dscloud.me/assets/pdf/choix-techniques-archi-mvp.pdf](https://veille.cpierres.dscloud.me/assets/pdf/choix-techniques-archi-mvp.pdf)
- Vous pourrez également consulter mon site de veille technologique sur les Architectures, Spring et Angular :  

Application accessible depuis internet : [https://mdd.cpierres.dscloud.me/](https://mdd.cpierres.dscloud.me/)
Documentation swagger de l'API : [http://apimdd.cpierres.dscloud.me:8068/swagger-ui/](http://apimdd.cpierres.dscloud.me:8068/swagger-ui/)

## Introduction
A la base, l'application présente vise à répondre au cahier des charges du projet 6 MDD d'OpenClassrooms (réseau social MDD : le "Monde Des Développeurs").

Ce projet autorise une liberté concernant les choix des technologies mais doit respecter les maquettes d'écran fournies (pour deskop et mobile) :
[Maquettes Figma (desktop et mobile)](https://www.figma.com/file/Rflr3TVBog35BNMnn0DF09/Maquettes-MDD-(desktop-et-mobile)?node-id=0%3A1)

Il s'agit d'un **MVP** (Minimum Viable Product) devant servir de base du développement complet futur.
Une "base" de développement **se doit d'être solide et performante**. J'ai souhaité vraiment travailler les éléments
qualitatifs pour y parvenir.

Ce projet MVP a peu de fonctionnalités mais celles-ci sont bien pensées car suffisantes pour couvrir la plupart des uses case techniques (relations de 1-1, de 1 à plusieurs, tables d'association) afin de couvrir les mécanismes utiles à mettre en oeuvre.

Je précise que ce projet n'aborde pas certains points non demandés :
- pas de gestion du multi-langues
- pas de tests. Ce sujet a déjà été bien développé dans le projet précédent : https://github.com/cpierres/P5-Test-full-stack.

## Préalables d'installation

- Pour être testée, cette applicatio MVP n'a pas besoin d'être installée car déployée sur internet.

- Si néanmoins, vous souhaitez effectuer l'installation sur votre poste de développement, vous avez deux possibilités d'installation :
  - Option 1 : via maven avec pom parent multi-modules (profils : prodlocal et docker-image)
  - Option 2 : installation classique pour le dévelopeur (juste la base en docker) et exécution des modules back et front depuis les sources

Les étapes détaillées sont décrites ci-après.

### Pré-requis
  - Avoir Docker installé
  - Avoir Maven installé
  - Avoir node 22.14.0 installé (nécessaire uniquement pour l'installation classique développeur (option 2). Pour une installation de prodlocal, maven se charge de tout)
  - Charger l'application depuis le dépôt git : `https://github.com/cpierres/p6mdd`

### Installation option 1 (la plus rapide) : installation via le pom multi-modules maven (avec profils prodlocal et docker-image)

L'application est installée entièrement sous docker (base de données, back et front), prête pour la prod, via le pom parent.
Les fichiers d'environnement ne doivent théoriquement jamais être versionnés. 
- Pour cette application de démonstration, vous avez tout de même un fichier modèle : `_env.prodlocal.yml` que vous devez renommer en `.env.prodlocal.yml`
- Ouvrir `docker-compose.prodlocal.yml`
  - Adapter si besoin le port de PostgreSQL exposé localement (exposé sur 5437)
  - Adapter si besoin le port du backend exposé localement (exposé sur 8067)
  - Le frontend est exposé sur le port : 67
- Ensuite, depuis le répertoire parent, exécutez : 
  ```
  mvn clean install -P prodlocal,docker-image
  ```
  
- Mise en route :
  ```
  docker-compose --env-file .env.prodlocal -f docker-compose.prodlocal.yml -p p6-mdd-prodlocal up
  ```
- Accès à l'application, url : [`http://localhost:67/`](http://localhost:67/)
- Documentation swagger de l'API : [`http://localhost:8067/swagger-ui/index.html`](http://localhost:8067/swagger-ui/index.html)

### Installation option 2 : installation classique (pour le développement)
La démarche globale est la suivante :
- La base de données est installée/exécutée sous docker. 
- Puis on exécute le backend depuis les sources du module back.
- Enfin, on exécute le frontend depuis les sources du module front.

#### Installation de la base de données Postgresql depuis docker-compose

Depuis le répertoire racine :
  - Dupliquez ou bien renommez le fichier `.env.example` vers un fichier nommé `.env`
  - Ouvrir `docker-compose.yml`
    - Adapter éventuellement le port de PostgreSQL exposé localement si besoin (par défaut c'est 5432)
 
- Lancer `docker-compose.yml` :
  ```
  docker-compose -p p6mdd-dev up -d
  ```

#### Exécution de l'application

##### Backend (Spring Boot)

1. Naviguez vers le répertoire du backend :
   ```
   cd .\modules\back\
   ```

2. Exécutez l'application avec Maven :
   ```
   ./mvnw spring-boot:run -P dev
   ```

   Autre possibilité. Sous Windows, utilisez :
   ```
   mvnw.cmd spring-boot:run -P dev
   ```

#### Frontend (Angular)

1. Naviguez vers le répertoire du frontend :
   ```
   cd .\modules\front\
   ```

2. Installez les dépendances :
   ```
   npm install
   ```

3. Lancez le serveur de développement :
   ```
   npm start
   ```
   ou
   ```
   ng serve
   ```

- Ouvrez votre navigateur et accédez à `http://localhost:4200/`
- Documentation de l'API : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Documentation de l'API

- En mode développement : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Auteur et contexte
- Auteur : Christophe Pierrès
- Dans le cadre du projet N°6 d'OpenClassrooms, en vue d'obtenir la certification
  **Expert en développement logiciel**. Plus d'informations
  disponibles [ici](https://www.francecompetences.fr/recherche/rncp/36912/)




### Conclusion
La démarche d'OpenClassrooms est de réclamer une grande autonomie dans la recherche et développement pour aboutir aux différents objectifs.
Cela m'a parfaitement convenu puisque c'est ce que j'ai toujours fait.
Ma curiosité et ma volonté d'adopter les meilleures bonnes pratiques m'ont fait passer plus de temps sur les projets techniques par rapport aux projets non-techniques à venir qu'il me reste encore à faire.
L'essentiel pour moi est d'avoir atteint mes objectifs techniques. Je me devais de retrouver un niveau d'expertise correspondant à mon niveau d'expérience.
les projets non-techniques à venir n'ont pas de complexité et je pense les réaliser très rapidement pour les terminer d'ici fin août.

Vous aurez noté que mes projets sont gérés avec Docker et Docker-compose.
Le projet 6 a prévu un profil de déploiement pour la "prodlocal" et la "prodnas"
Je les ai déployé sur un NAS Synology Linux pour rendre publiques en https via un reverse proxy.

Ces projets pédagogiques m'ont permis :
- de maitriser les bonnes pratiques et de retrouver un niveau d'expertise sur les technologies les plus récentes,
- de mettre en ligne des projets consultables par des directions techniques afin de montrer mon savoir-faire.
  Par le passé, j'avais une obligation de confidentialité dans mes contrats de travail, ce qui m'interdisait de diffuser mon savoir faire sur internet.
  Donc pour moi, ces petits projets suffisants et bien pensés en termes techniques, sont satisfaisants pour montrer rapidement mon savoir-faire sur les dernières technologies.


## Technologies et bonnes pratiques appliquées pour le projet 6

### Automatisation des installations et déploiements avec Docker et Docker-compose
- Gestion automatisée de 3 profils de déploiements (dev, prodlocal sous windows, prodnas vers linux)
- Chargement automatisé des données initiales (via DataInitializer et DemoDataInitializer)

### Gestion automatisée des migrations de données avec flyway

### Backend avec SpringBoot 3.4.4 et Spring WebFlux

#### Mise à jour instantanée
- Mise à jour instantanée pour tous les utilisateurs suite à l'ajout d'un article ou d'un commentaire (SSE)
  - Les statistiques de popularité et les ajouts d'éléments sont actualisés en temps réel pour tous.
  - Voici un diagramme de séquence pour illustrer la mise en oeuvre d'un SSE avec Spring WebFlux.
Le use case est la mise à jour du SSE suite à l'ajout d'un commentaire (ce qui envoie l'information du commentaire ainsi que la mise à jour des statistiques de popularité pour tous les clients) :
![postCommentSSE.png](modules/front/docs/assets/diagrams/sequence/postCommentSSE.png)

#### Sécurité basée sur token


### Frontend avec Angular 19.2
- Full standalone components
- Mise en oeuvre des signaux

### Bonnes pratiques
#### Gestion des messages, exceptions et erreurs
- transmission des messages backend selon différents niveaux de sévérité (erreur, warning, info, success)
- centralisation de la gestion des erreurs tant au niveau backend (via handler) que frontend (via Interceptor) ; permet rigueur et simplification de la gestion côté frontend.
- erreurs backend regroupées lorsque nécessaires (par exemple, double contrôle d'unicité sur email et username en une seule passe).
- response empaquetée avec requestId, timestamp (utile pour tracer l'aspect asynchrone et le traitement d'une requête dans une architecture micro-services)

Ergonomie :
- doubler certaines règles de gestion backend vers le frontend pour améliorer l'ergonomie (contrôles de surface tels que validité du mot de passe côté backend et côté frontend pour un meilleur guidage et éviter du trafic réseau).

#### Respect rigoureux des principes SOLID

##### Backend (Spring Boot)

###### Single Responsibility Principle (SRP)
Le principe de responsabilité unique est bien respecté dans l'architecture backend :

- **Séparation claire des couches** : Le code est organisé en packages distincts (`controller`, `services`, `repositories`, `entities`, etc.) où chaque classe a une responsabilité unique.
- **Services spécialisés** : Les services sont divisés selon leurs responsabilités spécifiques :
  - `AuthenticationService` : Gère uniquement l'authentification des utilisateurs
  - `UserRegistrationService` : Responsable uniquement de l'enregistrement des utilisateurs
  - `UserProfileService` : Gère uniquement les profils utilisateurs
- **Façade pour simplifier l'accès** : La classe `AuthFacade` agit comme une façade qui délègue les appels aux services spécifiques, sans implémenter elle-même la logique métier.

###### Open/Closed Principle (OCP)
Le principe ouvert/fermé est respecté par :

- **Utilisation d'interfaces** : Les services sont définis par des interfaces (`IAuthenticationService`, `IUserRegistrationService`, `IUserProfileService`), permettant d'étendre les fonctionnalités sans modifier le code existant.
- **Architecture en couches** : L'architecture permet d'ajouter de nouvelles fonctionnalités sans modifier les composants existants.

###### Liskov Substitution Principle (LSP)
Le principe de substitution de Liskov est respecté par :

- **Implémentations cohérentes des interfaces** : Les classes comme `AuthenticationService` implémentent fidèlement les contrats définis par leurs interfaces, garantissant qu'elles peuvent être substituées sans affecter le comportement du programme.
- **Utilisation de types génériques** : L'utilisation de `Mono<T>` dans les signatures de méthodes permet une substitution cohérente des types de retour.

###### Interface Segregation Principle (ISP)
Le principe de ségrégation des interfaces est particulièrement bien appliqué :

- **Interfaces spécifiques** : Au lieu d'avoir une grande interface monolithique pour l'authentification, le code utilise trois interfaces distinctes :
  - `IAuthenticationService` : Méthodes liées à l'authentification
  - `IUserRegistrationService` : Méthodes liées à l'enregistrement
  - `IUserProfileService` : Méthodes liées à la gestion de profil
- **Interfaces ciblées** : Chaque interface ne contient que les méthodes nécessaires à sa responsabilité spécifique.

###### Dependency Inversion Principle (DIP)
Le principe d'inversion des dépendances est respecté par :

- **Injection de dépendances** : Utilisation systématique de l'injection de dépendances via les constructeurs.
- **Dépendances vers des abstractions** : Les classes dépendent d'interfaces plutôt que d'implémentations concrètes.
- **Configuration Spring** : L'utilisation de Spring facilite l'inversion de contrôle et l'injection de dépendances.

##### Frontend (Angular)

###### Single Responsibility Principle (SRP)
Le frontend respecte également le principe de responsabilité unique :

- **Architecture par fonctionnalités** : Organisation du code en dossiers par domaine fonctionnel (`auth`, `user`, `topic`, etc.).
- **Composants autonomes** : Chaque composant a une responsabilité unique :
  - `RegisterComponent` : Gère uniquement l'inscription
  - `UserFormComponent` : Responsable uniquement de l'affichage et de la validation du formulaire
- **Services spécialisés** :
  - `AuthService` : Gère uniquement les opérations d'authentification
  - `ErrorHandlingService` : Responsable uniquement de la gestion des erreurs
  - `SessionService` : Gère uniquement l'état de la session utilisateur

###### Open/Closed Principle (OCP)
Le principe ouvert/fermé est respecté par :

- **Architecture standalone** : L'utilisation de composants autonomes facilite l'extension sans modification.
- **Interfaces TypeScript** : Définition d'interfaces pour les modèles de données (`User`, `RegisterRequest`, etc.).
- **Séparation des préoccupations** : La séparation entre composants, services et interfaces permet d'étendre les fonctionnalités sans modifier le code existant.

###### Liskov Substitution Principle (LSP)
Le principe de substitution de Liskov est respecté par :

- **Utilisation cohérente des types** : Les interfaces TypeScript garantissent que les objets peuvent être substitués sans affecter le comportement.
- **Héritage approprié** : Les composants et services respectent les contrats définis par leurs interfaces.

###### Interface Segregation Principle (ISP)
Le principe de ségrégation des interfaces est respecté par :

- **Interfaces ciblées** : Chaque interface définit uniquement les propriétés nécessaires à son contexte d'utilisation.
- **Interfaces spécifiques** : Utilisation d'interfaces distinctes pour différents besoins :
  - `RegisterRequest` pour l'inscription
  - `LoginRequest` pour la connexion
  - `UserUpdate` pour la mise à jour du profil

###### Dependency Inversion Principle (DIP)
Le principe d'inversion des dépendances est respecté par :

- **Injection de dépendances Angular** : Utilisation du système d'injection de dépendances d'Angular.
- **Services injectables** : Les services sont déclarés avec `@Injectable()` et injectés dans les composants.
- **Dépendances vers des abstractions** : Les composants dépendent des interfaces des services plutôt que de leurs implémentations.

#### Conclusion

Le projet P6MDD respecte rigoureusement les principes SOLID tant au niveau du backend que du frontend :

1. **SRP** : Séparation claire des responsabilités dans les services, contrôleurs et composants.
2. **OCP** : Architecture extensible grâce aux interfaces et à la séparation des préoccupations.
3. **LSP** : Implémentations cohérentes des interfaces permettant la substitution.
4. **ISP** : Interfaces spécifiques et ciblées pour chaque besoin fonctionnel.
5. **DIP** : Utilisation systématique de l'injection de dépendances et de l'abstraction.

Cette adhérence aux principes SOLID contribue à la maintenabilité, l'extensibilité et la robustesse du code, facilitant ainsi les évolutions futures et la collaboration entre développeurs.

## Scénarios destinés à mettre en valeur l'apport des technologies utilisées pour l'utilisateur
Ces scénarios vous guident sur l'utilisation de l'application et sont faits pour illustrer et commenter les apports techniques :
- mise à jour en temps réel de l'IHM pour tous les utilisateurs (un article ou un commentaire nouveau ainsi que les statistiques de popularité apparaissent instantanément pour tous les utilisateurs sans besoin d'actualiser le browser)
- messages des règles de gestion

L'objectif est de mettre en valeur les apports des technologies utilisées :
- mises à jour en temps réel pour tous les utilisateurs, sur tous les browsers et téléphones mobiles (avec une capacité de montée en charge maximale)
- ergonomie

## Affichez l'application sur 2 navigateurs différents ainsi que sur votre téléphone mobile (avec des logins différents)

L'objectif sera de constater la mise à jour simultanée de tous les accès, ceci avec différents noms d'utilisateur.

- Affichez l'application depuis [https://mdd.cpierres.dscloud.me/](https://mdd.cpierres.dscloud.me/) sur un browser
  Chrome
- Affichez l'application depuis [https://mdd.cpierres.dscloud.me/](https://mdd.cpierres.dscloud.me/) sur un browser Edge
- Affichez l'application depuis [https://mdd.cpierres.dscloud.me/](https://mdd.cpierres.dscloud.me/) sur votre téléphone
  mobile


## Scénarios d'inscription et de connexions

### Inscription via le browser chrome (utilisateur u1)
Enregistrez-vous via le bouton `S'inscrire` :
- Dans un premier temps, saisissez volontairement un utilisateur qui existe déjà : email `u2@test.com` ainsi que le username : `u2`
  - Notez les contrôles de surface telles que :
    - la présence d'un `@` dans l'email par exemple,
    - le fait que les informations soient obligatoires,
    - la longueur du username qui doit être d'au moins 2 caractères etc...
- Pour le mot de passe, saisissez une valeur ne correspondant pas aux contraintes de mot de passe, par exemple : `Test`
- Cliquez sur le bouton `S'inscrire` (qui s'active uniquement lorsque les contrôles de surface sont valides)
  Les contrôles de surface côté frontend se déclenchent, empêchant l'envoi au serveur :
![register-ctrl-surface.jpg](modules/front/docs/assets/screens/register-ctrl-surface.jpg)
- Corrigez le mot de passe avec une stucture valide, par exemple : `Test!1234`
  - vous aurez noté qu'au fur et à mesure de votre saisie, des guidages instantanés apparaissent

- Maintenant que les contrôles de surface ont été validés, ce sont les contrôles backend d'unicité qui apparaissent :
![register-unicity.jpg](modules/front/docs/assets/screens/register-unicity.jpg)
  - Notez que les deux contrôles d'unicité apparaissent en une seule passe (gérée sur le backend via `MultipleResourceAlreadyExistException` de type 409).
  - La bulle d'avertissement générale en haut d'écran s'effacera automatiquement après un laps de temps
    - La sévérité des messages est qualifiée via le backend et gérée automatiquement par le frontend d'une manière centralisée (via Interceptor et structure des erreurs toujours homogène) 
  - Les erreurs sous les champs s'effaceront dès lors qu'on corrige

- Corrigez l'email en indiquant une valeur qui n'existe pas déjà ; pour la démo, indiquez `u1@test.com`
- Cliquez sur le bouton `S'inscrire`
- Corrigez la dernière erreur de contrôle d'unicité, en indiquant `u1` pour le username 
- Cliquez sur le bouton `S'inscrire`
- A la suite d'un enregistrement valide, l'utilisateur `u1` est directement connecté : 
![connexion-u1.jpg](modules/front/docs/assets/screens/connexion-u1.jpg)

### Connexion de l'utilisateur u2 via le browser edge
Pour cette deuxième connexion via Edge, vous allez utiliser le username `u2` (qui existe déjà comme déjà vu).
- Cliquez sur le bouton `Se connecter`
  - A noter que vous pouvez vous connecter aussi bien avec le username qu'avec son email.
  - Si vous faites une erreur sur le mot de passe ou bien sur le nom d'utilisateur, l'erreur qui est affichée est volontairement vague afin de ne pas donner d'indication à un hacker.
  
![connexion-u2-failed.jpg](modules/front/docs/assets/screens/connexion-u2-failed.jpg)

- Voici u1 et u2 connectés dans deux browsers différents :

![connexions-u1-u2-ok.jpg](modules/front/docs/assets/screens/connexions-u1-u2-ok.jpg)

### Enregistrez-vous également via votre téléphone mobile avec vos propres références
- puis affichez la page des Thèmes :
screen


## Scénarios d'abonnement

### u2 doit s'abonner au Thème 
Si vous cliquez sur le logo Profil, vous avez un guidage qui vous indique que vous n'avez pas encore d'abonnement :

Un message vous indique que vous devez aller sur la page de Thèmes :



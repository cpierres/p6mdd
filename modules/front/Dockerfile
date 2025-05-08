# Etape 1 : Construction de l'application Angular
FROM node:20 AS build
WORKDIR /app

# Copier les fichiers de configuration
COPY package*.json ./
RUN npm install

# Copier le code source
COPY . .

# Variable d'environnement pour gérer la configuration Angular dynamiquement (valeur par défaut : `prodlocal`)
ARG ENV_NAME=prodlocal
ENV ENV_NAME=${ENV_NAME}

# Construire l'application en fonction de la configuration Angular spécifiée
RUN npm run build -- --configuration=${ENV_NAME}

# Etape 2 : Configuration du serveur Nginx pour le déploiement
FROM nginx:alpine

# Re-déclarer la variable ARG dans cette étape pour qu'elle soit disponible
ARG ENV_NAME=prodlocal

# Copier les fichiers Angular générés
COPY --from=build /app/dist/front/browser /usr/share/nginx/html

# Configuration de prod pour rediriger les requêtes vers l'API backend
# Copier le fichier de configuration Nginx en fonction de la variable
# d'environnement
COPY nginx.${ENV_NAME}.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

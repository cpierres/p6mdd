FROM node:20 as build
WORKDIR /app

# Copier les fichiers de configuration
COPY package*.json ./
RUN npm install

# Copier le code source
COPY . .

# Construire l'application en mode production
RUN npm run build -- --configuration=production

# Étape de production avec Nginx
FROM nginx:alpine
COPY --from=build /app/dist/front/browser /usr/share/nginx/html

# Configuration pour rediriger les requêtes vers l'API backend
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

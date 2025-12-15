#!/bin/bash
set -e

echo "🧪 Test de la branche feature/reverse-proxy-adapt"
echo "================================================="

# 1. Récupérer la version Maven
MAVEN_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "📦 Version Maven détectée: $MAVEN_VERSION"

# 2. Vérifier l'état Git
echo "🌿 Branche actuelle:"
git branch --show-current

echo "📊 État des modifications:"
git status --short

# 3. Construire les images Docker
echo "🔨 Construction des images Docker..."
mvn clean install -P prodnas,docker-image -q

# 4. Vérifier que les images ont été créées
echo "🐳 Images Docker créées:"
docker images | grep "cpierres/ocr-p6-mdd.*$MAVEN_VERSION"

# 5. Arrêter les containers existants s'ils existent
echo "🛑 Arrêt des containers existants..."
docker-compose --env-file .env.prodnas -f docker-compose.prodnas.yml -p p6-mdd-test down 2>/dev/null || true

# 6. Déployer avec la version Maven
echo "🚢 Déploiement test avec version $MAVEN_VERSION..."
export MAVEN_PROJECT_VERSION=$MAVEN_VERSION
docker-compose --env-file .env.prodnas -f docker-compose.prodnas.yml -p p6-mdd-test up -d

# 7. Attendre que les services démarrent
echo "⏳ Attente du démarrage des services..."
sleep 30

# 8. Vérifier l'état des containers
echo "📋 État des containers:"
docker-compose --env-file .env.prodnas -f docker-compose.prodnas.yml -p p6-mdd-test ps

# 9. Tester la connectivité
echo "🔍 Test de connectivité:"
echo "  - Backend: http://localhost:8066/actuator/health"
curl -s http://localhost:8066/actuator/health | jq '.' || echo "Backend non accessible"

echo "  - Frontend: http://localhost:66"
curl -s -o /dev/null -w "%{http_code}" http://localhost:66 || echo "Frontend non accessible"

echo ""
echo "✅ Test terminé !"
echo "🌐 URLs de test:"
echo "  - Frontend: http://localhost:66"
echo "  - Backend: http://localhost:8066"
echo "  - API Swagger: http://localhost:8066/swagger-ui/index.html"
echo ""
echo "🛠️  Commandes utiles:"
echo "  - Voir les logs: docker-compose --env-file .env.prodnas -f docker-compose.prodnas.yml -p p6-mdd-test logs -f"
echo "  - Arrêter: docker-compose --env-file .env.prodnas -f docker-compose.prodnas.yml -p p6-mdd-test down"

# Guide de Démarrage - Premiers Pas avec le Projet RAG

Ce guide vous accompagne pas à pas pour lancer le projet et comprendre le RAG.

## ⏱️ Temps estimé: 15-20 minutes

## Étape 1: Lancer les Services Docker (5 min)

### 1.1 Démarrer Docker Desktop

Assurez-vous que Docker Desktop est lancé sur votre Mac.

### 1.2 Lancer les conteneurs

```bash
cd ~/Desktop/IA/JavaRAG
docker-compose up -d
```

Vous devriez voir :
```
✔ Container rag-postgres  Started
✔ Container rag-ollama    Started
```

### 1.3 Vérifier que les services sont prêts

```bash
# Vérifier PostgreSQL
docker-compose ps

# Vérifier Ollama
curl http://localhost:11434/api/tags
```

## Étape 2: Installer les Modèles d'IA (5-10 min)

Les modèles d'IA doivent être téléchargés. C'est la partie la plus longue !

```bash
chmod +x scripts/setup-ollama.sh
./scripts/setup-ollama.sh
```

Cela télécharge :
- **nomic-embed-text** (~274 MB) : Pour les embeddings
- **llama3.2** (~2 GB) : Pour générer les réponses

☕ Prenez un café pendant le téléchargement...

## Étape 3: Lancer l'Application Spring Boot (2 min)

```bash
./gradlew bootRun
```

Attendez de voir :
```
Started JavaRagApplication in X.XXX seconds
```

L'application est maintenant accessible sur `http://localhost:8080`

## Étape 4: Tester l'API (2 min)

### 4.1 Vérifier que l'API fonctionne

```bash
curl http://localhost:8080/api/health
```

Réponse attendue :
```json
{
  "status": "UP",
  "service": "Java RAG API"
}
```

### 4.2 Charger des données d'exemple

```bash
chmod +x scripts/load-sample-data.sh
./scripts/load-sample-data.sh
```

Cela indexe 4 documents sur Java, Spring Boot, RAG et les embeddings.

### 4.3 Vérifier les statistiques

```bash
curl http://localhost:8080/api/stats
```

Réponse attendue :
```json
{
  "totalDocuments": 4,
  "status": "ready"
}
```

## Étape 5: Poser votre Première Question ! 🎉

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Qu'\''est-ce que Java ?",
    "topK": 3
  }'
```

Vous devriez recevoir une réponse générée par l'IA basée sur les documents indexés !

## 🎓 Comprendre ce qui s'est passé

Quand vous posez une question, le système RAG fait ceci :

```
1️⃣ RETRIEVAL (Récupération)
   └─> Transforme votre question en vecteur (embedding)
   └─> Cherche les 3 documents les plus similaires dans la base
   └─> Trouve: "Java est un langage...", "Spring Boot...", etc.

2️⃣ AUGMENTATION (Enrichissement)
   └─> Construit un contexte avec ces 3 documents
   └─> Crée un prompt structuré

3️⃣ GENERATION (Génération)
   └─> Envoie le prompt + contexte au LLM (llama3.2)
   └─> Le LLM génère une réponse basée sur les documents
   └─> Retourne la réponse
```

## 📝 Exercices Pratiques

### Exercice 1: Indexer votre propre document

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Docker est une plateforme qui permet de créer, déployer et exécuter des applications dans des conteneurs. Les conteneurs sont des environnements isolés qui contiennent tout ce dont une application a besoin pour fonctionner.",
    "source": "docker-intro"
  }'
```

Maintenant, posez une question sur Docker :

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Qu'\''est-ce que Docker ?",
    "topK": 3
  }'
```

### Exercice 2: Voir tous les documents

```bash
curl http://localhost:8080/api/documents | json_pp
```

### Exercice 3: Tester la pertinence

Posez une question qui N'EST PAS dans les documents :

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Quelle est la capitale de la France ?",
    "topK": 3
  }'
```

Le système devrait dire qu'il ne trouve pas l'information dans le contexte !

## 🔍 Explorer le Code

Maintenant que tout fonctionne, explorez le code :

1. **RAGService.java** : Le cœur de la logique RAG
2. **EmbeddingService.java** : Comment on génère les vecteurs
3. **LLMService.java** : Comment on génère les réponses
4. **RAGController.java** : Les endpoints de l'API

Tous les fichiers sont commentés pour faciliter la compréhension !

## 🛑 Arrêter les Services

Quand vous avez fini :

```bash
# Arrêter l'application Spring Boot
Ctrl + C

# Arrêter les conteneurs Docker
docker-compose down
```

## 🚨 Problèmes Courants

### "Connection refused" sur PostgreSQL

```bash
# Attendre un peu plus que les services démarrent
docker-compose ps

# Si un service est "unhealthy", le redémarrer
docker-compose restart postgres
```

### "Model not found" dans Ollama

```bash
# Relancer le script de setup
./scripts/setup-ollama.sh

# Vérifier les modèles installés
docker exec rag-ollama ollama list
```

### L'application ne démarre pas

```bash
# Vérifier que Java 17+ est installé
java -version

# Nettoyer et recompiler
./gradlew clean build
./gradlew bootRun
```

## ✅ Checklist de Démarrage

- [ ] Docker Desktop est lancé
- [ ] `docker-compose up -d` a réussi
- [ ] Les modèles Ollama sont téléchargés
- [ ] L'application Spring Boot démarre
- [ ] `curl http://localhost:8080/api/health` retourne "UP"
- [ ] Les données d'exemple sont chargées
- [ ] Une première question retourne une réponse

## 🎯 Prochaines Étapes

1. Lisez le **README.md** pour comprendre l'architecture complète
2. Explorez les fichiers dans `examples/api-examples.http`
3. Modifiez le code pour expérimenter
4. Indexez vos propres documents (PDF, articles, documentation)
5. Testez avec différents modèles dans `application.yml`

**Bravo ! Vous avez un système RAG fonctionnel ! 🎉**

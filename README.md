# 🚀 Java RAG - Apprendre le Retrieval Augmented Generation

Un projet pédagogique complet pour découvrir et comprendre le **RAG (Retrieval Augmented Generation)** avec Java Spring Boot.

## 📚 Qu'est-ce que le RAG ?

Le **RAG** (Retrieval Augmented Generation) est une technique d'intelligence artificielle qui combine :

1. **🔍 Retrieval (Récupération)** : Recherche d'informations pertinentes dans une base de données
2. **➕ Augmentation** : Enrichissement du contexte avec ces informations
3. **✨ Generation** : Génération de réponses avec un LLM (Large Language Model) basé sur ce contexte enrichi

### Pourquoi le RAG ?

Les LLMs (comme GPT, Llama, etc.) ont des limites :
- ❌ Connaissances figées à leur date d'entraînement
- ❌ Pas d'accès à vos données privées
- ❌ Peuvent inventer des informations (hallucinations)

Le RAG résout ces problèmes en :
- ✅ Permettant d'utiliser des données récentes ou privées
- ✅ Ancrant les réponses dans des sources vérifiables
- ✅ Réduisant les hallucinations

## 🏗️ Architecture du Projet

```
┌─────────────────────────────────────────────────────────────┐
│                    Utilisateur                               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Question
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   API REST (Spring Boot)                     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │         RAGService (Orchestrateur)                  │    │
│  │                                                      │    │
│  │  1️⃣ Retrieval: Cherche documents pertinents        │    │
│  │  2️⃣ Augmentation: Construit le contexte            │    │
│  │  3️⃣ Generation: Génère la réponse                  │    │
│  └─────┬─────────────────┬──────────────────┬─────────┘    │
│        │                 │                  │               │
│        ▼                 ▼                  ▼               │
│  ┌──────────┐    ┌──────────────┐   ┌─────────────┐       │
│  │Embedding │    │ Document     │   │ LLM         │       │
│  │Service   │    │ Repository   │   │ Service     │       │
│  └────┬─────┘    └──────┬───────┘   └──────┬──────┘       │
│       │                 │                   │               │
└───────┼─────────────────┼───────────────────┼───────────────┘
        │                 │                   │
        ▼                 ▼                   ▼
   ┌─────────┐      ┌──────────┐       ┌─────────┐
   │ Ollama  │      │PostgreSQL│       │ Ollama  │
   │(embed)  │      │          │       │ (LLM)   │
   └─────────┘      └──────────┘       └─────────┘
```

## 🛠️ Technologies Utilisées

- **Java 17** : Langage de programmation
- **Spring Boot 3.2** : Framework backend
- **PostgreSQL** : Base de données relationnelle
- **Ollama** : Modèles d'IA en local (embeddings + LLM)
- **Docker & Docker Compose** : Containerisation
- **Gradle** : Gestion des dépendances

## 📋 Prérequis

- Java 17 ou supérieur
- Docker et Docker Compose
- Gradle 8.0+ (ou utiliser le wrapper inclus)
- Git

## 🚀 Démarrage Rapide

### 1. Cloner le projet

```bash
git clone <votre-repo>
cd JavaRAG
```

### 2. Démarrer les services Docker

```bash
# Démarrer PostgreSQL et Ollama
docker-compose up -d

# Attendre que les services soient prêts (environ 30 secondes)
```

### 3. Configurer Ollama

```bash
# Télécharger les modèles nécessaires
chmod +x scripts/setup-ollama.sh
./scripts/setup-ollama.sh
```

Cela va télécharger :
- `nomic-embed-text` : Modèle pour générer les embeddings (vecteurs)
- `llama3.2` : Modèle pour générer les réponses

### 4. Lancer l'application Spring Boot

```bash
# Compiler et lancer
./gradlew bootRun
```

L'application démarre sur `http://localhost:8080`

### 5. Charger des données d'exemple

```bash
# Indexer des documents d'exemple
chmod +x scripts/load-sample-data.sh
./scripts/load-sample-data.sh
```

### 6. Tester l'API

```bash
# Poser une question au système RAG
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Qu'\''est-ce que Java ?",
    "topK": 3
  }'
```

## 📖 Guide d'Utilisation

### Indexer un Document

Pour ajouter un document à la base de connaissances :

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Votre contenu ici...",
    "source": "nom-de-la-source"
  }'
```

### Poser une Question

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Votre question ?",
    "topK": 3
  }'
```

Le paramètre `topK` définit combien de documents pertinents seront récupérés (par défaut 3).

### Voir tous les Documents

```bash
curl http://localhost:8080/api/documents
```

### Statistiques

```bash
curl http://localhost:8080/api/stats
```

## 🧪 Exemples d'API

Consultez le fichier `examples/api-examples.http` pour voir tous les exemples d'utilisation.

Si vous utilisez VS Code ou IntelliJ, vous pouvez exécuter ces requêtes directement depuis l'éditeur.

## 🔍 Comprendre le Code

### 1. EmbeddingService

Transforme du texte en vecteurs numériques (embeddings).

```java
// Génère un vecteur de 384 dimensions représentant le texte
List<Double> embedding = embeddingService.generateEmbedding("Bonjour le monde");
```

### 2. RAGService - La Logique RAG

Le cœur du système en 3 étapes :

```java
public String answerQuestion(String question, int topK) {
    // 1️⃣ RETRIEVAL: Trouver les documents pertinents
    List<Document> docs = retrieveRelevantDocuments(question, topK);
    
    // 2️⃣ AUGMENTATION: Construire le contexte
    String context = buildContext(docs);
    
    // 3️⃣ GENERATION: Générer la réponse
    String prompt = buildPrompt(context, question);
    return llmService.generateResponse(prompt);
}
```

### 3. LLMService

Génère des réponses en langage naturel avec un LLM.

```java
String response = llmService.generateResponse(prompt);
```

## 🎯 Cas d'Usage du RAG

1. **Chatbot d'entreprise** : Répondre à des questions sur des documents internes
2. **Assistant de documentation** : Rechercher dans une documentation technique
3. **Analyse de contrats** : Extraire des informations de documents légaux
4. **Support client** : Réponses basées sur une base de connaissances
5. **Recherche académique** : Questions sur des articles scientifiques

## 🔧 Configuration

### Utiliser OpenAI au lieu d'Ollama

Modifiez `src/main/resources/application.yml` :

```yaml
ai:
  provider: openai  # au lieu de "ollama"
  openai:
    api-key: votre-clé-api
```

### Ajuster les Modèles

```yaml
ai:
  ollama:
    embedding-model: nomic-embed-text  # ou un autre modèle
    chat-model: llama3.2               # ou llama2, mistral, etc.
```

## 📊 Concepts Clés du RAG

### Embeddings (Vecteurs)

Les embeddings transforment du texte en vecteurs numériques :

```
"Java est un langage" → [0.23, -0.45, 0.67, ..., 0.12]
                         (vecteur de 384 dimensions)
```

Des textes similaires ont des vecteurs proches dans l'espace vectoriel.

### Similarité Cosinus

Mesure la proximité entre deux vecteurs :

```
similarité("Java", "langage programmation") = 0.85  ← Très similaire
similarité("Java", "cuisine italienne")     = 0.12  ← Peu similaire
```

### Le Prompt RAG

Le prompt envoyé au LLM contient :

1. **Instructions** : Comment répondre
2. **Contexte** : Documents pertinents récupérés
3. **Question** : La question de l'utilisateur

Exemple :

```
Tu es un assistant qui répond aux questions en te basant sur le contexte fourni.

Contexte:
Document 1: Java est un langage de programmation...
Document 2: Spring Boot facilite le développement...

Question: Qu'est-ce que Java ?

Réponse:
```

## 🐛 Dépannage

### Ollama ne démarre pas

```bash
# Vérifier les logs
docker logs rag-ollama

# Redémarrer le conteneur
docker-compose restart ollama
```

### L'application ne se connecte pas à PostgreSQL

```bash
# Vérifier que PostgreSQL est prêt
docker-compose ps

# Voir les logs
docker logs rag-postgres
```

### Les embeddings génèrent des erreurs

Vérifiez que les modèles Ollama sont bien téléchargés :

```bash
docker exec rag-ollama ollama list
```

Si absent, relancez le script de setup :

```bash
./scripts/setup-ollama.sh
```

## 🎓 Aller Plus Loin

### Améliorations Possibles

1. **pgvector** : Utiliser l'extension PostgreSQL pour une vraie recherche vectorielle
2. **Chunking** : Découper les longs documents en chunks
3. **Reranking** : Réordonner les résultats avec un modèle spécialisé
4. **Cache** : Mettre en cache les embeddings et réponses
5. **Streaming** : Streamer les réponses du LLM en temps réel
6. **Interface Web** : Créer un frontend React/Vue
7. **Authentification** : Ajouter Spring Security
8. **Métriques** : Collecter des métriques sur les performances

### Ressources

- [Documentation Ollama](https://ollama.ai/)
- [pgvector](https://github.com/pgvector/pgvector)
- [Spring AI](https://spring.io/projects/spring-ai)
- [LangChain4j](https://github.com/langchain4j/langchain4j) - Alternative avec plus de fonctionnalités

## 📝 Structure du Projet

```
JavaRAG/
├── src/
│   └── main/
│       ├── java/com/example/javarag/
│       │   ├── JavaRagApplication.java      # Point d'entrée
│       │   ├── config/
│       │   │   └── AIConfig.java            # Configuration IA
│       │   ├── controller/
│       │   │   └── RAGController.java       # API REST
│       │   ├── dto/                         # Objets de transfert
│       │   ├── model/
│       │   │   └── Document.java            # Entité JPA
│       │   ├── repository/
│       │   │   └── DocumentRepository.java  # Accès BD
│       │   └── service/
│       │       ├── EmbeddingService.java    # Génération embeddings
│       │       ├── LLMService.java          # Génération réponses
│       │       └── RAGService.java          # Orchestration RAG
│       └── resources/
│           └── application.yml              # Configuration
├── scripts/
│   ├── setup-ollama.sh                      # Setup Ollama
│   └── load-sample-data.sh                  # Charger données
├── examples/
│   └── api-examples.http                    # Exemples d'API
├── docker-compose.yml                       # Services Docker
├── build.gradle                             # Configuration Gradle
├── settings.gradle                          # Paramètres Gradle
└── README.md                                # Ce fichier
```

## 🤝 Contribution

Ce projet est à but pédagogique. N'hésitez pas à :
- Poser des questions via les issues
- Proposer des améliorations
- Partager vos cas d'usage

## 📜 Licence

MIT

## 👨‍💻 Auteur

Projet créé pour apprendre le RAG avec Java Spring Boot.

---

**Bon apprentissage ! 🚀**

Si vous avez des questions, consultez le code source - il est abondamment commenté pour faciliter la compréhension.

# 🚀 Java RAG avec LangChain4j

Cette branche démontre l'implémentation du RAG en utilisant **LangChain4j**, un framework Java puissant pour les applications d'IA générative.

## 🔄 Comparaison avec la branche `main`

| Aspect | Branche `main` (Manuel) | Branche `langchain4j` |
|--------|-------------------------|----------------------|
| **Code** | ~500 lignes | ~300 lignes (-40%) |
| **Complexité** | Gestion HTTP manuelle | Abstraite par le framework |
| **Pédagogie** | ✅ Excellent pour apprendre | ⚠️ Cache les détails |
| **Production** | ❌ Nécessite maintenance | ✅ Framework maintenu |
| **Fonctionnalités** | RAG basique | RAG + outils avancés |

## 🎯 Avantages de LangChain4j

### 1. Code simplifié

**Avant (Manuel)** :
```java
// 50+ lignes pour gérer HTTP, JSON, erreurs
Map<String, Object> request = Map.of("model", model, "prompt", text);
String response = webClient.post()
    .uri("/api/embeddings")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(String.class)
    .block();
List<Double> embedding = parseOllamaEmbedding(response);
```

**Après (LangChain4j)** :
```java
// 2 lignes !
TextSegment segment = TextSegment.from(text);
Embedding embedding = embeddingModel.embed(segment).content();
```

### 2. Support multi-providers

Changez de provider en modifiant juste la configuration :

```yaml
# Ollama (local)
ai:
  provider: ollama
  
# OU OpenAI (cloud)
ai:
  provider: openai
```

Le code reste **identique** !

### 3. Fonctionnalités avancées disponibles

LangChain4j inclut :
- 🔄 **Chunking** : Découpage intelligent de documents
- 📊 **Reranking** : Améliorer la pertinence des résultats
- 💬 **Chat Memory** : Conversations avec contexte
- 🤖 **Agents** : Systèmes autonomes avec outils
- 🔗 **Vector Stores** : Intégration Pinecone, Weaviate, etc.

## 🚀 Démarrage

Les commandes sont **identiques** à la branche `main` :

```bash
# 1. Démarrer Docker
docker compose up -d

# 2. Installer les modèles Ollama
./scripts/setup-ollama.sh

# 3. Lancer l'application
./gradlew bootRun

# 4. Charger les données d'exemple
./scripts/load-sample-data.sh
```

## 📝 Différences de Code

### Structure des fichiers

**Branche main** :
- `EmbeddingService.java` - Génération d'embeddings manuelle
- `LLMService.java` - Communication avec LLM manuelle
- `RAGService.java` - Orchestration RAG
- `RAGController.java` - API REST

**Branche langchain4j** :
- `LangChain4jConfig.java` - ✨ Configuration des modèles
- `LangChain4jRAGService.java` - Service RAG simplifié
- `LangChain4jRAGController.java` - API REST

### API REST

L'API reste **100% compatible** :

```bash
# Indexer un document
POST http://localhost:8080/api/documents
{
  "content": "Java est un langage...",
  "source": "java-intro"
}

# Poser une question
POST http://localhost:8080/api/ask
{
  "question": "Qu'est-ce que Java ?",
  "topK": 3
}
```

## 🎓 Quelle branche choisir ?

### Utilisez la branche `main` si :
- ✅ Vous voulez **apprendre** comment fonctionne le RAG en détail
- ✅ Vous avez besoin de **comprendre** chaque étape
- ✅ C'est un projet **pédagogique**
- ✅ Vous voulez **personnaliser** la logique en profondeur

### Utilisez la branche `langchain4j` si :
- ✅ Vous construisez une **application de production**
- ✅ Vous voulez un **code maintenable**
- ✅ Vous avez besoin de **fonctionnalités avancées**
- ✅ Vous voulez être **productif rapidement**

## 📚 Ressources

- [Documentation LangChain4j](https://docs.langchain4j.dev/)
- [Exemples LangChain4j](https://github.com/langchain4j/langchain4j-examples)
- [Discord LangChain4j](https://discord.gg/JRbGXq6w)

## 🔄 Basculer entre les branches

```bash
# Aller sur la branche main (implémentation manuelle)
git checkout main

# Aller sur la branche langchain4j
git checkout langchain4j

# Comparer les deux implémentations
git diff main langchain4j
```

## 🎯 Exercices Pratiques

1. **Comparez le code** : Regardez `RAGService.java` vs `LangChain4jRAGService.java`
2. **Testez les performances** : Mesurez le temps de réponse des deux versions
3. **Ajoutez une fonctionnalité** : Par exemple, le chunking de documents
4. **Changez de provider** : Basculez d'Ollama vers OpenAI

---

**Note** : Cette branche utilise LangChain4j **0.35.0**. Le framework évolue rapidement, consultez la documentation officielle pour les dernières fonctionnalités.

#!/bin/bash

echo "📚 Chargement des documents d'exemple dans la base RAG"
echo ""

# Vérifier que l'API est accessible
echo "📡 Vérification que l'API est accessible..."
if ! curl -s http://localhost:8080/api/health > /dev/null; then
    echo "❌ Erreur: L'API n'est pas accessible sur http://localhost:8080"
    echo "   Assurez-vous que l'application Spring Boot est lancée"
    exit 1
fi

echo "✅ API accessible"
echo ""

# Indexer les documents d'exemple
echo "📝 Indexation du document 1: Java"
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Java est un langage de programmation orienté objet développé par Sun Microsystems en 1995. Il est conçu pour être portable et fonctionne sur différentes plateformes grâce à la JVM (Java Virtual Machine). Java est utilisé pour développer des applications web, mobiles (Android) et des systèmes d entreprise.",
    "source": "java-introduction"
  }'

echo ""
echo ""

echo "📝 Indexation du document 2: Spring Boot"
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring Boot est un framework Java qui facilite le développement d applications. Il offre une configuration automatique, un serveur embarqué et simplifie la création de microservices. Spring Boot est basé sur le framework Spring et utilise le principe de convention plutôt que configuration.",
    "source": "spring-boot-intro"
  }'

echo ""
echo ""

echo "📝 Indexation du document 3: RAG"
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Le RAG (Retrieval Augmented Generation) est une technique qui combine la recherche d informations avec la génération de texte. Il fonctionne en trois étapes: 1) Recherche de documents pertinents dans une base de données, 2) Construction d un contexte avec ces documents, 3) Génération d une réponse avec un LLM en utilisant ce contexte.",
    "source": "rag-explanation"
  }'

echo ""
echo ""

echo "📝 Indexation du document 4: Embeddings"
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Les embeddings sont des représentations vectorielles de textes. Ils transforment des mots ou des phrases en vecteurs de nombres, permettant de mesurer la similarité sémantique entre différents textes. Les modèles d embedding comme text-embedding-ada-002 d OpenAI ou nomic-embed-text d Ollama sont utilisés pour créer ces vecteurs.",
    "source": "embeddings-explanation"
  }'

echo ""
echo ""

echo "✅ Documents indexés avec succès!"
echo ""

# Afficher les statistiques
echo "📊 Statistiques:"
curl -s http://localhost:8080/api/stats | json_pp

echo ""

#!/bin/bash

echo "🚀 Configuration initiale d'Ollama pour le projet RAG"
echo ""

# Vérifier qu'Ollama est en cours d'exécution
echo "📡 Vérification qu'Ollama est accessible..."
if ! curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "❌ Erreur: Ollama n'est pas accessible sur http://localhost:11434"
    echo "   Assurez-vous que Docker est lancé avec: docker-compose up -d"
    exit 1
fi

echo "✅ Ollama est accessible"
echo ""

# Télécharger le modèle d'embedding
echo "📥 Téléchargement du modèle d'embedding: nomic-embed-text"
echo "   (Ce modèle permet de transformer du texte en vecteurs)"
docker exec rag-ollama ollama pull nomic-embed-text

echo ""
echo "📥 Téléchargement du modèle de chat: llama3.2"
echo "   (Ce modèle permet de générer des réponses)"
docker exec rag-ollama ollama pull llama3.2

echo ""
echo "✅ Configuration terminée!"
echo ""
echo "Les modèles suivants sont disponibles:"
docker exec rag-ollama ollama list

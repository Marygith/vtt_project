from keybert import KeyBERT
from keybert.backend import BaseEmbedder
from sentence_transformers import SentenceTransformer
import sys

with open(sys.argv[1], "r", encoding='cp1251') as f:
    text = f.readlines()
    f.close()

class CustomEmbedder(BaseEmbedder):
    def __init__(self, embedding_model):
        super().__init__()
        self.embedding_model = embedding_model

    def embed(self, documents, verbose=False):
        embeddings = self.embedding_model.encode(documents, show_progress_bar=verbose)
        return embeddings

custom_model = SentenceTransformer("model")
custom_embedder = CustomEmbedder(embedding_model=custom_model)
kw_modell = KeyBERT(model=custom_embedder)

for paragraph in text:
    if len(paragraph.split(" ")) > 10:
        print(kw_modell.extract_keywords(paragraph, keyphrase_ngram_range=(1, 1)))

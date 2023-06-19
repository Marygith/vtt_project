import sys
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from scipy.signal import argrelextrema
import math
import re

with open(sys.argv[1], "r", encoding='utf-8') as f:
    text = f.readlines()
    f.close()
text = text[0].replace("?", ".")
text = text.replace("!", ".")
text = text.replace("...", ".")
text = re.sub(r'[^\w\s.]', '', text)

sentences = text.split('. ')
model = SentenceTransformer('model')

embeddings = model.encode(sentences)

similarities = cosine_similarity(embeddings)


def sigm(x: float) -> float:
    return (1 / (1 + math.exp(0.5 * x)))


def calculate(similarities: np.array, p_size=10) -> np.array:
    x = np.linspace(-10, 10, p_size)
    y = np.vectorize(sigm)
    activation_weights = np.pad(y(x), (0, similarities.shape[0] - p_size))
    diagonals = [similarities.diagonal(each) for each in range(0, similarities.shape[0])]
    diagonals = [np.pad(each, (0, similarities.shape[0] - len(each))) for each in diagonals]
    diagonals = np.stack(diagonals)
    diagonals = diagonals * activation_weights.reshape(-1, 1)
    activated_similarities = np.sum(diagonals, axis=0)
    return activated_similarities

activated_similarities = calculate(similarities, min(10, similarities.shape[0]))
minmimas = argrelextrema(activated_similarities, np.less, order=2) 
split_points = [each for each in minmimas[0]]
text = ''
for num, each in enumerate(sentences):
    if num in split_points:
        text += f'\n\n {each}. '
    else:
        text += f'{each}. '

print(text)


import os
import sys
import locale
from faster_whisper import WhisperModel
import codecs

model = "C:\\Users\\maria\\IdeaProjects\\vtt-front\\faster-whisper-large-v2"
model = WhisperModel(model, device="cpu", compute_type="int8", cpu_threads=4)
segments, info = model.transcribe("audio.wav", beam_size=5, language="ru")


for segment in segments:
    print("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text), end="\n", flush=True)
# VTT_project 

## Overview

It's an app for getting text version of a video with key words. It allows you to upload your video, play it, get transcription and key words.

## Details

**Faster Whisper** was used as a speech recognition library.

**KeyBERT** was used as key extraction library.

**paraphrase-multilingual-MiniLM-L12-v2** is a recommended model for key extraction and division on paragraphs,
but you can choose whatever you like and consider appropriate. Python notebook in a train folder can be used to train your own model.

## Getting started

Java >= 8 and Python 3.1.0 must be installed.
Don't forget to set up frontend, mentioned in a front folder.

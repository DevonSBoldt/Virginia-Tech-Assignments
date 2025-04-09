import os
import threading
import wave
import asyncio
import tempfile
import pyaudio
import pygame
from dotenv import load_dotenv
from gtts import gTTS
from services import llm
import httpx
from openai import OpenAI

# Load .env file
load_dotenv()

# Initialize the OpenAI client with the modern API pattern
client = OpenAI(
    api_key=os.getenv('OPENAI_API_KEY'),
    base_url=os.getenv('OPENAI_API_BASE_URL', 'http://aitools.cs.vt.edu:7860/openai/v1')
)

# Initialize pygame speech mixer with explicit parameters for robustness
try:
    pygame.mixer.init(frequency=22050, size=-16, channels=2, buffer=8192)
    print("Pygame mixer initialized successfully.")
except Exception as e:
    print(f"Error initializing pygame mixer: {e}")

# Set up audio recording parameters
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 16000  # Whisper supports 16kHz for best results
CHUNK = 1024
RECORD_SECONDS = 5  # Adjust as needed
WAVE_OUTPUT_FILENAME = os.path.join("data", "audio", "voice_chat.wav")

# Ensure the output directory exists
os.makedirs(os.path.dirname(WAVE_OUTPUT_FILENAME), exist_ok=True)

def record_audio():
    """
    Capture audio from the microphone and save it to a file.
    """
    audio = pyaudio.PyAudio()

    # Open stream
    stream = audio.open(format=FORMAT, channels=CHANNELS,
                        rate=RATE, input=True,
                        frames_per_buffer=CHUNK)

    print("Recording...")

    frames = []

    # Record for the specified duration
    for _ in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
        data = stream.read(CHUNK)
        frames.append(data)

    print("Finished recording.")

    # Stop and close the stream
    stream.stop_stream()
    stream.close()
    audio.terminate()

    # Save the recorded audio as a .wav file
    try:
        with wave.open(WAVE_OUTPUT_FILENAME, 'wb') as wf:
            wf.setnchannels(CHANNELS)
            wf.setsampwidth(audio.get_sample_size(FORMAT))
            wf.setframerate(RATE)
            wf.writeframes(b''.join(frames))
        print(f"Audio saved successfully at {WAVE_OUTPUT_FILENAME}")
    except FileNotFoundError:
        print(f"Directory not found: {WAVE_OUTPUT_FILENAME}")
    except Exception as e:
        print(f"Error saving audio: {e}")

async def transcribe_audio():
    """
    Transcribe the recorded audio file using OpenAI's Whisper model.
    """
    try:
        async with httpx.AsyncClient() as client:
            with open(WAVE_OUTPUT_FILENAME, "rb") as audio_file:
                response = await client.post(
                    url=f"{client.base_url}/audio/transcriptions",
                    headers={"Authorization": f"Bearer {client.api_key}"},
                    files={"file": audio_file},
                    data={"model": "whisper-1"}
                )
                response.raise_for_status()
                return response.json()['text']
    except FileNotFoundError:
        print(f"Audio file not found at {WAVE_OUTPUT_FILENAME}")
        return ""
    except Exception as e:
        print(f"Error in transcribing audio: {e}")
        return ""

async def generate_gpt_response(prompt, messages=None):
    """
    Send transcribed text to GPT-4 for a response.
    """
    try:
        if not messages:
            messages = [{"role": "user", "content": prompt}]
        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=f"{client.base_url}/chat/completions",
                headers={"Authorization": f"Bearer {client.api_key}"},
                json={"model": "gpt-4-turbo", "messages": messages}
            )
            response.raise_for_status()
            return response.json()['choices'][0]['message']['content']
    except Exception as e:
        print(f"Error generating GPT response: {e}")
        return "Sorry, I couldn't generate a response."

def speak_text(text, lang="en"):
    def _speak():
        try:
            print("Converting text to speech...")
            tts = gTTS(text=text, lang=lang)
            with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as tmp_file:
                filename = tmp_file.name
                tts.save(filename)
            print(f"Saved audio file: {filename}")

            print("Loading audio file...")
            pygame.mixer.music.load(filename)
            pygame.mixer.music.play()

            while pygame.mixer.music.get_busy():
                pygame.time.Clock().tick(10)

            os.remove(filename)
            print(f"Removed audio file: {filename}")
        except Exception as e:
            print(f"Error in speak_text: {e}")

    _speak()


async def process_audio():
    """
    Orchestrate recording, transcribing, generating GPT response, and text-to-speech.
    """
    record_audio()  # Synchronous recording
    print("Recording completed. Processing audio...")

    # Transcribe audio
    transcription = await transcribe_audio()
    if transcription:
        print("Transcription:", transcription)
    else:
        print("Transcription failed.")
        return  # Exit if transcription fails

    # Generate GPT-4 response if transcription is successful
    response = await generate_gpt_response(transcription)
    if response:
        print("GPT-4 Response:", response)
    else:
        print("Failed to get a response from GPT-4.")
        return  # Exit if response generation fails

    # Speak the GPT-4 response
    speak_text(response)
    print("Response audio playback completed.")

# Run the async process_audio function
if __name__ == "__main__":
    asyncio.run(process_audio())

import os
import time
import asyncio
import httpx
import streamlit as st
from services.audio import record_audio, transcribe_audio, generate_gpt_response, speak_text

# Constants
WAVE_OUTPUT_FILENAME = os.path.join("data", "audio", "voice_chat.wav")

# Synchronous wrapper for transcribe_audio
def sync_transcribe_audio():
    """Wrapper to run the asynchronous transcribe_audio function synchronously."""
    return asyncio.run(transcribe_audio())

# Synchronous wrapper for generate_gpt_response
def sync_generate_gpt_response(prompt):
    """Wrapper to run the asynchronous generate_gpt_response function synchronously."""
    return asyncio.run(generate_gpt_response(prompt))

async def generate_gpt_response(prompt, messages=None):
    """
    Send transcribed text to GPT-4 for a response.
    
    Args:
        prompt (str): The prompt text to send to GPT-4.
        messages (list, optional): List of message dictionaries for the conversation context.
    
    Returns:
        str: GPT-4's response text.
    """
    api_key = os.getenv("OPENAI_API_KEY")
    base_url = os.getenv("OPENAI_API_BASE_URL", "http://aitools.cs.vt.edu:7860/openai/v1")

    if not messages:
        messages = [{"role": "user", "content": prompt}]

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=f"{base_url}/chat/completions",
                headers={"Authorization": f"Bearer {api_key}"},
                json={"model": "gpt-4-turbo", "messages": messages}
            )
            response.raise_for_status()
            return response.json()['choices'][0]['message']['content']
    except Exception as e:
        print(f"Error generating GPT response: {e}")
        return "Sorry, I couldn't generate a response."

async def transcribe_audio():
    """
    Transcribe the recorded audio file using OpenAI's Whisper model.
    
    Returns:
        str: Transcription text from the audio file.
    """
    api_key = os.getenv("OPENAI_API_KEY")
    base_url = os.getenv("OPENAI_API_BASE_URL", "http://aitools.cs.vt.edu:7860/openai/v1")

    try:
        async with httpx.AsyncClient() as client:
            with open(WAVE_OUTPUT_FILENAME, "rb") as audio_file:
                response = await client.post(
                    url=f"{base_url}/audio/transcriptions",
                    headers={"Authorization": f"Bearer {api_key}"},
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

# Streamlit page configuration
st.set_page_config(
    page_title="🎤 Voice Chat",
    page_icon="🎤",
    layout="wide"
)

st.header("🎤 Voice Chat")
st.write("Get instant answers to your software development and coding questions using the microphone.")

# Add a record button in the UI
if st.button("🔴 Record For Five Seconds"):
    with st.spinner("Recording..."):
        record_audio()  # Record audio for the defined duration
    st.success("Recording completed. Processing audio...")

    with st.spinner("Transcribing audio..."):
        # Transcribe the recorded audio file to text
        transcription = sync_transcribe_audio()
        if transcription:
            st.write("**Transcription:**")
            st.write(transcription)
        else:
            st.error("Transcription failed.")

    if transcription:
        with st.spinner("Generating response..."):
            # Generate response from GPT-4
            response = sync_generate_gpt_response(transcription)
            if response:
                st.write("**GPT-4 Response:**")
                st.write(response)
            else:
                st.error("Failed to get a response from GPT-4.")

        with st.spinner("Converting response to speech..."):
            # Speak the GPT-4 response
            speak_text(response)
            st.success("Response audio playback completed.")

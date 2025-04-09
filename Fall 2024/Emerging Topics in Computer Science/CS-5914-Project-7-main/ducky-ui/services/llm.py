import os
import openai
from dotenv import load_dotenv
import traceback
from typing import List, Dict, AsyncGenerator

# Load environment variables from .env file
load_dotenv()

openai.api_key = os.getenv('OPENAI_API_KEY')
openai_model = os.getenv('OPENAI_API_MODEL')

# Print loaded model and API info for debugging
print(f"openai_model: {openai_model} openai.api_key: {os.getenv('OPENAI_API_KEY')} openai.api_base: {os.getenv('OPENAI_API_BASE_URL')}")


async def converse(messages: List[Dict[str, str]]) -> AsyncGenerator[str, None]:
    """
    Given a conversation history, generate an iterative response of strings from the OpenAI API.
    
    :param messages: a conversation history with the following format:
    `[ { "role": "user", "content": "Hello, how are you?" },
       { "role": "assistant", "content": "I am doing well, how can I help you today?" } ]`
    
    :return: a generator of delta string responses
    """
    aclient = openai.AsyncOpenAI(api_key=os.getenv('OPENAI_API_KEY'),
                                 base_url=os.getenv('OPENAI_API_BASE_URL'))
    try:
        async for chunk in await aclient.chat.completions.create(model=openai_model,
                                                                 messages=messages,
                                                                 max_tokens=1600,
                                                                 stream=True):
            content = chunk.choices[0].delta.content
            if content:
                yield content

    except openai.OpenAIError as e:
        traceback.print_exc()
        yield f"oaiEXCEPTION {str(e)}"
    except Exception as e:
        yield f"EXCEPTION {str(e)}"


def create_conversation_starter(user_prompt: str) -> List[Dict[str, str]]:
    """
    Given a user prompt, create a conversation history with the following format:
    `[ { "role": "user", "content": user_prompt } ]`

    :param user_prompt: a user prompt string
    :return: a conversation history
    """
    return [{"role": "user", "content": user_prompt}]


async def generate_embedding(text: str) -> List[float]:
    """
    Generate an embedding for a given text using OpenAI's embedding model.
    
    :param text: The input text to generate an embedding for.
    :return: A list of floating-point numbers representing the text embedding.
    """
    try:
        response = await openai.Embedding.create(
            input=text,
            model="text-embedding-ada-002"  # Specify the embedding model
        )
        embedding = response['data'][0]['embedding']
        return embedding
    except openai.OpenAIError as e:
        print(f"OpenAI API error: {e}")
        traceback.print_exc()
        return None
    except Exception as e:
        print(f"An error occurred while generating embedding: {e}")
        traceback.print_exc()
        return None
    
    

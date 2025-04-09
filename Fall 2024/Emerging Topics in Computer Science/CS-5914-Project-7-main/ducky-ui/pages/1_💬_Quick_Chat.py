import os
import pandas as pd
import numpy as np
import streamlit as st
import asyncio
from sklearn.metrics.pairwise import cosine_similarity
from dotenv import load_dotenv
from PyPDF2 import PdfReader
from typing import List, Tuple
import tiktoken as tkn
import services.prompts as prompts
import services.llm as llm
import helpers.sidebar
import helpers.util
from openai import OpenAI
import pdb

from sklearn.neighbors import NearestNeighbors
from pdf2image import convert_from_path  # For generating image data from PDF

# Load environment variables
load_dotenv()

# Define file paths and constants
pdf_path = "data/ThePragmaticProgrammer.pdf"
embedding_file = "data/ThePragmaticProgrammer.embeddings.csv"
chunk_size = 1500
overlap = 20

df_embeddings = None
embeddings = []
nbrs = None

# Function to extract text from each page of the PDF
def extract_text_by_page(pdf_path: str) -> List[str]:
    with open(pdf_path, 'rb') as file:
        reader = PdfReader(file)
        text_by_page = [page.extract_text() for page in reader.pages]
    return text_by_page


# Function to chunk text page by page with overlap
def chunk_text_by_page(text_by_page: List[str], chunk_size: int = 1500, overlap: int = 20) -> List[Tuple[int, str]]:
    encoding = tkn.encoding_for_model("gpt-3.5-turbo")
    chunks = []

    for page_number, text in enumerate(text_by_page, start=1):
        tokens = list(encoding.encode(text))
        if len(tokens) <= chunk_size:
            chunks.append((page_number, text))
        else:
            position = 0
            while position < len(tokens):
                start_pos = max(0, position - overlap)
                end_pos = min(position + chunk_size, len(tokens))
                chunk_tokens = tokens[start_pos:end_pos]
                chunk_text = ''.join(encoding.decode_bytes(chunk_tokens).decode('utf-8', errors='ignore'))
                chunks.append((page_number, chunk_text))
                position += chunk_size
    return chunks

# Function to chunk user query with the same method as PDF text
def process_user_query(user_query: str, chunk_size: int = 1500, overlap: int = 20) -> List[str]:
    encoding = tkn.encoding_for_model("gpt-3.5-turbo")
    tokens = list(encoding.encode(user_query))
    query_chunks = []

    position = 0
    while position < len(tokens):
        start_pos = max(0, position - overlap)
        end_pos = min(position + chunk_size, len(tokens))
        chunk_tokens = tokens[start_pos:end_pos]
        chunk_text = ''.join(encoding.decode_bytes(chunk_tokens).decode('utf-8', errors='ignore'))
        query_chunks.append(chunk_text)
        position += chunk_size

    return query_chunks

# Function to extract an image of a PDF page
def get_pdf_page_image(pdf_path: str, page_number: int):
    try:
        images = convert_from_path(pdf_path, first_page=page_number, last_page=page_number)
        if images:
            return images[0]
        else:
            return None
    except Exception as e:
        print(f"Error generating image for page {page_number}: {e}")
        return None

# Asynchronous function to handle embedding generation
async def generate_embeddings():
    global df_embeddings, embeddings
    text_by_page = extract_text_by_page(pdf_path)

    if text_by_page:
        documents = chunk_text_by_page(text_by_page)

        client = OpenAI(base_url='http://aitools.cs.vt.edu:7860/openai/v1', api_key="aitools")
        EMBEDDING_MODEL = "text-embedding-3-small"
        BATCH_SIZE = 20

        embeddings = []
        document_name = "The Pragmatic Programmer"

        for batch_start in range(0, len(documents), BATCH_SIZE):
            batch_end = batch_start + BATCH_SIZE
            batch = [doc[1] for doc in documents[batch_start:batch_end]]

            try:
                response = client.embeddings.create(
                    model=EMBEDDING_MODEL, input=batch, encoding_format="float"
                )
                if response and hasattr(response, 'data'):
                    batch_embeddings = [e.embedding for e in response.data]
                    for i, embedding in enumerate(batch_embeddings):
                        page_number = documents[batch_start + i][0]
                        embeddings.append({
                            "document_name": document_name,
                            "page_number": page_number,
                            "embedding": list(map(float, embedding)),
                            "context": documents[batch_start + i][1]
                        })
                else:
                    print(f"Empty response for batch {batch_start} to {batch_end - 1}")
                    embeddings.extend([None] * len(batch))
            except Exception as e:
                print(f"Error processing batch {batch_start} to {batch_end - 1}: {e}")
                embeddings.extend([None] * len(batch))

        df = pd.DataFrame(embeddings)
        df['embedding'] = df['embedding'].apply(lambda x: np.nan if x is None else x)
        df.to_csv(embedding_file, index=False)
        df_embeddings = df
    else:
        st.error("No text extracted from the PDF.")

# Function to load and clean embeddings from a CSV file
def load_embeddings():
    global df_embeddings, embeddings
    if os.path.exists(embedding_file):
        df_embeddings = pd.read_csv(embedding_file)

        def clean_embedding(embedding):
            try:
                # Ensure the embedding is a list of floats
                embedding = eval(embedding)
                if isinstance(embedding, list) and all(isinstance(x, (int, float)) for x in embedding):
                    return np.array(embedding, dtype=float)
            except (SyntaxError, ValueError, TypeError):
                return np.nan

        df_embeddings["embedding"] = df_embeddings["embedding"].apply(clean_embedding)

        # Filter out rows with NaN values in the 'embedding' column
        original_count = len(df_embeddings)
        df_embeddings = df_embeddings.dropna(subset=["embedding"])
        cleaned_count = len(df_embeddings)

        st.write(f"Dropped {original_count - cleaned_count} embeddings due to NaN values.")

        # Convert embeddings to a NumPy array, filtering out any non-array types
        embeddings = np.array([emb for emb in df_embeddings["embedding"] if isinstance(emb, np.ndarray)])

        # Ensure embeddings are 2D
        if embeddings.ndim == 1:
            embeddings = embeddings.reshape(1, -1)

        # Check if any NaN values exist in the embeddings
        if np.isnan(embeddings).any():
            st.error("Embeddings still contain NaN values after cleaning.")
            raise ValueError("Embeddings contain NaN values after loading and cleaning.")
        else:
            st.write(f"Loaded {len(embeddings)} embeddings successfully.")

# Function to fit the NearestNeighbors model
def fit_nearest_neighbors():
    global nbrs
    if len(embeddings) > 0:
        # Check for NaNs in embeddings
        if np.isnan(embeddings).any():
            st.error("Embeddings contain NaN values. Cannot fit NearestNeighbors model.")
            raise ValueError("Embeddings contain NaN values. Cannot fit NearestNeighbors model.")

        # Fit the NearestNeighbors model
        st.write("Fitting NearestNeighbors model...")
        nbrs = NearestNeighbors(n_neighbors=5, algorithm='ball_tree').fit(embeddings)
        st.write("NearestNeighbors model fitted successfully.")
    else:
        st.error("No embeddings available for fitting the NearestNeighbors model.")

# Function to perform nearest-neighbors search for the closest matching page
async def find_closest_matching_page(chat_input: str) -> int:
    # Process the user query into chunks
    user_query_chunks = process_user_query(chat_input)
    
    closest_pages = []
    
    # Generate embeddings for each query chunk and find matches
    for query_chunk in user_query_chunks:
        user_embedding = await llm.generate_embedding(query_chunk)
        user_embedding = np.array(user_embedding).reshape(1, -1)
        
        # Find the distances and indices for the nearest neighbors
        distances, indices = nbrs.kneighbors(user_embedding)
        
        # Identify the index of the page with the smallest distance (closest match)
        closest_match_index = indices[0][0]
        closest_page_number = df_embeddings.iloc[closest_match_index]["page_number"]
        closest_pages.append(closest_page_number)
    
    # Return the first matching page as the most relevant context
    return closest_pages[0] if closest_pages else None

# Streamlit interface for Quick Chat
st.title("Quick Chat")

# Add a checkbox to use 'The Pragmatic Programmer' as context
use_pragmatic_programmer = st.checkbox("Use The Pragmatic Programmer as context")

# Add a place for chat input
chat_input = st.text_input("Enter your message here")

# Load embeddings
load_embeddings()

# Fit the nearest neighbors model
fit_nearest_neighbors()

# LLM's response placeholder
if chat_input:
    try:
        st.write("This is the answer from the LLM:")

        # Define the async function for streaming conversation
        async def stream_conversation():
            response_text = ""  # Initialize the response text locally in the async function

            # Prepare the messages correctly
            messages = [
                {"role": "user", "content": chat_input}  # Wrap the chat_input in the 'user' role
            ]

            # Call the llm.converse with the correct message format
            async for chunk in llm.converse(messages):
                response_text += chunk  # Accumulate chunks in response_text
            return response_text

        # Use asyncio.run to call the async function and get the result
        response_text = asyncio.run(stream_conversation())
        st.write(response_text)

        # Conditionally display the "Evidence" section only if the checkbox is selected
        if use_pragmatic_programmer:
            # Find the first relevant chunk's page number
            selected_page = asyncio.run(find_closest_matching_page(chat_input))

            with st.expander("Evidence"):
                st.write(f"Selected page number: {selected_page}")

                # Generate and display the image of the selected page from the PDF
                image_data = get_pdf_page_image(pdf_path, selected_page)
                if image_data:
                    st.image(image_data)
                else:
                    st.write("No image found for the selected page.")
    except Exception as e:
        st.error(f"An error occurred while getting the response: {e}")

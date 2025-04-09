import os
from datetime import datetime
from typing import Literal, Tuple
from urllib.parse import urlparse

import httpx
import pandas as pd
from dotenv import load_dotenv
from openai import OpenAI
import aiofiles

# Load .env file
load_dotenv()

# Base folder for storing images
__IMAGES_BASE_FOLDER = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'data/images')


def get_all_images() -> pd.DataFrame:
    """
    Retrieves a DataFrame containing information about all images stored in a folder.

    Returns:
        pd.DataFrame: A DataFrame with columns 'Image', 'Description', and 'Date Created',
                      where 'Image' is the file path, 'Description' is the associated
                      description, and 'Date Created' is the creation timestamp.
    """
    images_data = []
    for file_name in os.listdir(__IMAGES_BASE_FOLDER):
        if file_name.endswith('.png'):
            image_path = os.path.join(__IMAGES_BASE_FOLDER, file_name)
            description_file = os.path.splitext(image_path)[0] + '.txt'
            description = 'No description available'
            
            if os.path.exists(description_file):
                with open(description_file, 'r') as f:
                    description = f.read().strip()
            
            date_created = datetime.fromtimestamp(os.path.getctime(image_path)).strftime('%Y-%m-%d %H:%M:%S')
            images_data.append({
                'Image': image_path,
                'Description': description,
                'Date Created': date_created
            })
    
    return pd.DataFrame(images_data, columns=['Image', 'Description', 'Date Created'])


def delete_image(image_path: str):
    """
    Deletes an image file and its associated description file (if it exists).
    
    Args:
        image_path (str): The path to the image file to be deleted.
    """
    if os.path.exists(image_path):
        os.remove(image_path)
        print(f"Deleted image file: {image_path}")
    else:
        print(f"Image file not found: {image_path}")

    # Identify the associated description file
    description_file = os.path.splitext(image_path)[0] + '.txt'
    print(f"Looking for description file: {description_file}")
    
    if os.path.exists(description_file):
        os.remove(description_file)
        print(f"Deleted description file: {description_file}")
    else:
        print(f"Description file not found for image: {image_path}")



async def generate_image(
    prompt: str,
    model: str = "dall-e-3",
    style: Literal["vivid", "natural"] = "vivid",
    quality: Literal["standard", "hd"] = "hd",
    timeout: int = 100,
    size: Literal["256x256", "512x512", "1024x1024", "1792x1024", "1024x1792"] = "1024x1024"
) -> Tuple[str, str]:
    """
    Generates an image based on a given text prompt using the OpenAI DALL-E model
    and saves the image to a local folder.
    
    Args:
        prompt (str): The description or prompt for generating the image.
        model (str): The model version to use (default is "dall-e-3").
        style (Literal): The style of the image, e.g., "vivid" or "natural".
        quality (Literal): The quality setting, either "standard" or "hd".
        timeout (int): The maximum time (in seconds) to wait for the image to be generated.
        size (Literal): The size of the image, e.g., "1024x1024", "1792x1024", etc.
    
    Returns:
        Tuple[str, str]: A tuple containing the prompt and the file path of the saved image.
    """ 
    if not os.path.exists(__IMAGES_BASE_FOLDER):
        os.makedirs(__IMAGES_BASE_FOLDER)

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    image_filename = f"{timestamp}.png"
    text_filename = f"{timestamp}.txt"
    image_path = os.path.join(__IMAGES_BASE_FOLDER, image_filename)
    text_path = os.path.join(__IMAGES_BASE_FOLDER, text_filename)

    client = OpenAI(
        base_url='http://aitools.cs.vt.edu:7860/openai/v1',
        api_key='aitools'
    )
    
    response = client.images.generate(
        model=model,
        prompt=prompt,
        size=size,
        quality=quality,
        style=style,
        n=1
    )
    image_url = response.data[0].url
    
    async with httpx.AsyncClient() as client:
        response = await client.get(image_url, timeout=timeout)
        response.raise_for_status()
        
        with open(image_path, 'wb') as f:
            f.write(response.content)
        
        with open(text_path, 'w', encoding='utf-8') as f:
            f.write(prompt)
    
    return prompt, image_path


def _extract_filename_from_url(url: str) -> str:
    """
    Extracts the filename from a given URL.
    
    Args:
        url (str): The URL from which to extract the filename.
    
    Returns:
        str: The extracted filename.
    """
    return os.path.basename(urlparse(url).path)

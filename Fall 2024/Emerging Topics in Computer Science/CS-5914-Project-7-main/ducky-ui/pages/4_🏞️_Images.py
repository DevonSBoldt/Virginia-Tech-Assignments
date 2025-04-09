import os
import asyncio
import pandas as pd
import streamlit as st
import helpers.sidebar
from services.images import generate_image, get_all_images, delete_image

# Page configuration
st.set_page_config(
    page_title="Images",
    page_icon="🏞️",
    layout="wide"
)

# Display the sidebar
helpers.sidebar.show()

# Header setup
st.header("🏞️ Image Generation")

# Create tabs for Image Generation and Image List
tab1, tab2 = st.tabs(["Image Generation", "Image List"])

# First tab: Image Generation
with tab1:
    prompt = st.text_input(
        label="Prompt",
        placeholder="Enter a prompt for the image generation model"
    )
    
    if st.button("Generate Image"):
        if prompt.strip():
            async def generate_and_display_image():
                try:
                    prompt_text, image_path = await generate_image(prompt)
                    st.image(image_path, caption=prompt_text, use_column_width=True)
                except Exception as e:
                    st.error(f"An error occurred: {e}")
            
            asyncio.run(generate_and_display_image())
        else:
            st.warning("Please enter a prompt to generate an image.")

# Function to display the image list
def display_image_list():
    try:
        images_df = get_all_images()
        
        if not images_df.empty:
            images_df['Date Created'] = pd.to_datetime(images_df['Date Created'], errors='coerce')
            images_df['Date Created'] = images_df['Date Created'].apply(
                lambda x: x.strftime("%Y-%m-%d %H:%M:%S") if pd.notnull(x) else "Unknown"
            )

            st.write(
                "<div style='display: flex; font-weight: bold; gap: 10px; margin-bottom: 10px;'>"
                "<div style='flex: 1;'>Image</div>"
                "<div style='flex: 2;'>Description</div>"
                "<div style='flex: 1;'>Date Created</div>"
                "<div style='flex: 1;'>Actions</div>"
                "</div>",
                unsafe_allow_html=True
            )

            for index, row in images_df.iterrows():
                image, description, date_created = row["Image"], row["Description"], row["Date Created"]
                
                col1, col2, col3, col4 = st.columns([1, 2, 1, 1])
                
                with col1:
                    st.image(image, use_column_width=True)
                
                with col2:
                    st.write(description)
                
                with col3:
                    st.write(date_created)
                
                with col4:
                    if st.button("View", key=f"view_{image}"):
                        st.image(image, use_column_width=True)
                    
                    if st.button("Delete", key=f"delete_{index}"):
                        delete_image(image)
                        st.cache_data.clear()  # Clear cache to refresh the image list
                        st.session_state.query_params = {}  # Refresh the view
                        display_image_list()  # Re-call the function to update the list
                        return
        else:
            st.info("No images found.")
    except Exception as e:
        st.error(f"An error occurred while loading the image list: {e}")

# Second tab: Image List
with tab2:
    display_image_list()
